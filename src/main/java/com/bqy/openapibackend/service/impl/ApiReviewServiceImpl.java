package com.bqy.openapibackend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.dao.ApiReviewDao;
import com.bqy.openapibackend.dao.UserDao;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.entity.ApiReview;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.model.enums.ApiStatusEnum;
import com.bqy.openapibackend.model.request.review.BatchReviewRequest;
import com.bqy.openapibackend.model.vo.ReviewProgressVO;
import com.bqy.openapibackend.model.vo.ReviewStatsVO;
import com.bqy.openapibackend.service.IApiReviewService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * API 审核服务实现
 */
@Slf4j
@Service
public class ApiReviewServiceImpl implements IApiReviewService {

    @Resource
    private ApiReviewDao apiReviewDao;

    @Resource
    private ApiInfoDao apiInfoDao;

    @Resource
    private UserDao userDao;

    @Override
    public ReviewProgressVO getReviewProgress(Long apiId) {
        ApiInfo apiInfo = apiInfoDao.getById(apiId);
        if (apiInfo == null) {
            return null;
        }

        ApiReview review = apiReviewDao.lambdaQuery()
                .eq(ApiReview::getApiId, apiId)
                .orderByDesc(ApiReview::getReviewTime)
                .last("LIMIT 1")
                .one();

        ReviewProgressVO vo = ReviewProgressVO.builder()
                .apiId(apiId)
                .apiName(apiInfo.getApiName())
                .status(convertStatusToString(apiInfo.getStatus()))
                .submitTime(apiInfo.getCreateTime())
                .build();

        if (review != null) {
            vo.setReviewComment(review.getComment());
            vo.setReviewTime(review.getReviewTime());
            User reviewer = userDao.getById(review.getAdminId());
            if (reviewer != null) {
                vo.setReviewerName(reviewer.getUserName());
            }

            // 计算审核用时
            if (review.getReviewTime() != null && apiInfo.getCreateTime() != null) {
                long minutes = java.time.temporal.ChronoUnit.MINUTES.between(
                        apiInfo.getCreateTime(), review.getReviewTime());
                vo.setReviewDuration(minutes);
            }
        }

        // 计算进度百分比
        vo.setProgressPercentage(calculateProgressPercentage(convertStatusToString(apiInfo.getStatus())));

        // 估计审核时间
        vo.setEstimatedReviewHours(estimateReviewHours(apiInfo));

        return vo;
    }

    @Override
    public Page<ReviewProgressVO> getPendingReviewList(int pageNum, int pageSize) {
        Page<ApiInfo> pageInfo = apiInfoDao.lambdaQuery()
                .eq(ApiInfo::getStatus, ApiStatusEnum.RELEASING.getCode())
                .orderByAsc(ApiInfo::getCreateTime)
                .page(new Page<>(pageNum, pageSize));

        List<ReviewProgressVO> voList = pageInfo.getRecords().stream()
                .map(this::convertToReviewProgressVO)
                .collect(Collectors.toList());

        Page<ReviewProgressVO> voPage = new Page<>(pageNum, pageSize, pageInfo.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean batchReview(BatchReviewRequest request) {
        for (Long apiId : request.getApiIds()) {
            ApiInfo apiInfo = apiInfoDao.getById(apiId);
            if (apiInfo == null || apiInfo.getStatus() == null || !Objects.equals(apiInfo.getStatus(), ApiStatusEnum.RELEASING.getCode())) {
                continue;
            }

            // 更新 API 状态
            if ("approved".equals(request.getResult())) {
                apiInfo.setStatus(ApiStatusEnum.RELEASE_SUCCESS.getCode());
            } else {
                apiInfo.setStatus(ApiStatusEnum.RELEASE_FAIL.getCode());
            }
            apiInfoDao.updateById(apiInfo);

            // 创建审核记录
            ApiReview review = new ApiReview();
            review.setApiId(apiId);
            review.setAdminId(1L); // TODO: 从上下文获取当前登录用户
            review.setResult(request.getResult());
            review.setComment(request.getComment());
            review.setReviewTime(LocalDateTime.now());
            apiReviewDao.save(review);
        }
        return true;
    }

    @Override
    public ReviewStatsVO getReviewStats() {
        return getReviewStatsByDateRange(
                LocalDate.now().minusMonths(1).toString(),
                LocalDate.now().toString()
        );
    }

    @Override
    public ReviewStatsVO getReviewStatsByDateRange(String startDate, String endDate) {
        LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);

        // 获取待审核数
        long pendingCount = apiInfoDao.lambdaQuery()
                .eq(ApiInfo::getStatus, ApiStatusEnum.RELEASING.getCode())
                .count();

        // 获取已批准和已拒绝的数据
        List<ApiReview> reviews = apiReviewDao.lambdaQuery()
                .ge(ApiReview::getReviewTime, startDateTime)
                .le(ApiReview::getReviewTime, endDateTime)
                .list();

        long approvedCount = reviews.stream()
                .filter(r -> "approved".equals(r.getResult()) || ApiStatusEnum.RELEASE_SUCCESS.getMessage().equals(r.getResult()))
                .count();
        long rejectedCount = reviews.stream()
                .filter(r -> "rejected".equals(r.getResult()) || ApiStatusEnum.RELEASE_FAIL.getMessage().equals(r.getResult()))
                .count();
        long totalCount = pendingCount + approvedCount + rejectedCount;

        double approvalRate = totalCount == 0 ? 0 : (double) approvedCount / totalCount * 100;
        double rejectionRate = totalCount == 0 ? 0 : (double) rejectedCount / totalCount * 100;

        // 计算平均审核时间
        double avgReviewTime = calculateAverageReviewTime(reviews);
        List<Long> reviewTimes = calculateReviewTimes(reviews);
        long minReviewTime = reviewTimes.isEmpty() ? 0 : reviewTimes.get(0);
        long maxReviewTime = reviewTimes.isEmpty() ? 0 : reviewTimes.get(reviewTimes.size() - 1);

        // 本周和本月新增
        long weeklyNew = countNewInWeek();
        long monthlyNew = countNewInMonth();

        // 按天统计
        List<ReviewStatsVO.DailyStats> dailyStats = calculateDailyStats(startDate, endDate);

        // 按审核员统计
        List<ReviewStatsVO.ReviewerStats> reviewerStats = calculateReviewerStats(reviews);

        // 按分类统计
        List<ReviewStatsVO.CategoryStats> categoryStats = calculateCategoryStats(reviews);

        return ReviewStatsVO.builder()
                .pendingCount(pendingCount)
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .totalCount(totalCount)
                .approvalRate(approvalRate)
                .rejectionRate(rejectionRate)
                .avgReviewTime(avgReviewTime)
                .minReviewTime(minReviewTime)
                .maxReviewTime(maxReviewTime)
                .weeklyNew(weeklyNew)
                .monthlyNew(monthlyNew)
                .dailyStats(dailyStats)
                .reviewerStats(reviewerStats)
                .categoryStats(categoryStats)
                .build();
    }

    @Override
    public ReviewStatsVO.ReviewerStats getReviewerPerformance(Long reviewerId) {
        List<ApiReview> reviews = apiReviewDao.lambdaQuery()
                .eq(ApiReview::getAdminId, reviewerId)
                .list();

        long totalReviewed = reviews.size();
        long approved = reviews.stream()
                .filter(r -> "approved".equals(r.getResult()) || ApiStatusEnum.RELEASE_SUCCESS.getMessage().equals(r.getResult()))
                .count();
        long rejected = totalReviewed - approved;

        double approvalRate = totalReviewed == 0 ? 0 : (double) approved / totalReviewed * 100;
        List<Long> reviewTimes = calculateReviewTimes(reviews);
        long avgReviewTime = reviewTimes.isEmpty() ? 0 : (long) reviewTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        User reviewer = userDao.getById(reviewerId);
        String reviewerName = reviewer != null ? reviewer.getUserName() : "Unknown";

        return ReviewStatsVO.ReviewerStats.builder()
                .reviewerId(reviewerId)
                .reviewerName(reviewerName)
                .totalReviewed(totalReviewed)
                .approved(approved)
                .rejected(rejected)
                .approvalRate(approvalRate)
                .avgReviewTime(avgReviewTime)
                .build();
    }

    @Override
    public List<ReviewStatsVO.ReviewerStats> getReviewerRankings() {
        List<ApiReview> allReviews = apiReviewDao.list();

        return allReviews.stream()
                .collect(Collectors.groupingBy(ApiReview::getAdminId))
                .entrySet().stream()
                .map(entry -> getReviewerPerformance(entry.getKey()))
                .sorted(Comparator.comparingDouble(ReviewStatsVO.ReviewerStats::getApprovalRate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> autoAssignReviews(Long reviewerId, Integer count) {
        // 获取待审核的 API（按创建时间排序，优先分配最早的）
        List<ApiInfo> pendingApis = apiInfoDao.lambdaQuery()
                .eq(ApiInfo::getStatus, ApiStatusEnum.RELEASING.getCode())
                .orderByAsc(ApiInfo::getCreateTime)
                .last("LIMIT " + count)
                .list();

        return pendingApis.stream()
                .map(ApiInfo::getId)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ReviewProgressVO> getReviewHistory(Long apiId, int pageNum, int pageSize) {
        Page<ApiReview> reviewPage = apiReviewDao.lambdaQuery()
                .eq(apiId != null, ApiReview::getApiId, apiId)
                .orderByDesc(ApiReview::getReviewTime)
                .page(new Page<>(pageNum, pageSize));

        List<ReviewProgressVO> voList = reviewPage.getRecords().stream()
                .map(review -> {
                    ApiInfo apiInfo = apiInfoDao.getById(review.getApiId());
                    User reviewer = userDao.getById(review.getAdminId());

                    return ReviewProgressVO.builder()
                            .apiId(review.getApiId())
                            .apiName(apiInfo != null ? apiInfo.getApiName() : "Unknown")
                            .status(review.getResult())
                            .reviewComment(review.getComment())
                            .reviewTime(review.getReviewTime())
                            .reviewerName(reviewer != null ? reviewer.getUserName() : "Unknown")
                            .build();
                })
                .collect(Collectors.toList());

        Page<ReviewProgressVO> voPage = new Page<>(pageNum, pageSize, reviewPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    // ==================== 私有方法 ====================

    private ReviewProgressVO convertToReviewProgressVO(ApiInfo apiInfo) {
        return getReviewProgress(apiInfo.getId());
    }

    private String convertStatusToString(Integer status) {
        if (status == null) {
            return "unknown";
        }
        return switch (status) {
            case 0 -> "draft";
            case 1 -> "releasing";
            case 2 -> "approved";
            case 3 -> "rejected";
            default -> "unknown";
        };
    }

    private int calculateProgressPercentage(String status) {
        return switch (status) {
            case "draft" -> 25;
            case "releasing" -> 50;
            case "approved" -> 100;
            case "rejected" -> 0;
            default -> 0;
        };
    }

    private Integer estimateReviewHours(ApiInfo apiInfo) {
        // 根据 API 复杂度估计审核时间
        // 这是一个简单的估算，实际应根据更多因素计算
        return 24; // 默认 24 小时
    }

    private double calculateAverageReviewTime(List<ApiReview> reviews) {
        List<Long> times = calculateReviewTimes(reviews);
        return times.isEmpty() ? 0 : times.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0) / 60.0; // 转换为小时
    }

    private List<Long> calculateReviewTimes(List<ApiReview> reviews) {
        return reviews.stream()
                .filter(r -> r.getReviewTime() != null)
                .map(review -> {
                    ApiInfo apiInfo = apiInfoDao.getById(review.getApiId());
                    if (apiInfo != null && apiInfo.getCreateTime() != null) {
                        return java.time.temporal.ChronoUnit.MINUTES.between(
                                apiInfo.getCreateTime(), review.getReviewTime());
                    }
                    return 0L;
                })
                .sorted()
                .collect(Collectors.toList());
    }

    private long countNewInWeek() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        return apiInfoDao.lambdaQuery()
                .ge(ApiInfo::getCreateTime, oneWeekAgo)
                .eq(ApiInfo::getStatus, ApiStatusEnum.RELEASING.getCode())
                .count();
    }

    private long countNewInMonth() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        return apiInfoDao.lambdaQuery()
                .ge(ApiInfo::getCreateTime, oneMonthAgo)
                .eq(ApiInfo::getStatus, ApiStatusEnum.RELEASING.getCode())
                .count();
    }

    private List<ReviewStatsVO.DailyStats> calculateDailyStats(String startDate, String endDate) {
        List<ReviewStatsVO.DailyStats> dailyStats = new ArrayList<>();
        LocalDate current = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        while (!current.isAfter(end)) {
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(23, 59, 59);

            long pending = apiInfoDao.lambdaQuery()
                    .eq(ApiInfo::getStatus, ApiStatusEnum.RELEASING.getCode())
                    .ge(ApiInfo::getCreateTime, dayStart)
                    .le(ApiInfo::getCreateTime, dayEnd)
                    .count();

            List<ApiReview> dayReviews = apiReviewDao.lambdaQuery()
                    .ge(ApiReview::getReviewTime, dayStart)
                    .le(ApiReview::getReviewTime, dayEnd)
                    .list();

            long approved = dayReviews.stream()
                    .filter(r -> "approved".equals(r.getResult()) || ApiStatusEnum.RELEASE_SUCCESS.getMessage().equals(r.getResult()))
                    .count();
            long rejected = dayReviews.stream()
                    .filter(r -> "rejected".equals(r.getResult()) || ApiStatusEnum.RELEASE_FAIL.getMessage().equals(r.getResult()))
                    .count();

             List<Long> times = calculateReviewTimes(dayReviews);
             long avgTime = times.isEmpty() ? 0 : Math.round(times.stream()
                     .mapToLong(Long::longValue)
                     .average()
                     .orElse(0));

             dailyStats.add(ReviewStatsVO.DailyStats.builder()
                     .date(current.toString())
                     .pending(pending)
                     .approved(approved)
                     .rejected(rejected)
                     .avgTime(avgTime)
                     .build());

            current = current.plusDays(1);
        }

        return dailyStats;
    }

    private List<ReviewStatsVO.ReviewerStats> calculateReviewerStats(List<ApiReview> reviews) {
        return reviews.stream()
                .collect(Collectors.groupingBy(ApiReview::getAdminId))
                .entrySet().stream()
                .map(entry -> getReviewerPerformance(entry.getKey()))
                .collect(Collectors.toList());
    }

    private List<ReviewStatsVO.CategoryStats> calculateCategoryStats(List<ApiReview> reviews) {
        Map<Long, List<ApiReview>> grouped = reviews.stream()
                .collect(Collectors.groupingBy(ApiReview::getApiId));

        Map<Long, List<Long>> categoryMap = new HashMap<>();
        for (Long apiId : grouped.keySet()) {
            ApiInfo apiInfo = apiInfoDao.getById(apiId);
            if (apiInfo != null) {
                categoryMap.computeIfAbsent(apiInfo.getCategoryId(), k -> new ArrayList<>()).add(apiId);
            }
        }

        List<ReviewStatsVO.CategoryStats> result = new ArrayList<>();
        for (Map.Entry<Long, List<Long>> entry : categoryMap.entrySet()) {
            List<ApiReview> categoryReviews = entry.getValue().stream()
                    .flatMap(apiId -> grouped.getOrDefault(apiId, new ArrayList<>()).stream())
                    .collect(Collectors.toList());

            long approved = categoryReviews.stream()
                    .filter(r -> "approved".equals(r.getResult()) || ApiStatusEnum.RELEASE_SUCCESS.getMessage().equals(r.getResult()))
                    .count();
            double approvalRate = categoryReviews.isEmpty() ? 0 : (double) approved / categoryReviews.size() * 100;

            long pending = apiInfoDao.lambdaQuery()
                    .eq(ApiInfo::getCategoryId, entry.getKey())
                    .eq(ApiInfo::getStatus, ApiStatusEnum.RELEASING.getCode())
                    .count();

            result.add(ReviewStatsVO.CategoryStats.builder()
                    .categoryId(entry.getKey())
                    .pending(pending)
                    .approved(approved)
                    .rejected(categoryReviews.size() - approved)
                    .approvalRate(approvalRate)
                    .build());
        }

        return result;
    }
}

