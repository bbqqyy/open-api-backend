package com.bqy.openapibackend.model.vo;

import lombok.Data;

@Data
public class ApiApplyVO {
    private Long id;
    private Long apiId;
    private String apiName;
    private Long ownerId;
    private String status;
}
