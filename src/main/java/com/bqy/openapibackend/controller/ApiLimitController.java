package com.bqy.openapibackend.controller;

import com.bqy.openapibackend.annotation.CheckApiOwner;
import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.model.request.api.ApiLimitDeleteRequest;
import com.bqy.openapibackend.model.request.api.ApiLimitAddRequest;
import com.bqy.openapibackend.model.request.api.ApiLimitUpdateRequest;
import com.bqy.openapibackend.service.IApiLimitService;
import com.bqy.openapibackend.util.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
@Tag(name = "API限流管理", description = "API限流规则的增删改接口")
@RestController
@RequestMapping("/apiLimit")
public class ApiLimitController {

    @Resource
    private IApiLimitService apiLimitService;

    @Operation(summary = "添加API限流规则", description = "API拥有者添加限流规则")
    @PostMapping("/add")
    @CheckApiOwner
    public ApiResponse<Boolean> addApiLimit(@Valid @RequestBody ApiLimitAddRequest request) {
        ThrowUtils.throwIf(request == null, "请求体不能为空");
        return ApiResponse.success(apiLimitService.addApiLimit(request));
    }

    @Operation(summary = "更新API限流规则", description = "API拥有者更新限流规则")
    @PostMapping("/update")
    @CheckApiOwner
    public ApiResponse<Boolean> updateApiLimit(@Valid @RequestBody ApiLimitUpdateRequest request) {
        ThrowUtils.throwIf(request == null, "请求体不能为空");
        return ApiResponse.success(apiLimitService.updateApiLimit(request));
    }

    @Operation(summary = "删除API限流规则", description = "API拥有者删除限流规则")
    @DeleteMapping("/delete")
    @CheckApiOwner
    public ApiResponse<Boolean> deleteApiLimit(@Valid @RequestBody ApiLimitDeleteRequest request) {
        ThrowUtils.throwIf(request == null, "请求体不能为空");
        return ApiResponse.success(apiLimitService.deleteApiLimit(request));
    }
}
