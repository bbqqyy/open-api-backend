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
        Long userId = (Long) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(userId), StatusCode.NOT_LOGIN_ERROR);
        User loginUser = userDao.getUserById(userId);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(loginUser), StatusCode.NOT_LOGIN_ERROR);
        return loginUser;
    }
}
