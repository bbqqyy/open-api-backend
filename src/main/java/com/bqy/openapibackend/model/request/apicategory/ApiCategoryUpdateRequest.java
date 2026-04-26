package com.bqy.openapibackend.model.request.apicategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ApiCategoryUpdateRequest {

    @Positive
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String description;
}
