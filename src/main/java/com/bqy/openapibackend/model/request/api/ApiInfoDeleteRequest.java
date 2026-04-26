package com.bqy.openapibackend.model.request.api;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ApiInfoDeleteRequest {
    @Positive
    private Long id;
}
