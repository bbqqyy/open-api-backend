package com.bqy.openapibackend.model.request.api;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ApiLimitAddRequest {

    @Positive(message = "apiId必须大于等于0")
    private Long apiId;

    private Integer qps = 10;

    private Integer dailyLimit = 1000;
}
