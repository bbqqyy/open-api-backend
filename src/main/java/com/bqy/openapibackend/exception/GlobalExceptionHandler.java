package com.bqy.openapibackend.exception;

import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.common.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException:" + e.getMessage(), e);
        return ApiResponse.fail(StatusCode.SYSTEM_ERROR, e.getMessage());
    }

    @ExceptionHandler(OpzException.class)
    public ApiResponse<?> handleOpzException(OpzException e) {
        log.error("OpzException:" + e.getMessage(), e);
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        // 获取第一个验证错误信息
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("参数验证失败");
        return ApiResponse.fail(StatusCode.PARAMS_ERROR, errorMessage);
    }
}
