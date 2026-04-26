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
@TableName("api_statistics")
@Schema(description = "")
public class ApiStatistics implements Serializable {

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
     * 调用次数
     */
    @Schema(description = "调用次数")
    private Integer callCount;

    /**
     * 成功次数
     */
    @Schema(description = "成功次数")
    private Integer successCount;

    /**
     * 失败次数
     */
    @Schema(description = "失败次数")
    private Integer failCount;

    /**
     * 统计日期
     */
    @Schema(description = "统计日期")
    private LocalDateTime statDate;
}
