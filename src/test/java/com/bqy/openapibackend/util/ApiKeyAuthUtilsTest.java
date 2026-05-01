package com.bqy.openapibackend.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApiKeyAuthUtils 单元测试
 * 覆盖 HMAC-SHA256 签名生成与验证的核心逻辑
 */
@DisplayName("API 密钥认证工具类测试")
class ApiKeyAuthUtilsTest {

    private static final String SECRET_KEY = "test-secret-key-12345";
    private static final String METHOD = "POST";
    private static final String PATH = "/api/apiInfo/invoke-with-key/1";
    private static final String NONCE = "nonce_abc123";
    private static final String BODY = "{\"param\":\"value\"}";

    // ===================== 签名生成测试 =====================

    @Test
    @DisplayName("相同参数应生成相同签名")
    void generateSignature_sameparams_sameResult() {
        Long timestamp = System.currentTimeMillis();

        String sig1 = ApiKeyAuthUtils.generateSignature(METHOD, PATH, timestamp, NONCE, BODY, SECRET_KEY);
        String sig2 = ApiKeyAuthUtils.generateSignature(METHOD, PATH, timestamp, NONCE, BODY, SECRET_KEY);

        assertNotNull(sig1, "签名不应为 null");
        assertEquals(sig1, sig2, "相同参数应生成相同签名");
    }

    @Test
    @DisplayName("不同 SecretKey 应生成不同签名")
    void generateSignature_differentSecretKey_differentResult() {
        Long timestamp = System.currentTimeMillis();

        String sig1 = ApiKeyAuthUtils.generateSignature(METHOD, PATH, timestamp, NONCE, BODY, "secret-key-A");
        String sig2 = ApiKeyAuthUtils.generateSignature(METHOD, PATH, timestamp, NONCE, BODY, "secret-key-B");

        assertNotEquals(sig1, sig2, "不同 SecretKey 应生成不同签名");
    }

    @Test
    @DisplayName("GET 请求 body 为空时也能正常生成签名")
    void generateSignature_emptyBody_shouldWork() {
        Long timestamp = System.currentTimeMillis();

        String sig = ApiKeyAuthUtils.generateSignature("GET", PATH, timestamp, NONCE, null, SECRET_KEY);

        assertNotNull(sig, "body 为空时签名不应为 null");
        assertFalse(sig.isBlank(), "签名不应为空字符串");
    }

    @Test
    @DisplayName("任意参数变化应导致签名不同")
    void generateSignature_anyParamChanged_differentResult() {
        Long timestamp = System.currentTimeMillis();
        String baseSig = ApiKeyAuthUtils.generateSignature(METHOD, PATH, timestamp, NONCE, BODY, SECRET_KEY);

        // 修改 method
        String changedMethod = ApiKeyAuthUtils.generateSignature("GET", PATH, timestamp, NONCE, BODY, SECRET_KEY);
        assertNotEquals(baseSig, changedMethod, "改变 method 应生成不同签名");

        // 修改 path
        String changedPath = ApiKeyAuthUtils.generateSignature(METHOD, "/other/path", timestamp, NONCE, BODY, SECRET_KEY);
        assertNotEquals(baseSig, changedPath, "改变 path 应生成不同签名");

        // 修改 nonce
        String changedNonce = ApiKeyAuthUtils.generateSignature(METHOD, PATH, timestamp, "other-nonce", BODY, SECRET_KEY);
        assertNotEquals(baseSig, changedNonce, "改变 nonce 应生成不同签名");

        // 修改 body
        String changedBody = ApiKeyAuthUtils.generateSignature(METHOD, PATH, timestamp, NONCE, "{\"other\":1}", SECRET_KEY);
        assertNotEquals(baseSig, changedBody, "改变 body 应生成不同签名");
    }

    // ===================== 签名验证测试 =====================

    @Test
    @DisplayName("正确签名应通过验证")
    void verifySignature_validSignature_returnsTrue() {
        Long timestamp = System.currentTimeMillis();
        String sig = ApiKeyAuthUtils.generateSignature(METHOD, PATH, timestamp, NONCE, BODY, SECRET_KEY);

        boolean result = ApiKeyAuthUtils.verifySignature(METHOD, PATH, timestamp, NONCE, BODY, SECRET_KEY, sig);

        assertTrue(result, "正确签名应通过验证");
    }

    @Test
    @DisplayName("篡改签名应验证失败")
    void verifySignature_tamperedSignature_returnsFalse() {
        Long timestamp = System.currentTimeMillis();
        String sig = ApiKeyAuthUtils.generateSignature(METHOD, PATH, timestamp, NONCE, BODY, SECRET_KEY);
        String tamperedSig = sig.substring(0, sig.length() - 4) + "XXXX";

        boolean result = ApiKeyAuthUtils.verifySignature(METHOD, PATH, timestamp, NONCE, BODY, SECRET_KEY, tamperedSig);

        assertFalse(result, "篡改签名应验证失败");
    }

    @Test
    @DisplayName("时间戳超过5分钟应验证失败（防重放攻击）")
    void verifySignature_expiredTimestamp_returnsFalse() {
        // 6 分钟前的时间戳
        Long expiredTimestamp = System.currentTimeMillis() - 6 * 60 * 1000L;
        String sig = ApiKeyAuthUtils.generateSignature(METHOD, PATH, expiredTimestamp, NONCE, BODY, SECRET_KEY);

        boolean result = ApiKeyAuthUtils.verifySignature(METHOD, PATH, expiredTimestamp, NONCE, BODY, SECRET_KEY, sig);

        assertFalse(result, "过期时间戳应验证失败（防重放攻击）");
    }

    @Test
    @DisplayName("时间戳为 null 应验证失败")
    void verifySignature_nullTimestamp_returnsFalse() {
        boolean result = ApiKeyAuthUtils.verifySignature(METHOD, PATH, null, NONCE, BODY, SECRET_KEY, "any-sig");

        assertFalse(result, "null 时间戳应验证失败");
    }

    @Test
    @DisplayName("错误的 SecretKey 应验证失败")
    void verifySignature_wrongSecretKey_returnsFalse() {
        Long timestamp = System.currentTimeMillis();
        String sig = ApiKeyAuthUtils.generateSignature(METHOD, PATH, timestamp, NONCE, BODY, SECRET_KEY);

        boolean result = ApiKeyAuthUtils.verifySignature(METHOD, PATH, timestamp, NONCE, BODY, "wrong-key", sig);

        assertFalse(result, "错误 SecretKey 应验证失败");
    }

    // ===================== Nonce 生成测试 =====================

    @Test
    @DisplayName("generateNonce 每次应生成不同值")
    void generateNonce_multipleCall_uniqueResults() throws InterruptedException {
        String nonce1 = ApiKeyAuthUtils.generateNonce();
        Thread.sleep(1); // 保证纳秒级不同
        String nonce2 = ApiKeyAuthUtils.generateNonce();

        assertNotNull(nonce1);
        assertNotNull(nonce2);
        assertNotEquals(nonce1, nonce2, "每次生成的 Nonce 应不同");
    }
}

