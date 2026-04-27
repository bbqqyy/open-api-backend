package com.bqy.openapibackend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.annotation.AuthorCheck;
import com.bqy.openapibackend.annotation.CheckApiOwner;
import com.bqy.openapibackend.annotation.RequireApiKeyAuth;
import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.common.UserConstant;
import com.bqy.openapibackend.model.request.api.*;
import com.bqy.openapibackend.model.vo.ApiApplyVO;
import com.bqy.openapibackend.model.vo.ApiInfoDetailVO;
import com.bqy.openapibackend.model.vo.ApiInfoVO;
import com.bqy.openapibackend.service.IApiInfoService;
import com.bqy.openapibackend.util.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    @Operation(
        summary = "添加API",
        description = "创建新的API接口。用户可以上传新的API配置信息，系统会将其标记为待审核状态。" +
                     "API 创建者需要配置 API 的名称、描述、URL、请求方法、分类等基本信息。",
        tags = {"API管理"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "0", description = "API 创建成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
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

    @Operation(
        summary = "删除API",
        description = "删除指定的API接口及其所有关联信息，包括权限记录、调用日志、参数定义、限流规则等。" +
                     "仅 API 创建者或管理员可以删除 API。删除操作不可撤销，请谨慎操作。",
        tags = {"API管理"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "0", description = "API 删除成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "请求参数错误或API不存在"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权删除该API"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @DeleteMapping("/delete")
    public ApiResponse<Boolean> deleteApiInfo(@Valid @RequestBody ApiInfoDeleteRequest request, HttpServletRequest servletRequest) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(apiInfoService.deleteApiInfo(request, servletRequest));
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

    @Operation(
        summary = "获取API详情",
        description = "根据 API ID 获取 API 的完整详细信息，包括基本信息、请求参数、响应参数、限流配置等。",
        tags = {"API管理"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "0", description = "获取成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "API ID 无效"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API 不存在"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/detail/{apiId}")
    @Parameter(name = "apiId", description = "API 的唯一标识符", example = "123", required = true, in = ParameterIn.PATH)
    public ApiResponse<ApiInfoDetailVO> getDetailedApiInfo(@PathVariable Long apiId) {
        ThrowUtils.throwIf(apiId <= 0, StatusCode.PARAMS_ERROR);
        return ApiResponse.success(apiInfoService.getDetailedApiInfo(apiId));
    }

    @Operation(
        summary = "申请API",
        description = "当前用户申请使用指定的 API。申请后，API 的创建者需要审批该申请，审批通过后才能调用该 API。",
        tags = {"API申请"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "0", description = "申请成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "API ID 无效"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API 不存在"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @GetMapping("/apply/{apiId}")
    @Parameter(name = "apiId", description = "要申请的 API 的唯一标识符", example = "123", required = true, in = ParameterIn.PATH)
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
    @Operation(
        summary = "调用API",
        description = "通用API调用入口，支持GET/POST/PUT/PATCH/DELETE等多种HTTP方法。" +
                     "用户需要先获得该API的使用权限。系统会自动进行权限检查、限流控制、请求转发和响应返回。" +
                     "调用详情会被记录在API调用日志中。",
        tags = {"API调用"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "0", description = "API 调用成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "API ID 无效"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权调用或超过限流限制"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API 不存在或不在线"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "API 调用失败或服务器错误")
    })
    @RequestMapping("/invoke/{apiId}")
    @Parameter(name = "apiId", description = "要调用的 API 的唯一标识符", example = "123", required = true, in = ParameterIn.PATH)
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

    @RequestMapping("/invoke-with-key/{apiId}")
    @RequireApiKeyAuth
    @Operation(
        summary = "通过 API Key 调用 API",
        description = "使用 AccessKey + 签名 认证的 API 调用入口，适用于跨系统调用场景。" +
                     "客户端需要在请求头中提供: X-Access-Key, X-Signature, X-Timestamp, X-Nonce。" +
                     "签名使用 HMAC-SHA256 算法，密钥为用户的 SecretKey。",
        tags = {"API调用"}
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "0", description = "API 调用成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "认证失败（密钥无效、签名错误等）"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权调用或超过限流限制"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API 不存在或不在线"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "API 调用失败或服务器错误")
    })
    @Parameter(name = "apiId", description = "要调用的 API 的唯一标识符", example = "123", required = true, in = ParameterIn.PATH)
    public ApiResponse<Object> invokeApiWithKey(@PathVariable Long apiId,
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
