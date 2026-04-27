package com.bqy.openapibackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 仪表板首页展示数据
 * 用于前端首页展示系统的核心运营指标统计信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仪表板首页展示数据",
        title = "DashboardVO",
        example = "{\"totalApiCount\": 25, \"successRate\": 98.5, \"onlineUserCount\": 15, \"categoryCount\": 5}")
public class DashboardVO implements Serializable {

    /**
     * API 总数
     * 统计系统中所有已发布和未发布的 API 总数量
     */
    @Schema(description = "API 总数", example = "25", minimum = "0")
    private Long totalApiCount;

    /**
     * 接口调用成功率 (百分比，例如 98.5)
     * 计算公式: (成功调用次数 / 总调用次数) * 100，保留两位小数
     */
    @Schema(description = "接口调用成功率 (%)", example = "98.5", minimum = "0", maximum = "100")
    private Double successRate;

    /**
     * 在线用户数
     * 目前统计系统中的活跃用户数量
     */
    @Schema(description = "在线用户数", example = "15", minimum = "0")
    private Long onlineUserCount;

    /**
     * 接口类别总数
     * 统计系统中所有的 API 分类数量
     */
    @Schema(description = "接口类别数", example = "5", minimum = "0")
    private Long categoryCount;

    /**
     * 总调用次数
     * 统计所有用户对所有 API 的调用总次数
     */
    @Schema(description = "总调用次数", example = "1500", minimum = "0")
    private Long totalCallCount;

    /**
     * 成功调用次数
     * 统计状态为 'success' 的 API 调用次数
     */
    @Schema(description = "成功调用次数", example = "1477", minimum = "0")
    private Long successCallCount;

    /**
     * 失败调用次数
     * 统计状态为 'fail' 的 API 调用次数
     */
    @Schema(description = "失败调用次数", example = "23", minimum = "0")
    private Long failCallCount;

    /**
     * 各分类的 API 数量统计
     * 按 API 分类统计每个分类下的 API 数量
     */
    @Schema(description = "各分类的 API 数量统计列表")
    private List<CategoryStatVO> categoryStats;

    /**
     * 分类统计 VO
     * 统计每个 API 分类下的 API 数量
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分类统计信息",
            title = "CategoryStatVO",
            example = "{\"categoryName\": \"用户管理\", \"apiCount\": 8}")
    public static class CategoryStatVO {
        /**
         * 分类名称
         * API 分类的名称标识
         */
        @Schema(description = "分类名称", example = "用户管理", maxLength = 100)
        private String categoryName;

        /**
         * 该分类下的 API 数量
         * 指定分类中包含的 API 总数
         */
        @Schema(description = "该分类下的 API 数量", example = "8", minimum = "0")
        private Long apiCount;
    }
}

