package com.bqy.openapibackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.annotation.AuthorCheck;
import com.bqy.openapibackend.annotation.CheckApiOwner;
import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.common.UserConstant;
import com.bqy.openapibackend.model.request.api.ApplyApiQueryRequest;
import com.bqy.openapibackend.model.request.api.*;
import com.bqy.openapibackend.model.vo.ApiApplyVO;
import com.bqy.openapibackend.model.vo.ApiInfoDetailVO;
import com.bqy.openapibackend.model.vo.ApiInfoVO;
import com.bqy.openapibackend.service.IApiInfoService;
import com.bqy.openapibackend.util.ThrowUtils;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
@Tag(name = "API管理", description = "API信息管理、审核、申请、调用等接口")
@RestController
@RequestMapping("/apiInfo")
public class ApiInfoController {
    @Resource
    private IApiInfoService apiInfoService;

    @Operation(summary = "添加API", description = "创建新的API接口")
    @PostMapping("/add")
    public ApiResponse<Boolean> addApiInfo(@Valid @RequestBody ApiInfoAddRequest request, HttpServletRequest servletRequest) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(apiInfoService.addApiInfo(request, servletRequest));
    }

    @Operation(summary = "更新API", description = "更新API接口信息")
    @PutMapping("/update")
    public ApiResponse<Boolean> updateApiInfo(@Valid @RequestBody ApiInfoUpdateRequest request, HttpServletRequest servletRequest) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(apiInfoService.updateApiInfo(request, servletRequest));
    }

    @Operation(summary = "分页查询API", description = "分页获取API列表")
    @PostMapping("/page")
    public ApiResponse<Page<ApiInfoVO>> getApiPage(@RequestBody ApiInfoQueryRequest request) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(apiInfoService.getApiPage(request));
    }

    @Operation(summary = "删除API", description = "删除指定API")
    @DeleteMapping("delete")
    public ApiResponse<Boolean> deleteApiInfo(@Valid @RequestBody ApiInfoDeleteRequest request) {
        ThrowUtils.throwIf(request == null, "请求体为空");
//        return ApiResponse.success(apiInfoService.deleApiInfo(request));
        return null;
    }

    @Operation(summary = "获取待审核API", description = "管理员获取待审核的API列表")
    @PostMapping("/page/releasing")
    @AuthorCheck(mustRole = UserConstant.ADMIN)
    public ApiResponse<Page<ApiInfoVO>> getReleasingApiPage(@RequestBody ApiInfoQueryRequest request) {
        return ApiResponse.success(apiInfoService.getReleasingApiPage(request));
    }

    @Operation(summary = "审核通过API", description = "管理员审核通过API")
    @PostMapping("/approve")
    @AuthorCheck(mustRole = UserConstant.ADMIN)
    public ApiResponse<Boolean> approveApiInfo(@Valid @RequestBody ApiInfoReviewRequest request, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(apiInfoService.approveApiInfo(request, httpServletRequest));
    }

    @Operation(summary = "审核拒绝API", description = "管理员审核拒绝API")
    @PostMapping("/reject")
    @AuthorCheck(mustRole = UserConstant.ADMIN)
    public ApiResponse<Boolean> rejectApiInfo(@Valid @RequestBody ApiInfoReviewRequest request, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(apiInfoService.rejectApiInfo(request, httpServletRequest));
    }

    @Operation(summary = "修改API上下线状态", description = "修改API的上线/下线状态")
    @PostMapping("/line")
    public ApiResponse<Boolean> changeApiLineStatus(@Valid @RequestBody ApiInfoLineRequest apiInfoLineRequest, HttpServletRequest request) {
        return ApiResponse.success(apiInfoService.changeApiLineStatus(apiInfoLineRequest, request));
    }

    @Operation(summary = "获取我的API", description = "分页获取当前用户创建的API列表")
    @PostMapping("/page/my")
    public ApiResponse<Page<ApiInfoVO>> getMyApiPage(@RequestBody ApiInfoQueryRequest request, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(apiInfoService.getMyApiPage(request, httpServletRequest));
    }

    @Operation(summary = "获取API详情", description = "根据ID获取API详细信息")
    @GetMapping("/detail/{apiId}")
    public ApiResponse<ApiInfoDetailVO> getDetailedApiInfo(@PathVariable Long apiId) {
        ThrowUtils.throwIf(apiId <= 0, StatusCode.PARAMS_ERROR);
        return ApiResponse.success(apiInfoService.getDetailedApiInfo(apiId));
    }

    @Operation(summary = "申请API", description = "申请使用指定API")
    @GetMapping("/apply/{apiId}")
    public ApiResponse<Boolean> applyApiInfo(@PathVariable Long apiId, HttpServletRequest request) {
        ThrowUtils.throwIf(apiId <= 0, StatusCode.PARAMS_ERROR);
        return ApiResponse.success(apiInfoService.applyApiInfo(apiId, request));
    }

    @Operation(summary = "通过API申请", description = "API拥有者通过用户的API使用申请")
    @PostMapping("/apply/approve")
    @CheckApiOwner
    public ApiResponse<Boolean> approveApiApply(@Valid @RequestBody ApiApplyRequest request) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(apiInfoService.approveApiApply(request));
    }

    @Operation(summary = "拒绝API申请", description = "API拥有者拒绝用户的API使用申请")
    @PostMapping("/apply/reject")
    @CheckApiOwner
    public ApiResponse<Boolean> rejectApiApply(@Valid @RequestBody ApiApplyRequest request) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(apiInfoService.rejectApiApply(request));
    }

    @Operation(summary = "获取我的申请", description = "获取当前用户发起的API申请列表")
    @PostMapping("/page/my/apply")
    public ApiResponse<Page<ApiApplyVO>> getMyApiApply(@RequestBody ApplyApiQueryRequest applyApiQueryRequest, HttpServletRequest request) {
        return ApiResponse.success(apiInfoService.getMyApiApply(applyApiQueryRequest, request));
    }

    @Operation(summary = "获取收到的申请", description = "获取当前用户收到的API申请列表")
    @PostMapping("/page/my/receive")
    public ApiResponse<Page<ApiApplyVO>> getMyReceivedApiApply(@RequestBody ApplyApiQueryRequest applyApiQueryRequest, HttpServletRequest request) {
        return ApiResponse.success(apiInfoService.getMyReceivedApiApply(applyApiQueryRequest, request));
    }

    /**
     * 通用 API 调用入口，支持任意 HTTP 方法（GET/POST/PUT/PATCH/DELETE）。
     *
     * <p>参数传递规则：
     * <ul>
     *   <li>GET / DELETE：参数写在 URL QueryString，此处 body 为空，框架会从 queryString 自动解析到 params。</li>
     *   <li>POST / PUT / PATCH：参数写在请求体（JSON Object），框架解析后传入 params。</li>
     * </ul>
     *
     * <p>如果请求体为空或不是合法 JSON Object，则 params 默认为空 Map，不会报错。
     */
    @Operation(summary = "调用API", description = "通用API调用入口，支持GET/POST/PUT/PATCH/DELETE方法")
    @RequestMapping("/invoke/{apiId}")
    public ApiResponse<Object> invokeApi(@PathVariable Long apiId,
                                         HttpServletRequest request) throws IOException {
        ThrowUtils.throwIf(apiId <= 0, StatusCode.PARAMS_ERROR);

        // 1. 先取 QueryString 参数（GET/DELETE 场景）
        Map<String, Object> params = new HashMap<>();
        Map<String, String[]> queryParams = request.getParameterMap();
        if (queryParams != null) {
            queryParams.forEach((k, v) -> params.put(k, v.length == 1 ? v[0] : v));
        }

        // 2. 若请求体非空，尝试解析为 JSON Object，合并进 params（POST/PUT/PATCH 场景）
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            String bodyStr = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            if (bodyStr != null && !bodyStr.isBlank()) {
                try {
                    Map<String, Object> bodyMap = JSONUtil.toBean(bodyStr, Map.class);
                    params.putAll(bodyMap);
                } catch (Exception ignored) {
                    // 请求体不是 JSON Object，忽略
                }
            }
        }

        return ApiResponse.success(apiInfoService.invokeApi(apiId, params, request));
    }
}
