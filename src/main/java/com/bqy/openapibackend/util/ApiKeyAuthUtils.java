package com.bqy.openapibackend.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * API 密钥认证工具类
 *
 * 提供 HMAC-SHA256 签名生成和验证功能
 */
@Slf4j
public class ApiKeyAuthUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String TIMESTAMP_TOLERANCE = "300000"; // 5 分钟的毫秒数

    /**
     * 生成签名（客户端使用）
     *
     * @param method HTTP 方法 (GET, POST, PUT, DELETE 等)
     * @param path URL 路径 (例如: /invoke/123)
     * @param timestamp 时间戳（毫秒）
     * @param nonce 随机数
     * @param body 请求体（GET/DELETE 可为空）
     * @param secretKey 密钥（从平台获取）
     * @return 签名字符串
     */
    public static String generateSignature(
            String method,
            String path,
            Long timestamp,
            String nonce,
            String body,
            String secretKey) {
        try {
            // 1. 构建待签名的字符串
            String signaturePayload = buildSignaturePayload(method, path, timestamp, nonce, body);

            // 2. 计算 HMAC-SHA256
            return calculateHmacSha256(signaturePayload, secretKey);

        } catch (Exception e) {
            log.error("生成签名失败", e);
            return null;
        }
    }

    /**
     * 验证签名（服务端使用）
     *
     * @param method HTTP 方法
     * @param path URL 路径
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param body 请求体
     * @param secretKey 密钥
     * @param receivedSignature 客户端发送的签名
     * @return 签名是否有效
     */
    public static boolean verifySignature(
            String method,
            String path,
            Long timestamp,
            String nonce,
            String body,
            String secretKey,
            String receivedSignature) {
        try {
            // 1. 验证时间戳（防重放攻击）
            if (!isTimestampValid(timestamp)) {
                log.warn("时间戳验证失败，请求时间戳: {}, 当前时间: {}",
                    timestamp, System.currentTimeMillis());
                return false;
            }

            // 2. 重新生成签名
            String expectedSignature = generateSignature(method, path, timestamp, nonce, body, secretKey);

            // 3. 比对签名（使用恒定时间比较防止时序攻击）
            return constantTimeEquals(expectedSignature, receivedSignature);

        } catch (Exception e) {
            log.error("验证签名失败", e);
            return false;
        }
    }

    /**
     * 构建待签名的字符串
     *
     * 格式: METHOD\nPATH\nTIMESTAMP\nNONCE\nBODY
     */
    private static String buildSignaturePayload(
            String method,
            String path,
            Long timestamp,
            String nonce,
            String body) {
        StringBuilder payload = new StringBuilder();
        payload.append(StringUtils.defaultString(method, "")).append("\n");
        payload.append(StringUtils.defaultString(path, "")).append("\n");
        payload.append(timestamp).append("\n");
        payload.append(StringUtils.defaultString(nonce, "")).append("\n");
        payload.append(StringUtils.defaultString(body, ""));

        return payload.toString();
    }

    /**
     * 计算 HMAC-SHA256
     */
    private static String calculateHmacSha256(String data, String secretKey) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            secretKey.getBytes(StandardCharsets.UTF_8),
            0,
            secretKey.getBytes(StandardCharsets.UTF_8).length,
            HMAC_SHA256
        );

        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(secretKeySpec);

        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // 返回 Base64 编码的签名
        return Base64.getEncoder().encodeToString(hmacBytes);
    }

    /**
     * 验证时间戳有效性
     *
     * 请求时间戳与当前时间的差距不能超过 5 分钟
     * 这样可以防止重放攻击
     */
    private static boolean isTimestampValid(Long timestamp) {
        if (timestamp == null || timestamp <= 0) {
            return false;
        }

        long now = System.currentTimeMillis();
        long diff = Math.abs(now - timestamp);

        // 差距不能超过 5 分钟 (300000 毫秒)
        return diff <= Long.parseLong(TIMESTAMP_TOLERANCE);
    }

    /**
     * 恒定时间字符串比较
     *
     * 防止时序攻击（timing attack）
     * 即使两个字符串不相等，也总是花费相同的时间
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }

        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        int result = 0;
        int aLen = aBytes.length;
        int bLen = bBytes.length;

        // 比较长度
        result |= aLen ^ bLen;

        // 比较每一个字节
        int minLen = Math.min(aLen, bLen);
        for (int i = 0; i < minLen; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }

        return result == 0;
    }

    /**
     * 生成随机 Nonce
     *
     * 客户端可调用此方法生成随机数
     */
    public static String generateNonce() {
        return "nonce_" + System.nanoTime();
    }
}

