package com.bqy.openapibackend.controller;

import com.bqy.openapibackend.annotation.CheckApiAnalysisAccess;
import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.exception.OpzException;
import com.bqy.openapibackend.model.vo.ApiAnalysisReportVO;
import com.bqy.openapibackend.service.IApiAnalysisService;
import com.bqy.openapibackend.util.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * API 分析控制器
 * 提供基于 AI 的 API 调用数据分析功能
 */
@Slf4j
@Tag(name = "API 分析", description = "基于 AI 的 API 调用数据分析接口")
@RestController
@RequestMapping("/api/analysis")
public class ApiAnalysisController {

    @Resource
    private IApiAnalysisService analysisService;

    @Operation(
        summary = "获取 API 分析报告数据",
        description = "获取指定 API 的调用数据分析报告，包括基础信息、调用统计、调用记录等。",
        tags = {"API 分析"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "0", description = "获取成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "API ID 无效"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API 不存在"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/report/{apiId}")
    @Parameter(name = "apiId", description = "API 的唯一标识符", example = "123", required = true, in = ParameterIn.PATH)
    public ApiResponse<ApiAnalysisReportVO> getAnalysisReport(@PathVariable Long apiId) {
        ThrowUtils.throwIf(apiId <= 0, StatusCode.PARAMS_ERROR);

        try {
            ApiAnalysisReportVO report = analysisService.getAnalysisReportData(apiId);
            return ApiResponse.success(report);
        } catch (Exception e) {
            return ApiResponse.fail(StatusCode.SYSTEM_ERROR, "获取分析报告失败: " + e.getMessage());
        }
    }

    @Operation(
        summary = "流式 AI 分析报告",
        description = "基于 API 的调用数据进行 AI 分析，以流式方式返回分析结果。" +
                     "支持 Server-Sent Events (SSE) 流式推送，前端可以实时显示 AI 分析内容。",
        tags = {"API 分析"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "流式分析成功",
            content = @Content(mediaType = "text/event-stream")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "API ID 无效"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API 不存在"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping(value = "/stream/{apiId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @CheckApiAnalysisAccess
    @Parameter(name = "apiId", description = "API 的唯一标识符", example = "123", required = true, in = ParameterIn.PATH)
    public void streamAnalysisReport(@PathVariable Long apiId, HttpServletResponse response) {
        ThrowUtils.throwIf(apiId <= 0, StatusCode.PARAMS_ERROR);

        try {
            // 设置 SSE 响应头
            response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");

            PrintWriter writer = response.getWriter();

            // 获取分析流
            Flux.from(analysisService.generateAnalysisReportStream(apiId))
                .subscribe(
                    content -> {
                        try {
                            // 发送 SSE 事件
                            writer.println("event: message");
                            writer.println("data: " + content);
                            writer.println();
                            writer.flush();
                        } catch (Exception e) {
                            log.error("写入流式数据失败", e);
                        }
                    },
                    error -> {
                        try {
                            // 发送错误事件
                            writer.println("event: error");
                            writer.println("data: 分析过程中出现错误: " + error.getMessage());
                            writer.println();
                            writer.flush();
                        } catch (Exception e) {
                            log.error("写入错误事件失败", e);
                        }
                    },
                    () -> {
                        try {
                            // 发送完成事件
                            writer.println("event: complete");
                            writer.println("data: 分析完成");
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

    @Operation(
        summary = "获取纯文本 AI 分析结果",
        description = "基于 API 的调用数据进行 AI 分析，返回纯文本格式的分析结果。",
        tags = {"API 分析"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "分析成功",
            content = @Content(mediaType = "text/plain")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "API ID 无效"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API 不存在"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping(value = "/text/{apiId}", produces = MediaType.TEXT_PLAIN_VALUE)
    @CheckApiAnalysisAccess
    @Parameter(name = "apiId", description = "API 的唯一标识符", example = "123", required = true, in = ParameterIn.PATH)
    public String getAnalysisText(@PathVariable Long apiId) {
        ThrowUtils.throwIf(apiId <= 0, StatusCode.PARAMS_ERROR);

        try {
            // 获取分析数据
            ApiAnalysisReportVO report = analysisService.getAnalysisReportData(apiId);

            // 构建纯文本输出
            StringBuilder text = new StringBuilder();
            text.append("=== API 调用数据分析报告 ===\n\n");

            text.append("【API 基础信息】\n");
            text.append("名称: ").append(report.getApiBasicInfo().getApiName()).append("\n");
            text.append("描述: ").append(report.getApiBasicInfo().getApiDescription()).append("\n");
            text.append("URL: ").append(report.getApiBasicInfo().getUrl()).append("\n");
            text.append("方法: ").append(report.getApiBasicInfo().getMethod()).append("\n");
            text.append("创建者: ").append(report.getApiBasicInfo().getCreatorName()).append("\n\n");

            text.append("【调用统计】\n");
            text.append("总调用次数: ").append(report.getCallStatistics().getTotalCalls()).append("\n");
            text.append("成功次数: ").append(report.getCallStatistics().getSuccessCalls()).append("\n");
            text.append("失败次数: ").append(report.getCallStatistics().getFailCalls()).append("\n");
            text.append("成功率: ").append(report.getCallStatistics().getSuccessRate()).append("%\n");
            text.append("平均响应时间: ").append(report.getCallStatistics().getAvgResponseTime()).append("ms\n");
            text.append("最大响应时间: ").append(report.getCallStatistics().getMaxResponseTime()).append("ms\n");
            text.append("最小响应时间: ").append(report.getCallStatistics().getMinResponseTime()).append("ms\n");
            text.append("唯一调用用户数: ").append(report.getCallStatistics().getUniqueUserCount()).append("\n\n");

            text.append("【最近调用记录】\n");
            for (int i = 0; i < Math.min(10, report.getCallLogs().size()); i++) {
                ApiAnalysisReportVO.CallLogItem log = report.getCallLogs().get(i);
                text.append(String.format("%d. [%s] 用户: %d, 响应时间: %dms, 时间: %s\n",
                    i + 1, log.getStatus(), log.getUserId(), log.getResponseTime(), log.getCallTime()));
            }

            return text.toString();
        } catch (Exception e) {
            return "获取分析报告失败: " + e.getMessage();
        }
    }

    @GetMapping("/save/{apiId}")
    @CheckApiAnalysisAccess
    @Operation(summary = "生成并保存 API 的分析报告", description = "根据 API 调用数据进行 AI 分析，并自动保存报告到数据库。需要是 API 所有者且 API 已发布，每天最多可进行 5 次分析")
    @Parameter(name = "apiId", description = "API 的唯一标识符", example = "123", required = true, in = ParameterIn.PATH)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "分析并保存成功",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ApiResponse<Object> generateAndSaveReport(@PathVariable Long apiId) {
        ThrowUtils.throwIf(apiId <= 0, StatusCode.PARAMS_ERROR);

        // 调用 AI 生成并保存报告
        var savedReport = analysisService.generateAndSaveAIReport(apiId);
        return ApiResponse.success(savedReport);
    }

    @GetMapping("/reports/{apiId}")
    @Operation(summary = "获取 API 的分析报告列表", description = "返回该 API 已保存的分析报告摘要列表")
    @Parameters({
            @Parameter(name = "apiId", description = "API 的唯一标识符", example = "123", required = true, in = ParameterIn.PATH),
            @Parameter(name = "limit", description = "返回记录数，最多 100 条，默认 10", example = "10", in = ParameterIn.QUERY)
    })
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功返回报告列表",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ApiResponse<Object> listReports(
            @PathVariable Long apiId,
            @RequestParam(defaultValue = "10") int limit) {
        ThrowUtils.throwIf(apiId <= 0, StatusCode.PARAMS_ERROR);
        if (limit <= 0 || limit > 100) {
            ThrowUtils.throwIf(true, StatusCode.PARAMS_ERROR);
        }

        var reports = analysisService.listReports(apiId, limit);
        return ApiResponse.success(reports);
    }

    @GetMapping("/latest-report/{apiId}")
    @Operation(summary = "获取 API 的最新报告", description = "返回该 API 最新生成的完整分析报告")
    @Parameter(name = "apiId", description = "API 的唯一标识符", example = "123", required = true, in = ParameterIn.PATH)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功返回最新报告",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ApiResponse<Object> getLatestReport(@PathVariable Long apiId) {
        ThrowUtils.throwIf(apiId <= 0, StatusCode.PARAMS_ERROR);

        var report = analysisService.getLatestReport(apiId);
        if (report == null) {
            throw new OpzException(StatusCode.NOT_FOUND_ERROR, "未找到分析报告");
        }

        return ApiResponse.success(report);
    }

    @DeleteMapping("/reports/{apiId}")
    @Operation(summary = "清空 API 的所有分析报告", description = "删除该 API 所有已保存的分析报告数据")
    @Parameter(name = "apiId", description = "API 的唯一标识符", example = "123", required = true, in = ParameterIn.PATH)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "成功删除",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ApiResponse<Object> clearReports(@PathVariable Long apiId) {
        ThrowUtils.throwIf(apiId <= 0, StatusCode.PARAMS_ERROR);

        int count = analysisService.clearReports(apiId);
        return ApiResponse.success(String.format("已删除 %d 条分析报告", count));
    }
}

