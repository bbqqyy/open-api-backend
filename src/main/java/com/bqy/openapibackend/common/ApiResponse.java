package com.bqy.openapibackend.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiResponse<T> implements Serializable {
    private int code;

    private T data;

    private String message;

    public ApiResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public ApiResponse(int code, T data) {
        this(code, data, "");
    }

    public ApiResponse(StatusCode statusCode) {
        this(statusCode.getCode(), null, statusCode.getMessage());
    }

    public ApiResponse(StatusCode statusCode, T data) {
        this(statusCode.getCode(), data, statusCode.getMessage());
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(StatusCode.SUCCESS);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(StatusCode.SUCCESS, data);
    }

    public static <T> ApiResponse<T> fail(StatusCode statusCode) {
        return new ApiResponse<>(statusCode);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, null, message);
    }

    public static <T> ApiResponse<T> fail(int code, T data, String message) {
        return new ApiResponse<>(code, data, message);
    }

    public static <T> ApiResponse<T> fail(StatusCode statusCode, String message) {
        return new ApiResponse<>(statusCode.getCode(), null, message);
    }

    public static <T> ApiResponse<T> fail(StatusCode statusCode, T data, String message) {
        return new ApiResponse<>(statusCode.getCode(), data, message);
    }
}
