package com.bqy.openapibackend.service;

import com.bqy.openapibackend.model.request.chat.AiChatRequest;
import com.bqy.openapibackend.model.vo.AiRecommendationVO;

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
}

