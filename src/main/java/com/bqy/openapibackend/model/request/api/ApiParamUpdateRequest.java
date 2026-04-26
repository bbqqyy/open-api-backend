package com.bqy.openapibackend.model.request.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ApiParamUpdateRequest {
    @Positive(message = "Id必须大于0")
    private Long Id;

    @Positive(message = "apiId必须大于0")
    private Long apiId;

    @NotBlank(message = "参数名称不能为空")
    private String paramName;

    @NotBlank(message = "参数类型不能为空")
    private String paramType;

    private Integer required = 0;

    private String description;
}
