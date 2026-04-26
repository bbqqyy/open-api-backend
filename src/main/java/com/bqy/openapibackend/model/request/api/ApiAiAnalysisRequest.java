package com.bqy.openapibackend.model.request.api;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApiAiAnalysisRequest {

    @NotNull(message = "apiId不能为空")
    private Long apiId;
}
