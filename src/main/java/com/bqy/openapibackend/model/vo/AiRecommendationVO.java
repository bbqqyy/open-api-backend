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
 * AI 推荐结果 VO
 *
 * 根据用户的自然语言描述，AI 分析用户需求并推荐适合的 API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 推荐结果")
public class AiRecommendationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * AI 对用户需求的理解和分析
     * 解释 AI 是如何理解用户的自然语言描述的
     */
    @Schema(
        description = "AI 对用户需求的理解和分析",
        example = "用户需要一个能够获取用户个人信息和订单历史的API解决方案"
    )
    private String requirementAnalysis;

    /**
     * 推荐的 API 列表
     * 根据用户需求，推荐最相关的 API
     */
    @Schema(description = "推荐的 API 列表")
    private List<RecommendedApiVO> recommendedApis;

    /**
     * 推荐理由总结
     * 解释为什么这些 API 适合用户的需求
     */
    @Schema(
        description = "推荐理由总结",
        example = "这些API组合能够满足你对用户信息和订单数据的全部需求..."
    )
    private String recommendationReason;

    /**
     * 是否找到了推荐的 API
     * true: 找到了相关的 API
     * false: 未找到相关的 API
     */
    @Schema(
        description = "是否找到了推荐的 API",
        example = "true"
    )
    private Boolean hasRecommendation;

    /**
     * 推荐的 API 详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "推荐的 API 详情")
    public static class RecommendedApiVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * API ID
         */
        @Schema(description = "API ID", example = "123")
        private Long apiId;

        /**
         * API 名称
         */
        @Schema(description = "API 名称", example = "获取用户信息 API")
        private String apiName;

        /**
         * API 描述
         */
        @Schema(
            description = "API 描述",
            example = "根据用户 ID 获取用户的个人信息，包括昵称、头像、邮箱等"
        )
        private String apiDescription;

        /**
         * API 分类名称
         */
        @Schema(description = "API 所属分类名称", example = "数据分析")
        private String categoryName;

        /**
         * API 请求方法
         * 0: GET, 1: POST, 2: PUT, 3: PATCH, 4: DELETE
         */
        @Schema(description = "HTTP 请求方法", example = "GET")
        private String httpMethod;

        /**
         * 平台调用路径（隐藏真实后端 URL，统一通过平台代理调用）
         * 格式：/apiInfo/invoke/{apiId}
         */
        @Schema(
            description = "平台调用路径，通过平台代理转发，不暴露真实后端地址",
            example = "/apiInfo/invoke/123"
        )
        private String invokeUrl;

        /**
         * 匹配度评分
         * 0-100，表示该 API 与用户需求的匹配程度
         */
        @Schema(
            description = "匹配度评分（0-100）",
            example = "95",
            minimum = "0",
            maximum = "100"
        )
        private Integer matchScore;

        /**
         * 为什么这个 API 适合用户的需求
         */
        @Schema(
            description = "推荐原因",
            example = "该API可以获取用户完整的个人信息，完全满足你的需求"
        )
        private String reason;

        /**
         * API 创建者
         */
        @Schema(description = "API 创建者", example = "admin")
        private String creatorName;
    }
}

