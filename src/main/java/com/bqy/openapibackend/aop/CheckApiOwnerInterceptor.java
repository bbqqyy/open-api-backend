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

    @Resource
    private LoginUserUtils loginUserUtils;

    @Around("@annotation(checkApiOwner)")
    public Object checkApiOwner(ProceedingJoinPoint joinPoint, CheckApiOwner checkApiOwner) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        User user = loginUserUtils.getLoginUser(request);
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
        // 第一轮：优先从请求体对象中通过反射提取（支持 @RequestBody 场景）
        for (Object arg : args) {
            if (arg == null || arg instanceof Long || arg instanceof HttpServletRequest
                    || arg instanceof jakarta.servlet.http.HttpServletResponse) {
                continue;
            }
            try {
                String methodName = "get" + apiFieldName.substring(0, 1).toUpperCase() + apiFieldName.substring(1);
                Method method = arg.getClass().getMethod(methodName);
                Object value = method.invoke(arg);
                if (value instanceof Long) {
                    return (Long) value;
                }
            } catch (Exception e) {
                log.debug("Failed to extract API ID from parameter via getter", e);
            }
        }
        // 第二轮：尝试直接匹配 Long 类型参数（支持 @PathVariable Long apiId 场景）
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        throw new IllegalArgumentException("无法从请求参数中提取 apiId");
    }
}
