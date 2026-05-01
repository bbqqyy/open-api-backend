package com.bqy.openapibackend.model.request.api;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ApiLimitDeleteRequest {

    @Positive(message = "id必须大于0")
    private Long id;

    @Positive(message = "apiId必须大于0")
    private Long apiId;

}
