package com.bqy.openapibackend.service;

import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.dao.ApiLimitDao;
import com.bqy.openapibackend.exception.OpzException;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.entity.ApiLimit;
import com.bqy.openapibackend.model.request.api.ApiLimitAddRequest;
import com.bqy.openapibackend.model.request.api.ApiLimitDeleteRequest;
import com.bqy.openapibackend.model.request.api.ApiLimitUpdateRequest;
import com.bqy.openapibackend.service.impl.ApiLimitServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ApiLimitService 单元测试
 * 覆盖 API 限流配置的 CRUD 操作
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("API 限流服务测试")
class ApiLimitServiceTest {

    @Mock
    private ApiLimitDao apiLimitDao;

    @Mock
    private ApiInfoDao apiInfoDao;

    @InjectMocks
    private ApiLimitServiceImpl apiLimitService;

    // ===================== 新增限流配置 =====================

    @Test
    @DisplayName("新增限流：apiId 有效时成功保存")
    void addApiLimit_validApiId_success() {
        ApiInfo mockApi = new ApiInfo();
        mockApi.setId(1L);
        when(apiInfoDao.getApiInfoById(1L)).thenReturn(mockApi);
        when(apiLimitDao.save(any(ApiLimit.class))).thenReturn(true);

        ApiLimitAddRequest req = new ApiLimitAddRequest();
        req.setApiId(1L);

        Boolean result = apiLimitService.addApiLimit(req);

        assertTrue(result);
        verify(apiLimitDao, times(1)).save(any(ApiLimit.class));
    }

    @Test
    @DisplayName("新增限流：apiId 无效时抛出异常")
    void addApiLimit_invalidApiId_throwsException() {
        when(apiInfoDao.getApiInfoById(99L)).thenReturn(null);

        ApiLimitAddRequest req = new ApiLimitAddRequest();
        req.setApiId(99L);

        OpzException ex = assertThrows(OpzException.class,
                () -> apiLimitService.addApiLimit(req));
        assertEquals("apiId无效", ex.getMessage());
        verify(apiLimitDao, never()).save(any());
    }

    @Test
    @DisplayName("新增限流：DAO 保存失败时抛出异常")
    void addApiLimit_saveFails_throwsException() {
        ApiInfo mockApi = new ApiInfo();
        mockApi.setId(2L);
        when(apiInfoDao.getApiInfoById(2L)).thenReturn(mockApi);
        when(apiLimitDao.save(any(ApiLimit.class))).thenReturn(false);

        ApiLimitAddRequest req = new ApiLimitAddRequest();
        req.setApiId(2L);

        OpzException ex = assertThrows(OpzException.class,
                () -> apiLimitService.addApiLimit(req));
        assertEquals("保存失败,数据库发生异常", ex.getMessage());
    }

    // ===================== 更新限流配置 =====================

    @Test
    @DisplayName("更新限流：记录存在时成功更新")
    void updateApiLimit_exists_success() {
        ApiLimit existing = new ApiLimit();
        existing.setId(1L);
        when(apiLimitDao.getById(1L)).thenReturn(existing);
        when(apiLimitDao.updateById(any(ApiLimit.class))).thenReturn(true);

        ApiLimitUpdateRequest req = new ApiLimitUpdateRequest();
        req.setId(1L);

        Boolean result = apiLimitService.updateApiLimit(req);
        assertTrue(result);
    }

    @Test
    @DisplayName("更新限流：记录不存在时抛出异常")
    void updateApiLimit_notExists_throwsException() {
        when(apiLimitDao.getById(99L)).thenReturn(null);

        ApiLimitUpdateRequest req = new ApiLimitUpdateRequest();
        req.setId(99L);

        OpzException ex = assertThrows(OpzException.class,
                () -> apiLimitService.updateApiLimit(req));
        assertEquals("数据不存在", ex.getMessage());
    }

    // ===================== 删除限流配置 =====================

    @Test
    @DisplayName("删除限流：成功删除")
    void deleteApiLimit_success() {
        when(apiLimitDao.removeById(1L)).thenReturn(true);

        ApiLimitDeleteRequest req = new ApiLimitDeleteRequest();
        req.setId(1L);

        Boolean result = apiLimitService.deleteApiLimit(req);
        assertTrue(result);
    }

    @Test
    @DisplayName("删除限流：DAO 删除失败时抛出异常")
    void deleteApiLimit_fails_throwsException() {
        when(apiLimitDao.removeById(99L)).thenReturn(false);

        ApiLimitDeleteRequest req = new ApiLimitDeleteRequest();
        req.setId(99L);

        OpzException ex = assertThrows(OpzException.class,
                () -> apiLimitService.deleteApiLimit(req));
        assertEquals("删除失败,数据库发生异常", ex.getMessage());
    }
}

