package com.bqy.openapibackend.model.request.api;

import com.bqy.openapibackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApplyApiQueryRequest extends PageRequest {
    private String status;
}
