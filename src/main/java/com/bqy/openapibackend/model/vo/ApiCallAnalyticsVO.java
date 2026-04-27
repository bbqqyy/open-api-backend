package com.bqy.openapibackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * API 调用分析数据 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 调用分析数据")
public class ApiCallAnalyticsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 统计概览
     */
    @Schema(description = "统计概览")
    private Overview overview;

    /**
     * 时间序列数据（用于趋势图）
     */
    @Schema(description = "时间序列数据")
    private List<TimeSeriesData> timeSeries;

    /**
     * 响应时间分布
     */
    @Schema(description = "响应时间分布")
    private ResponseTimeDistribution responseTimeDistribution;

    /**
     * 状态分布
     */
    @Schema(description = "状态分布")
    private StatusDistribution statusDistribution;

    /**
     * Top 调用 API
     */
    @Schema(description = "Top 调用 API")
    private List<TopApiData> topApis;

    /**
     * Top 调用用户
     */
    @Schema(description = "Top 调用用户")
    private List<TopUserData> topUsers;

    /**
     * 统计概览
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Overview {
        @Schema(description = "总调用数", example = "1000")
        private long totalCalls;

        @Schema(description = "成功数", example = "950")
        private long successCount;

        @Schema(description = "失败数", example = "50")
        private long failureCount;

        @Schema(description = "成功率(%)", example = "95.0")
        private double successRate;

        @Schema(description = "平均响应时间(ms)", example = "120.5")
        private double avgResponseTime;

        @Schema(description = "最小响应时间(ms)", example = "10")
        private long minResponseTime;

        @Schema(description = "最大响应时间(ms)", example = "5000")
        private long maxResponseTime;

        @Schema(description = "中位数响应时间(ms)", example = "100")
        private long medianResponseTime;

        @Schema(description = "95分位响应时间(ms)", example = "800")
        private long p95ResponseTime;

        @Schema(description = "99分位响应时间(ms)", example = "2000")
        private long p99ResponseTime;
    }

    /**
     * 时间序列数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesData {
        @Schema(description = "时间点", example = "2026-04-27 10:00")
        private String timestamp;

        @Schema(description = "总调用数", example = "100")
        private long totalCount;

        @Schema(description = "成功数", example = "95")
        private long successCount;

        @Schema(description = "失败数", example = "5")
        private long failureCount;

        @Schema(description = "平均响应时间", example = "120")
        private long avgResponseTime;
    }

    /**
     * 响应时间分布
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseTimeDistribution {
        @Schema(description = "0-100ms", example = "300")
        private long veryFast;

        @Schema(description = "100-500ms", example = "500")
        private long fast;

        @Schema(description = "500-1000ms", example = "150")
        private long normal;

        @Schema(description = "1000-5000ms", example = "40")
        private long slow;

        @Schema(description = ">5000ms", example = "10")
        private long verySlow;
    }

    /**
     * 状态分布
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusDistribution {
        @Schema(description = "成功", example = "950")
        private long success;

        @Schema(description = "失败", example = "50")
        private long failure;
    }

    /**
     * Top API 数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopApiData {
        @Schema(description = "API ID", example = "123")
        private Long apiId;

        @Schema(description = "API 名称", example = "获取用户信息")
        private String apiName;

        @Schema(description = "调用次数", example = "500")
        private long callCount;

        @Schema(description = "成功数", example = "475")
        private long successCount;

        @Schema(description = "失败数", example = "25")
        private long failureCount;

        @Schema(description = "成功率(%)", example = "95.0")
        private double successRate;

        @Schema(description = "平均响应时间(ms)", example = "150")
        private long avgResponseTime;
    }

    /**
     * Top 用户数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopUserData {
        @Schema(description = "用户 ID", example = "1")
        private Long userId;

        @Schema(description = "用户账号", example = "user001")
        private String userAccount;

        @Schema(description = "调用次数", example = "300")
        private long callCount;

        @Schema(description = "成功数", example = "285")
        private long successCount;

        @Schema(description = "失败数", example = "15")
        private long failureCount;

        @Schema(description = "成功率(%)", example = "95.0")
        private double successRate;
    }
}

