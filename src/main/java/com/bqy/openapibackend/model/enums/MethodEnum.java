package com.bqy.openapibackend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum MethodEnum {

    GET(0, "GET"),
    POST(1, "POST"),
    PUT(2, "PUT"),
    PATCH(3, "PATCH"),
    DELETE(4, "DELETE");


    private final int code;
    private final String name;

    public static String getNameByCode(int code) {
        return Arrays.stream(MethodEnum.values())
                .filter(e -> e.getCode() == code)
                .map(MethodEnum::getName)
                .findFirst()
                .orElse(null);
    }

}
