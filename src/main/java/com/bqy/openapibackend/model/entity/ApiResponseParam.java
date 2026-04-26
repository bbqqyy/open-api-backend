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
@TableName("api_response_param")
@Schema(description = "")
public class ApiResponseParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * API ID
     */
    @Schema(description = "API ID")
    private Long apiId;

    /**
     * 字段名称
     */
    @Schema(description = "字段名称")
    private String fieldName;

    /**
     * 字段类型
     */
    @Schema(description = "字段类型")
    private String fieldType;

    /**
     * 字段说明
     */
    @Schema(description = "字段说明")
    private String description;
}
