package com.bqy.openapibackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册结果 VO
 *
 * 包含用户 ID 以及自动生成的 AccessKey 和 SecretKey
 * 用户需要妥善保存这些密钥，用于后续 API 调用认证
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户注册结果")
public class RegisterResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @Schema(
        description = "用户 ID",
        example = "123"
    )
    private Long userId;

    /**
     * 用户账号
     */
    @Schema(
        description = "用户账号",
        example = "john_doe"
    )
    private String userAccount;

    /**
     * 用户昵称
     */
    @Schema(
        description = "用户昵称",
        example = "约翰"
    )
    private String userName;

    /**
     * 访问密钥 (Access Key)
     *
     * ⚠️ 重要：用户应该妥善保存此密钥，丢失后无法恢复
     * 仅在注册时显示一次，之后不再显示
     */
    @Schema(
        description = "访问密钥 (Access Key) - 用于 API 认证，仅在注册时显示",
        example = "ak_c8d3e9f2b1a4c7d6e9f2b1a4c7d6e9f2"
    )
    private String accessKey;

    /**
     * 秘密密钥 (Secret Key)
     *
     * ⚠️ 重要：用户应该妥善保存此密钥，丢失后无法恢复
     * 仅在注册时显示一次，之后不再显示
     * 不要在网络中明文传输或在代码中硬编码
     */
    @Schema(
        description = "秘密密钥 (Secret Key) - 用于 API 签名，仅在注册时显示，请妥善保存",
        example = "sk_a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6"
    )
    private String secretKey;

    /**
     * 提示信息
     * 提醒用户妥善保存密钥
     */
    @Schema(
        description = "提示信息",
        example = "请妥善保存您的 AccessKey 和 SecretKey，这是您跨系统调用 API 的唯一凭证"
    )
    private String message;
}

