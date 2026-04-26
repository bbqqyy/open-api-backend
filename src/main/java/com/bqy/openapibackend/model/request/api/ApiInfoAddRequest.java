package com.bqy.openapibackend.model.request.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
/**
 * API信息添加请求类
 * 用于封装添加API信息时的请求数据
 */
public class ApiInfoAddRequest {
    // 该类用于接收前端传递的添加API信息的请求参数
    @NotBlank(message = "api名称不能为空")
    private String apiName;

    private String apiDescription;

    private Long categoryId;

    @NotBlank(message = "api地址不能为空")
    private String url;

    @NotNull(message = "请求方式不能为空")
    private Integer method;
}
