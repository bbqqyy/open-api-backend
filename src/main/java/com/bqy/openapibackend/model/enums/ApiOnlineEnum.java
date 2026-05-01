package com.bqy.openapibackend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiOnlineEnum {

    OFFLINE(0, "下线"),
    ONLINE(1, "上线");

    private final int code;
    private final String message;
}
