package com.bqy.openapibackend.model.request.analytics;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 生成并保存 API 分析报告请求体
 */
@Data
@Schema(description = "生成并保存 API 分析报告请求体")
public class ApiAnalysisSaveRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "apiId 不能为空")
    @Schema(description = "API ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long apiId;
}

