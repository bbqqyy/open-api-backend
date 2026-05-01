package com.bqy.openapibackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.model.vo.ApiCallAnalyticsVO;
import com.bqy.openapibackend.model.vo.ApiCallLogVO;
import com.bqy.openapibackend.service.IApiCallLogAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 调用日志分析 Controller
 */
@RestController
@RequestMapping("/api/analytics/call-logs")
@Tag(name = "API 调用日志分析", description = "提供 API 调用日志查询和可视化分析功能")
public class ApiCallLogAnalyticsController {

    @Resource
    private IApiCallLogAnalyticsService apiCallLogAnalyticsService;

    /**
     * 分页查询 API 调用日志
     */
    @GetMapping("/query")
    @Operation(summary = "分页查询 API 调用日志", description = "分页查询 API 调用日志，支持按 API、用户、状态和时间范围过滤")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误")
    })
    public ApiResponse<Page<ApiCallLogVO>> queryCallLogs(
            @Parameter(description = "API ID", example = "1")
            @RequestParam(required = false) Long apiId,

            @Parameter(description = "用户 ID", example = "1")
            @RequestParam(required = false) Long userId,

            @Parameter(description = "调用状态 (success/fail)", example = "success")
            @RequestParam(required = false) String status,

            @Parameter(description = "开始时间", example = "2026-04-01T00:00:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,

            @Parameter(description = "结束时间", example = "2026-04-30T23:59:59")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endTime,

            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") int pageNum,

            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<ApiCallLogVO> result = apiCallLogAnalyticsService.queryCallLogs(
                apiId, userId, status, startTime, endTime, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    /**
     * 获取整体分析数据
     */
    @GetMapping("/overall")
    @Operation(summary = "获取整体分析数据", description = "获取指定时间范围内的整体 API 调用分析数据，包括成功率、响应时间等")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误")
    })
    public ApiResponse<ApiCallAnalyticsVO> getOverallAnalytics(
            @Parameter(description = "开始时间", example = "2026-04-01T00:00:00")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,

            @Parameter(description = "结束时间", example = "2026-04-30T23:59:59")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endTime) {

        ApiCallAnalyticsVO result = apiCallLogAnalyticsService.getOverallAnalytics(startTime, endTime);
        return ApiResponse.success(result);
    }

    /**
     * 获取特定 API 的分析数据
     */
    @GetMapping("/api/{apiId}")
    @Operation(summary = "获取特定 API 的分析数据", description = "获取指定 API 的详细分析数据，包括调用次数、成功率、响应时间分布等")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API 不存在")
    })
    public ApiResponse<ApiCallAnalyticsVO> getApiAnalytics(
            @Parameter(description = "API ID", example = "1")
            @PathVariable Long apiId,

            @Parameter(description = "开始时间", example = "2026-04-01T00:00:00")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,

            @Parameter(description = "结束时间", example = "2026-04-30T23:59:59")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endTime) {

        ApiCallAnalyticsVO result = apiCallLogAnalyticsService.getApiAnalytics(apiId, startTime, endTime);
        return ApiResponse.success(result);
    }

    /**
     * 获取时间序列数据（用于趋势图）
     */
    @GetMapping("/time-series")
    @Operation(summary = "获取时间序列数据", description = "获取按时间间隔分组的数据，用于绘制调用趋势图。根据时间范围自动调整时间间隔（1小时/6小时/1天）")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误")
    })
    public ApiResponse<List<ApiCallAnalyticsVO.TimeSeriesData>> getTimeSeriesData(
            @Parameter(description = "开始时间", example = "2026-04-01T00:00:00")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,

            @Parameter(description = "结束时间", example = "2026-04-30T23:59:59")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endTime,

            @Parameter(description = "时间间隔（分钟）", example = "60")
            @RequestParam(defaultValue = "60") int intervalMinutes) {

        List<ApiCallAnalyticsVO.TimeSeriesData> result = apiCallLogAnalyticsService
                .getTimeSeriesData(startTime, endTime, intervalMinutes);
        return ApiResponse.success(result);
    }

    /**
     * 获取 Top API 列表
     */
    @GetMapping("/top-apis")
    @Operation(summary = "获取 Top API 列表", description = "获取指定时间范围内调用最频繁的 Top 10 API")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误")
    })
    public ApiResponse<List<ApiCallAnalyticsVO.TopApiData>> getTopApis(
            @Parameter(description = "开始时间", example = "2026-04-01T00:00:00")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,

            @Parameter(description = "结束时间", example = "2026-04-30T23:59:59")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endTime) {

        ApiCallAnalyticsVO analytics = apiCallLogAnalyticsService.getOverallAnalytics(startTime, endTime);
        return ApiResponse.success(analytics.getTopApis());
    }

    /**
     * 获取 Top 用户列表
     */
    @GetMapping("/top-users")
    @Operation(summary = "获取 Top 用户列表", description = "获取指定时间范围内调用最频繁的 Top 10 用户")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误")
    })
    public ApiResponse<List<ApiCallAnalyticsVO.TopUserData>> getTopUsers(
            @Parameter(description = "开始时间", example = "2026-04-01T00:00:00")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,

            @Parameter(description = "结束时间", example = "2026-04-30T23:59:59")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endTime) {

        ApiCallAnalyticsVO analytics = apiCallLogAnalyticsService.getOverallAnalytics(startTime, endTime);
        return ApiResponse.success(analytics.getTopUsers());
    }

}

