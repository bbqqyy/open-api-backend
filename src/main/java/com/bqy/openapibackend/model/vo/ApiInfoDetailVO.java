package com.bqy.openapibackend.model.vo;

import com.bqy.openapibackend.model.entity.ApiLimit;
import com.bqy.openapibackend.model.entity.ApiParam;
import com.bqy.openapibackend.model.entity.ApiResponseParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ApiInfoDetailVO {

    private String apiName;

    private String apiDescription;

    private String url;

    private Integer method;

    private Integer status;

    private Integer isOnline;

    private String userName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<ApiParam> paramList;

    private List<ApiResponseParam> apiResponseParamList;

    private ApiLimit apiLimit;
}
