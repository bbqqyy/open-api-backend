package com.bqy.openapibackend.util;

import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.exception.OpzException;

public class ThrowUtils {
    public static void throwIf(boolean condition) {
        if (condition) {
            throw new OpzException(StatusCode.PARAMS_ERROR);
        }
    }

    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new OpzException(StatusCode.PARAMS_ERROR, message);
        }
    }

    public static void throwIf(boolean condition, StatusCode statusCode) {
        if (condition) {
            throw new OpzException(statusCode);
        }
    }
}
