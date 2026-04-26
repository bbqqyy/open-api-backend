package com.bqy.openapibackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
@Getter
@Setter
@ToString
@TableName("api_review")
@Schema(description = "")
public class ApiReview implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 审核ID
     */
    @Schema(description = "审核ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * API ID
     */
    @Schema(description = "API ID")
    private Long apiId;

    /**
     * 管理员ID
     */
    @Schema(description = "管理员ID")
    private Long adminId;

    /**
     * 审核结果 approved/rejected
     */
    @Schema(description = "审核结果 approved/rejected")
    private String result;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String comment;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;
}
