package com.bqy.openapibackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * API 分析报告 VO
 * 包含 API 调用数据分析、问题反映和优化建议
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 分析报告")
public class ApiAnalysisReportVO implements Serializable {

    /**
     * API 基础信息
     */
    @Schema(description = "API 基础信息")
    private ApiBasicInfo apiBasicInfo;

    /**
     * 调用统计信息
     */
    @Schema(description = "调用统计信息")
    private CallStatistics callStatistics;

    /**
     * 调用记录列表（最近100条）
     */
    @Schema(description = "调用记录列表")
    private List<CallLogItem> callLogs;

    /**
     * AI 分析问题列表
     */
    @Schema(description = "AI 分析发现的问题")
    private List<String> identifiedIssues;

    /**
     * AI 优化建议列表
     */
    @Schema(description = "AI 提供的优化建议")
    private List<String> optimizationSuggestions;

    /**
     * 报告生成时间
     */
    @Schema(description = "报告生成时间")
    private LocalDateTime generatedAt;

    /**
     * API 基础信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "API 基础信息")
    public static class ApiBasicInfo {
        @Schema(description = "API ID")
        private Long apiId;

        @Schema(description = "API 名称")
        private String apiName;

        @Schema(description = "API 描述")
        private String apiDescription;

        @Schema(description = "API URL")
        private String url;

        @Schema(description = "请求方法")
        private String method;

        @Schema(description = "API 状态")
        private Integer status;

        @Schema(description = "是否上线")
        private Integer isOnline;

        @Schema(description = "创建者")
        private String creatorName;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;
    }

    /**
     * 调用统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "调用统计信息")
    public static class CallStatistics {
        @Schema(description = "总调用次数")
        private Long totalCalls;

        @Schema(description = "成功调用次数")
        private Long successCalls;

        @Schema(description = "失败调用次数")
        private Long failCalls;

        @Schema(description = "成功率 (%)")
        private Double successRate;

        @Schema(description = "平均响应时间 (ms)")
        private Double avgResponseTime;

        @Schema(description = "最大响应时间 (ms)")
        private Long maxResponseTime;

        @Schema(description = "最小响应时间 (ms)")
        private Long minResponseTime;

        @Schema(description = "唯一调用用户数")
        private Long uniqueUserCount;
    }

    /**
     * 调用日志项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "调用日志项")
    public static class CallLogItem {
        @Schema(description = "日志 ID")
        private Long logId;

        @Schema(description = "调用用户 ID")
        private Long userId;

        @Schema(description = "请求参数")
        private String requestParam;

        @Schema(description = "响应时间 (ms)")
        private Long responseTime;

        @Schema(description = "调用状态 (success/fail)")
        private String status;

        @Schema(description = "调用时间")
        private LocalDateTime callTime;
    }
}

