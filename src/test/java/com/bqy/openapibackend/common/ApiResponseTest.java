package com.bqy.openapibackend.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiResponse 统一响应类单元测试
 * 验证成功/失败响应的构建与字段正确性
 */
@DisplayName("统一响应类测试")
class ApiResponseTest {

    // ===================== 成功响应测试 =====================

    @Test
    @DisplayName("success() 无数据：code=200, data=null")
    void success_noData_correctCodeAndNullData() {
        ApiResponse<Void> response = ApiResponse.success();

        assertEquals(StatusCode.SUCCESS.getCode(), response.getCode());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("success(data) 有数据：code=200, data 正确")
    void success_withData_correctCodeAndData() {
        String payload = "hello world";
        ApiResponse<String> response = ApiResponse.success(payload);

        assertEquals(StatusCode.SUCCESS.getCode(), response.getCode());
        assertEquals(payload, response.getData());
    }

    @Test
    @DisplayName("success(data) 支持任意泛型类型")
    void success_genericType_correctData() {
        record UserInfo(Long id, String name) {}
        UserInfo user = new UserInfo(1L, "张三");

        ApiResponse<UserInfo> response = ApiResponse.success(user);

        assertEquals(StatusCode.SUCCESS.getCode(), response.getCode());
        assertEquals(1L, response.getData().id());
        assertEquals("张三", response.getData().name());
    }

    // ===================== 失败响应测试 =====================

    @Test
    @DisplayName("fail(StatusCode) 返回正确错误码和消息")
    void fail_withStatusCode_correctCodeAndMessage() {
        ApiResponse<Void> response = ApiResponse.fail(StatusCode.NOT_LOGIN_ERROR);

        assertEquals(StatusCode.NOT_LOGIN_ERROR.getCode(), response.getCode());
        assertEquals(StatusCode.NOT_LOGIN_ERROR.getMessage(), response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("fail(StatusCode, message) 自定义消息覆盖默认消息")
    void fail_withCustomMessage_overridesDefaultMessage() {
        String customMsg = "请先完成实名认证后再操作";
        ApiResponse<Void> response = ApiResponse.fail(StatusCode.NO_AUTH_ERROR, customMsg);

        assertEquals(StatusCode.NO_AUTH_ERROR.getCode(), response.getCode());
        assertEquals(customMsg, response.getMessage());
    }

    @Test
    @DisplayName("fail(code, message) 自定义 code 和消息")
    void fail_withCustomCodeAndMessage_correctFields() {
        ApiResponse<Void> response = ApiResponse.fail(99999, "自定义错误");

        assertEquals(99999, response.getCode());
        assertEquals("自定义错误", response.getMessage());
    }

    // ===================== StatusCode 枚举完整性测试 =====================

    @Test
    @DisplayName("所有 StatusCode 枚举值 code 应大于 0")
    void statusCode_allValues_positiveCode() {
        for (StatusCode sc : StatusCode.values()) {
            assertTrue(sc.getCode() > 0, sc.name() + " 的 code 应大于 0");
        }
    }

    @Test
    @DisplayName("所有 StatusCode 枚举值 message 不应为空")
    void statusCode_allValues_nonEmptyMessage() {
        for (StatusCode sc : StatusCode.values()) {
            assertNotNull(sc.getMessage(), sc.name() + " 的 message 不应为 null");
            assertFalse(sc.getMessage().isBlank(), sc.name() + " 的 message 不应为空");
        }
    }

    @Test
    @DisplayName("SUCCESS 的 code 应为 200")
    void statusCode_success_code200() {
        assertEquals(200, StatusCode.SUCCESS.getCode());
    }
}

