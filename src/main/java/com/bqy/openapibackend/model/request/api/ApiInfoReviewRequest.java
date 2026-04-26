package com.bqy.openapibackend.model.request.api;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ApiInfoReviewRequest {

    @Positive(message = "apiId必须大于0")
    private Long apiId;

    private String comment;

}
