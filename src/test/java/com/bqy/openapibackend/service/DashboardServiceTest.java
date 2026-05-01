package com.bqy.openapibackend.service;

import com.bqy.openapibackend.dao.ApiCallLogDao;
import com.bqy.openapibackend.dao.ApiCategoryDao;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.dao.UserDao;
import com.bqy.openapibackend.model.entity.ApiCategory;
import com.bqy.openapibackend.model.vo.DashboardVO;
import com.bqy.openapibackend.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * DashboardService 单元测试
 * 覆盖仪表板统计数据的计算逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("仪表板服务测试")
class DashboardServiceTest {

    @Mock
    private ApiInfoDao apiInfoDao;

    @Mock
    private ApiCallLogDao apiCallLogDao;

    @Mock
    private UserDao userDao;

    @Mock
    private ApiCategoryDao apiCategoryDao;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    // ===================== getDashboardData 正常流程 =====================

    @Test
    @DisplayName("获取仪表板数据：有调用记录时正确计算成功率")
    void getDashboardData_withCalls_correctSuccessRate() {
        when(apiInfoDao.getApiCount()).thenReturn(10L);
        when(apiCallLogDao.getTotalCallCount()).thenReturn(100L);
        when(apiCallLogDao.getSuccessCallCount()).thenReturn(95L);
        when(apiCallLogDao.getFailCallCount()).thenReturn(5L);
        when(userDao.getUserCount()).thenReturn(20L);
        when(apiCategoryDao.getCategoryCount()).thenReturn(3L);
        when(apiCategoryDao.list()).thenReturn(Collections.emptyList());

        DashboardVO result = dashboardService.getDashboardData();

        assertNotNull(result);
        assertEquals(10L, result.getTotalApiCount());
        assertEquals(100L, result.getTotalCallCount());
        assertEquals(95L, result.getSuccessCallCount());
        assertEquals(5L, result.getFailCallCount());
        assertEquals(20L, result.getOnlineUserCount());
        assertEquals(3L, result.getCategoryCount());
        // 成功率 = 95 / 100 * 100 = 95.0
        assertEquals(95.0, result.getSuccessRate(), 0.01);
    }

    @Test
    @DisplayName("获取仪表板数据：无调用记录时成功率为 0")
    void getDashboardData_noCallLogs_successRateIsZero() {
        when(apiInfoDao.getApiCount()).thenReturn(5L);
        when(apiCallLogDao.getTotalCallCount()).thenReturn(0L);
        when(apiCallLogDao.getSuccessCallCount()).thenReturn(0L);
        when(apiCallLogDao.getFailCallCount()).thenReturn(0L);
        when(userDao.getUserCount()).thenReturn(2L);
        when(apiCategoryDao.getCategoryCount()).thenReturn(1L);
        when(apiCategoryDao.list()).thenReturn(Collections.emptyList());

        DashboardVO result = dashboardService.getDashboardData();

        assertNotNull(result);
        assertEquals(0.0, result.getSuccessRate(), 0.001);
    }

    @Test
    @DisplayName("获取仪表板数据：包含分类统计列表")
    void getDashboardData_withCategories_categoryStatsCorrect() {
        ApiCategory cat1 = new ApiCategory();
        cat1.setId(1L);
        cat1.setName("用户服务");
        ApiCategory cat2 = new ApiCategory();
        cat2.setId(2L);
        cat2.setName("支付服务");

        when(apiInfoDao.getApiCount()).thenReturn(8L);
        when(apiCallLogDao.getTotalCallCount()).thenReturn(50L);
        when(apiCallLogDao.getSuccessCallCount()).thenReturn(48L);
        when(apiCallLogDao.getFailCallCount()).thenReturn(2L);
        when(userDao.getUserCount()).thenReturn(10L);
        when(apiCategoryDao.getCategoryCount()).thenReturn(2L);
        when(apiCategoryDao.list()).thenReturn(Arrays.asList(cat1, cat2));
        when(apiInfoDao.getApiCountByCategory(1L)).thenReturn(5L);
        when(apiInfoDao.getApiCountByCategory(2L)).thenReturn(3L);

        DashboardVO result = dashboardService.getDashboardData();

        assertNotNull(result);
        assertNotNull(result.getCategoryStats());
        assertEquals(2, result.getCategoryStats().size());
        assertEquals("用户服务", result.getCategoryStats().get(0).getCategoryName());
        assertEquals(5L, result.getCategoryStats().get(0).getApiCount());
        assertEquals("支付服务", result.getCategoryStats().get(1).getCategoryName());
        assertEquals(3L, result.getCategoryStats().get(1).getApiCount());
    }

    @Test
    @DisplayName("成功率保留两位小数精度")
    void getDashboardData_successRateRoundedToTwoDecimalPlaces() {
        when(apiInfoDao.getApiCount()).thenReturn(1L);
        when(apiCallLogDao.getTotalCallCount()).thenReturn(3L);
        when(apiCallLogDao.getSuccessCallCount()).thenReturn(2L);
        when(apiCallLogDao.getFailCallCount()).thenReturn(1L);
        when(userDao.getUserCount()).thenReturn(1L);
        when(apiCategoryDao.getCategoryCount()).thenReturn(0L);
        when(apiCategoryDao.list()).thenReturn(Collections.emptyList());

        DashboardVO result = dashboardService.getDashboardData();

        // 2/3 * 100 = 66.666... 保留两位小数 = 66.67
        assertEquals(66.67, result.getSuccessRate(), 0.01);
    }

    // ===================== getDashboardData 异常容错 =====================

    @Test
    @DisplayName("DAO 抛出异常时：返回默认零值数据，不上抛")
    void getDashboardData_daoThrowsException_returnsDefaultValues() {
        when(apiInfoDao.getApiCount()).thenThrow(new RuntimeException("数据库连接失败"));

        // 不应抛出异常
        DashboardVO result = assertDoesNotThrow(() -> dashboardService.getDashboardData());

        assertNotNull(result);
        assertEquals(0L, result.getTotalApiCount());
        assertEquals(0.0, result.getSuccessRate());
        assertEquals(0L, result.getTotalCallCount());
        assertNotNull(result.getCategoryStats());
        assertTrue(result.getCategoryStats().isEmpty());
    }
}

