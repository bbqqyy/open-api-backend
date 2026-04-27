package com.bqy.openapibackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * API 密钥信息 VO
 *
 * 用于显示用户的 AccessKey 和 SecretKey
 * 仅用于已登录用户查询自己的密钥
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户的 API 密钥信息")
public class ApiKeysVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 访问密钥 (Access Key)
     *
     * 用于在 API 请求头中标识用户身份
     * 公开的，可以安全地在网络中传输
     */
    @Schema(
        description = "访问密钥 (Access Key) - 用于 API 请求头",
        example = "ak_c8d3e9f2b1a4c7d6e9f2b1a4c7d6e9f2"
    )
    private String accessKey;

    /**
     * 秘密密钥 (Secret Key) - 部分显示
     *
     * ⚠️ 出于安全考虑，只显示前 4 个字符和后 4 个字符
     * 完整密钥仅在注册时显示，之后不再显示
     */
    @Schema(
        description = "秘密密钥 (Secret Key) - 部分显示（仅前4个和后4个字符）",
        example = "sk_a*****p6"
    )
    private String secretKeyMasked;

    /**
     * 提示信息
     */
    @Schema(
        description = "提示信息",
        example = "完整的 Secret Key 仅在注册时显示一次，如果遗忘可以重新生成（会立即失效之前的密钥）"
    )
    private String message;

    /**
     * 最后更新时间
     */
    @Schema(
        description = "密钥最后更新时间",
        example = "2026-04-10 10:30:00"
    )
    private String lastUpdated;
}

