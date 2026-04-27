package com.bqy.openapibackend.service;

import com.bqy.openapibackend.model.request.chat.AiChatRequest;
import com.bqy.openapibackend.model.vo.AiRecommendationVO;
import org.reactivestreams.Publisher;

/**
 * AI 推荐服务接口
 *
 * 根据用户的自然语言描述，使用 AI 推荐适合的 API
 */
public interface IAiRecommendationService {

    /**
     * 基于用户自然语言描述推荐 API
     *
     * @param request 用户的自然语言描述请求
     * @return 推荐结果
     */
    AiRecommendationVO recommendApisByNaturalLanguage(AiChatRequest request);

    /**
     * 流式推荐 API（Server-Sent Events）
     *
     * 实时流式返回推荐结果，用户可以实时看到 AI 的推荐过程
     *
     * @param request 用户的自然语言描述请求
     * @return 流式推荐结果
     */
    Publisher<String> recommendApisByNaturalLanguageStream(AiChatRequest request);
}

