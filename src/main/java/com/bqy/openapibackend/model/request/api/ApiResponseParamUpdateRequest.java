package com.bqy.openapibackend.model.request.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ApiResponseParamUpdateRequest {
    @Positive(message = "id必须大于0")
    private Long id;

    @Positive(message = "apiId必须大于0")
    private Long apiId;

    @NotBlank(message = "字段名称不能为空")
    private String fieldName;

    @NotBlank(message = "字段类型不能为空")
    private String fieldType;

    private String description;
}
