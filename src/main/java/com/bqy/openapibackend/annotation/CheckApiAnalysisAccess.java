package com.bqy.openapibackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 检查 API 分析权限和限流的注解
 *
 * 功能：
 * 1. 验证 API 的所有者身份
 * 2. 验证 API 已发布（状态为 3）
 * 3. 检查每日分析使用次数限制（最多 5 次/天）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckApiAnalysisAccess {
    /**
     * API ID 的参数字段名，默认为 "apiId"
     */
    String apiFieldName() default "apiId";

    /**
     * 是否将本次请求计入每日使用次数（默认 true）
     * 设置为 false 时只做权限校验，不消耗限流配额
     */
    boolean countAsUsage() default true;
}

