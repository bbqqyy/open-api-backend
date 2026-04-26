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
@TableName("api_call_log")
@Schema(description = "ApiCallLog对象")
public class ApiCallLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @Schema(description = "日志ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * API ID
     */
    @Schema(description = "API ID")
    private Long apiId;

    /**
     * 调用用户
     */
    @Schema(description = "调用用户")
    private Long userId;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private String requestParam;

    /**
     * 响应时间(ms)
     */
    @Schema(description = "响应时间(ms)")
    private Long responseTime;

    /**
     * success/fail
     */
    @Schema(description = "success/fail")
    private String status;

    /**
     * 调用时间
     */
    @Schema(description = "调用时间")
    private LocalDateTime callTime;
}
