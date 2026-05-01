package com.bqy.openapibackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.dao.ApiCategoryDao;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.exception.OpzException;
import com.bqy.openapibackend.model.entity.ApiCategory;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryAddRequest;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryDeleteRequest;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryQueryRequest;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryUpdateRequest;
import com.bqy.openapibackend.model.vo.ApiCategoryVO;
import com.bqy.openapibackend.service.impl.ApiCategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ApiCategoryService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("API 分类服务测试")
class ApiCategoryServiceTest {

    @Mock
    private ApiCategoryDao apiCategoryDao;

    @Mock
    private ApiInfoDao apiInfoDao;

    @InjectMocks
    private ApiCategoryServiceImpl apiCategoryService;

    // ===================== 新增分类 =====================

    @Test
    @DisplayName("新增分类成功")
    void addAppCategory_success() {
        when(apiCategoryDao.save(any(ApiCategory.class))).thenReturn(true);

        ApiCategoryAddRequest req = new ApiCategoryAddRequest();
        req.setName("数据服务");
        req.setDescription("数据相关接口");

        Boolean result = apiCategoryService.addAppCategory(req);

        assertTrue(result);
        verify(apiCategoryDao, times(1)).save(any(ApiCategory.class));
    }

    @Test
    @DisplayName("新增分类失败：DAO 保存返回 false 时抛出异常")
    void addAppCategory_saveFails_throwsException() {
        when(apiCategoryDao.save(any(ApiCategory.class))).thenReturn(false);

        ApiCategoryAddRequest req = new ApiCategoryAddRequest();
        req.setName("失败分类");

        OpzException ex = assertThrows(OpzException.class,
                () -> apiCategoryService.addAppCategory(req));
        assertEquals("添加失败，数据库异常", ex.getMessage());
    }

    // ===================== 删除分类 =====================

    @Test
    @DisplayName("删除分类：分类下无 API，直接删除")
    void deleteApiCategory_noApis_directDelete() {
        when(apiInfoDao.getApiInfoListByCategoryId(1L)).thenReturn(Collections.emptyList());
        when(apiCategoryDao.removeById(1L)).thenReturn(true);

        ApiCategoryDeleteRequest req = new ApiCategoryDeleteRequest();
        req.setId(1L);

        Boolean result = apiCategoryService.deleteApiCategory(req);

        assertTrue(result);
        verify(apiInfoDao, never()).updateBatchById(any());
        verify(apiCategoryDao, times(1)).removeById(1L);
    }

    @Test
    @DisplayName("删除分类：分类下有 API，先将 API 移至未分类再删除")
    void deleteApiCategory_withApis_migratesAndDeletes() {
        ApiInfo api1 = new ApiInfo();
        api1.setId(10L);
        api1.setCategoryId(2L);
        ApiInfo api2 = new ApiInfo();
        api2.setId(11L);
        api2.setCategoryId(2L);

        when(apiInfoDao.getApiInfoListByCategoryId(2L)).thenReturn(Arrays.asList(api1, api2));
        when(apiInfoDao.updateBatchById(any())).thenReturn(true);
        when(apiCategoryDao.removeById(2L)).thenReturn(true);

        ApiCategoryDeleteRequest req = new ApiCategoryDeleteRequest();
        req.setId(2L);

        Boolean result = apiCategoryService.deleteApiCategory(req);

        assertTrue(result);
        verify(apiInfoDao, times(1)).updateBatchById(any());
        verify(apiCategoryDao, times(1)).removeById(2L);
    }

    @Test
    @DisplayName("删除分类失败：DAO 返回 false 时抛出异常")
    void deleteApiCategory_removeFails_throwsException() {
        when(apiInfoDao.getApiInfoListByCategoryId(3L)).thenReturn(Collections.emptyList());
        when(apiCategoryDao.removeById(3L)).thenReturn(false);

        ApiCategoryDeleteRequest req = new ApiCategoryDeleteRequest();
        req.setId(3L);

        OpzException ex = assertThrows(OpzException.class,
                () -> apiCategoryService.deleteApiCategory(req));
        assertEquals("删除API分类失败，数据库异常", ex.getMessage());
    }

    // ===================== 更新分类 =====================

    @Test
    @DisplayName("更新分类成功")
    void updateApiCategory_success() {
        when(apiCategoryDao.updateById(any(ApiCategory.class))).thenReturn(true);

        ApiCategoryUpdateRequest req = new ApiCategoryUpdateRequest();
        req.setId(1L);
        req.setName("新名称");

        Boolean result = apiCategoryService.updateApiCategory(req);

        assertTrue(result);
    }

    @Test
    @DisplayName("更新分类失败：DAO 返回 false 时抛出异常")
    void updateApiCategory_updateFails_throwsException() {
        when(apiCategoryDao.updateById(any(ApiCategory.class))).thenReturn(false);

        ApiCategoryUpdateRequest req = new ApiCategoryUpdateRequest();
        req.setId(99L);
        req.setName("不存在的分类");

        assertThrows(OpzException.class, () -> apiCategoryService.updateApiCategory(req));
    }

    // ===================== 查询分类列表 =====================

    @Test
    @DisplayName("listApiCategories：返回所有分类 VO 列表")
    void listApiCategories_returnsList() {
        ApiCategory cat1 = buildCategory(1L, "分类A");
        ApiCategory cat2 = buildCategory(2L, "分类B");
        when(apiCategoryDao.list()).thenReturn(Arrays.asList(cat1, cat2));

        List<ApiCategoryVO> result = apiCategoryService.listApiCategories();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("分类A", result.get(0).getName());
        assertEquals("分类B", result.get(1).getName());
    }

    @Test
    @DisplayName("listApiCategories：空列表时返回空集合")
    void listApiCategories_emptyList() {
        when(apiCategoryDao.list()).thenReturn(Collections.emptyList());

        List<ApiCategoryVO> result = apiCategoryService.listApiCategories();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ===================== 分页查询 =====================

    @Test
    @DisplayName("getApiCategoryPage：分页结果转换正确")
    void getApiCategoryPage_correctConversion() {
        ApiCategory cat = buildCategory(1L, "测试分类");
        Page<ApiCategory> dbPage = new Page<>(1, 10, 1L);
        dbPage.setRecords(List.of(cat));

        ApiCategoryQueryRequest req = new ApiCategoryQueryRequest();
        req.setCurrent(1);
        req.setPageSize(10);
        when(apiCategoryDao.getApiCategoryPage(req)).thenReturn(dbPage);

        Page<ApiCategoryVO> result = apiCategoryService.getApiCategoryPage(req);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("测试分类", result.getRecords().get(0).getName());
    }

    // ===================== 辅助方法 =====================

    private ApiCategory buildCategory(Long id, String name) {
        ApiCategory cat = new ApiCategory();
        cat.setId(id);
        cat.setName(name);
        cat.setDescription(name + " 描述");
        return cat;
    }
}

