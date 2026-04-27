package com.bqy.openapibackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核进度 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "审核进度")
public class ReviewProgressVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * API ID
     */
    @Schema(description = "API ID", example = "1")
    private Long apiId;

    /**
     * API 名称
     */
    @Schema(description = "API 名称", example = "获取用户信息")
    private String apiName;

    /**
     * 当前状态
     */
    @Schema(description = "当前状态 (draft/releasing/approved/rejected)", example = "releasing")
    private String status;

    /**
     * 提交时间
     */
    @Schema(description = "提交时间", example = "2026-04-27T10:30:00")
    private LocalDateTime submitTime;

    /**
     * 审核用时（分钟）
     */
    @Schema(description = "审核用时（分钟）", example = "120")
    private Long reviewDuration;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见", example = "需要完善文档")
    private String reviewComment;

    /**
     * 审核人
     */
    @Schema(description = "审核人", example = "admin")
    private String reviewerName;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间", example = "2026-04-27T12:30:00")
    private LocalDateTime reviewTime;

    /**
     * 预计审核时间（工作小时）
     */
    @Schema(description = "预计审核时间（工作小时）", example = "24")
    private Integer estimatedReviewHours;

    /**
     * 进度百分比（0-100）
     */
    @Schema(description = "进度百分比", example = "50")
    private Integer progressPercentage;
}

