package com.bqy.openapibackend.controller;

import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.exception.GlobalExceptionHandler;
import com.bqy.openapibackend.exception.OpzException;
import com.bqy.openapibackend.model.vo.ApiCategoryVO;
import com.bqy.openapibackend.service.IApiCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ApiCategoryController 单元测试（Standalone MockMvc）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("API 分类 Controller 测试")
class ApiCategoryControllerTest {

    @Mock
    private IApiCategoryService apiCategoryService;

    @InjectMocks
    private ApiCategoryController apiCategoryController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(apiCategoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ===================== POST /apiCategory/add =====================

    @Test
    @DisplayName("POST /apiCategory/add 成功添加分类")
    void addApiCategory_success() throws Exception {
        when(apiCategoryService.addAppCategory(any())).thenReturn(true);

        mockMvc.perform(post("/apiCategory/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"数据服务\",\"description\":\"数据相关接口\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("POST /apiCategory/add 分类名为空：返回参数错误")
    void addApiCategory_emptyName_returnsParamError() throws Exception {
        mockMvc.perform(post("/apiCategory/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"描述\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StatusCode.PARAMS_ERROR.getCode()));
    }

    @Test
    @DisplayName("POST /apiCategory/add 数据库异常：返回业务错误")
    void addApiCategory_dbFails_returnsError() throws Exception {
        when(apiCategoryService.addAppCategory(any()))
                .thenThrow(new OpzException(StatusCode.PARAMS_ERROR, "添加失败，数据库异常"));

        mockMvc.perform(post("/apiCategory/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"失败分类\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(StatusCode.PARAMS_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value("添加失败，数据库异常"));
    }

    // ===================== DELETE /apiCategory/delete =====================

    @Test
    @DisplayName("DELETE /apiCategory/delete 成功删除分类")
    void deleteApiCategory_success() throws Exception {
        when(apiCategoryService.deleteApiCategory(any())).thenReturn(true);

        mockMvc.perform(delete("/apiCategory/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    // ===================== PUT /apiCategory/update =====================

    @Test
    @DisplayName("PUT /apiCategory/update 成功更新分类")
    void updateApiCategory_success() throws Exception {
        when(apiCategoryService.updateApiCategory(any())).thenReturn(true);

        mockMvc.perform(put("/apiCategory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"name\":\"新分类名\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    // ===================== GET /apiCategory/list =====================

    @Test
    @DisplayName("GET /apiCategory/list 返回分类列表")
    void listApiCategories_returnsList() throws Exception {
        ApiCategoryVO vo1 = new ApiCategoryVO();
        vo1.setId(1L);
        vo1.setName("数据服务");
        ApiCategoryVO vo2 = new ApiCategoryVO();
        vo2.setId(2L);
        vo2.setName("用户服务");
        when(apiCategoryService.listApiCategories()).thenReturn(Arrays.asList(vo1, vo2));

        mockMvc.perform(get("/apiCategory/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("数据服务"))
                .andExpect(jsonPath("$.data[1].name").value("用户服务"));
    }

    @Test
    @DisplayName("GET /apiCategory/list 空列表：返回空数组")
    void listApiCategories_emptyList() throws Exception {
        when(apiCategoryService.listApiCategories()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/apiCategory/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ===================== POST /apiCategory/page =====================

    @Test
    @DisplayName("POST /apiCategory/page 分页查询成功")
    void getApiCategoryPage_success() throws Exception {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ApiCategoryVO> mockPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10, 0L);
        mockPage.setRecords(Collections.emptyList());
        when(apiCategoryService.getApiCategoryPage(any())).thenReturn(mockPage);

        mockMvc.perform(post("/apiCategory/page")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"current\":1,\"pageSize\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}

