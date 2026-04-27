package com.bqy.openapibackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * API 测试工具 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 测试工具")
public class ApiTestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 测试请求
     */
    @Schema(description = "测试请求")
    private TestRequest request;

    /**
     * 测试响应
     */
    @Schema(description = "测试响应")
    private TestResponse response;

    /**
     * 测试报告
     */
    @Schema(description = "测试报告")
    private TestReport report;

    /**
     * 测试请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestRequest {
        @Schema(description = "请求方法", example = "GET")
        private String method;

        @Schema(description = "请求 URL", example = "http://localhost:8080/api/test")
        private String url;

        @Schema(description = "请求头")
        private Map<String, String> headers;

        @Schema(description = "请求参数")
        private Map<String, Object> params;

        @Schema(description = "请求体")
        private Object body;

        @Schema(description = "超时时间（秒）", example = "30")
        private Integer timeout;
    }

    /**
     * 测试响应
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestResponse {
        @Schema(description = "HTTP 状态码", example = "200")
        private Integer statusCode;

        @Schema(description = "响应时间（毫秒）", example = "150")
        private Long responseTime;

        @Schema(description = "响应头")
        private Map<String, String> headers;

        @Schema(description = "响应体")
        private Object body;

        @Schema(description = "响应大小（字节）", example = "1024")
        private Long responseSize;

        @Schema(description = "错误信息")
        private String errorMessage;

        @Schema(description = "是否成功", example = "true")
        private Boolean success;
    }

    /**
     * 测试报告
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestReport {
        @Schema(description = "测试 ID", example = "1")
        private Long testId;

        @Schema(description = "API ID", example = "123")
        private Long apiId;

        @Schema(description = "测试名称", example = "基础功能测试")
        private String testName;

        @Schema(description = "测试场景", example = "正常请求")
        private String scenario;

        @Schema(description = "测试结果 (pass/fail/error)", example = "pass")
        private String result;

        @Schema(description = "总用时（毫秒）", example = "500")
        private Long totalTime;

        @Schema(description = "测试用例数", example = "10")
        private Integer testCaseCount;

        @Schema(description = "通过用例数", example = "10")
        private Integer passedCaseCount;

        @Schema(description = "失败用例数", example = "0")
        private Integer failedCaseCount;

        @Schema(description = "错误用例数", example = "0")
        private Integer errorCaseCount;

        @Schema(description = "成功率（%）", example = "100")
        private Double successRate;

        @Schema(description = "平均响应时间（毫秒）", example = "150")
        private Long avgResponseTime;

        @Schema(description = "最小响应时间（毫秒）", example = "80")
        private Long minResponseTime;

        @Schema(description = "最大响应时间（毫秒）", example = "300")
        private Long maxResponseTime;

        @Schema(description = "测试用例详情")
        private List<TestCaseDetail> testCases;

        @Schema(description = "性能评级 (A/B/C/D)", example = "A")
        private String performanceRating;

        @Schema(description = "测试建议")
        private List<String> suggestions;
    }

    /**
     * 测试用例详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseDetail {
        @Schema(description = "用例 ID", example = "1")
        private Long caseId;

        @Schema(description = "用例名称", example = "测试有效请求")
        private String caseName;

        @Schema(description = "预期结果", example = "返回 200")
        private String expectedResult;

        @Schema(description = "实际结果", example = "返回 200")
        private String actualResult;

        @Schema(description = "用例状态 (pass/fail/error)", example = "pass")
        private String status;

        @Schema(description = "执行时间（毫秒）", example = "150")
        private Long executionTime;

        @Schema(description = "错误信息")
        private String errorMessage;
    }
}

