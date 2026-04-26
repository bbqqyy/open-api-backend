package com.bqy.openapibackend.model.request.apicategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApiCategoryAddRequest {
    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String description;

}
