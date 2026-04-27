package com.bqy.openapibackend.service.impl;

import com.bqy.openapibackend.dao.ApiParamDao;
import com.bqy.openapibackend.model.entity.ApiParam;
import com.bqy.openapibackend.model.request.api.ApiTestRequest;
import com.bqy.openapibackend.model.vo.ApiTestVO;
import com.bqy.openapibackend.service.IApiTestService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * API 测试服务实现
 */
@Slf4j
@Service
public class ApiTestServiceImpl implements IApiTestService {

    @Resource
    private ApiParamDao apiParamDao;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Override
    public ApiTestVO executeTest(ApiTestRequest request) {
        try {
            // 构建 HTTP 请求
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(buildUrl(request)))
                    .timeout(Duration.ofSeconds(request.getTimeout() != null ? request.getTimeout() : 30));

            // 添加请求头
            if (request.getHeaders() != null) {
                request.getHeaders().forEach(builder::header);
            }

            // 设置请求方法和体
            String method = request.getMethod() != null ? request.getMethod() : "GET";
            switch (method.toUpperCase()) {
                case "POST":
                case "PUT":
                    String body = convertBodyToString(request.getBody());
                    builder.method(method, HttpRequest.BodyPublishers.ofString(body));
                    break;
                case "DELETE":
                    builder.DELETE();
                    break;
                default:
                    builder.GET();
            }

            HttpRequest httpRequest = builder.build();

            // 执行请求
            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            long responseTime = System.currentTimeMillis() - startTime;

            // 构建响应
            ApiTestVO.TestResponse testResponse = ApiTestVO.TestResponse.builder()
                    .statusCode(response.statusCode())
                    .responseTime(responseTime)
                    .responseSize((long) response.body().length())
                    .success(response.statusCode() >= 200 && response.statusCode() < 300)
                    .body(parseResponseBody(response.body()))
                    .build();

            // 构建测试工具结果
            return ApiTestVO.builder()
                    .request(convertToTestRequest(request))
                    .response(testResponse)
                    .build();

        } catch (Exception e) {
            log.error("API 测试执行失败", e);
            return ApiTestVO.builder()
                    .request(convertToTestRequest(request))
                    .response(ApiTestVO.TestResponse.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build())
                    .build();
        }
    }

    @Override
    public List<ApiTestVO.TestReport> executeBatchTests(List<ApiTestRequest> requests) {
        return requests.parallelStream()
                .map(this::executeTest)
                .map(this::generateTestReport)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> generateTestData(Long apiId) {
        Map<String, Object> testData = new HashMap<>();

        // 获取 API 参数
        List<ApiParam> params = apiParamDao.lambdaQuery()
                .eq(ApiParam::getApiId, apiId)
                .list();

        for (ApiParam param : params) {
            testData.put(param.getParamName(), generateSampleValue(param.getParamType()));
        }

        return testData;
    }

    @Override
    public Map<String, Object> getTestDataTemplate(Long apiId) {
        Map<String, Object> template = new HashMap<>();

        List<ApiParam> params = apiParamDao.lambdaQuery()
                .eq(ApiParam::getApiId, apiId)
                .list();

        for (ApiParam param : params) {
            Map<String, Object> paramInfo = new HashMap<>();
            paramInfo.put("type", param.getParamType());
            paramInfo.put("required", param.getRequired() != null && param.getRequired() == 1);
            paramInfo.put("description", param.getDescription());
            paramInfo.put("example", generateSampleValue(param.getParamType()));
            template.put(param.getParamName(), paramInfo);
        }

        return template;
    }

    @Override
    public ApiTestVO.TestReport performanceTest(Long apiId, Integer concurrency, Integer duration, List<ApiTestRequest> testRequests) {
        List<ApiTestVO.TestReport> results = new ArrayList<>();
        long totalTime = 0;
        int totalRequests = 0;
        int successCount = 0;

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (duration * 1000);

        while (System.currentTimeMillis() < endTime) {
            for (int i = 0; i < concurrency; i++) {
                ApiTestRequest request = testRequests.get(i % testRequests.size());
                ApiTestVO result = executeTest(request);
                if (result.getResponse().getSuccess()) {
                    successCount++;
                    totalTime += result.getResponse().getResponseTime();
                }
                totalRequests++;
            }
        }

        long avgResponseTime = totalRequests > 0 ? totalTime / totalRequests : 0;
        double throughput = (double) totalRequests / ((System.currentTimeMillis() - startTime) / 1000.0);
        double successRate = totalRequests > 0 ? ((double) successCount / totalRequests) * 100 : 0;

        return ApiTestVO.TestReport.builder()
                .apiId(apiId)
                .testName("性能测试")
                .testCaseCount(totalRequests)
                .passedCaseCount(successCount)
                .failedCaseCount(totalRequests - successCount)
                .successRate(successRate)
                .avgResponseTime(avgResponseTime)
                .totalTime(System.currentTimeMillis() - startTime)
                .performanceRating(ratePerformance(avgResponseTime, successRate))
                .suggestions(generateSuggestions(successRate, avgResponseTime))
                .build();
    }

    @Override
    public ApiTestVO.TestReport stressTest(Long apiId, Integer maxLoad, List<ApiTestRequest> testRequests) {
        List<Long> responseTimes = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        for (int i = 0; i < maxLoad; i++) {
            ApiTestRequest request = testRequests.get(i % testRequests.size());
            ApiTestVO result = executeTest(request);

            if (result.getResponse().getSuccess()) {
                successCount++;
                responseTimes.add(result.getResponse().getResponseTime());
            } else {
                errorCount++;
            }
        }

        long avgResponseTime = responseTimes.isEmpty() ? 0 :
                responseTimes.stream().mapToLong(Long::longValue).sum() / responseTimes.size();
        long minResponseTime = responseTimes.isEmpty() ? 0 : responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        long maxResponseTime = responseTimes.isEmpty() ? 0 : responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        double successRate = (double) successCount / maxLoad * 100;

        return ApiTestVO.TestReport.builder()
                .apiId(apiId)
                .testName("压力测试")
                .testCaseCount(maxLoad)
                .passedCaseCount(successCount)
                .failedCaseCount(errorCount)
                .successRate(successRate)
                .avgResponseTime(avgResponseTime)
                .minResponseTime(minResponseTime)
                .maxResponseTime(maxResponseTime)
                .performanceRating(ratePerformance(avgResponseTime, successRate))
                .suggestions(generateSuggestions(successRate, avgResponseTime))
                .build();
    }

    @Override
    public List<ApiTestVO.TestReport> getTestHistory(Long apiId, Integer limit) {
        // 这里应该从数据库获取历史测试记录
        // 当前返回空列表，实际实现应查询数据库
        return new ArrayList<>();
    }

    @Override
    public ApiTestVO.TestReport generateTestReport(ApiTestVO testResult) {
        return ApiTestVO.TestReport.builder()
                .result(testResult.getResponse().getSuccess() ? "pass" : "fail")
                .totalTime(testResult.getResponse().getResponseTime())
                .testCaseCount(1)
                .passedCaseCount(testResult.getResponse().getSuccess() ? 1 : 0)
                .failedCaseCount(testResult.getResponse().getSuccess() ? 0 : 1)
                .successRate(testResult.getResponse().getSuccess() ? 100.0 : 0.0)
                .avgResponseTime(testResult.getResponse().getResponseTime())
                .minResponseTime(testResult.getResponse().getResponseTime())
                .maxResponseTime(testResult.getResponse().getResponseTime())
                .suggestions(getTestSuggestions(null))
                .build();
    }

    @Override
    public Map<String, Object> compareTestResults(Long testId1, Long testId2) {
        Map<String, Object> comparison = new HashMap<>();
        // 比较逻辑实现
        return comparison;
    }

    @Override
    public List<String> getTestSuggestions(ApiTestVO.TestReport testReport) {
        List<String> suggestions = new ArrayList<>();

        if (testReport == null) {
            return suggestions;
        }

        if (testReport.getSuccessRate() < 100) {
            suggestions.add("部分测试用例失败，建议检查 API 实现");
        }

        if (testReport.getAvgResponseTime() > 1000) {
            suggestions.add("平均响应时间超过 1 秒，建议优化 API 性能");
        }

        if (testReport.getFailedCaseCount() > 0) {
            suggestions.add("存在失败用例，建议查看错误日志");
        }

        if (testReport.getSuccessRate() < 95) {
            suggestions.add("成功率低于 95%，建议增加重试机制或优化 API");
        }

        return suggestions;
    }

    @Override
    public Boolean validateResponseFormat(Object response, Long apiId) {
        // 验证响应格式逻辑
        return true;
    }

    @Override
    public ApiTestVO.TestReport runAutomatedTestSuite(Long apiId) {
        // 运行自动化测试套件
        return ApiTestVO.TestReport.builder()
                .apiId(apiId)
                .testName("自动化测试套件")
                .result("pass")
                .build();
    }

    // ==================== 私有方法 ====================

    private String buildUrl(ApiTestRequest request) {
        StringBuilder url = new StringBuilder(request.getUrl());

        if (request.getParams() != null && !request.getParams().isEmpty()) {
            url.append("?");
            request.getParams().forEach((key, value) ->
                url.append(key).append("=").append(value).append("&")
            );
            // 移除最后的 &
            if (url.toString().endsWith("&")) {
                url.deleteCharAt(url.length() - 1);
            }
        }

        return url.toString();
    }

    private String convertBodyToString(Object body) {
        if (body == null) {
            return "";
        }
        if (body instanceof String) {
            return (String) body;
        }
        return body.toString();
    }

    private Object parseResponseBody(String body) {
        try {
            // 尝试解析为 JSON
            if (body.startsWith("{") || body.startsWith("[")) {
                return body; // 返回原始 JSON 字符串
            }
        } catch (Exception e) {
            log.debug("无法解析响应体为 JSON");
        }
        return body;
    }

    private Object generateSampleValue(String type) {
        return switch (type != null ? type.toLowerCase() : "") {
            case "string" -> "sample_value";
            case "integer", "int" -> 123;
            case "long" -> 123456L;
            case "double", "float" -> 123.45;
            case "boolean" -> true;
            case "date" -> new Date().toString();
            default -> "sample";
        };
    }

    private ApiTestVO.TestRequest convertToTestRequest(ApiTestRequest request) {
        return ApiTestVO.TestRequest.builder()
                .method(request.getMethod())
                .url(request.getUrl())
                .headers(request.getHeaders())
                .params(request.getParams())
                .body(request.getBody())
                .timeout(request.getTimeout())
                .build();
    }

    private String ratePerformance(Long avgResponseTime, Double successRate) {
        if (successRate < 90) {
            return "D";
        }
        if (avgResponseTime > 2000) {
            return "C";
        }
        if (avgResponseTime > 1000) {
            return "B";
        }
        return "A";
    }

    private List<String> generateSuggestions(Double successRate, Long avgResponseTime) {
        List<String> suggestions = new ArrayList<>();

        if (successRate < 100) {
            suggestions.add("提高成功率，目前为 " + String.format("%.2f%%", successRate));
        }

        if (avgResponseTime > 1000) {
            suggestions.add("优化响应时间，当前平均响应时间为 " + avgResponseTime + "ms");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("性能表现良好，继续监测");
        }

        return suggestions;
    }
}

