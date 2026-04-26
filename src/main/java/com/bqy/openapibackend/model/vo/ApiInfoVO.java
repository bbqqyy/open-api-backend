package com.bqy.openapibackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ApiInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String apiName;

    private String apiDescription;

    private Integer isOnline;

    private String userName;
}
