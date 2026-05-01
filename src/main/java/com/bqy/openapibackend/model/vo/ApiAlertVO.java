package com.bqy.openapibackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API 告警展示 VO
 */
@Data
@Builder
@Schema(description = "API 告警信息")
public class ApiAlertVO {

    @Schema(description = "告警 ID")
    private Long id;

    @Schema(description = "触发告警的 API ID")
    private Long apiId;

    @Schema(description = "API 名称")
    private String apiName;

    @Schema(description = "告警类型：HIGH_FAIL_RATE / HIGH_RESPONSE_TIME / NO_CALLS")
    private String alertType;

    @Schema(description = "告警类型中文标签")
    private String alertTypeLabel;

    @Schema(description = "告警级别：WARNING / CRITICAL")
    private String alertLevel;

    @Schema(description = "告警级别中文标签")
    private String alertLevelLabel;

    @Schema(description = "告警描述信息")
    private String alertMessage;

    @Schema(description = "触发告警时的实际失败率（%），高失败率告警时有值")
    private BigDecimal failRate;

    @Schema(description = "触发告警时的平均响应时间（ms）")
    private BigDecimal avgResponseTime;

    @Schema(description = "统计周期内总调用次数")
    private Integer totalCalls;

    @Schema(description = "告警状态：ACTIVE(待处理) / ACKNOWLEDGED(已确认) / IGNORED(已忽略)")
    private String status;

    @Schema(description = "告警状态中文标签")
    private String statusLabel;

    @Schema(description = "告警创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "告警更新时间")
    private LocalDateTime updatedAt;
}

