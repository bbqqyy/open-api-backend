package com.bqy.openapibackend.model.request.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ApiInfoUpdateRequest {

    @Positive(message = "id必须大于0")
    private Long id;

    @NotBlank(message = "api名称不能为空")
    private String apiName;

    private String apiDescription;

    private Long categoryId;

    @NotBlank(message = "api地址不能为空")
    private String url;

    @NotNull(message = "请求方式不能为空")
    private Integer method;
}
