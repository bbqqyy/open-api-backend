package com.bqy.openapibackend.service;

import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.dao.ApiParamDao;
import com.bqy.openapibackend.exception.OpzException;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.entity.ApiParam;
import com.bqy.openapibackend.model.request.api.ApiParamAddRequest;
import com.bqy.openapibackend.model.request.api.ApiParamDeleteRequest;
import com.bqy.openapibackend.model.request.api.ApiParamUpdateRequest;
import com.bqy.openapibackend.service.impl.ApiParamServiceImpl;
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
 * ApiParamService 单元测试
 * 覆盖 API 请求参数的 CRUD 操作
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("API 请求参数服务测试")
class ApiParamServiceTest {

    @Mock
    private ApiParamDao apiParamDao;

    @Mock
    private ApiInfoDao apiInfoDao;

    @InjectMocks
    private ApiParamServiceImpl apiParamService;

    // ===================== 新增参数 =====================

    @Test
    @DisplayName("新增参数：apiId 有效时成功")
    void addApiParam_validApiId_success() {
        ApiInfo mockApi = new ApiInfo();
        mockApi.setId(1L);
        when(apiInfoDao.getApiInfoById(1L)).thenReturn(mockApi);
        when(apiParamDao.save(any(ApiParam.class))).thenReturn(true);

        ApiParamAddRequest req = new ApiParamAddRequest();
        req.setApiId(1L);

        Boolean result = apiParamService.addApiParam(req);

        assertTrue(result);
        verify(apiParamDao, times(1)).save(any(ApiParam.class));
    }

    @Test
    @DisplayName("新增参数：apiId 无效时抛出异常")
    void addApiParam_invalidApiId_throwsException() {
        when(apiInfoDao.getApiInfoById(99L)).thenReturn(null);

        ApiParamAddRequest req = new ApiParamAddRequest();
        req.setApiId(99L);

        OpzException ex = assertThrows(OpzException.class,
                () -> apiParamService.addApiParam(req));
        assertEquals("apiId无效", ex.getMessage());
        verify(apiParamDao, never()).save(any());
    }

    @Test
    @DisplayName("新增参数：DAO 保存失败时抛出异常")
    void addApiParam_saveFails_throwsException() {
        ApiInfo mockApi = new ApiInfo();
        mockApi.setId(2L);
        when(apiInfoDao.getApiInfoById(2L)).thenReturn(mockApi);
        when(apiParamDao.save(any(ApiParam.class))).thenReturn(false);

        ApiParamAddRequest req = new ApiParamAddRequest();
        req.setApiId(2L);

        OpzException ex = assertThrows(OpzException.class,
                () -> apiParamService.addApiParam(req));
        assertEquals("保存失败，数据库发生异常", ex.getMessage());
    }

    // ===================== 更新参数 =====================

    @Test
    @DisplayName("更新参数：记录存在时成功")
    void updateApiParam_exists_success() {
        ApiParam existing = new ApiParam();
        existing.setId(1L);
        when(apiParamDao.getById(1L)).thenReturn(existing);
        when(apiParamDao.updateById(any(ApiParam.class))).thenReturn(true);

        ApiParamUpdateRequest req = new ApiParamUpdateRequest();
        req.setId(1L);

        Boolean result = apiParamService.updateApiParam(req);
        assertTrue(result);
    }

    @Test
    @DisplayName("更新参数：记录不存在时抛出异常")
    void updateApiParam_notExists_throwsException() {
        when(apiParamDao.getById(99L)).thenReturn(null);

        ApiParamUpdateRequest req = new ApiParamUpdateRequest();
        req.setId(99L);

        OpzException ex = assertThrows(OpzException.class,
                () -> apiParamService.updateApiParam(req));
        assertEquals("数据不存在", ex.getMessage());
    }

    @Test
    @DisplayName("更新参数：DAO 更新失败时抛出异常")
    void updateApiParam_updateFails_throwsException() {
        ApiParam existing = new ApiParam();
        existing.setId(3L);
        when(apiParamDao.getById(3L)).thenReturn(existing);
        when(apiParamDao.updateById(any(ApiParam.class))).thenReturn(false);

        ApiParamUpdateRequest req = new ApiParamUpdateRequest();
        req.setId(3L);

        OpzException ex = assertThrows(OpzException.class,
                () -> apiParamService.updateApiParam(req));
        assertEquals("更新失败，数据库发生异常", ex.getMessage());
    }

    // ===================== 删除参数 =====================

    @Test
    @DisplayName("删除参数：成功")
    void deleteApiParam_success() {
        when(apiParamDao.removeById(1L)).thenReturn(true);

        ApiParamDeleteRequest req = new ApiParamDeleteRequest();
        req.setId(1L);

        Boolean result = apiParamService.deleteApiParam(req);
        assertTrue(result);
    }

    @Test
    @DisplayName("删除参数：失败时抛出异常")
    void deleteApiParam_fails_throwsException() {
        when(apiParamDao.removeById(99L)).thenReturn(false);

        ApiParamDeleteRequest req = new ApiParamDeleteRequest();
        req.setId(99L);

        OpzException ex = assertThrows(OpzException.class,
                () -> apiParamService.deleteApiParam(req));
        assertEquals("删除失败，数据库发生异常", ex.getMessage());
    }
}

