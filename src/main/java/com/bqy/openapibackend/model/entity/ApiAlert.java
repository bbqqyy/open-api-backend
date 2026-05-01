package com.bqy.openapibackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * API 告警实体类
 * 记录由定时任务扫描发现的 API 异常告警信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("api_alert")
@Schema(description = "API 告警记录")
public class ApiAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "告警 ID")
    private Long id;

    @TableField("api_id")
    @Schema(description = "触发告警的 API ID")
    private Long apiId;

    @TableField("api_name")
    @Schema(description = "API 名称（冗余存储，方便查询）")
    private String apiName;

    /**
     * API 创建者 ID，告警通知的接收人
     */
    @TableField("owner_id")
    @Schema(description = "API 所有者 ID（告警通知对象）")
    private Long ownerId;

    /**
     * 告警类型：HIGH_FAIL_RATE / HIGH_RESPONSE_TIME / NO_CALLS
     */
    @TableField("alert_type")
    @Schema(description = "告警类型：HIGH_FAIL_RATE(高失败率) / HIGH_RESPONSE_TIME(高响应时间) / NO_CALLS(长时间无调用)")
    private String alertType;

    /**
     * 告警级别：WARNING / CRITICAL
     */
    @TableField("alert_level")
    @Schema(description = "告警级别：WARNING(警告) / CRITICAL(严重)")
    private String alertLevel;

    @TableField("alert_message")
    @Schema(description = "告警描述信息")
    private String alertMessage;

    /**
     * 触发告警时的实际失败率（百分比，0-100），高失败率告警时有值
     */
    @TableField("fail_rate")
    @Schema(description = "触发告警时的实际失败率（%），高失败率告警时有值")
    private BigDecimal failRate;

    /**
     * 触发告警时的平均响应时间（ms），高响应时间告警时有值
     */
    @TableField("avg_response_time")
    @Schema(description = "触发告警时的平均响应时间（ms）")
    private BigDecimal avgResponseTime;

    /**
     * 统计周期内总调用次数
     */
    @TableField("total_calls")
    @Schema(description = "统计周期内总调用次数")
    private Integer totalCalls;

    /**
     * 告警状态：ACTIVE（待处理）/ ACKNOWLEDGED（已确认）/ IGNORED（已忽略）
     */
    @TableField("status")
    @Schema(description = "告警状态：ACTIVE(待处理) / ACKNOWLEDGED(已确认) / IGNORED(已忽略)")
    private String status;

    @TableField("created_at")
    @Schema(description = "告警创建时间")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @Schema(description = "告警更新时间")
    private LocalDateTime updatedAt;
}

