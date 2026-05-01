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

            // 5. 按需检查每日生成次数限流（仅 countAsUsage=true 的接口计入）
            if (checkApiAnalysisAccess.countAsUsage()) {
                // key 格式：api:analysis:{apiId}  dailyKey 内部会追加 userId + 日期，按 (api, user, day) 隔离
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
                    "每个 API 每天最多可生成 " + DAILY_ANALYSIS_LIMIT + " 次分析报告"
                );
            }

            log.info("API 分析权限检查通过, apiId={}, userId={}, countAsUsage={}", apiId, userId, checkApiAnalysisAccess.countAsUsage());

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

