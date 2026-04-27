package com.bqy.openapibackend.model.request.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 对话推荐请求
 *
 * 用户通过自然语言描述其需求，系统基于 AI 理解用户意图
 * 并从已发布的 API 库中推荐适合的 API
 */
@Data
@Schema(description = "AI 对话推荐请求")
public class AiChatRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户的自然语言描述
     * 用户描述自己的需求，例如：
     * - "我需要获取用户的个人信息"
     * - "请帮我推荐一个可以处理订单支付的接口"
     * - "我想要一个发送短信验证码的API"
     */
    @NotBlank(message = "请描述您的需求")
    @Schema(
        description = "用户的自然语言描述",
        example = "我需要获取用户的个人信息和订单历史",
        minLength = 1,
        maxLength = 500
    )
    private String userQuery;

    /**
     * 是否进行流式输出（可选）
     * true: 流式推荐结果
     * false: 一次性返回所有推荐
     * 默认为 false
     */
    @Schema(
        description = "是否使用流式输出推荐结果",
        defaultValue = "false",
        example = "false"
    )
    private Boolean streamOutput = false;
}

