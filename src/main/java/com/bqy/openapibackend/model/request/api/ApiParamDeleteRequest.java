package com.bqy.openapibackend.model.request.api;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ApiParamDeleteRequest {

    @Positive(message = "Id必须大于0")
    private Long Id;

    @Positive(message = "apiId必须大于0")
    private Long apiId;
}
