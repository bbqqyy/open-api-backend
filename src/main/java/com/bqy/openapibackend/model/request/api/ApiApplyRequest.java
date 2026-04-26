package com.bqy.openapibackend.model.request.api;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ApiApplyRequest {

    @Positive(message = "id必须大于0")
    private Long id;

    @Positive
    private Long apiId;
}
