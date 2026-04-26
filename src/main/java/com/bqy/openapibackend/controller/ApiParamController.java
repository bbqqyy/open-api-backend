package com.bqy.openapibackend.controller;

import com.bqy.openapibackend.annotation.CheckApiOwner;
import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.model.request.api.ApiParamAddRequest;
import com.bqy.openapibackend.model.request.api.ApiParamDeleteRequest;
import com.bqy.openapibackend.model.request.api.ApiParamUpdateRequest;
import com.bqy.openapibackend.service.IApiParamService;
import com.bqy.openapibackend.util.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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
@Tag(name = "API参数管理", description = "API请求参数的增删改接口")
@RestController
@RequestMapping("/apiParam")
public class ApiParamController {

    @Resource
    private IApiParamService apiParamService;

    @Operation(summary = "添加API参数", description = "API拥有者添加请求参数")
    @PostMapping("/add")
    @CheckApiOwner
    public ApiResponse<Boolean> addApiParam(@Valid @RequestBody ApiParamAddRequest request) {
        ThrowUtils.throwIf(request == null, "请求体不能为空");
        return ApiResponse.success(apiParamService.addApiParam(request));
    }

    @Operation(summary = "更新API参数", description = "API拥有者更新请求参数")
    @PostMapping("/update")
    @CheckApiOwner
    public ApiResponse<Boolean> updateApiParam(@Valid @RequestBody ApiParamUpdateRequest request) {
        ThrowUtils.throwIf(request == null, "请求体不能为空");
        return ApiResponse.success(apiParamService.updateApiParam(request));
    }

    @Operation(summary = "删除API参数", description = "API拥有者删除请求参数")
    @DeleteMapping("/delete")
    @CheckApiOwner
    public ApiResponse<Boolean> deleteApiParam(@Valid @RequestBody ApiParamDeleteRequest request) {
        ThrowUtils.throwIf(request == null, "请求体不能为空");
        return ApiResponse.success(apiParamService.deleteApiParam(request));
    }

}
