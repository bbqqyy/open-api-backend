package com.bqy.openapibackend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiStatusEnum {

    WAITING_RELEASE(0, "待发布"),
    RELEASING(1, "发布中"),
    RELEASE_FAIL(2, "发布失败"),
    RELEASE_SUCCESS(3, "发布成功");

    private final int code;

    private final String message;


}
