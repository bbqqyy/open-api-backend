package com.bqy.openapibackend.util;

import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.common.UserConstant;
import com.bqy.openapibackend.dao.UserDao;
import com.bqy.openapibackend.model.entity.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
public class LoginUserUtils {
    @Resource
    private UserDao userDao;

    public User getLoginUser(HttpServletRequest request) {
        // 优先检查 API Key 认证存入的用户（invoke-with-key 场景）
        Object apiAuthUser = request.getAttribute("apiAuthUser");
        if (apiAuthUser instanceof User) {
            return (User) apiAuthUser;
        }
        // 再检查 Session 认证（普通登录场景）
        Long userId = (Long) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(userId), StatusCode.NOT_LOGIN_ERROR);
        User loginUser = userDao.getUserById(userId);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(loginUser), StatusCode.NOT_LOGIN_ERROR);
        return loginUser;
    }
}
