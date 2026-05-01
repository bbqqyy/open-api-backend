package com.bqy.openapibackend.service;

import com.bqy.openapibackend.dao.ApiStatisticsDao;
import com.bqy.openapibackend.model.entity.ApiStatistics;
import com.bqy.openapibackend.service.impl.ApiStatisticsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ApiStatisticsService 单元测试
 * 覆盖 API 调用统计的核心业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("API 统计服务测试")
class ApiStatisticsServiceTest {

    @Mock
    private ApiStatisticsDao apiStatisticsDao;

    @InjectMocks
    private ApiStatisticsServiceImpl apiStatisticsService;

    // ===================== recordCall 测试 =====================

    @Test
    @DisplayName("首次调用成功：自动创建统计记录，callCount=1, successCount=1")
    void recordCall_firstSuccessCall_createsNewRecord() {
        Long apiId = 1L;
        when(apiStatisticsDao.getByApiId(apiId)).thenReturn(null);
        when(apiStatisticsDao.saveOrUpdate(any())).thenReturn(true);

        apiStatisticsService.recordCall(apiId, true);

        ArgumentCaptor<ApiStatistics> captor = ArgumentCaptor.forClass(ApiStatistics.class);
        verify(apiStatisticsDao).saveOrUpdate(captor.capture());

        ApiStatistics saved = captor.getValue();
        assertEquals(apiId, saved.getApiId());
        assertEquals(1, saved.getCallCount(), "总调用次数应为 1");
        assertEquals(1, saved.getSuccessCount(), "成功次数应为 1");
        assertEquals(0, saved.getFailCount(), "失败次数应为 0");
    }

    @Test
    @DisplayName("首次调用失败：自动创建统计记录，callCount=1, failCount=1")
    void recordCall_firstFailCall_createsNewRecord() {
        Long apiId = 2L;
        when(apiStatisticsDao.getByApiId(apiId)).thenReturn(null);
        when(apiStatisticsDao.saveOrUpdate(any())).thenReturn(true);

        apiStatisticsService.recordCall(apiId, false);

        ArgumentCaptor<ApiStatistics> captor = ArgumentCaptor.forClass(ApiStatistics.class);
        verify(apiStatisticsDao).saveOrUpdate(captor.capture());

        ApiStatistics saved = captor.getValue();
        assertEquals(1, saved.getCallCount(), "总调用次数应为 1");
        assertEquals(0, saved.getSuccessCount(), "成功次数应为 0");
        assertEquals(1, saved.getFailCount(), "失败次数应为 1");
    }

    @Test
    @DisplayName("累计调用：已有记录时正确累加计数")
    void recordCall_existingRecord_incrementsCorrectly() {
        Long apiId = 3L;
        ApiStatistics existing = buildStatistics(apiId, 10, 8, 2);
        when(apiStatisticsDao.getByApiId(apiId)).thenReturn(existing);
        when(apiStatisticsDao.saveOrUpdate(any())).thenReturn(true);

        // 再记录一次成功调用
        apiStatisticsService.recordCall(apiId, true);

        ArgumentCaptor<ApiStatistics> captor = ArgumentCaptor.forClass(ApiStatistics.class);
        verify(apiStatisticsDao).saveOrUpdate(captor.capture());

        ApiStatistics updated = captor.getValue();
        assertEquals(11, updated.getCallCount(), "总调用次数应累加为 11");
        assertEquals(9, updated.getSuccessCount(), "成功次数应累加为 9");
        assertEquals(2, updated.getFailCount(), "失败次数不变，仍为 2");
    }

    @Test
    @DisplayName("DAO 异常时不应向上抛出，保证主流程稳定")
    void recordCall_daoThrowsException_doesNotPropagate() {
        Long apiId = 4L;
        when(apiStatisticsDao.getByApiId(apiId)).thenThrow(new RuntimeException("数据库连接失败"));

        // 不应抛出异常，异常被内部吞掉并记录日志
        assertDoesNotThrow(() -> apiStatisticsService.recordCall(apiId, true));
    }

    // ===================== getByApiId 测试 =====================

    @Test
    @DisplayName("查询存在的统计记录")
    void getByApiId_exists_returnsStatistics() {
        Long apiId = 5L;
        ApiStatistics stats = buildStatistics(apiId, 100, 95, 5);
        when(apiStatisticsDao.getByApiId(apiId)).thenReturn(stats);

        ApiStatistics result = apiStatisticsService.getByApiId(apiId);

        assertNotNull(result);
        assertEquals(100, result.getCallCount());
        assertEquals(95, result.getSuccessCount());
    }

    @Test
    @DisplayName("查询不存在的统计记录返回 null")
    void getByApiId_notExists_returnsNull() {
        when(apiStatisticsDao.getByApiId(99L)).thenReturn(null);

        ApiStatistics result = apiStatisticsService.getByApiId(99L);

        assertNull(result);
    }

    // ===================== listByApiIdLastDays 测试 =====================

    @Test
    @DisplayName("获取最近 N 天统计列表")
    void listByApiIdLastDays_returnsOrderedList() {
        Long apiId = 6L;
        List<ApiStatistics> mockList = Arrays.asList(
                buildStatistics(apiId, 30, 28, 2),
                buildStatistics(apiId, 50, 47, 3)
        );
        when(apiStatisticsDao.listByApiIdLastDays(apiId, 7)).thenReturn(mockList);

        List<ApiStatistics> result = apiStatisticsService.listByApiIdLastDays(apiId, 7);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ===================== deleteByApiId 测试 =====================

    @Test
    @DisplayName("删除 API 统计记录成功")
    void deleteByApiId_success() {
        Long apiId = 7L;
        when(apiStatisticsDao.deleteByApiId(apiId)).thenReturn(true);

        boolean result = apiStatisticsService.deleteByApiId(apiId);

        assertTrue(result);
        verify(apiStatisticsDao, times(1)).deleteByApiId(apiId);
    }

    // ===================== 辅助方法 =====================

    private ApiStatistics buildStatistics(Long apiId, int callCount, int successCount, int failCount) {
        ApiStatistics stats = new ApiStatistics();
        stats.setApiId(apiId);
        stats.setCallCount(callCount);
        stats.setSuccessCount(successCount);
        stats.setFailCount(failCount);
        return stats;
    }
}

