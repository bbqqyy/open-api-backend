package com.bqy.openapibackend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.dao.ApiCallLogDao;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.dao.UserDao;
import com.bqy.openapibackend.model.entity.ApiCallLog;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.model.vo.ApiCallAnalyticsVO;
import com.bqy.openapibackend.model.vo.ApiCallLogVO;
import com.bqy.openapibackend.service.IApiCallLogAnalyticsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * API 调用日志分析服务实现
 */
@Slf4j
@Service
public class ApiCallLogAnalyticsServiceImpl implements IApiCallLogAnalyticsService {

    @Resource
    private ApiCallLogDao apiCallLogDao;

    @Resource
    private ApiInfoDao apiInfoDao;

    @Resource
    private UserDao userDao;

    @Override
    public Page<ApiCallLogVO> queryCallLogs(Long apiId, Long userId, String status,
                                             LocalDateTime startTime, LocalDateTime endTime,
                                             int pageNum, int pageSize) {
        LocalDateTime normalizedEnd = normalizeEndTime(endTime);
        // 查询日志
        Page<ApiCallLog> logPage = apiCallLogDao.lambdaQuery()
                .eq(apiId != null, ApiCallLog::getApiId, apiId)
                .eq(userId != null, ApiCallLog::getUserId, userId)
                .eq(status != null && !status.isEmpty(), ApiCallLog::getStatus, status)
                .ge(startTime != null, ApiCallLog::getCallTime, startTime)
                .le(normalizedEnd != null, ApiCallLog::getCallTime, normalizedEnd)
                .orderByDesc(ApiCallLog::getCallTime)
                .page(new Page<>(pageNum, pageSize));

        // 转换为 VO
        List<ApiCallLogVO> voList = logPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 创建返回页面
        Page<ApiCallLogVO> voPage = new Page<>(pageNum, pageSize, logPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ApiCallAnalyticsVO getOverallAnalytics(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime normalizedEnd = normalizeEndTime(endTime);
        List<ApiCallLog> logs = apiCallLogDao.lambdaQuery()
                .ge(ApiCallLog::getCallTime, startTime)
                .le(ApiCallLog::getCallTime, normalizedEnd)
                .list();

        return buildAnalytics(logs, startTime, normalizedEnd);
    }

    @Override
    public ApiCallAnalyticsVO getApiAnalytics(Long apiId, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime normalizedEnd = normalizeEndTime(endTime);
        List<ApiCallLog> logs = apiCallLogDao.lambdaQuery()
                .eq(ApiCallLog::getApiId, apiId)
                .ge(ApiCallLog::getCallTime, startTime)
                .le(ApiCallLog::getCallTime, normalizedEnd)
                .list();

        return buildAnalytics(logs, startTime, normalizedEnd);
    }

    @Override
    public List<ApiCallAnalyticsVO.TimeSeriesData> getTimeSeriesData(LocalDateTime startTime,
                                                                      LocalDateTime endTime,
                                                                      int intervalMinutes) {
        LocalDateTime normalizedEnd = normalizeEndTime(endTime);
        List<ApiCallLog> logs = apiCallLogDao.lambdaQuery()
                .ge(ApiCallLog::getCallTime, startTime)
                .le(ApiCallLog::getCallTime, normalizedEnd)
                .list();

        // 按时间间隔分组
        Map<LocalDateTime, List<ApiCallLog>> groupedByTime = new TreeMap<>();

        for (LocalDateTime time = startTime; time.isBefore(normalizedEnd); time = time.plusMinutes(intervalMinutes)) {
            LocalDateTime nextTime = time.plusMinutes(intervalMinutes);
            final LocalDateTime timePoint = time;

            List<ApiCallLog> logsInInterval = logs.stream()
                    .filter(log -> !log.getCallTime().isBefore(timePoint) && log.getCallTime().isBefore(nextTime))
                    .collect(Collectors.toList());

            if (!logsInInterval.isEmpty()) {
                groupedByTime.put(timePoint, logsInInterval);
            }
        }

        // 转换为时间序列数据
        return groupedByTime.entrySet().stream()
                .map(entry -> {
                    List<ApiCallLog> groupLogs = entry.getValue();
                    long successCount = groupLogs.stream()
                            .filter(log -> "success".equals(log.getStatus()))
                            .count();
                    long failureCount = groupLogs.size() - successCount;
                    long avgResponseTime = Math.round(groupLogs.stream()
                            .mapToLong(ApiCallLog::getResponseTime)
                            .average()
                            .orElse(0));

                    return ApiCallAnalyticsVO.TimeSeriesData.builder()
                            .timestamp(entry.getKey().toString())
                            .totalCount(groupLogs.size())
                            .successCount(successCount)
                            .failureCount(failureCount)
                            .avgResponseTime(avgResponseTime)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    /**
     * 构建分析数据
     */
    private ApiCallAnalyticsVO buildAnalytics(List<ApiCallLog> logs, LocalDateTime startTime, LocalDateTime endTime) {
        if (logs.isEmpty()) {
            return ApiCallAnalyticsVO.builder()
                    .overview(ApiCallAnalyticsVO.Overview.builder()
                            .totalCalls(0)
                            .successCount(0)
                            .failureCount(0)
                            .successRate(0)
                            .avgResponseTime(0)
                            .build())
                    .timeSeries(Collections.emptyList())
                    .responseTimeDistribution(buildEmptyResponseTimeDistribution())
                    .statusDistribution(buildEmptyStatusDistribution())
                    .topApis(Collections.emptyList())
                    .topUsers(Collections.emptyList())
                    .build();
        }

        return ApiCallAnalyticsVO.builder()
                .overview(buildOverview(logs))
                .timeSeries(buildTimeSeries(logs, startTime, endTime))
                .responseTimeDistribution(buildResponseTimeDistribution(logs))
                .statusDistribution(buildStatusDistribution(logs))
                .topApis(buildTopApis(logs))
                .topUsers(buildTopUsers(logs))
                .build();
    }

    /**
     * 构建概览数据
     */
    private ApiCallAnalyticsVO.Overview buildOverview(List<ApiCallLog> logs) {
        long totalCalls = logs.size();
        long successCount = logs.stream()
                .filter(log -> "success".equals(log.getStatus()))
                .count();
        long failureCount = totalCalls - successCount;
        double successRate = totalCalls == 0 ? 0 : (double) successCount / totalCalls * 100;

        List<Long> responseTimes = logs.stream()
                .map(ApiCallLog::getResponseTime)
                .sorted()
                .collect(Collectors.toList());

        double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        long minResponseTime = responseTimes.isEmpty() ? 0 : responseTimes.get(0);
        long maxResponseTime = responseTimes.isEmpty() ? 0 : responseTimes.get(responseTimes.size() - 1);
        long medianResponseTime = responseTimes.isEmpty() ? 0 : responseTimes.get(responseTimes.size() / 2);
        long p95ResponseTime = responseTimes.isEmpty() ? 0 : responseTimes.get((int) (responseTimes.size() * 0.95));
        long p99ResponseTime = responseTimes.isEmpty() ? 0 : responseTimes.get((int) Math.min(responseTimes.size() - 1, responseTimes.size() * 0.99));

        return ApiCallAnalyticsVO.Overview.builder()
                .totalCalls(totalCalls)
                .successCount(successCount)
                .failureCount(failureCount)
                .successRate(successRate)
                .avgResponseTime(avgResponseTime)
                .minResponseTime(minResponseTime)
                .maxResponseTime(maxResponseTime)
                .medianResponseTime(medianResponseTime)
                .p95ResponseTime(p95ResponseTime)
                .p99ResponseTime(p99ResponseTime)
                .build();
    }

    /**
     * 构建时间序列数据
     */
    private List<ApiCallAnalyticsVO.TimeSeriesData> buildTimeSeries(List<ApiCallLog> logs,
                                                                     LocalDateTime startTime,
                                                                     LocalDateTime endTime) {
        // 按小时分组
        int intervalMinutes = 60;
        long hoursBetween = ChronoUnit.HOURS.between(startTime, endTime);

        if (hoursBetween > 24) {
            intervalMinutes = 1440; // 按天分组
        } else if (hoursBetween > 7) {
            intervalMinutes = 360; // 按6小时分组
        }

        return getTimeSeriesData(startTime, endTime, intervalMinutes);
    }

    /**
     * 构建响应时间分布
     */
    private ApiCallAnalyticsVO.ResponseTimeDistribution buildResponseTimeDistribution(List<ApiCallLog> logs) {
        long veryFast = logs.stream().filter(log -> log.getResponseTime() <= 100).count();
        long fast = logs.stream().filter(log -> log.getResponseTime() > 100 && log.getResponseTime() <= 500).count();
        long normal = logs.stream().filter(log -> log.getResponseTime() > 500 && log.getResponseTime() <= 1000).count();
        long slow = logs.stream().filter(log -> log.getResponseTime() > 1000 && log.getResponseTime() <= 5000).count();
        long verySlow = logs.stream().filter(log -> log.getResponseTime() > 5000).count();

        return ApiCallAnalyticsVO.ResponseTimeDistribution.builder()
                .veryFast(veryFast)
                .fast(fast)
                .normal(normal)
                .slow(slow)
                .verySlow(verySlow)
                .build();
    }

    /**
     * 构建状态分布
     */
    private ApiCallAnalyticsVO.StatusDistribution buildStatusDistribution(List<ApiCallLog> logs) {
        long success = logs.stream().filter(log -> "success".equals(log.getStatus())).count();
        long failure = logs.size() - success;

        return ApiCallAnalyticsVO.StatusDistribution.builder()
                .success(success)
                .failure(failure)
                .build();
    }

    /**
     * 构建 Top API 数据
     */
    private List<ApiCallAnalyticsVO.TopApiData> buildTopApis(List<ApiCallLog> logs) {
        return logs.stream()
                .collect(Collectors.groupingBy(ApiCallLog::getApiId))
                .entrySet().stream()
                .map(entry -> {
                    Long apiId = entry.getKey();
                    List<ApiCallLog> apiLogs = entry.getValue();

                    ApiInfo apiInfo = apiInfoDao.getById(apiId);
                    String apiName = apiInfo != null ? apiInfo.getApiName() : "未知 API";

                    long successCount = apiLogs.stream()
                            .filter(log -> "success".equals(log.getStatus()))
                            .count();
                    long failureCount = apiLogs.size() - successCount;
                    double successRate = (double) successCount / apiLogs.size() * 100;
                    long avgResponseTime = Math.round(apiLogs.stream()
                            .mapToLong(ApiCallLog::getResponseTime)
                            .average()
                            .orElse(0));

                    return ApiCallAnalyticsVO.TopApiData.builder()
                            .apiId(apiId)
                            .apiName(apiName)
                            .callCount(apiLogs.size())
                            .successCount(successCount)
                            .failureCount(failureCount)
                            .successRate(successRate)
                            .avgResponseTime(avgResponseTime)
                            .build();
                })
                .sorted(Comparator.comparing(ApiCallAnalyticsVO.TopApiData::getCallCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 构建 Top 用户数据
     */
    private List<ApiCallAnalyticsVO.TopUserData> buildTopUsers(List<ApiCallLog> logs) {
        return logs.stream()
                .collect(Collectors.groupingBy(ApiCallLog::getUserId))
                .entrySet().stream()
                .map(entry -> {
                    Long userId = entry.getKey();
                    List<ApiCallLog> userLogs = entry.getValue();

                    User user = userDao.getById(userId);
                    String userAccount = user != null ? user.getUserAccount() : "未知用户";

                    long successCount = userLogs.stream()
                            .filter(log -> "success".equals(log.getStatus()))
                            .count();
                    long failureCount = userLogs.size() - successCount;
                    double successRate = (double) successCount / userLogs.size() * 100;

                    return ApiCallAnalyticsVO.TopUserData.builder()
                            .userId(userId)
                            .userAccount(userAccount)
                            .callCount(userLogs.size())
                            .successCount(successCount)
                            .failureCount(failureCount)
                            .successRate(successRate)
                            .build();
                })
                .sorted(Comparator.comparing(ApiCallAnalyticsVO.TopUserData::getCallCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 构建空的响应时间分布
     */
    private ApiCallAnalyticsVO.ResponseTimeDistribution buildEmptyResponseTimeDistribution() {
        return ApiCallAnalyticsVO.ResponseTimeDistribution.builder()
                .veryFast(0)
                .fast(0)
                .normal(0)
                .slow(0)
                .verySlow(0)
                .build();
    }

    /**
     * 构建空的状态分布
     */
    private ApiCallAnalyticsVO.StatusDistribution buildEmptyStatusDistribution() {
        return ApiCallAnalyticsVO.StatusDistribution.builder()
                .success(0)
                .failure(0)
                .build();
    }

    /**
     * 将 endTime 补全到当天末尾（23:59:59.999）
     * 解决前端传入的 endTime 精确到某时刻，导致当天剩余时间数据被漏查的问题
     */
    private LocalDateTime normalizeEndTime(LocalDateTime endTime) {
        if (endTime == null) return null;
        return endTime.toLocalDate().atTime(23, 59, 59, 999_000_000);
    }

    /**
     * 转换为 VO
     */
    private ApiCallLogVO convertToVO(ApiCallLog log) {
        ApiInfo apiInfo = apiInfoDao.getById(log.getApiId());
        User user = userDao.getById(log.getUserId());

        return ApiCallLogVO.builder()
                .id(log.getId())
                .apiId(log.getApiId())
                .apiName(apiInfo != null ? apiInfo.getApiName() : "未知 API")
                .userId(log.getUserId())
                .userAccount(user != null ? user.getUserAccount() : "未知用户")
                .requestParam(log.getRequestParam())
                .responseTime(log.getResponseTime())
                .status(log.getStatus())
                .callTime(log.getCallTime())
                .build();
    }
}

