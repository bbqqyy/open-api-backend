package com.bqy.openapibackend.controller;

import com.bqy.openapibackend.annotation.CheckApiOwner;
import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.model.request.api.ApiResponseParamAddRequest;
import com.bqy.openapibackend.model.request.api.ApiResponseParamDeleteRequest;
import com.bqy.openapibackend.model.request.api.ApiResponseParamUpdateRequest;
import com.bqy.openapibackend.service.IApiResponseParamService;
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
@Tag(name = "API响应参数管理", description = "API响应参数的增删改接口")
@RestController
@RequestMapping("/apiResponseParam")
public class ApiResponseParamController {
    @Resource
    private IApiResponseParamService apiResponseParamService;

    @Operation(summary = "添加API响应参数", description = "API拥有者添加响应参数")
    @PostMapping("/add")
    @CheckApiOwner
    public ApiResponse<Boolean> addApiResponseParam(@Valid @RequestBody ApiResponseParamAddRequest request) {
        ThrowUtils.throwIf(request == null, "请求体不能为空");
        return ApiResponse.success(apiResponseParamService.addApiResponseParam(request));
    }

    @Operation(summary = "更新API响应参数", description = "API拥有者更新响应参数")
    @PostMapping("/update")
    @CheckApiOwner
    public ApiResponse<Boolean> updateApiResponseParam(@Valid @RequestBody ApiResponseParamUpdateRequest request) {
        ThrowUtils.throwIf(request == null, "请求体不能为空");
        return ApiResponse.success(apiResponseParamService.updateApiResponseParam(request));
    }

    @Operation(summary = "删除API响应参数", description = "API拥有者删除响应参数")
    @DeleteMapping("/delete")
    @CheckApiOwner
    public ApiResponse<Boolean> deleteApiResponseParam(@Valid @RequestBody ApiResponseParamDeleteRequest request) {
        ThrowUtils.throwIf(request == null, "请求体不能为空");
        return ApiResponse.success(apiResponseParamService.deleteApiResponseParam(request));
    }
}
