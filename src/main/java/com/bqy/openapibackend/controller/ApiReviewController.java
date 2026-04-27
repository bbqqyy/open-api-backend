package com.bqy.openapibackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.model.request.review.BatchReviewRequest;
import com.bqy.openapibackend.model.vo.ReviewProgressVO;
import com.bqy.openapibackend.model.vo.ReviewStatsVO;
import com.bqy.openapibackend.service.IApiReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API 审核管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/review")
@Tag(name = "API审核管理", description = "审核流程管理和统计分析")
public class ApiReviewController {

    @Resource
    private IApiReviewService apiReviewService;

    @Operation(summary = "获取审核进度", description = "获取单个 API 的审核进度信息")
    @GetMapping("/progress/{apiId}")
    public ApiResponse<ReviewProgressVO> getReviewProgress(@PathVariable Long apiId) {
        return ApiResponse.success(apiReviewService.getReviewProgress(apiId));
    }

    @Operation(summary = "获取待审核列表", description = "分页获取待审核的 API 列表")
    @GetMapping("/pending")
    public ApiResponse<Page<ReviewProgressVO>> getPendingReviewList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(apiReviewService.getPendingReviewList(pageNum, pageSize));
    }

    @Operation(summary = "批量审核", description = "批量审核多个 API")
    @PostMapping("/batch")
    public ApiResponse<Boolean> batchReview(@RequestBody BatchReviewRequest request) {
        return ApiResponse.success(apiReviewService.batchReview(request));
    }

    @Operation(summary = "获取审核统计", description = "获取审核统计数据和分析")
    @GetMapping("/stats")
    public ApiResponse<ReviewStatsVO> getReviewStats() {
        return ApiResponse.success(apiReviewService.getReviewStats());
    }

    @Operation(summary = "获取日期范围内的审核统计", description = "获取指定时间范围内的审核统计数据")
    @GetMapping("/stats/range")
    public ApiResponse<ReviewStatsVO> getReviewStatsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ApiResponse.success(apiReviewService.getReviewStatsByDateRange(startDate, endDate));
    }

    @Operation(summary = "获取审核员绩效", description = "获取指定审核员的绩效数据")
    @GetMapping("/reviewer/{reviewerId}/performance")
    public ApiResponse<ReviewStatsVO.ReviewerStats> getReviewerPerformance(@PathVariable Long reviewerId) {
        return ApiResponse.success(apiReviewService.getReviewerPerformance(reviewerId));
    }

    @Operation(summary = "获取审核员排名", description = "获取所有审核员的绩效排名")
    @GetMapping("/reviewer/rankings")
    public ApiResponse<List<ReviewStatsVO.ReviewerStats>> getReviewerRankings() {
        return ApiResponse.success(apiReviewService.getReviewerRankings());
    }

    @Operation(summary = "自动分配待审核", description = "自动为审核员分配待审核的 API")
    @PostMapping("/auto-assign/{reviewerId}")
    public ApiResponse<List<Long>> autoAssignReviews(
            @PathVariable Long reviewerId,
            @RequestParam(defaultValue = "10") Integer count) {
        return ApiResponse.success(apiReviewService.autoAssignReviews(reviewerId, count));
    }

    @Operation(summary = "获取审核历史", description = "获取审核历史记录")
    @GetMapping("/history")
    public ApiResponse<Page<ReviewProgressVO>> getReviewHistory(
            @RequestParam(required = false) Long apiId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(apiReviewService.getReviewHistory(apiId, pageNum, pageSize));
    }
}

