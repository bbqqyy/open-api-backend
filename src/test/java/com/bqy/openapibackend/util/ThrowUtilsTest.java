package com.bqy.openapibackend.util;

import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.exception.OpzException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ThrowUtils 单元测试
 * 验证条件判断抛异常工具类的各种重载行为
 */
@DisplayName("ThrowUtils 工具类测试")
class ThrowUtilsTest {

    // ===================== throwIf(condition) =====================

    @Test
    @DisplayName("条件为 false：不抛出异常")
    void throwIf_conditionFalse_noException() {
        assertDoesNotThrow(() -> ThrowUtils.throwIf(false));
    }

    @Test
    @DisplayName("条件为 true：抛出 OpzException，默认 PARAMS_ERROR")
    void throwIf_conditionTrue_throwsParamsError() {
        OpzException ex = assertThrows(OpzException.class, () -> ThrowUtils.throwIf(true));
        assertEquals(StatusCode.PARAMS_ERROR.getCode(), ex.getCode());
    }

    // ===================== throwIf(condition, message) =====================

    @Test
    @DisplayName("条件为 false + message：不抛出异常")
    void throwIf_conditionFalse_message_noException() {
        assertDoesNotThrow(() -> ThrowUtils.throwIf(false, "不应触发"));
    }

    @Test
    @DisplayName("条件为 true + 自定义消息：抛出 OpzException，消息正确")
    void throwIf_conditionTrue_withMessage_correctMessage() {
        OpzException ex = assertThrows(OpzException.class,
                () -> ThrowUtils.throwIf(true, "账号已存在"));
        assertEquals("账号已存在", ex.getMessage());
        assertEquals(StatusCode.PARAMS_ERROR.getCode(), ex.getCode());
    }

    // ===================== throwIf(condition, statusCode) =====================

    @Test
    @DisplayName("条件为 true + StatusCode：抛出对应 code 的 OpzException")
    void throwIf_conditionTrue_withStatusCode_correctCode() {
        OpzException ex = assertThrows(OpzException.class,
                () -> ThrowUtils.throwIf(true, StatusCode.NOT_LOGIN_ERROR));
        assertEquals(StatusCode.NOT_LOGIN_ERROR.getCode(), ex.getCode());
        assertEquals(StatusCode.NOT_LOGIN_ERROR.getMessage(), ex.getMessage());
    }

    @Test
    @DisplayName("条件为 false + StatusCode：不抛出异常")
    void throwIf_conditionFalse_withStatusCode_noException() {
        assertDoesNotThrow(() -> ThrowUtils.throwIf(false, StatusCode.NOT_LOGIN_ERROR));
    }

    // ===================== throwIf(condition, statusCode, message) =====================

    @Test
    @DisplayName("条件为 true + StatusCode + message：抛出正确 code 和 message")
    void throwIf_conditionTrue_statusCode_message_correctBoth() {
        OpzException ex = assertThrows(OpzException.class,
                () -> ThrowUtils.throwIf(true, StatusCode.NO_AUTH_ERROR, "无权限访问此资源"));
        assertEquals(StatusCode.NO_AUTH_ERROR.getCode(), ex.getCode());
        assertEquals("无权限访问此资源", ex.getMessage());
    }

    @Test
    @DisplayName("条件为 false + StatusCode + message：不抛出异常")
    void throwIf_conditionFalse_statusCode_message_noException() {
        assertDoesNotThrow(() -> ThrowUtils.throwIf(false, StatusCode.NO_AUTH_ERROR, "无权限"));
    }

    // ===================== 边界：所有 StatusCode 都可使用 =====================

    @Test
    @DisplayName("各 StatusCode 都能正确携带 code 传递")
    void throwIf_allStatusCodes_correctCodes() {
        for (StatusCode sc : StatusCode.values()) {
            OpzException ex = assertThrows(OpzException.class,
                    () -> ThrowUtils.throwIf(true, sc));
            assertEquals(sc.getCode(), ex.getCode(),
                    sc.name() + " code 不匹配");
        }
    }
}

