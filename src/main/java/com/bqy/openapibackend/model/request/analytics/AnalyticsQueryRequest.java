package com.bqy.openapibackend.model.request.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分析查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分析查询请求")
public class AnalyticsQueryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * API ID（可选）
     */
    @Schema(description = "API ID", example = "1")
    private Long apiId;

    /**
     * 用户 ID（可选）
     */
    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    /**
     * 调用状态（可选）
     */
    @Schema(description = "调用状态 (success/fail)", example = "success")
    private String status;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间", example = "2026-04-01T00:00:00")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间", example = "2026-04-30T23:59:59")
    private LocalDateTime endTime;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private int pageNum = 1;

    /**
     * 每页数量
     */
    @Schema(description = "每页数量", example = "10")
    private int pageSize = 10;

    /**
     * 时间间隔（分钟，用于时间序列数据）
     */
    @Schema(description = "时间间隔（分钟）", example = "60")
    private int intervalMinutes = 60;
}

