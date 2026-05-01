package com.bqy.openapibackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.common.UserConstant;
import com.bqy.openapibackend.dao.UserDao;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.model.request.user.UserQueryRequest;
import com.bqy.openapibackend.model.request.user.UserUpdateRequest;
import com.bqy.openapibackend.model.vo.ApiKeysVO;
import com.bqy.openapibackend.model.vo.LoginUserVO;
import com.bqy.openapibackend.model.vo.RegisterResultVO;
import com.bqy.openapibackend.model.vo.UserVO;
import com.bqy.openapibackend.service.IUserService;
import com.bqy.openapibackend.util.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户 服务实现类
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    /** 允许上传的图片类型 */
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Resource
    private UserDao userDao;

    @Value("${avatar.upload.dir:./uploads/avatars}")
    private String avatarUploadDir;

    @Value("${avatar.access.prefix:http://localhost:8080/api/uploads/avatars}")
    private String avatarAccessPrefix;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public RegisterResultVO register(String userAccount, String userPassWord, String checkPassWord, String userName, String phoneNumber) {
        ThrowUtils.throwIf(!StringUtils.equals(userPassWord, checkPassWord), "两次密码输入不一致");
        synchronized (userAccount.intern()) {
            long count = userDao.getCountByUserAccount(userAccount);
            ThrowUtils.throwIf(count > 0, "账号已存在，请更换");
            User user = new User();
            user.setUserAccount(userAccount);
            // 使用 BCrypt 加密密码（自带随机 salt，每次生成结果不同）
            user.setUserPassword(PASSWORD_ENCODER.encode(userPassWord));
            user.setUserName(userName);
            user.setPhoneNumber(phoneNumber);

            // 生成 AccessKey 和 SecretKey
            String accessKey = generateAccessKey(userAccount);
            String secretKey = generateSecretKey(userAccount);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);

            boolean result = userDao.save(user);
            ThrowUtils.throwIf(!result, "注册失败");

            log.info("用户注册成功: userId={}, userAccount={}", user.getId(), userAccount);

            // 返回注册结果，包含密钥信息
            return RegisterResultVO.builder()
                    .userId(user.getId())
                    .userAccount(user.getUserAccount())
                    .userName(user.getUserName())
                    .accessKey(accessKey)
                    .secretKey(secretKey)
                    .message("✅ 注册成功！请妥善保存您的 AccessKey 和 SecretKey，这是您调用 API 的唯一凭证。" +
                            "SecretKey 仅在注册时显示一次，之后无法再次查看，若遗忘可在用户中心重新生成。")
                    .build();
        }
    }

    @Override
    public LoginUserVO login(String userAccount, String userPassword, HttpServletRequest request) {
        // 先按账号查出用户
        User loginUser = userDao.getByUserAccount(userAccount);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(loginUser), "账号密码错误");

        String storedPassword = loginUser.getUserPassword();
        boolean passwordValid = false;
        boolean needUpgrade = false;

        if (isBCryptHash(storedPassword)) {
            // 新用户：BCrypt 验证
            passwordValid = PASSWORD_ENCODER.matches(userPassword, storedPassword);
        } else {
            // 旧用户：明文比对（迁移兼容），验证通过后自动升级
            passwordValid = StringUtils.equals(userPassword, storedPassword);
            if (passwordValid) {
                needUpgrade = true;
            }
        }

        ThrowUtils.throwIf(!passwordValid, "账号密码错误");

        // 旧密码验证通过，立即升级为 BCrypt 存储，下次登录走新路径
        if (needUpgrade) {
            try {
                User upgradeUser = new User();
                upgradeUser.setId(loginUser.getId());
                upgradeUser.setUserPassword(PASSWORD_ENCODER.encode(userPassword));
                userDao.updateById(upgradeUser);
                log.info("用户密码已自动升级为 BCrypt: userId={}", loginUser.getId());
            } catch (Exception e) {
                // 升级失败不影响本次登录
                log.warn("用户密码升级失败, userId={}", loginUser.getId(), e);
            }
        }

        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(loginUser, loginUserVO);
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS, loginUser.getId());
        return loginUserVO;
    }

    /**
     * 判断一个字符串是否是 BCrypt 哈希值
     * BCrypt 哈希以 $2a$、$2b$、$2y$ 开头，长度固定为 60 位
     */
    private boolean isBCryptHash(String password) {
        return StringUtils.isNotBlank(password)
                && password.length() == 60
                && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Long userId = (Long) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(userId), StatusCode.NOT_LOGIN_ERROR);
        User loginUser = userDao.getUserById(userId);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(loginUser), StatusCode.NOT_LOGIN_ERROR);
        return loginUser;
    }

    @Override
    public Boolean logout(HttpServletRequest request) {
        Object o = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(o), StatusCode.NOT_LOGIN_ERROR);
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATUS);
        return Boolean.TRUE;
    }

    @Override
    public List<UserVO> getUserList(UserQueryRequest request) {
        return userDao.listByRequest(request).stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtil.copyProperties(user, userVO);
                    return userVO;
                }).collect(Collectors.toList());
    }

    @Override
    public Boolean deleteUser(Long id) {
        User user = userDao.getUserById(id);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(user), "用户不存在");
        return userDao.removeById(id);
    }

    @Override
    public Page<UserVO> getUserPage(UserQueryRequest request) {
        Page<User> userPage = userDao.getUserPage(request);
        List<UserVO> userVOList = userPage.getRecords().stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtil.copyProperties(user, userVO);
                    return userVO;
                }).toList();
        Page<UserVO> userVOPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

    @Override
    public Boolean updateUser(UserUpdateRequest request) {
        User oldUser = userDao.getUserById(request.getId());
        ThrowUtils.throwIf(ObjectUtils.isEmpty(oldUser), "用户不存在");
        User user = new User();
        BeanUtil.copyProperties(request, user);
        ThrowUtils.throwIf(!userDao.updateById(user), "更新失败");
        return Boolean.TRUE;

    }

    @Override
    public ApiKeysVO getApiKeys(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        User user = userDao.getUserById(loginUser.getId());
        ThrowUtils.throwIf(ObjectUtils.isEmpty(user), "用户不存在");

        // 对 SecretKey 进行掩码处理
        String secretKeyMasked = maskSecretKey(user.getSecretKey());

        return ApiKeysVO.builder()
                .accessKey(user.getAccessKey())
                .secretKeyMasked(secretKeyMasked)
                .message("完整的 Secret Key 仅在注册时显示一次。若遗忘可在此重新生成（会立即失效之前的密钥）。")
                .lastUpdated(user.getUpdateTime() != null ?
                    user.getUpdateTime().format(DATE_FORMATTER) :
                    user.getCreateTime().format(DATE_FORMATTER))
                .build();
    }

    @Override
    public RegisterResultVO regenerateApiKeys(HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        User user = userDao.getUserById(loginUser.getId());
        ThrowUtils.throwIf(ObjectUtils.isEmpty(user), "用户不存在");

        // 生成新的密钥
        String newAccessKey = generateAccessKey(user.getUserAccount());
        String newSecretKey = generateSecretKey(user.getUserAccount());

        // 更新用户密钥
        user.setAccessKey(newAccessKey);
        user.setSecretKey(newSecretKey);
        user.setUpdateTime(LocalDateTime.now());

        boolean result = userDao.updateById(user);
        ThrowUtils.throwIf(!result, "更新密钥失败");

        log.info("用户重新生成密钥: userId={}, userAccount={}", user.getId(), user.getUserAccount());

        // 返回新的密钥信息
        return RegisterResultVO.builder()
                .userId(user.getId())
                .userAccount(user.getUserAccount())
                .userName(user.getUserName())
                .accessKey(newAccessKey)
                .secretKey(newSecretKey)
                .message("✅ 密钥已重新生成！旧的密钥已失效，请立即更新您的应用配置。" +
                        "新的 SecretKey 仅显示一次，请妥善保存。")
                .build();
    }

    @Override
    public String uploadAvatar(MultipartFile file, HttpServletRequest request) {
        // 1. 校验文件不为空
        ThrowUtils.throwIf(file == null || file.isEmpty(), "上传文件不能为空");

        // 2. 校验文件类型
        String contentType = file.getContentType();
        ThrowUtils.throwIf(!ALLOWED_CONTENT_TYPES.contains(contentType), "仅支持 JPG、PNG、GIF、WEBP 格式的图片");

        // 3. 校验文件大小（5MB）
        ThrowUtils.throwIf(file.getSize() > 5 * 1024 * 1024, "文件大小不能超过 5MB");

        // 4. 获取当前登录用户
        User loginUser = getLoginUser(request);

        // 5. 生成唯一文件名，保留原始扩展名
        String originalFilename = file.getOriginalFilename();
        String suffix = StringUtils.isNotBlank(originalFilename) && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String fileName = loginUser.getId() + "_" + UUID.randomUUID().toString().replace("-", "") + suffix;

        // 6. 将配置路径转为绝对路径（避免 Tomcat 相对路径解析到临时目录的问题）
        java.nio.file.Path uploadPath = java.nio.file.Paths.get(avatarUploadDir).toAbsolutePath().normalize();
        try {
            java.nio.file.Files.createDirectories(uploadPath);
        } catch (IOException e) {
            log.error("头像目录创建失败, path={}", uploadPath, e);
            ThrowUtils.throwIf(true, "头像上传失败，服务器目录创建异常");
        }

        // 7. 保存文件到本地（使用 NIO，绕开 Tomcat 工作目录限制）
        java.nio.file.Path destPath = uploadPath.resolve(fileName);
        try {
            java.nio.file.Files.copy(file.getInputStream(), destPath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("头像文件保存失败, userId={}, dest={}", loginUser.getId(), destPath, e);
            ThrowUtils.throwIf(true, "头像上传失败，请重试");
        }

        // 8. 拼接访问 URL
        String avatarUrl = avatarAccessPrefix + "/" + fileName;

        // 9. 更新数据库中的用户头像字段
        User updateUser = new User();
        updateUser.setId(loginUser.getId());
        updateUser.setUserAvatar(avatarUrl);
        updateUser.setUpdateTime(LocalDateTime.now());
        ThrowUtils.throwIf(!userDao.updateById(updateUser), "头像信息保存失败");

        log.info("用户头像上传成功, userId={}, avatarUrl={}", loginUser.getId(), avatarUrl);
        return avatarUrl;
    }

    /**
     * 生成 AccessKey（使用 UUID 保证唯一性）
     */
    private String generateAccessKey(String userAccount) {
        return "ak_" + DigestUtil.md5Hex(userAccount + RandomUtil.randomNumbers(5) + UUID.randomUUID());
    }

    /**
     * 生成 SecretKey（使用 UUID 保证唯一性）
     */
    private String generateSecretKey(String userAccount) {
        return "sk_" + DigestUtil.md5Hex(userAccount + RandomUtil.randomNumbers(8) + UUID.randomUUID());
    }

    /**
     * 对 SecretKey 进行掩码处理
     * 仅显示前 4 个和后 4 个字符
     */
    private String maskSecretKey(String secretKey) {
        if (StringUtils.length(secretKey) <= 8) {
            return "****";
        }
        return secretKey.substring(0, 4) + "*".repeat(secretKey.length() - 8) + secretKey.substring(secretKey.length() - 4);
    }
}
