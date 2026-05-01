package com.bqy.openapibackend.service;

import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.dao.ApiResponseParamDao;
import com.bqy.openapibackend.exception.OpzException;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.entity.ApiResponseParam;
import com.bqy.openapibackend.model.request.api.ApiResponseParamAddRequest;
import com.bqy.openapibackend.model.request.api.ApiResponseParamDeleteRequest;
import com.bqy.openapibackend.model.request.api.ApiResponseParamUpdateRequest;
import com.bqy.openapibackend.service.impl.ApiResponseParamServiceImpl;
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
 * ApiResponseParamService 单元测试
 * 覆盖 API 响应参数的 CRUD 操作
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("API 响应参数服务测试")
class ApiResponseParamServiceTest {

    @Mock
    private ApiResponseParamDao apiResponseParamDao;

    @Mock
    private ApiInfoDao apiInfoDao;

    @InjectMocks
    private ApiResponseParamServiceImpl apiResponseParamService;

    // ===================== 新增响应参数 =====================

    @Test
    @DisplayName("新增响应参数：apiId 有效时成功")
    void addApiResponseParam_validApiId_success() {
        ApiInfo mockApi = new ApiInfo();
        mockApi.setId(1L);
        when(apiInfoDao.getApiInfoById(1L)).thenReturn(mockApi);
        when(apiResponseParamDao.save(any(ApiResponseParam.class))).thenReturn(true);

        ApiResponseParamAddRequest req = new ApiResponseParamAddRequest();
        req.setApiId(1L);

        Boolean result = apiResponseParamService.addApiResponseParam(req);

        assertTrue(result);
        verify(apiResponseParamDao, times(1)).save(any(ApiResponseParam.class));
    }

    @Test
    @DisplayName("新增响应参数：apiId 不存在时抛出异常")
    void addApiResponseParam_invalidApiId_throwsException() {
        when(apiInfoDao.getApiInfoById(99L)).thenReturn(null);

        ApiResponseParamAddRequest req = new ApiResponseParamAddRequest();
        req.setApiId(99L);

        OpzException ex = assertThrows(OpzException.class,
                () -> apiResponseParamService.addApiResponseParam(req));
        assertEquals("apiId不存在", ex.getMessage());
        verify(apiResponseParamDao, never()).save(any());
    }

    @Test
    @DisplayName("新增响应参数：DAO 保存失败时抛出异常")
    void addApiResponseParam_saveFails_throwsException() {
        ApiInfo mockApi = new ApiInfo();
        mockApi.setId(2L);
        when(apiInfoDao.getApiInfoById(2L)).thenReturn(mockApi);
        when(apiResponseParamDao.save(any(ApiResponseParam.class))).thenReturn(false);

        ApiResponseParamAddRequest req = new ApiResponseParamAddRequest();
        req.setApiId(2L);

        OpzException ex = assertThrows(OpzException.class,
                () -> apiResponseParamService.addApiResponseParam(req));
        assertEquals("保存失败,数据库发生异常", ex.getMessage());
    }

    // ===================== 更新响应参数 =====================

    @Test
    @DisplayName("更新响应参数：记录存在时成功")
    void updateApiResponseParam_exists_success() {
        ApiResponseParam existing = new ApiResponseParam();
        existing.setId(1L);
        when(apiResponseParamDao.getById(1L)).thenReturn(existing);
        when(apiResponseParamDao.updateById(any(ApiResponseParam.class))).thenReturn(true);

        ApiResponseParamUpdateRequest req = new ApiResponseParamUpdateRequest();
        req.setId(1L);

        Boolean result = apiResponseParamService.updateApiResponseParam(req);
        assertTrue(result);
    }

    @Test
    @DisplayName("更新响应参数：记录不存在时抛出异常")
    void updateApiResponseParam_notExists_throwsException() {
        when(apiResponseParamDao.getById(99L)).thenReturn(null);

        ApiResponseParamUpdateRequest req = new ApiResponseParamUpdateRequest();
        req.setId(99L);

        OpzException ex = assertThrows(OpzException.class,
                () -> apiResponseParamService.updateApiResponseParam(req));
        assertEquals("数据不存在", ex.getMessage());
    }

    // ===================== 删除响应参数 =====================

    @Test
    @DisplayName("删除响应参数：成功")
    void deleteApiResponseParam_success() {
        when(apiResponseParamDao.removeById(1L)).thenReturn(true);

        ApiResponseParamDeleteRequest req = new ApiResponseParamDeleteRequest();
        req.setId(1L);

        Boolean result = apiResponseParamService.deleteApiResponseParam(req);
        assertTrue(result);
    }

    @Test
    @DisplayName("删除响应参数：失败时抛出异常")
    void deleteApiResponseParam_fails_throwsException() {
        when(apiResponseParamDao.removeById(99L)).thenReturn(false);

        ApiResponseParamDeleteRequest req = new ApiResponseParamDeleteRequest();
        req.setId(99L);

        OpzException ex = assertThrows(OpzException.class,
                () -> apiResponseParamService.deleteApiResponseParam(req));
        assertEquals("删除失败,数据库发生异常", ex.getMessage());
    }
}

