package com.bqy.openapibackend.aop;

import com.bqy.openapibackend.annotation.AuthorCheck;
import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.util.LoginUserUtils;
import com.bqy.openapibackend.util.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private LoginUserUtils loginUserUtils;

    @Around("@annotation(authorCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthorCheck authorCheck) throws Throwable {
        String mustRole = authorCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = loginUserUtils.getLoginUser(request);
        if (StringUtils.isNotBlank(mustRole)) {
            ThrowUtils.throwIf(!StringUtils.equals(mustRole, loginUser.getUserRole()), StatusCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}
