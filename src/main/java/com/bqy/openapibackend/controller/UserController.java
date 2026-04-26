package com.bqy.openapibackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.annotation.AuthorCheck;
import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.common.UserConstant;
import com.bqy.openapibackend.model.request.user.*;
import com.bqy.openapibackend.model.vo.LoginUserVO;
import com.bqy.openapibackend.model.vo.UserVO;
import com.bqy.openapibackend.service.IUserService;
import com.bqy.openapibackend.util.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "用户注册", description = "注册新用户账号")
    @PostMapping("/register")
    public ApiResponse<Long> register(@Valid @RequestBody UserRegisterRequest request) {
        ThrowUtils.throwIf(request == null, "请求体不能为空");
        long result = userService.register(request.getUserAccount(), request.getUserPassWord(), request.getCheckPassWord(), request.getUserName(), request.getPhoneNumber());
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
    public ApiResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
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


}
