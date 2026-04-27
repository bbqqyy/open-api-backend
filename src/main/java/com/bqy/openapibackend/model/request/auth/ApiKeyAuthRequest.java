package com.bqy.openapibackend.model.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * API 密钥认证请求
 *
 * 用于跨系统调用时的身份验证
 */
@Data
@Schema(description = "API 密钥认证请求")
public class ApiKeyAuthRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 访问密钥 (Access Key)
     * 用于识别用户身份的公钥
     */
    @Schema(
        description = "访问密钥 (Access Key)",
        example = "ak_1234567890abcdef"
    )
    private String accessKey;

    /**
     * 签名 (Signature)
     * 使用 SecretKey 对请求数据进行 HMAC-SHA256 签名
     *
     * 签名生成方式:
     * signature = HmacSHA256(
     *   "HTTP_METHOD\nURL_PATH\nTIMESTAMP\nNONCE\nBODY",
     *   secretKey
     * )
     */
    @Schema(
        description = "请求签名 (HMAC-SHA256)",
        example = "abc123def456ghi789"
    )
    private String signature;

    /**
     * 时间戳 (毫秒)
     * 用于防止重放攻击
     * 平台会检查时间戳与当前时间的差距，超过 5 分钟则拒绝
     */
    @Schema(
        description = "请求时间戳 (毫秒)",
        example = "1704067200000"
    )
    private Long timestamp;

    /**
     * 随机数 (Nonce)
     * 用于额外的安全保护，防止相同签名被重复使用
     */
    @Schema(
        description = "随机数 (Nonce)",
        example = "nonce_abc123"
    )
    private String nonce;

    /**
     * 请求体 (可选)
     * POST/PUT/PATCH 请求需要包含
     */
    @Schema(
        description = "请求体内容 (用于签名计算)",
        example = "{\"name\": \"value\"}"
    )
    private String body;
}

