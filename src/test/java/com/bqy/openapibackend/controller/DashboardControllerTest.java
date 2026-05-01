package com.bqy.openapibackend.controller;

import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.exception.GlobalExceptionHandler;
import com.bqy.openapibackend.model.vo.AiRecommendationVO;
import com.bqy.openapibackend.model.vo.DashboardVO;
import com.bqy.openapibackend.service.IAiRecommendationService;
import com.bqy.openapibackend.service.IDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DashboardController 单元测试（Standalone MockMvc）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("仪表板 Controller 测试")
class DashboardControllerTest {

    @Mock
    private IDashboardService dashboardService;

    @Mock
    private IAiRecommendationService aiRecommendationService;

    @InjectMocks
    private DashboardController dashboardController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(dashboardController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ===================== GET /dashboard/data =====================

    @Test
    @DisplayName("GET /dashboard/data 返回仪表板数据")
    void getDashboardData_success() throws Exception {
        DashboardVO mockVO = DashboardVO.builder()
                .totalApiCount(10L)
                .successRate(98.5)
                .onlineUserCount(20L)
                .categoryCount(5L)
                .totalCallCount(1000L)
                .successCallCount(985L)
                .failCallCount(15L)
                .categoryStats(Collections.emptyList())
                .build();
        when(dashboardService.getDashboardData()).thenReturn(mockVO);

        mockMvc.perform(get("/dashboard/data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalApiCount").value(10))
                .andExpect(jsonPath("$.data.successRate").value(98.5))
                .andExpect(jsonPath("$.data.onlineUserCount").value(20))
                .andExpect(jsonPath("$.data.categoryCount").value(5))
                .andExpect(jsonPath("$.data.totalCallCount").value(1000));
    }

    @Test
    @DisplayName("GET /dashboard/data 服务返回 null：抛出异常并返回错误响应")
    void getDashboardData_serviceReturnsNull_returnsError() throws Exception {
        when(dashboardService.getDashboardData()).thenReturn(null);

        mockMvc.perform(get("/dashboard/data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StatusCode.PARAMS_ERROR.getCode()));
    }

    // ===================== POST /dashboard/ai/recommend =====================

    @Test
    @DisplayName("POST /dashboard/ai/recommend AI 推荐成功")
    void recommendApis_success() throws Exception {
        AiRecommendationVO mockVO = AiRecommendationVO.builder()
                .requirementAnalysis("用户需要数据处理API")
                .recommendedApis(Collections.emptyList())
                .recommendationReason("推荐以下接口")
                .hasRecommendation(true)
                .build();
        when(aiRecommendationService.recommendApisByNaturalLanguage(any())).thenReturn(mockVO);

        mockMvc.perform(post("/dashboard/ai/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userQuery\":\"我需要数据处理接口\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.hasRecommendation").value(true));
    }

    @Test
    @DisplayName("POST /dashboard/ai/recommend userQuery 为空：返回参数错误")
    void recommendApis_emptyQuery_returnsError() throws Exception {
        mockMvc.perform(post("/dashboard/ai/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userQuery\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StatusCode.PARAMS_ERROR.getCode()));
    }

    @Test
    @DisplayName("POST /dashboard/ai/recommend AI 服务抛出异常：返回 500 错误提示")
    void recommendApis_aiServiceThrows_returns500() throws Exception {
        when(aiRecommendationService.recommendApisByNaturalLanguage(any()))
                .thenThrow(new RuntimeException("AI 服务不可用"));

        mockMvc.perform(post("/dashboard/ai/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userQuery\":\"我需要接口\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}

