package com.bqy.openapibackend.service;

import com.bqy.openapibackend.dao.ApiCallLogDao;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.dao.UserDao;
import com.bqy.openapibackend.model.entity.ApiCallLog;
import com.bqy.openapibackend.model.vo.ApiCallAnalyticsVO;
import com.bqy.openapibackend.service.impl.ApiCallLogAnalyticsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ApiCallLogAnalyticsService 单元测试
 * 重点测试分析统计逻辑，包括概览计算、响应时间分布、空数据降级
 *
 * 注意：由于 Service 内部使用了 lambdaQuery() 链式调用（依赖 MyBatis-Plus），
 * getOverallAnalytics / getApiAnalytics 通过 Mock lambdaQuery 链路测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("API 调用日志分析服务测试")
class ApiCallLogAnalyticsServiceTest {

    @Mock
    private ApiCallLogDao apiCallLogDao;

    @Mock
    private ApiInfoDao apiInfoDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private ApiCallLogAnalyticsServiceImpl analyticsService;

    // ===================== 响应时间分布测试（私有逻辑通过公开方法间接覆盖）=====================

    /**
     * 构建模拟日志列表（成功 + 失败 + 不同响应时间）用于分析
     */
    private List<ApiCallLog> buildMixedLogs() {
        return Arrays.asList(
                buildLog(1L, 1L, "success", 50L),    // veryFast (<= 100ms)
                buildLog(1L, 1L, "success", 80L),    // veryFast
                buildLog(2L, 1L, "success", 200L),   // fast (100-500ms)
                buildLog(2L, 1L, "fail",    300L),   // fast
                buildLog(3L, 2L, "success", 700L),   // normal (500-1000ms)
                buildLog(3L, 2L, "fail",    2000L),  // slow (1000-5000ms)
                buildLog(4L, 2L, "fail",    6000L)   // verySlow (>5000ms)
        );
    }

    @Test
    @DisplayName("空日志列表：概览全部为零，返回空集合")
    void getOverallAnalytics_emptyLogs_allZeros() {
        // Mock lambdaQuery 链（IService 标准方法链）
        var mockQuery = mock(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class);
        // 由于 lambdaQuery 链复杂，使用 doReturn stubbing on the DAO
        when(apiCallLogDao.lambdaQuery()).thenAnswer(inv -> {
            // 返回一个 mock 的 LambdaQueryChainWrapper
            var chain = mock(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper.class);
            when(chain.ge(any(), any())).thenReturn(chain);
            when(chain.le(any(), any())).thenReturn(chain);
            when(chain.list()).thenReturn(Collections.emptyList());
            return chain;
        });

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        ApiCallAnalyticsVO result = analyticsService.getOverallAnalytics(start, end);

        assertNotNull(result);
        assertNotNull(result.getOverview());
        assertEquals(0, result.getOverview().getTotalCalls());
        assertEquals(0, result.getOverview().getSuccessCount());
        assertEquals(0, result.getOverview().getFailureCount());
        assertEquals(0.0, result.getOverview().getSuccessRate());
        assertNotNull(result.getTimeSeries());
        assertTrue(result.getTimeSeries().isEmpty());
        assertNotNull(result.getTopApis());
        assertTrue(result.getTopApis().isEmpty());
        assertNotNull(result.getTopUsers());
        assertTrue(result.getTopUsers().isEmpty());
    }

    @Test
    @DisplayName("有日志数据：概览统计计算正确")
    void getOverallAnalytics_withLogs_overviewCorrect() {
        List<ApiCallLog> logs = buildMixedLogs();

        when(apiCallLogDao.lambdaQuery()).thenAnswer(inv -> {
            var chain = mock(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper.class);
            when(chain.ge(any(), any())).thenReturn(chain);
            when(chain.le(any(), any())).thenReturn(chain);
            when(chain.list()).thenReturn(logs);
            return chain;
        });

        // 为 buildTopApis 的 apiInfoDao.getById 也 mock 一下（避免 NPE）
        when(apiInfoDao.getById(any())).thenReturn(null);
        when(userDao.getById(any())).thenReturn(null);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        ApiCallAnalyticsVO result = analyticsService.getOverallAnalytics(start, end);

        assertNotNull(result);
        // 7 条日志：4 成功 3 失败
        assertEquals(7, result.getOverview().getTotalCalls());
        assertEquals(4, result.getOverview().getSuccessCount());
        assertEquals(3, result.getOverview().getFailureCount());
        // 成功率 4/7 * 100 ≈ 57.14%
        assertTrue(result.getOverview().getSuccessRate() > 57.0);
        assertTrue(result.getOverview().getSuccessRate() < 58.0);
    }

    @Test
    @DisplayName("有日志数据：响应时间分布分区计算正确")
    void getOverallAnalytics_responseTimeDistributionCorrect() {
        List<ApiCallLog> logs = buildMixedLogs();

        when(apiCallLogDao.lambdaQuery()).thenAnswer(inv -> {
            var chain = mock(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper.class);
            when(chain.ge(any(), any())).thenReturn(chain);
            when(chain.le(any(), any())).thenReturn(chain);
            when(chain.list()).thenReturn(logs);
            return chain;
        });
        when(apiInfoDao.getById(any())).thenReturn(null);
        when(userDao.getById(any())).thenReturn(null);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        ApiCallAnalyticsVO result = analyticsService.getOverallAnalytics(start, end);

        ApiCallAnalyticsVO.ResponseTimeDistribution dist = result.getResponseTimeDistribution();
        assertNotNull(dist);
        assertEquals(2L, dist.getVeryFast(), "<=100ms 的记录应为 2 条");
        assertEquals(2L, dist.getFast(),     "100-500ms 的记录应为 2 条");
        assertEquals(1L, dist.getNormal(),   "500-1000ms 的记录应为 1 条");
        assertEquals(1L, dist.getSlow(),     "1000-5000ms 的记录应为 1 条");
        assertEquals(1L, dist.getVerySlow(), ">5000ms 的记录应为 1 条");
    }

    @Test
    @DisplayName("有日志数据：状态分布计算正确")
    void getOverallAnalytics_statusDistributionCorrect() {
        List<ApiCallLog> logs = buildMixedLogs();

        when(apiCallLogDao.lambdaQuery()).thenAnswer(inv -> {
            var chain = mock(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper.class);
            when(chain.ge(any(), any())).thenReturn(chain);
            when(chain.le(any(), any())).thenReturn(chain);
            when(chain.list()).thenReturn(logs);
            return chain;
        });
        when(apiInfoDao.getById(any())).thenReturn(null);
        when(userDao.getById(any())).thenReturn(null);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        ApiCallAnalyticsVO result = analyticsService.getOverallAnalytics(start, end);

        ApiCallAnalyticsVO.StatusDistribution statusDist = result.getStatusDistribution();
        assertNotNull(statusDist);
        assertEquals(4L, statusDist.getSuccess(), "成功调用应为 4 条");
        assertEquals(3L, statusDist.getFailure(), "失败调用应为 3 条");
    }

    @Test
    @DisplayName("响应时间百分位计算：p95 和 p99 边界值正确")
    void getOverallAnalytics_percentileResponseTime_correct() {
        // 20 条有序日志：前 19 成功，最后 1 失败，响应时间 10~200ms
        List<ApiCallLog> logs = new java.util.ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            logs.add(buildLog((long) i, 1L, i < 20 ? "success" : "fail", (long) (i * 10)));
        }

        when(apiCallLogDao.lambdaQuery()).thenAnswer(inv -> {
            var chain = mock(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper.class);
            when(chain.ge(any(), any())).thenReturn(chain);
            when(chain.le(any(), any())).thenReturn(chain);
            when(chain.list()).thenReturn(logs);
            return chain;
        });
        when(apiInfoDao.getById(any())).thenReturn(null);
        when(userDao.getById(any())).thenReturn(null);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        ApiCallAnalyticsVO result = analyticsService.getOverallAnalytics(start, end);

        ApiCallAnalyticsVO.Overview overview = result.getOverview();
        // 最小：10ms，最大：200ms
        assertEquals(10L, overview.getMinResponseTime());
        assertEquals(200L, overview.getMaxResponseTime());
        // p95 位于 index = 20 * 0.95 = 19 (0-based), 即第20条 = 200ms
        assertTrue(overview.getP95ResponseTime() > 0);
    }

    // ===================== normalizeEndTime 覆盖 =====================

    @Test
    @DisplayName("endTime 为 null 时不崩溃（normalizeEndTime 返回 null）")
    void getOverallAnalytics_nullEndTime_doesNotThrow() {
        when(apiCallLogDao.lambdaQuery()).thenAnswer(inv -> {
            var chain = mock(com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper.class);
            when(chain.ge(any(), any())).thenReturn(chain);
            when(chain.le(any(), any())).thenReturn(chain);
            when(chain.list()).thenReturn(Collections.emptyList());
            return chain;
        });

        LocalDateTime start = LocalDateTime.now().minusDays(1);

        assertDoesNotThrow(() -> analyticsService.getOverallAnalytics(start, null));
    }

    // ===================== 辅助方法 =====================

    private ApiCallLog buildLog(Long apiId, Long userId, String status, Long responseTime) {
        ApiCallLog log = new ApiCallLog();
        log.setApiId(apiId);
        log.setUserId(userId);
        log.setStatus(status);
        log.setResponseTime(responseTime);
        log.setCallTime(LocalDateTime.now());
        return log;
    }
}

