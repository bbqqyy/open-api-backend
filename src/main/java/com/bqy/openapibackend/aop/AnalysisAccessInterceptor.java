package com.bqy.openapibackend.aop;

import com.bqy.openapibackend.annotation.CheckApiAnalysisAccess;
import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.model.enums.ApiStatusEnum;
import com.bqy.openapibackend.util.LoginUserUtils;
import com.bqy.openapibackend.util.RedisRateLimiter;
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

/**
 * API 分析权限和限流拦截器
 *
 * 功能：
 * 1. 验证当前用户是否为 API 的所有者
 * 2. 验证 API 已发布（状态为 RELEASE_SUCCESS，即状态码 3）
 * 3. 检查每日分析使用次数限制（最多 5 次/天）
 */
@Aspect
@Component
@Slf4j
public class AnalysisAccessInterceptor {

    @Resource
    private ApiInfoDao apiInfoDao;

    @Resource
    private RedisRateLimiter redisRateLimiter;

    @Resource
    private LoginUserUtils loginUserUtils;

    /**
     * 分析功能的每日限流配额（次数）
     */
    private static final int DAILY_ANALYSIS_LIMIT = 5;

    @Around("@annotation(checkApiAnalysisAccess)")
    public Object checkAnalysisAccess(ProceedingJoinPoint joinPoint, CheckApiAnalysisAccess checkApiAnalysisAccess) throws Throwable {
        try {
            // 1. 获取当前登录用户和 API ID
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            User user = loginUserUtils.getLoginUser(request);
            Long userId = user.getId();

            Object[] args = joinPoint.getArgs();
            String apiFieldName = checkApiAnalysisAccess.apiFieldName();
            Long apiId = extractApiId(args, apiFieldName);

            // 2. 获取 API 信息
            ApiInfo apiInfo = apiInfoDao.getApiInfoById(apiId);
            ThrowUtils.throwIf(ObjectUtils.isEmpty(apiInfo), StatusCode.NOT_FOUND_ERROR, "API 不存在");

            // 3. 验证当前用户是否为 API 的所有者
            ThrowUtils.throwIf(
                ObjectUtils.notEqual(userId, apiInfo.getUserId()),
                StatusCode.NO_AUTH_ERROR,
                "只有 API 的所有者才能进行分析"
            );

            // 4. 验证 API 是否已发布
            ThrowUtils.throwIf(
                ObjectUtils.notEqual(apiInfo.getStatus(), ApiStatusEnum.RELEASE_SUCCESS.getCode()),
                StatusCode.UNPUBLISHED_API_ERROR,
                "该 API 未发布成功，无法进行分析"
            );

            // 5. 检查每日分析限流（最多 5 次/天）
            String rateLimitKey = "api:analysis:" + apiId;
            boolean allowed = redisRateLimiter.tryAcquire(
                rateLimitKey,
                userId,
                1,      // windowSeconds（不使用 QPS 限制，只用每日限流）
                100,    // maxQps（设置较大值，主要依赖 dailyLimit）
                DAILY_ANALYSIS_LIMIT
            );

            ThrowUtils.throwIf(
                !allowed,
                StatusCode.RATE_LIMIT_ERROR,
                "每天最多可进行 " + DAILY_ANALYSIS_LIMIT + " 次 API 分析"
            );

            log.info("API 分析权限检查通过, apiId={}, userId={}", apiId, userId);

            // 6. 权限验证通过，执行原方法
            return joinPoint.proceed();

        } catch (Throwable e) {
            // 重新抛出异常，由全局异常处理器处理
            throw e;
        }
    }

    /**
     * 从方法参数中提取 apiId
     *
     * @param args 方法参数
     * @param apiFieldName API 字段名称
     * @return API ID
     */
    private Long extractApiId(Object[] args, String apiFieldName) {
        for (Object arg : args) {
            try {
                String methodName = "get" + apiFieldName.substring(0, 1).toUpperCase() + apiFieldName.substring(1);
                Method method = arg.getClass().getMethod(methodName);
                return (Long) method.invoke(arg);
            } catch (Exception e) {
                // 继续尝试下一个参数
                log.debug("Failed to extract API ID from parameter", e);
            }
        }
        throw new IllegalArgumentException("无法从请求参数中提取 apiId");
    }
}

