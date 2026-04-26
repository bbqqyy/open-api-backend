package com.bqy.openapibackend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiOnlineEnum {

    ONLINE(0, "上线"),
    OFFLINE(1, "下线");

    private final int code;
    private final String message;
}
