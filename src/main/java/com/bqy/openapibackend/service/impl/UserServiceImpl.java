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
import com.bqy.openapibackend.util.LoginUserUtils;
import com.bqy.openapibackend.util.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    @Resource
    private UserDao userDao;

    @Resource
    private LoginUserUtils loginUserUtils;

    private static final String SALT = "quanweijiami";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public RegisterResultVO register(String userAccount, String userPassWord, String checkPassWord, String userName, String phoneNumber) {
        ThrowUtils.throwIf(!StringUtils.equals(userPassWord, checkPassWord), "两次密码输入不一致");
        synchronized (userAccount.intern()) {
            long count = userDao.getCountByUserAccount(userAccount);
            ThrowUtils.throwIf(count > 0, "账号已存在，请更换");
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(userPassWord);
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
        User loginUser = userDao.getLoginUser(userAccount, userPassword);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(loginUser), "账号密码错误");
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(loginUser, loginUserVO);
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS, loginUser.getId());
        return loginUserVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        return loginUserUtils.getLoginUser(request);
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
        User loginUser = loginUserUtils.getLoginUser(request);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(loginUser), "用户不存在");

        // 对 SecretKey 进行掩码处理
        String secretKeyMasked = maskSecretKey(loginUser.getSecretKey());

        return ApiKeysVO.builder()
                .accessKey(loginUser.getAccessKey())
                .secretKeyMasked(secretKeyMasked)
                .message("完整的 Secret Key 仅在注册时显示一次。若遗忘可在此重新生成（会立即失效之前的密钥）。")
                .lastUpdated(loginUser.getUpdateTime() != null ?
                    loginUser.getUpdateTime().format(DATE_FORMATTER) :
                    loginUser.getCreateTime().format(DATE_FORMATTER))
                .build();
    }

    @Override
    public RegisterResultVO regenerateApiKeys(HttpServletRequest request) {
        User loginUser = loginUserUtils.getLoginUser(request);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(loginUser), "用户不存在");

        // 生成新的密钥
        String newAccessKey = generateAccessKey(loginUser.getUserAccount());
        String newSecretKey = generateSecretKey(loginUser.getUserAccount());

        // 更新用户密钥
        loginUser.setAccessKey(newAccessKey);
        loginUser.setSecretKey(newSecretKey);
        loginUser.setUpdateTime(LocalDateTime.now());

        boolean result = userDao.updateById(loginUser);
        ThrowUtils.throwIf(!result, "更新密钥失败");

        log.info("用户重新生成密钥: userId={}, userAccount={}", loginUser.getId(), loginUser.getUserAccount());

        // 返回新的密钥信息
        return RegisterResultVO.builder()
                .userId(loginUser.getId())
                .userAccount(loginUser.getUserAccount())
                .userName(loginUser.getUserName())
                .accessKey(newAccessKey)
                .secretKey(newSecretKey)
                .message("✅ 密钥已重新生成！旧的密钥已失效，请立即更新您的应用配置。" +
                        "新的 SecretKey 仅显示一次，请妥善保存。")
                .build();
    }

    /**
     * 生成 AccessKey
     */
    private String generateAccessKey(String userAccount) {
        return "ak_" + DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
    }

    /**
     * 生成 SecretKey
     */
    private String generateSecretKey(String userAccount) {
        return "sk_" + DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
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
