package com.bqy.openapibackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.annotation.AuthorCheck;
import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.common.UserConstant;
import com.bqy.openapibackend.model.entity.ApiCategory;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryAddRequest;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryDeleteRequest;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryQueryRequest;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryUpdateRequest;
import com.bqy.openapibackend.model.vo.ApiCategoryVO;
import com.bqy.openapibackend.service.IApiCategoryService;
import com.bqy.openapibackend.util.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
@Tag(name = "API分类管理", description = "API分类的增删改查接口")
@RestController
@RequestMapping("/apiCategory")
public class ApiCategoryController {

    @Resource
    private IApiCategoryService apiCategoryService;

    @Operation(summary = "添加API分类", description = "管理员添加新的API分类")
    @PostMapping("/add")
    @AuthorCheck(mustRole = UserConstant.ADMIN)
    public ApiResponse<Boolean> addApiCategory(@Valid @RequestBody ApiCategoryAddRequest request) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(apiCategoryService.addAppCategory(request));
    }

    @Operation(summary = "删除API分类", description = "管理员删除API分类")
    @DeleteMapping("/delete")
    @AuthorCheck(mustRole = UserConstant.ADMIN)
    public ApiResponse<Boolean> deleteApiCategory(@Valid @RequestBody ApiCategoryDeleteRequest request) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(apiCategoryService.deleteApiCategory(request));
    }

    @Operation(summary = "更新API分类", description = "管理员更新API分类信息")
    @PutMapping("/update")
    @AuthorCheck(mustRole = UserConstant.ADMIN)
    public ApiResponse<Boolean> updateApiCategory(@Valid @RequestBody ApiCategoryUpdateRequest request) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(apiCategoryService.updateApiCategory(request));
    }

    @Operation(summary = "分页查询API分类", description = "分页获取API分类列表")
    @PostMapping("/page")
    public ApiResponse<Page<ApiCategoryVO>> getApiCategoryPage(@Valid @RequestBody ApiCategoryQueryRequest request) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(apiCategoryService.getApiCategoryPage(request));
    }

}
