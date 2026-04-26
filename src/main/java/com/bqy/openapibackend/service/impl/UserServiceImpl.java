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
import com.bqy.openapibackend.model.vo.LoginUserVO;
import com.bqy.openapibackend.model.vo.UserVO;
import com.bqy.openapibackend.service.IUserService;
import com.bqy.openapibackend.util.LoginUserUtils;
import com.bqy.openapibackend.util.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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
@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private UserDao userDao;

    private static final String SALT = "quanweijiami";

    @Override
    public long register(String userAccount, String userPassWord, String checkPassWord, String userName, String phoneNumber) {
        ThrowUtils.throwIf(!StringUtils.equals(userPassWord, checkPassWord), "两次密码输入不一致");
        synchronized (userAccount.intern()) {
            long count = userDao.getCountByUserAccount(userAccount);
            ThrowUtils.throwIf(count > 0, "账号已存在，请更换");
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(userPassWord);
            user.setUserName(userName);
            user.setPhoneNumber(phoneNumber);
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean result = userDao.save(user);
            ThrowUtils.throwIf(!result, "注册失败");
            return user.getId();
        }
    }

    @Override
    public LoginUserVO login(String userAccount, String userPassword, HttpServletRequest request) {
        User loginUser = userDao.getLoginUser(userAccount, userPassword);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(loginUser), "账号密码错误");
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(loginUser, loginUserVO);
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO getLoginUser(HttpServletRequest request) {
        User loginUser = LoginUserUtils.getLoginUser(request);
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(loginUser, loginUserVO);
        return loginUserVO;
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
}
