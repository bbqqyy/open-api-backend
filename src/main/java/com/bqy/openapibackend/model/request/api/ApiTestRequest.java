package com.bqy.openapibackend.model.request.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * API 测试请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 测试请求")
public class ApiTestRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * API ID
     */
    @Schema(description = "API ID", example = "1")
    private Long apiId;

    /**
     * 请求 URL
     */
    @Schema(description = "请求 URL", example = "http://localhost:8080/api/test")
    private String url;

    /**
     * 请求方法
     */
    @Schema(description = "请求方法 (GET/POST/PUT/DELETE)", example = "GET")
    private String method;

    /**
     * 请求头
     */
    @Schema(description = "请求头")
    private Map<String, String> headers;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private Map<String, Object> params;

    /**
     * 请求体
     */
    @Schema(description = "请求体")
    private Object body;

    /**
     * 超时时间（秒）
     */
    @Schema(description = "超时时间（秒）", example = "30")
    private Integer timeout;

    /**
     * 测试名称
     */
    @Schema(description = "测试名称", example = "基础功能测试")
    private String testName;

    /**
     * 测试场景
     */
    @Schema(description = "测试场景", example = "正常请求")
    private String scenario;

    /**
     * 期望响应状态码
     */
    @Schema(description = "期望响应状态码", example = "200")
    private Integer expectedStatusCode;

    /**
     * 期望响应包含的关键字
     */
    @Schema(description = "期望响应包含的关键字")
    private String expectedKeywords;

    /**
     * 是否生成测试数据
     */
    @Schema(description = "是否自动生成测试数据", example = "true")
    private Boolean autoGenerateTestData;

    /**
     * 测试类型
     */
    @Schema(description = "测试类型 (functional/performance/security)", example = "functional")
    private String testType;
}

