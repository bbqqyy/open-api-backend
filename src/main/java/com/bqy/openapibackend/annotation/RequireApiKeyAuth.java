package com.bqy.openapibackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 密钥认证注解
 *
 * 用于标记需要 API 密钥认证的 API 端点
 * 通常用于跨系统调用场景
 *
 * 使用方式:
 * @RequireApiKeyAuth
 * @GetMapping("/some-endpoint")
 * public ApiResponse<Object> someEndpoint() {
 *     ...
 * }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireApiKeyAuth {
    /**
     * 是否同时支持 Session 认证
     *
     * true: 支持 Session 或 API Key 双向认证
     * false: 只支持 API Key 认证，不支持 Session
     */
    boolean allowSessionAuth() default true;
}

