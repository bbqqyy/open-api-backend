package com.bqy.openapibackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * API 分析报告列表项 VO
 * 用于返回分析报告的摘要信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 分析报告列表项")
public class ApiAnalysisReportListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "报告 ID", example = "1")
    private Long reportId;

    @Schema(description = "API ID", example = "1")
    private Long apiId;

    @Schema(description = "报告摘要", example = "该 API 的调用成功率为 98%，建议优化数据库查询...")
    private String summary;

    @Schema(description = "报告状态", example = "completed")
    private String status;

    @Schema(description = "API 成功率 (%)", example = "98.5")
    private BigDecimal successRate;

    @Schema(description = "总调用次数", example = "1000")
    private Integer totalCalls;

    @Schema(description = "平均响应时间 (ms)", example = "120.5")
    private BigDecimal avgResponseTime;

    @Schema(description = "最大响应时间 (ms)", example = "1200")
    private Long maxResponseTime;

    @Schema(description = "AI 分析耗时 (毫秒)", example = "3500")
    private Long analysisTimeMs;

    @Schema(description = "报告生成时间", example = "2025-04-27T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "错误信息（仅当状态为 failed 时）")
    private String errorMessage;

    @Schema(description = "AI 识别的问题列表")
    private List<String> identifiedIssues;

    @Schema(description = "AI 优化建议列表")
    private List<String> optimizationSuggestions;
}

