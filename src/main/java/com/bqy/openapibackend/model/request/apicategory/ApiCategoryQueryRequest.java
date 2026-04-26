package com.bqy.openapibackend.model.request.apicategory;

import com.bqy.openapibackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApiCategoryQueryRequest extends PageRequest {

    private String name;

    private String description;
}
