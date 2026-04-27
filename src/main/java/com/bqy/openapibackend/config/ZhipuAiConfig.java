package com.bqy.openapibackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 智谱 AI 配置类
 * 用于管理智谱 AI API 的配置信息
 *
 * 使用方式：
 * 1. 在 application.properties 中配置：
 *    zhipu.api.key=YOUR_API_KEY
 *    zhipu.api.model=glm-5
 * 2. 注入该配置类到服务中使用
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "zhipu.api")
public class ZhipuAiConfig {
    /**
     * 智谱 AI API Key
     * 获取方式: https://bigmodel.cn/usercenter/proj-mgmt/apikeys
     */
    private String key;

    /**
     * 智谱 AI API URL
     * 默认值: https://open.bigmodel.cn/api/paas/v4/chat/completions
     */
    private String url = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    /**
     * 使用的 AI 模型
     * 默认值: glm-5
     * 可选值: glm-5, glm-4, glm-3.5-turbo 等
     */
    private String model = "glm-5";

    /**
     * API 请求超时时间（单位：秒）
     * 默认值: 60
     */
    private int timeout = 60;

    /**
     * 是否启用 AI 分析功能
     * 当 API Key 未配置时，自动禁用
     */
    public boolean isEnabled() {
        return key != null && !key.equals("4b287e90a22f9a6fd9cce34baf119df9.gc0IXqXCTa8Yp4nn") && !key.isEmpty();
    }
}

