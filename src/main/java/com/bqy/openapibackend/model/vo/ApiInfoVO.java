package com.bqy.openapibackend.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ApiInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String apiName;

    private String apiDescription;

    private String url;

    private Long categoryId;

    private String categoryName;

    private Integer status;

    private Integer isOnline;

    /** 请求方法：0 GET / 1 POST / 2 PUT / 3 PATCH / 4 DELETE */
    private Integer method;

    private String userName;

    private LocalDateTime createTime;
}
