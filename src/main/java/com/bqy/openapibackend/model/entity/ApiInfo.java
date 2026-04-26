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
@TableName("api_info")
@Schema(description = "")
public class ApiInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * API ID
     */
    @Schema(description = "API ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * API名称
     */
    @Schema(description = "API名称")
    private String apiName;

    /**
     * API描述
     */
    @Schema(description = "API描述")
    private String apiDescription;

    /**
     * 分类ID
     */
    @Schema(description = "分类ID")
    private Long categoryId;

    /**
     * 请求地址
     */
    @Schema(description = "请求地址")
    private String url;

    /**
     * 0 GET/1 POST/2 PUT/3 PATCH/4 DELETE
     */
    @Schema(description = "0 GET/1 POST/2 PUT/3 PATCH/4 DELETE")
    private Integer method;

    /**
     * 状态 0待发布/1发布中/2发布失败/3发布成功
     */
    @Schema(description = "状态 0待发布/1发布中/2发布失败/3发布成功")
    private Integer status;

    /**
     * 0 下线/1 上线
     */
    @Schema(description = "0 下线/1 上线")
    private Integer isOnline;

    /**
     * 创建用户
     */
    @Schema(description = "创建用户")
    private Long userId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
