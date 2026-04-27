package com.bqy.openapibackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * API 文档 VO
 *
 * 用于返回 API 快速开始指南
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 快速开始指南")
public class ApiDocumentationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文档标题
     */
    @Schema(description = "文档标题", example = "API 快速开始指南")
    private String title;

    /**
     * 文档描述
     */
    @Schema(description = "文档描述", example = "本指南将帮助您快速了解如何调用平台 API")
    private String description;

    /**
     * API 版本
     */
    @Schema(description = "API 版本", example = "1.0")
    private String version;

    /**
     * API 基础 URL
     */
    @Schema(description = "API 基础 URL", example = "https://your-api-platform.com")
    private String baseUrl;

    /**
     * 认证信息
     */
    @Schema(description = "认证方式信息")
    private Map<String, Object> authentication;

    /**
     * 快速开始步骤
     */
    @Schema(description = "快速开始步骤数组")
    private Map<String, String>[] steps;

    /**
     * 代码示例
     */
    @Schema(description = "各种编程语言的代码示例")
    private Map<String, String>[] codeExamples;

    /**
     * 常见错误
     */
    @Schema(description = "常见错误及解决方案")
    private Map<String, String>[] commonErrors;

    /**
     * 支持联系方式
     */
    @Schema(description = "技术支持联系方式", example = "support@your-platform.com")
    private String supportContact;
}

