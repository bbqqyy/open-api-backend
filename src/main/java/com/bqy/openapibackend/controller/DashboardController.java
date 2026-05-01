package com.bqy.openapibackend.controller;

import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.model.request.chat.AiChatRequest;
import com.bqy.openapibackend.model.vo.AiRecommendationVO;
import com.bqy.openapibackend.model.vo.DashboardVO;
import com.bqy.openapibackend.service.IAiRecommendationService;
import com.bqy.openapibackend.service.IDashboardService;
import com.bqy.openapibackend.util.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
/**
 * 仪表板控制器 - 首页数据展示和 AI 对话推荐
 */
@Slf4j
@Tag(name = "仪表板管理", description = "首页数据展示接口、AI 对话推荐等")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private IDashboardService dashboardService;

    @Resource
    private IAiRecommendationService aiRecommendationService;

    @Operation(
        summary = "获取仪表板数据",
        description = "获取首页展示的统计数据，包括API总数量、接口调用成功率、在线用户数、接口分类数、各分类API数量统计等信息。" +
                     "该接口用于首页仪表板展示系统的整体运营指标。"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "0",
            description = "成功获取仪表板数据",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardVO.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "服务器内部错误"
        )
    })
    @GetMapping("/data")
    public ApiResponse<DashboardVO> getDashboardData() {
        DashboardVO dashboardData = dashboardService.getDashboardData();
        ThrowUtils.throwIf(dashboardData == null, "获取仪表板数据失败");
        return ApiResponse.success(dashboardData);
    }

    @PostMapping("/ai/recommend")
    @Operation(
        summary = "AI 智能推荐 API",
        description = "根据用户的自然语言描述，使用 AI 智能理解用户需求，并从已发布的 API 库中推荐最适合的 API。" +
                     "支持流式和非流式两种返回方式。"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "推荐成功",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AiRecommendationVO.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "请求参数错误"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "服务器内部错误"
        )
    })
    public ApiResponse<AiRecommendationVO> recommendApis(@Valid @RequestBody AiChatRequest request) {
        ThrowUtils.throwIf(request == null, "请求体不能为空");
        ThrowUtils.throwIf(request.getUserQuery() == null || request.getUserQuery().trim().isEmpty(), "请描述您的需求");

        try {
            AiRecommendationVO recommendation = aiRecommendationService.recommendApisByNaturalLanguage(request);
            return ApiResponse.success(recommendation);
        } catch (Exception e) {
            log.error("AI 推荐失败", e);
            return ApiResponse.fail(500, "AI 推荐失败: " + e.getMessage());
        }
    }

}

