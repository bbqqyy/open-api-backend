package com.bqy.openapibackend.util;

import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.common.UserConstant;
import com.bqy.openapibackend.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;

public class LoginUserUtils {
    public static User getLoginUser(HttpServletRequest request) {
        Object o = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        User loginUser = (User) o;
        ThrowUtils.throwIf(ObjectUtils.isEmpty(loginUser), StatusCode.NOT_LOGIN_ERROR);
        return loginUser;
    }
}
