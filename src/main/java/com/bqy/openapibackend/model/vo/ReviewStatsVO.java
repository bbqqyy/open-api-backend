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
 * 审核统计分析 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "审核统计分析")
public class ReviewStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 待审核数
     */
    @Schema(description = "待审核数", example = "5")
    private Long pendingCount;

    /**
     * 已批准数
     */
    @Schema(description = "已批准数", example = "50")
    private Long approvedCount;

    /**
     * 已拒绝数
     */
    @Schema(description = "已拒绝数", example = "10")
    private Long rejectedCount;

    /**
     * 总数
     */
    @Schema(description = "总数", example = "65")
    private Long totalCount;

    /**
     * 批准率（%）
     */
    @Schema(description = "批准率（%）", example = "83.33")
    private Double approvalRate;

    /**
     * 拒绝率（%）
     */
    @Schema(description = "拒绝率（%）", example = "16.67")
    private Double rejectionRate;

    /**
     * 平均审核时间（小时）
     */
    @Schema(description = "平均审核时间（小时）", example = "24.5")
    private Double avgReviewTime;

    /**
     * 最快审核时间（分钟）
     */
    @Schema(description = "最快审核时间（分钟）", example = "15")
    private Long minReviewTime;

    /**
     * 最慢审核时间（小时）
     */
    @Schema(description = "最慢审核时间（小时）", example = "72")
    private Long maxReviewTime;

    /**
     * 本周新增
     */
    @Schema(description = "本周新增", example = "8")
    private Long weeklyNew;

    /**
     * 本月新增
     */
    @Schema(description = "本月新增", example = "25")
    private Long monthlyNew;

    /**
     * 按天统计的审核数
     */
    @Schema(description = "按天统计的审核数")
    private List<DailyStats> dailyStats;

    /**
     * 按审核员统计
     */
    @Schema(description = "按审核员统计")
    private List<ReviewerStats> reviewerStats;

    /**
     * 按 API 类别统计
     */
    @Schema(description = "按 API 类别统计")
    private List<CategoryStats> categoryStats;

    /**
     * 每日审核数统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStats {
        @Schema(description = "日期", example = "2026-04-27")
        private String date;

        @Schema(description = "当天待审核", example = "3")
        private Long pending;

        @Schema(description = "当天批准", example = "10")
        private Long approved;

        @Schema(description = "当天拒绝", example = "2")
        private Long rejected;

        @Schema(description = "当天平均审核时间（分钟）", example = "45")
        private Long avgTime;
    }

    /**
     * 审核员统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewerStats {
        @Schema(description = "审核员 ID", example = "1")
        private Long reviewerId;

        @Schema(description = "审核员名称", example = "admin")
        private String reviewerName;

        @Schema(description = "审核总数", example = "20")
        private Long totalReviewed;

        @Schema(description = "批准数", example = "18")
        private Long approved;

        @Schema(description = "拒绝数", example = "2")
        private Long rejected;

        @Schema(description = "批准率（%）", example = "90.0")
        private Double approvalRate;

        @Schema(description = "平均审核时间（分钟）", example = "30")
        private Long avgReviewTime;
    }

    /**
     * API 类别统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStats {
        @Schema(description = "分类 ID", example = "1")
        private Long categoryId;

        @Schema(description = "分类名称", example = "用户管理")
        private String categoryName;

        @Schema(description = "该分类 API 待审核", example = "2")
        private Long pending;

        @Schema(description = "该分类 API 已批准", example = "15")
        private Long approved;

        @Schema(description = "该分类 API 已拒绝", example = "3")
        private Long rejected;

        @Schema(description = "该分类 API 批准率（%）", example = "83.33")
        private Double approvalRate;
    }
}

