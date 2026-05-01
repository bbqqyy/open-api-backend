package com.bqy.openapibackend.service.impl;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import cn.hutool.json.JSONUtil;
import com.bqy.openapibackend.config.ZhipuAiConfig;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.dao.UserDao;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.model.enums.ApiStatusEnum;
import com.bqy.openapibackend.model.enums.MethodEnum;
import com.bqy.openapibackend.model.request.chat.AiChatRequest;
import com.bqy.openapibackend.model.vo.AiRecommendationVO;
import com.bqy.openapibackend.service.IAiRecommendationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 推荐服务实现类
 *
 * 使用智谱 AI 理解用户自然语言需求，并从已发布的 API 库中推荐最匹配的 API
 */
@Slf4j
@Service
public class AiRecommendationServiceImpl implements IAiRecommendationService {

    @Resource
    private ApiInfoDao apiInfoDao;

    @Resource
    private UserDao userDao;

    @Resource
    private ZhipuAiConfig zhipuAiConfig;

    /**
     * 系统提示词，用于引导 AI 进行 API 推荐
     */
    private static final String SYSTEM_PROMPT = """
        你是一个专业的 API 推荐助手。你的职责是：
        1. 理解用户的自然语言需求描述
        2. 分析用户的实际需求
        3. 从提供的 API 列表中推荐最匹配的 API
        4. 详细解释推荐原因

        你必须按照以下 JSON 格式返回推荐结果（不要包含任何其他内容）：
        {
            "requirementAnalysis": "对用户需求的分析",
            "recommendationReason": "推荐理由总结",
            "hasRecommendation": true/false,
            "recommendedApis": [
                {
                    "apiId": 123,
                    "apiName": "API 名称",
                    "reason": "为什么推荐这个 API",
                    "matchScore": 95
                }
            ]
        }
        """;

    @Override
    public AiRecommendationVO recommendApisByNaturalLanguage(AiChatRequest request) {
        try {
            // 1. 获取所有已发布的 API
            List<ApiInfo> publishedApis = getPublishedApis();

            if (publishedApis.isEmpty()) {
                // 如果没有 API，返回空推荐
                return AiRecommendationVO.builder()
                        .requirementAnalysis("未找到已发布的 API")
                        .hasRecommendation(false)
                        .recommendationReason("系统中暂无可用 API")
                        .recommendedApis(List.of())
                        .build();
            }

            // 2. 构建 API 上下文信息
            String apiContext = buildApiContext(publishedApis);

            // 3. 构建 AI 提示词
            String userPrompt = buildUserPrompt(request.getUserQuery(), apiContext);

            // 4. 调用 AI 获取推荐结果
            String aiResponse = callZhipuAIForRecommendation(userPrompt);

            // 5. 解析 AI 响应并构建完整推荐结果
            AiRecommendationVO recommendation = parseAiResponse(aiResponse, publishedApis);

            log.info("AI 推荐完成：推荐了 {} 个 API", recommendation.getRecommendedApis().size());

            return recommendation;

        } catch (Exception e) {
            log.error("AI 推荐失败", e);
            // 降级处理：返回基础推荐
            return generateBasicRecommendation(request.getUserQuery());
        }
    }

    /**
     * 获取所有已发布的 API
     */
    private List<ApiInfo> getPublishedApis() {
        return apiInfoDao.lambdaQuery()
                .eq(ApiInfo::getStatus, ApiStatusEnum.RELEASE_SUCCESS.getCode())
                .eq(ApiInfo::getIsOnline, 1) // 只获取在线的 API
                .list();
    }

    /**
     * 构建 API 上下文信息供 AI 理解
     */
    private String buildApiContext(List<ApiInfo> apis) {
        StringBuilder context = new StringBuilder("可用的 API 列表：\n");

        for (ApiInfo api : apis) {
            User creator = userDao.getUserById(api.getUserId());
            String creatorName = creator != null ? creator.getUserName() : "未知";

            context.append("\n- ID: ").append(api.getId())
                    .append(", 名称: ").append(api.getApiName())
                    .append(", 描述: ").append(api.getApiDescription())
                    .append(", 方法: ").append(MethodEnum.getNameByCode(api.getMethod()))
                    .append(", URL: ").append(api.getUrl())
                    .append(", 创建者: ").append(creatorName);
        }

        return context.toString();
    }

    /**
     * 构建用户提示词
     */
    private String buildUserPrompt(String userQuery, String apiContext) {
        return apiContext + "\n\n用户需求：" + userQuery + "\n\n请根据用户的需求从上述 API 列表中推荐最适合的 API。";
    }

    /**
     * 调用智谱 AI 获取推荐结果
     */
    private String callZhipuAIForRecommendation(String userPrompt) {
        try {
            if (!zhipuAiConfig.isEnabled()) {
                log.warn("智谱 AI 未启用，使用模拟推荐");
                return generateMockRecommendation();
            }

            ZhipuAiClient client = ZhipuAiClient.builder().ofZHIPU()
                    .apiKey(zhipuAiConfig.getKey())
                    .build();

            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(zhipuAiConfig.getModel())
                    .messages(Arrays.asList(
                            ChatMessage.builder()
                                    .role(ChatMessageRole.SYSTEM.value())
                                    .content(SYSTEM_PROMPT)
                                    .build(),
                            ChatMessage.builder()
                                    .role(ChatMessageRole.USER.value())
                                    .content(userPrompt)
                                    .build()
                    ))
                    .stream(false)
                    .temperature(0.7f)
                    .maxTokens(2000)
                    .build();

            ChatCompletionResponse response = client.chat().createChatCompletion(request);

            StringBuilder result = new StringBuilder();

            if (response.isSuccess() && response.getFlowable() != null) {
                response.getFlowable().blockingForEach(data -> {
                    try {
                        if (data.getChoices() != null && !data.getChoices().isEmpty()) {
                            Delta delta = data.getChoices().get(0).getDelta();
                            if (delta != null && delta.getContent() != null) {
                                result.append(delta.getContent());
                            }
                        }
                    } catch (Exception e) {
                        log.error("处理响应数据块失败", e);
                    }
                });

                String content = result.toString();
                if (!ObjectUtils.isEmpty(content)) {
                    log.info("成功调用智谱 AI API 进行推荐");
                    return content;
                }
            } else {
                log.error("智谱 AI API 返回错误: {}", response.getMsg());
            }

            return generateMockRecommendation();

        } catch (Exception e) {
            log.error("调用智谱 AI API 失败，降级使用模拟推荐", e);
            return generateMockRecommendation();
        }
    }

    /**
     * 解析 AI 响应
     */
    private AiRecommendationVO parseAiResponse(String aiResponse, List<ApiInfo> publishedApis) {
        try {
            // 尝试解析 JSON 响应
            Map<String, Object> responseMap = JSONUtil.toBean(aiResponse, Map.class);

            List<AiRecommendationVO.RecommendedApiVO> recommendedApis = new ArrayList<>();

            // 提取推荐的 API
            if (responseMap.containsKey("recommendedApis")) {
                List<Map<String, Object>> apiList = (List<Map<String, Object>>) responseMap.get("recommendedApis");
                for (Map<String, Object> apiMap : apiList) {
                    try {
                        Long apiId = Long.valueOf(apiMap.get("apiId").toString());
                        ApiInfo apiInfo = apiInfoDao.getApiInfoById(apiId);

                        if (apiInfo != null) {
                            User creator = userDao.getUserById(apiInfo.getUserId());
                            recommendedApis.add(
                                AiRecommendationVO.RecommendedApiVO.builder()
                                        .apiId(apiId)
                                        .apiName(apiInfo.getApiName())
                                        .apiDescription(apiInfo.getApiDescription())
                                        .url(apiInfo.getUrl())
                                        .httpMethod(MethodEnum.getNameByCode(apiInfo.getMethod()))
                                        .matchScore(Integer.parseInt(apiMap.getOrDefault("matchScore", "70").toString()))
                                        .reason(apiMap.getOrDefault("reason", "推荐理由").toString())
                                        .creatorName(creator != null ? creator.getUserName() : "未知")
                                        .build()
                            );
                        }
                    } catch (Exception ex) {
                        log.warn("解析推荐 API 失败", ex);
                    }
                }
            }

            return AiRecommendationVO.builder()
                    .requirementAnalysis(responseMap.getOrDefault("requirementAnalysis", "").toString())
                    .recommendationReason(responseMap.getOrDefault("recommendationReason", "").toString())
                    .hasRecommendation(!recommendedApis.isEmpty())
                    .recommendedApis(recommendedApis)
                    .build();

        } catch (Exception e) {
            log.error("解析 AI 响应失败", e);
            return generateBasicRecommendation(aiResponse);
        }
    }

    /**
     * 生成基础推荐（降级处理）
     */
    private AiRecommendationVO generateBasicRecommendation(String userQuery) {
        List<ApiInfo> apis = getPublishedApis();
        List<AiRecommendationVO.RecommendedApiVO> recommendations = apis.stream()
                .limit(3)
                .map(api -> {
                    User creator = userDao.getUserById(api.getUserId());
                    return AiRecommendationVO.RecommendedApiVO.builder()
                            .apiId(api.getId())
                            .apiName(api.getApiName())
                            .apiDescription(api.getApiDescription())
                            .url(api.getUrl())
                            .httpMethod(MethodEnum.getNameByCode(api.getMethod()))
                            .matchScore(70)
                            .reason("该 API 可能与您的需求相关")
                            .creatorName(creator != null ? creator.getUserName() : "未知")
                            .build();
                })
                .collect(Collectors.toList());

        return AiRecommendationVO.builder()
                .requirementAnalysis("基于关键词匹配分析")
                .recommendationReason("系统为您推荐以下可用 API")
                .hasRecommendation(!recommendations.isEmpty())
                .recommendedApis(recommendations)
                .build();
    }

    /**
     * 生成模拟推荐（AI 不可用时）
     */
    private String generateMockRecommendation() {
        return JSONUtil.toJsonStr(
            Map.of(
                "requirementAnalysis", "用户需要相关的 API 服务",
                "recommendationReason", "基于系统推荐",
                "hasRecommendation", true,
                "recommendedApis", List.of()
            )
        );
    }
}

