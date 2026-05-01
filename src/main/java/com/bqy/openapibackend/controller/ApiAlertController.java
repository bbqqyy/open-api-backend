package com.bqy.openapibackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.model.vo.ApiAlertVO;
import com.bqy.openapibackend.service.IApiAlertService;
import com.bqy.openapibackend.util.LoginUserUtils;
import com.bqy.openapibackend.util.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API 告警控制器
 * 提供告警查询、确认、忽略等接口
 */
@Slf4j
@Tag(name = "API 告警", description = "API 运行状态告警查询与处理接口")
@RestController
@RequestMapping("/api/alerts")
public class ApiAlertController {

    @Resource
    private IApiAlertService apiAlertService;

    @Resource
    private LoginUserUtils loginUserUtils;

    @GetMapping("/my")
    @Operation(
            summary = "获取我的告警列表",
            description = "分页查询当前登录用户所拥有的 API 告警记录。" +
                    "可通过 status 参数过滤告警状态：ACTIVE(待处理) / ACKNOWLEDGED(已确认) / IGNORED(已忽略)，" +
                    "不传 status 则返回全部。"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录")
    })
    @Parameters({
            @Parameter(name = "status", description = "告警状态过滤：ACTIVE / ACKNOWLEDGED / IGNORED，不传则查全部", in = ParameterIn.QUERY),
            @Parameter(name = "pageNum", description = "页码，默认 1", example = "1", in = ParameterIn.QUERY),
            @Parameter(name = "pageSize", description = "每页数量，默认 10", example = "10", in = ParameterIn.QUERY)
    })
    public ApiResponse<Page<ApiAlertVO>> getMyAlerts(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {

        User loginUser = loginUserUtils.getLoginUser(request);
        ThrowUtils.throwIf(pageNum <= 0 || pageSize <= 0 || pageSize > 100, StatusCode.PARAMS_ERROR);

        Page<ApiAlertVO> result = apiAlertService.getMyAlerts(loginUser.getId(), status, pageNum, pageSize);
        return ApiResponse.success(result);
    }

    @GetMapping("/count/active")
    @Operation(
            summary = "获取未处理告警数量",
            description = "获取当前登录用户名下所有处于 ACTIVE（待处理）状态的告警数量，用于前端角标展示。"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录")
    })
    public ApiResponse<Long> countActiveAlerts(HttpServletRequest request) {
        User loginUser = loginUserUtils.getLoginUser(request);
        long count = apiAlertService.countActiveAlerts(loginUser.getId());
        return ApiResponse.success(count);
    }

    @GetMapping("/api/{apiId}")
    @Operation(
            summary = "查询指定 API 的活跃告警",
            description = "查询指定 API 当前所有 ACTIVE（待处理）的告警记录，用于 API 详情页展示告警状态。"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录")
    })
    @Parameter(name = "apiId", description = "API ID", required = true, in = ParameterIn.PATH)
    public ApiResponse<List<ApiAlertVO>> getAlertsByApiId(
            @PathVariable Long apiId,
            HttpServletRequest request) {

        // 需要登录才能查看（安全性保证）
        loginUserUtils.getLoginUser(request);
        ThrowUtils.throwIf(apiId <= 0, StatusCode.PARAMS_ERROR);

        List<ApiAlertVO> alerts = apiAlertService.getActiveAlertsByApiId(apiId);
        return ApiResponse.success(alerts);
    }

    @PostMapping("/{alertId}/acknowledge")
    @Operation(
            summary = "确认告警",
            description = "将指定告警的状态标记为 ACKNOWLEDGED（已确认），表示用户已知晓该告警并正在处理。" +
                    "只有告警所有者才能操作，且只有 ACTIVE 状态的告警可以被确认。"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "操作成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误或告警已处理"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权操作该告警"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "告警不存在")
    })
    @Parameter(name = "alertId", description = "告警 ID", required = true, in = ParameterIn.PATH)
    public ApiResponse<Boolean> acknowledgeAlert(
            @PathVariable Long alertId,
            HttpServletRequest request) {

        User loginUser = loginUserUtils.getLoginUser(request);
        ThrowUtils.throwIf(alertId <= 0, StatusCode.PARAMS_ERROR);

        apiAlertService.acknowledgeAlert(alertId, loginUser.getId());
        return ApiResponse.success(true);
    }

    @PostMapping("/{alertId}/ignore")
    @Operation(
            summary = "忽略告警",
            description = "将指定告警的状态标记为 IGNORED（已忽略），表示用户认为该告警无需处理。" +
                    "只有告警所有者才能操作，且只有 ACTIVE 状态的告警可以被忽略。"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "操作成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误或告警已处理"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未登录"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "无权操作该告警"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "告警不存在")
    })
    @Parameter(name = "alertId", description = "告警 ID", required = true, in = ParameterIn.PATH)
    public ApiResponse<Boolean> ignoreAlert(
            @PathVariable Long alertId,
            HttpServletRequest request) {

        User loginUser = loginUserUtils.getLoginUser(request);
        ThrowUtils.throwIf(alertId <= 0, StatusCode.PARAMS_ERROR);

        apiAlertService.ignoreAlert(alertId, loginUser.getId());
        return ApiResponse.success(true);
    }
}

