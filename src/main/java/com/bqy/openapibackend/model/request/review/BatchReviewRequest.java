package com.bqy.openapibackend.model.request.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量审核请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量审核请求")
public class BatchReviewRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * API ID 列表
     */
    @Schema(description = "API ID 列表", example = "[1, 2, 3]")
    private List<Long> apiIds;

    /**
     * 审核结果
     */
    @Schema(description = "审核结果 (approved/rejected)", example = "approved")
    private String result;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见", example = "已验证，可发布")
    private String comment;

    /**
     * 优先级
     */
    @Schema(description = "处理优先级 (high/normal/low)", example = "normal")
    private String priority;

    /**
     * 标签（用于分类）
     */
    @Schema(description = "标签", example = "[\"important\", \"urgent\"]")
    private List<String> tags;
}

