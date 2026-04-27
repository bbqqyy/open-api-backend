package com.bqy.openapibackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API 分析报告实体类
 * 用于存储 AI 生成的 API 分析报告数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("api_analysis_report")
@Schema(description = "API 分析报告")
public class ApiAnalysisReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "报告 ID")
    private Long id;

    @TableField("api_id")
    @Schema(description = "API ID")
    private Long apiId;

    @TableField("report_content")
    @Schema(description = "AI 分析报告内容（JSON格式）")
    private String reportContent;

    @TableField("summary")
    @Schema(description = "报告摘要")
    private String summary;

    @TableField("identified_issues")
    @Schema(description = "识别的问题列表（JSON格式）")
    private String identifiedIssues;

    @TableField("optimization_suggestions")
    @Schema(description = "优化建议列表（JSON格式）")
    private String optimizationSuggestions;

    @TableField("success_rate")
    @Schema(description = "API 成功率 (%)")
    private BigDecimal successRate;

    @TableField("total_calls")
    @Schema(description = "统计周期内总调用次数")
    private Integer totalCalls;

    @TableField("avg_response_time")
    @Schema(description = "平均响应时间 (ms)")
    private BigDecimal avgResponseTime;

    @TableField("max_response_time")
    @Schema(description = "最大响应时间 (ms)")
    private Long maxResponseTime;

    @TableField("status")
    @Schema(description = "报告状态: generated(生成中), completed(已完成), failed(生成失败)")
    private String status;

    @TableField("error_message")
    @Schema(description = "生成失败时的错误信息")
    private String errorMessage;

    @TableField("analysis_time_ms")
    @Schema(description = "AI 分析耗时 (毫秒)")
    private Long analysisTimeMs;

    @TableField("created_at")
    @Schema(description = "报告生成时间")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}

