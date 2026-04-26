package com.bqy.openapibackend.model.request.apicategory;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ApiCategoryDeleteRequest {
    @Positive
    private Long id;
}
