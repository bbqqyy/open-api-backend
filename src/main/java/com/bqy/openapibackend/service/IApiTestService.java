package com.bqy.openapibackend.service;

import com.bqy.openapibackend.model.request.api.ApiTestRequest;
import com.bqy.openapibackend.model.vo.ApiTestVO;

import java.util.List;
import java.util.Map;

/**
 * API 测试服务接口
 */
public interface IApiTestService {

    /**
     * 执行单次 API 测试
     *
     * @param request 测试请求
     * @return 测试响应
     */
    ApiTestVO executeTest(ApiTestRequest request);

    /**
     * 执行多个测试用例
     *
     * @param requests 测试请求列表
     * @return 测试报告列表
     */
    List<ApiTestVO.TestReport> executeBatchTests(List<ApiTestRequest> requests);

    /**
     * 生成智能测试数据
     *
     * @param apiId API ID
     * @return 生成的测试数据
     */
    Map<String, Object> generateTestData(Long apiId);

    /**
     * 获取测试数据模板
     *
     * @param apiId API ID
     * @return 测试数据模板
     */
    Map<String, Object> getTestDataTemplate(Long apiId);

    /**
     * 执行性能测试
     *
     * @param apiId        API ID
     * @param concurrency  并发数
     * @param duration     测试持续时间（秒）
     * @param testRequests 测试请求列表
     * @return 性能测试报告
     */
    ApiTestVO.TestReport performanceTest(Long apiId, Integer concurrency, Integer duration, List<ApiTestRequest> testRequests);

    /**
     * 执行压力测试
     *
     * @param apiId        API ID
     * @param maxLoad      最大负载（请求数）
     * @param testRequests 测试请求列表
     * @return 压力测试报告
     */
    ApiTestVO.TestReport stressTest(Long apiId, Integer maxLoad, List<ApiTestRequest> testRequests);

    /**
     * 获取测试历史记录
     *
     * @param apiId API ID
     * @param limit 返回条数
     * @return 测试历史列表
     */
    List<ApiTestVO.TestReport> getTestHistory(Long apiId, Integer limit);

    /**
     * 生成测试报告
     *
     * @param testResult 测试结果
     * @return 完整的测试报告
     */
    ApiTestVO.TestReport generateTestReport(ApiTestVO testResult);

    /**
     * 对比测试结果
     *
     * @param testId1 第一次测试 ID
     * @param testId2 第二次测试 ID
     * @return 对比结果
     */
    Map<String, Object> compareTestResults(Long testId1, Long testId2);

    /**
     * 获取测试建议
     *
     * @param testReport 测试报告
     * @return 优化建议列表
     */
    List<String> getTestSuggestions(ApiTestVO.TestReport testReport);

    /**
     * 校验 API 响应格式
     *
     * @param response 响应对象
     * @param apiId    API ID
     * @return 是否符合格式
     */
    Boolean validateResponseFormat(Object response, Long apiId);

    /**
     * 执行自动化测试套件
     *
     * @param apiId API ID
     * @return 测试报告
     */
    ApiTestVO.TestReport runAutomatedTestSuite(Long apiId);
}

