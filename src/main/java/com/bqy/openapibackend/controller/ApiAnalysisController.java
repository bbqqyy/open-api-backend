package com.bqy.openapibackend.controller;

import com.bqy.openapibackend.annotation.CheckApiAnalysisAccess;
import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.exception.OpzException;
import com.bqy.openapibackend.model.request.analytics.ApiAnalysisSaveRequest;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping("/save")
    @CheckApiAnalysisAccess
    @Operation(summary = "生成并保存 API 的分析报告", description = "根据 API 调用数据进行 AI 分析，并自动保存报告到数据库。需要是 API 所有者且 API 已发布，每天最多可进行 5 次分析")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "分析并保存成功",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ApiResponse<Object> generateAndSaveReport(@RequestBody ApiAnalysisSaveRequest saveRequest) {
        Long apiId = saveRequest.getApiId();
        ThrowUtils.throwIf(apiId == null || apiId <= 0, StatusCode.PARAMS_ERROR);

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

}

