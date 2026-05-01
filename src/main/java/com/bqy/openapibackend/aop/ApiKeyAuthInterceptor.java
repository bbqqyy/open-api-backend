package com.bqy.openapibackend.aop;

import com.bqy.openapibackend.annotation.RequireApiKeyAuth;
import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.dao.UserDao;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.util.ApiKeyAuthUtils;
import com.bqy.openapibackend.util.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * API 密钥认证拦截器
 *
 * 用于验证跨系统 API 调用的身份
 * 通过 @RequireApiKeyAuth 注解标记需要认证的方法
 */
@Aspect
@Component
@Slf4j
public class ApiKeyAuthInterceptor {

    @Resource
    private UserDao userDao;

    /**
     * API 密钥认证请求头名称
     */
    private static final String HEADER_ACCESS_KEY = "X-Access-Key";
    private static final String HEADER_SIGNATURE = "X-Signature";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_NONCE = "X-Nonce";

    @Around("@annotation(requireApiKeyAuth)")
    public Object checkApiKeyAuth(ProceedingJoinPoint joinPoint, RequireApiKeyAuth requireApiKeyAuth) throws Throwable {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

            // 1. 获取认证信息
            String accessKey = request.getHeader(HEADER_ACCESS_KEY);
            String signature = request.getHeader(HEADER_SIGNATURE);
            String timestampStr = request.getHeader(HEADER_TIMESTAMP);
            String nonce = request.getHeader(HEADER_NONCE);

            // 2. 验证认证信息完整性
            validateAuthHeaders(accessKey, signature, timestampStr, nonce);

            // 3. 从数据库获取用户和密钥
            User user = userDao.lambdaQuery()
                    .eq(User::getAccessKey, accessKey)
                    .one();

            ThrowUtils.throwIf(
                user == null,
                StatusCode.NO_AUTH_ERROR,
                "AccessKey 无效"
            );

            ThrowUtils.throwIf(
                StringUtils.isBlank(user.getSecretKey()),
                StatusCode.NO_AUTH_ERROR,
                "用户未配置 SecretKey"
            );

            // 4. 提取请求信息
            Long timestamp = Long.parseLong(timestampStr);
            String method = request.getMethod();
            String path = request.getRequestURI();
            String body = getRequestBody(request);

            // 5. 验证签名
            boolean signatureValid = ApiKeyAuthUtils.verifySignature(
                method,
                path,
                timestamp,
                nonce,
                body,
                user.getSecretKey(),
                signature
            );

            ThrowUtils.throwIf(
                !signatureValid,
                StatusCode.NO_AUTH_ERROR,
                "签名验证失败，请检查 SecretKey 和签名生成方式"
            );

            // 6. 将用户信息存储到 request 属性，供后续使用
            request.setAttribute("apiAuthUser", user);

            log.info("API 密钥认证通过，用户: {}, AccessKey: {}", user.getUserName(), accessKey);

            // 7. 认证通过，执行原方法
            return joinPoint.proceed();

        } catch (Throwable e) {
            throw e;
        }
    }

    /**
     * 验证认证信息完整性
     */
    private void validateAuthHeaders(String accessKey, String signature, String timestamp, String nonce) {
        ThrowUtils.throwIf(
            StringUtils.isBlank(accessKey),
            StatusCode.NO_AUTH_ERROR,
            "缺少 X-Access-Key 请求头"
        );

        ThrowUtils.throwIf(
            StringUtils.isBlank(signature),
            StatusCode.NO_AUTH_ERROR,
            "缺少 X-Signature 请求头"
        );

        ThrowUtils.throwIf(
            StringUtils.isBlank(timestamp),
            StatusCode.NO_AUTH_ERROR,
            "缺少 X-Timestamp 请求头"
        );

        ThrowUtils.throwIf(
            StringUtils.isBlank(nonce),
            StatusCode.NO_AUTH_ERROR,
            "缺少 X-Nonce 请求头"
        );

        // 验证 timestamp 是否为数字
        try {
            Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            ThrowUtils.throwIf(
                true,
                StatusCode.PARAMS_ERROR,
                "X-Timestamp 必须是有效的毫秒级时间戳"
            );
        }
    }

    /**
     * 获取请求体内容
     *
     * <p>若 request 已经被 {@link ContentCachingRequestWrapper} 包装，
     * 则先触发一次读取来填充缓存，然后从缓存中获取内容，确保后续 Controller 仍可读取 body。
     * 若未包装，则直接读取（签名验证后 body 已消耗，但 GET/DELETE 无 body 不影响）。
     */
    private String getRequestBody(HttpServletRequest request) {
        try {
            if (request instanceof ContentCachingRequestWrapper cachingRequest) {
                // 触发内容读取填充缓存（如果尚未读取）
                byte[] content = cachingRequest.getContentAsByteArray();
                if (content.length == 0) {
                    // 缓存为空，尝试读取一次以触发缓存
                    org.springframework.util.StreamUtils.copyToByteArray(cachingRequest.getInputStream());
                    content = cachingRequest.getContentAsByteArray();
                }
                return new String(content, java.nio.charset.StandardCharsets.UTF_8);
            } else {
                // 未使用缓存包装，直接读取 Reader（注意：这会消耗 body，影响 POST 场景）
                StringBuilder body = new StringBuilder();
                try (BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }
                }
                return body.toString();
            }
        } catch (IOException e) {
            log.error("读取请求体失败", e);
            return "";
        }
    }
}

