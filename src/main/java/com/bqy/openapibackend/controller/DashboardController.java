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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.PrintWriter;

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

    @PostMapping(value = "/ai/recommend/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary = "AI 智能推荐 API（流式）",
        description = "根据用户的自然语言描述，使用 AI 智能理解用户需求，并以流式方式实时返回推荐结果。" +
                     "支持 Server-Sent Events (SSE) 流式推送，前端可以实时显示推荐过程。"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "流式推荐成功",
            content = @Content(mediaType = "text/event-stream")
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
    public void recommendApisStream(@Valid @RequestBody AiChatRequest request, HttpServletResponse response) {
        ThrowUtils.throwIf(request == null, "请求体不能为空");
        ThrowUtils.throwIf(request.getUserQuery() == null || request.getUserQuery().trim().isEmpty(), "请描述您的需求");

        try {
            // 设置 SSE 响应头
            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");

            PrintWriter writer = response.getWriter();

            // 获取推荐流
            Flux.from(aiRecommendationService.recommendApisByNaturalLanguageStream(request))
                .subscribe(
                    content -> {
                        try {
                            writer.println(content);
                            writer.println();
                            writer.flush();
                        } catch (Exception e) {
                            log.error("写入流式数据失败", e);
                        }
                    },
                    error -> {
                        try {
                            writer.println("event: error");
                            writer.println("data: 推荐过程中出现错误: " + error.getMessage());
                            writer.println();
                            writer.flush();
                        } catch (Exception e) {
                            log.error("写入错误事件失败", e);
                        }
                    },
                    () -> {
                        try {
                            writer.println("event: complete");
                            writer.println("data: 推荐完成");
                            writer.println();
                            writer.flush();
                        } catch (Exception e) {
                            log.error("写入完成事件失败", e);
                        }
                    }
                );
        } catch (IOException e) {
            log.error("流式响应失败", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}

