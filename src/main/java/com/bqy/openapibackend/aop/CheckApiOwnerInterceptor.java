package com.bqy.openapibackend.aop;

import com.bqy.openapibackend.annotation.CheckApiOwner;
import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.util.LoginUserUtils;
import com.bqy.openapibackend.util.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class CheckApiOwnerInterceptor {
    @Resource
    private ApiInfoDao apiInfoDao;

    @Around("@annotation(checkApiOwner)")
    public Object checkApiOwner(ProceedingJoinPoint joinPoint, CheckApiOwner checkApiOwner) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        User user = LoginUserUtils.getLoginUser(request);
        Long userId = user.getId();
        Object[] args = joinPoint.getArgs();
        String apiFieldName = checkApiOwner.apiFieldName();
        Long apiId = extractApiId(args, apiFieldName);
        ApiInfo apiInfo = apiInfoDao.getApiInfoById(apiId);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(apiInfo), "apiId不存在");
        ThrowUtils.throwIf(ObjectUtils.notEqual(userId, apiInfo.getUserId()), StatusCode.NO_AUTH_ERROR);
        return joinPoint.proceed();
    }

    private Long extractApiId(Object[] args, String apiFieldName) {
        for (Object arg : args) {
            try {
                String methodName = "get" + apiFieldName.substring(0, 1).toUpperCase() + apiFieldName.substring(1);
                Method method = arg.getClass().getMethod(methodName);
                return (Long) method.invoke(arg);
            } catch (Exception e) {
                log.error("Exception:{}", e.getMessage(), e);
            }
        }
        throw new IllegalArgumentException("无法从请求参数中提取 apiId");
    }
}
