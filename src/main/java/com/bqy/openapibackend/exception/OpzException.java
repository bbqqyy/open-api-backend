package com.bqy.openapibackend.exception;

import com.bqy.openapibackend.common.StatusCode;

public class OpzException extends RuntimeException {

    private final int code;

    public OpzException(int code, String message) {
        super(message);
        this.code = code;
    }

    public OpzException(StatusCode statusCode) {
        this(statusCode.getCode(), statusCode.getMessage());
    }

    public OpzException(StatusCode statusCode, String message) {
        this(statusCode.getCode(), message);
    }

    public int getCode() {
        return code;
    }
}
