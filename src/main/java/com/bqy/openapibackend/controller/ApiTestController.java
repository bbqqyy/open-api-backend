package com.bqy.openapibackend.controller;

import com.bqy.openapibackend.common.ApiResponse;
import com.bqy.openapibackend.model.request.api.ApiTestRequest;
import com.bqy.openapibackend.model.vo.ApiTestVO;
import com.bqy.openapibackend.service.IApiTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API 测试工具控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@Tag(name = "API测试工具", description = "API 测试执行和报告生成")
public class ApiTestController {

    @Resource
    private IApiTestService apiTestService;

    @Operation(summary = "执行单次测试", description = "执行单个 API 的测试")
    @PostMapping("/execute")
    public ApiResponse<ApiTestVO> executeTest(@RequestBody ApiTestRequest request) {
        return ApiResponse.success(apiTestService.executeTest(request));
    }

    @Operation(summary = "批量执行测试", description = "批量执行多个 API 的测试")
    @PostMapping("/batch")
    public ApiResponse<List<ApiTestVO.TestReport>> executeBatchTests(
            @RequestBody List<ApiTestRequest> requests) {
        return ApiResponse.success(apiTestService.executeBatchTests(requests));
    }

    @Operation(summary = "生成测试数据", description = "为 API 自动生成测试数据")
    @GetMapping("/data/{apiId}")
    public ApiResponse<Map<String, Object>> generateTestData(@PathVariable Long apiId) {
        return ApiResponse.success(apiTestService.generateTestData(apiId));
    }

    @Operation(summary = "获取测试数据模板", description = "获取 API 的测试数据模板")
    @GetMapping("/template/{apiId}")
    public ApiResponse<Map<String, Object>> getTestDataTemplate(@PathVariable Long apiId) {
        return ApiResponse.success(apiTestService.getTestDataTemplate(apiId));
    }

    @Operation(summary = "性能测试", description = "执行 API 的性能测试")
    @PostMapping("/performance/{apiId}")
    public ApiResponse<ApiTestVO.TestReport> performanceTest(
            @PathVariable Long apiId,
            @RequestParam(defaultValue = "10") Integer concurrency,
            @RequestParam(defaultValue = "60") Integer duration,
            @RequestBody List<ApiTestRequest> testRequests) {
        return ApiResponse.success(apiTestService.performanceTest(apiId, concurrency, duration, testRequests));
    }

    @Operation(summary = "压力测试", description = "执行 API 的压力测试")
    @PostMapping("/stress/{apiId}")
    public ApiResponse<ApiTestVO.TestReport> stressTest(
            @PathVariable Long apiId,
            @RequestParam(defaultValue = "1000") Integer maxLoad,
            @RequestBody List<ApiTestRequest> testRequests) {
        return ApiResponse.success(apiTestService.stressTest(apiId, maxLoad, testRequests));
    }

    @Operation(summary = "获取测试历史", description = "获取 API 的测试历史记录")
    @GetMapping("/history/{apiId}")
    public ApiResponse<List<ApiTestVO.TestReport>> getTestHistory(
            @PathVariable Long apiId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return ApiResponse.success(apiTestService.getTestHistory(apiId, limit));
    }

    @Operation(summary = "对比测试结果", description = "对比两次测试的结果")
    @GetMapping("/compare")
    public ApiResponse<Map<String, Object>> compareTestResults(
            @RequestParam Long testId1,
            @RequestParam Long testId2) {
        return ApiResponse.success(apiTestService.compareTestResults(testId1, testId2));
    }

    @Operation(summary = "运行自动化测试套件", description = "运行 API 的自动化测试套件")
    @PostMapping("/suite/{apiId}")
    public ApiResponse<ApiTestVO.TestReport> runAutomatedTestSuite(@PathVariable Long apiId) {
        return ApiResponse.success(apiTestService.runAutomatedTestSuite(apiId));
    }

    @Operation(summary = "验证响应格式", description = "验证 API 响应格式是否符合定义")
    @PostMapping("/validate/{apiId}")
    public ApiResponse<Boolean> validateResponseFormat(
            @PathVariable Long apiId,
            @RequestBody Object response) {
        return ApiResponse.success(apiTestService.validateResponseFormat(response, apiId));
    }
}

