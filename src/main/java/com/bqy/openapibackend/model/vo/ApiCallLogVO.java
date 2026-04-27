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
 * API 调用日志 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 调用日志")
public class ApiCallLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志 ID
     */
    @Schema(description = "日志 ID", example = "1")
    private Long id;

    /**
     * API ID
     */
    @Schema(description = "API ID", example = "123")
    private Long apiId;

    /**
     * API 名称
     */
    @Schema(description = "API 名称", example = "获取用户信息")
    private String apiName;

    /**
     * 调用用户 ID
     */
    @Schema(description = "调用用户 ID", example = "1")
    private Long userId;

    /**
     * 调用用户账号
     */
    @Schema(description = "调用用户账号", example = "user001")
    private String userAccount;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数", example = "{\"id\": \"123\"}")
    private String requestParam;

    /**
     * 响应时间 (毫秒)
     */
    @Schema(description = "响应时间(毫秒)", example = "150")
    private Long responseTime;

    /**
     * 调用状态
     */
    @Schema(description = "调用状态 (success/fail)", example = "success")
    private String status;

    /**
     * 调用时间
     */
    @Schema(description = "调用时间", example = "2026-04-27 10:30:00")
    private LocalDateTime callTime;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息（失败时返回）", example = "连接超时")
    private String errorMessage;
}

