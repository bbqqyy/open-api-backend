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
@TableName("api_param")
@Schema(description = "")
public class ApiParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 参数ID
     */
    @Schema(description = "参数ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * API ID
     */
    @Schema(description = "API ID")
    private Long apiId;

    /**
     * 参数名称
     */
    @Schema(description = "参数名称")
    private String paramName;

    /**
     * 参数类型
     */
    @Schema(description = "参数类型")
    private String paramType;

    /**
     * 是否必填 1是 0否
     */
    @Schema(description = "是否必填 1是 0否")
    private Integer required;

    /**
     * 参数说明
     */
    @Schema(description = "参数说明")
    private String description;
}
