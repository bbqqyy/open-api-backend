package com.bqy.openapibackend.model.request.api;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ApiLimitUpdateRequest {

    @Positive(message = "id必须大于0")
    private Long id;

    @Positive(message = "apiId必须大于0")
    private Long apiId;

    private Integer qps = 10;

    private Integer dailyLimit = 1000;
}
