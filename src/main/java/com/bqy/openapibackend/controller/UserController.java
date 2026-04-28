package com.bqy.openapibackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.annotation.AuthorCheck;
import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.common.UserConstant;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.model.request.user.*;
import com.bqy.openapibackend.model.vo.ApiKeysVO;
import com.bqy.openapibackend.model.vo.LoginUserVO;
import com.bqy.openapibackend.model.vo.RegisterResultVO;
import com.bqy.openapibackend.model.vo.UserVO;
import com.bqy.openapibackend.service.IUserService;
import com.bqy.openapibackend.util.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户 前端控制器
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private IUserService userService;

    @Operation(summary = "用户注册", description = "注册新用户账号，返回包含 AccessKey 和 SecretKey 的注册结果")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "0", description = "注册成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "40001", description = "账号已存在"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "40000", description = "密码不一致或其他参数错误")
    })
    @PostMapping("/register")
    public ApiResponse<RegisterResultVO> register(@Valid @RequestBody UserRegisterRequest request) {
        ThrowUtils.throwIf(request == null, "请求体不能为空");
        RegisterResultVO result = userService.register(request.getUserAccount(), request.getUserPassWord(), request.getCheckPassWord(), request.getUserName(), request.getPhoneNumber());
        return ApiResponse.success(result);
    }

    @Operation(summary = "用户登录", description = "用户账号密码登录")
    @PostMapping("/login")
    public ApiResponse<LoginUserVO> login(@Valid @RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, "请求体不能为空");
        return ApiResponse.success(userService.login(userLoginRequest.getUserAccount(), userLoginRequest.getUserPassword(), request));
    }

    @Operation(summary = "获取当前登录用户", description = "获取当前登录用户信息")
    @GetMapping("/get/login")
    public ApiResponse<User> getLoginUser(HttpServletRequest request) {
        return ApiResponse.success(userService.getLoginUser(request));
    }

    @Operation(summary = "用户登出", description = "退出登录")
    @GetMapping("/logout")
    public ApiResponse<Boolean> logout(HttpServletRequest request) {
        return ApiResponse.success(userService.logout(request));
    }

    @Operation(summary = "获取用户列表", description = "管理员获取用户列表")
    @PostMapping("/list")
    @AuthorCheck(mustRole = UserConstant.ADMIN)
    public ApiResponse<List<UserVO>> getUserList(@Valid @RequestBody UserQueryRequest request) {
        return ApiResponse.success(userService.getUserList(request));
    }

    @Operation(summary = "删除用户", description = "管理员删除用户")
    @DeleteMapping("/delete")
    @AuthorCheck(mustRole = UserConstant.ADMIN)
    public ApiResponse<Boolean> deleteUser(@Valid @RequestBody UserDeleteRequest request) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(userService.deleteUser(request.getId()));
    }

    @Operation(summary = "分页获取用户", description = "管理员分页查询用户列表")
    @PostMapping("/list/page")
    @AuthorCheck(mustRole = UserConstant.ADMIN)
    public ApiResponse<Page<UserVO>> getUserPage(@Valid @RequestBody UserQueryRequest request) {
        return ApiResponse.success(userService.getUserPage(request));
    }

    @Operation(summary = "更新用户信息", description = "更新用户个人信息")
    @PutMapping("update")
    public ApiResponse<Boolean> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        ThrowUtils.throwIf(request == null, "请求体为空");
        return ApiResponse.success(userService.updateUser(request));
    }

    @Operation(summary = "获取 API 密钥", description = "获取当前登录用户的 AccessKey 和 SecretKey（SecretKey 部分显示）")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "0", description = "获取成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "40100", description = "未登录")
    })
    @GetMapping("/api-keys")
    public ApiResponse<ApiKeysVO> getApiKeys(HttpServletRequest request) {
        return ApiResponse.success(userService.getApiKeys(request));
    }

    @Operation(summary = "重新生成 API 密钥", description = "为当前登录用户重新生成 AccessKey 和 SecretKey，旧的密钥将立即失效")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "0", description = "生成成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "40100", description = "未登录"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "50000", description = "生成失败")
    })
    @PostMapping("/regenerate-api-keys")
    public ApiResponse<RegisterResultVO> regenerateApiKeys(HttpServletRequest request) {
        return ApiResponse.success(userService.regenerateApiKeys(request));
    }

}
