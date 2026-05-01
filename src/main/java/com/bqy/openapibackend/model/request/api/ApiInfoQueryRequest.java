package com.bqy.openapibackend.model.request.api;

import com.bqy.openapibackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApiInfoQueryRequest extends PageRequest {

    private Long categoryId;

    private String apiName;

    private Integer status;

    private String apiDescription;

    private Integer isOnline;
}
