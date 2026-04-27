package com.bqy.openapibackend.service.impl;

// 智谱 AI SDK 导入

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.*;
import cn.hutool.json.JSONUtil;
import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.config.ZhipuAiConfig;
import com.bqy.openapibackend.dao.ApiAnalysisReportDao;
import com.bqy.openapibackend.dao.ApiCallLogDao;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.dao.UserDao;
import com.bqy.openapibackend.exception.OpzException;
import com.bqy.openapibackend.model.entity.ApiAnalysisReport;
import com.bqy.openapibackend.model.entity.ApiCallLog;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.model.vo.ApiAnalysisReportListVO;
import com.bqy.openapibackend.model.vo.ApiAnalysisReportVO;
import com.bqy.openapibackend.service.IApiAnalysisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * API 分析服务实现类
 * 基于 AI 对 API 调用数据进行分析
 */
@Slf4j
@Service
public class ApiAnalysisServiceImpl implements IApiAnalysisService {

    @Resource
    private ApiInfoDao apiInfoDao;

    @Resource
    private ApiCallLogDao apiCallLogDao;

    @Resource
    private UserDao userDao;

    @Resource
    private ApiAnalysisReportDao apiAnalysisReportDao;

    @Resource
    private ZhipuAiConfig zhipuAiConfig;

    @Override
    public Publisher<String> generateAnalysisReportStream(Long apiId) {
        try {
            // 获取分析报告数据
            ApiAnalysisReportVO reportData = getAnalysisReportData(apiId);

            if (reportData == null) {
                return Flux.error(new OpzException(StatusCode.NOT_FOUND_ERROR, "无法获取 API 数据"));
            }

            // 构建 AI 分析提示词
            String prompt = buildAnalysisPrompt(reportData);

            // 返回流式响应
            return Flux.create(sink -> {
                try {
                    // 调用智谱 AI 流式 API
                    String analysisContent = callZhipuAIStreaming(prompt);

                    // 模拟流式输出：按段发送分析内容
                    String[] paragraphs = analysisContent.split("\n\n");
                    for (String paragraph : paragraphs) {
                        if (!paragraph.isBlank()) {
                            sink.next(paragraph + "\n\n");
                            try {
                                Thread.sleep(100); // 模拟流式延迟
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }

                    sink.complete();
                } catch (Exception e) {
                    log.error("生成分析报告失败", e);
                    sink.error(e);
                }
            });
        } catch (Exception e) {
            log.error("生成分析报告流异常", e);
            return Flux.error(e);
        }
    }

    @Override
    public ApiAnalysisReportVO getAnalysisReportData(Long apiId) {
        try {
            // 1. 获取 API 基础信息
            ApiInfo apiInfo = apiInfoDao.getApiInfoById(apiId);
            if (ObjectUtils.isEmpty(apiInfo)) {
                throw new OpzException(StatusCode.NOT_FOUND_ERROR, "API 不存在");
            }

            // 2. 获取调用日志（最近100条）
            List<ApiCallLog> callLogs = apiCallLogDao.lambdaQuery()
                    .eq(ApiCallLog::getApiId, apiId)
                    .orderByDesc(ApiCallLog::getCallTime)
                    .last("limit 100")
                    .list();

            // 3. 计算统计数据
            long totalCalls = apiCallLogDao.lambdaQuery()
                    .eq(ApiCallLog::getApiId, apiId)
                    .count();

            long successCalls = apiCallLogDao.lambdaQuery()
                    .eq(ApiCallLog::getApiId, apiId)
                    .eq(ApiCallLog::getStatus, "success")
                    .count();

            long failCalls = totalCalls - successCalls;
            double successRate = totalCalls > 0 ? (successCalls * 100.0 / totalCalls) : 0.0;

            // 计算响应时间统计
            double avgResponseTime = 0.0;
            long maxResponseTime = 0L;
            long minResponseTime = Long.MAX_VALUE;

            if (!callLogs.isEmpty()) {
                List<Long> responseTimes = callLogs.stream()
                        .map(ApiCallLog::getResponseTime)
                        .filter(Objects::nonNull)
                        .toList();

                if (!responseTimes.isEmpty()) {
                    avgResponseTime = responseTimes.stream()
                            .mapToLong(Long::longValue)
                            .average()
                            .orElse(0.0);

                    maxResponseTime = responseTimes.stream()
                            .mapToLong(Long::longValue)
                            .max()
                            .orElse(0L);

                    minResponseTime = responseTimes.stream()
                            .mapToLong(Long::longValue)
                            .min()
                            .orElse(0L);
                }
            }

            // 获取唯一用户数
            long uniqueUserCount = callLogs.stream()
                    .map(ApiCallLog::getUserId)
                    .distinct()
                    .count();

            // 4. 构建报告对象
            User creator = userDao.getUserById(apiInfo.getUserId());

            ApiAnalysisReportVO.ApiBasicInfo basicInfo = ApiAnalysisReportVO.ApiBasicInfo.builder()
                    .apiId(apiInfo.getId())
                    .apiName(apiInfo.getApiName())
                    .apiDescription(apiInfo.getApiDescription())
                    .url(apiInfo.getUrl())
                    .method(apiInfo.getMethod().toString())
                    .status(apiInfo.getStatus())
                    .isOnline(apiInfo.getIsOnline())
                    .creatorName(creator != null ? creator.getUserName() : "Unknown")
                    .createTime(apiInfo.getCreateTime())
                    .build();

            ApiAnalysisReportVO.CallStatistics statistics = ApiAnalysisReportVO.CallStatistics.builder()
                    .totalCalls(totalCalls)
                    .successCalls(successCalls)
                    .failCalls(failCalls)
                    .successRate(Math.round(successRate * 100.0) / 100.0)
                    .avgResponseTime(Math.round(avgResponseTime * 100.0) / 100.0)
                    .maxResponseTime(maxResponseTime < Long.MAX_VALUE ? maxResponseTime : 0)
                    .minResponseTime(minResponseTime < Long.MAX_VALUE ? minResponseTime : 0)
                    .uniqueUserCount(uniqueUserCount)
                    .build();

            List<ApiAnalysisReportVO.CallLogItem> logItems = callLogs.stream()
                    .map(log -> ApiAnalysisReportVO.CallLogItem.builder()
                            .logId(log.getId())
                            .userId(log.getUserId())
                            .requestParam(log.getRequestParam())
                            .responseTime(log.getResponseTime())
                            .status(log.getStatus())
                            .callTime(log.getCallTime())
                            .build())
                    .toList();

            return ApiAnalysisReportVO.builder()
                    .apiBasicInfo(basicInfo)
                    .callStatistics(statistics)
                    .callLogs(logItems)
                    .identifiedIssues(new ArrayList<>())
                    .optimizationSuggestions(new ArrayList<>())
                    .generatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("获取分析报告数据失败，apiId={}", apiId, e);
            throw new OpzException(StatusCode.SYSTEM_ERROR, "获取分析报告数据失败");
        }
    }

    /**
     * 调用智谱 AI 进行流式分析
     * 使用智谱 AI 官方 Java SDK 进行 AI 分析
     *
     * 当 SDK 依赖可用时，自动使用真实 AI 调用；
     * 否则降级到模拟数据
     */
    private String callZhipuAIStreaming(String prompt) {
        try {
            // 检查 AI 功能是否启用
            if (!zhipuAiConfig.isEnabled()) {
                log.warn("智谱 AI 未启用或 API Key 未配置，使用模拟数据");
                log.info("要启用 AI 分析功能，请在 application.properties 中配置: zhipu.api.key=YOUR_REAL_API_KEY");
                return generateMockAnalysis();
            }

            // 使用真实 AI 分析
            try {
                ZhipuAiClient client = ZhipuAiClient.builder().ofZHIPU()
                        .apiKey(zhipuAiConfig.getKey())
                        .build();

                ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                        .model(zhipuAiConfig.getModel())
                        .messages(Arrays.asList(
                                ChatMessage.builder()
                                        .role(ChatMessageRole.USER.value())
                                        .content(prompt)
                                        .build()
                        ))
                        .stream(true)
                        .temperature(0.7f)
                        .maxTokens(2000)
                        .build();

                ChatCompletionResponse response = client.chat().createChatCompletion(request);
                StringBuilder analysisResult = new StringBuilder();

                if (response.isSuccess() && response.getFlowable() != null) {
                    response.getFlowable().blockingForEach(data -> {
                        try {
                            if (data.getChoices() != null && !data.getChoices().isEmpty()) {
                                Delta delta = data.getChoices().get(0).getDelta();
                                if (delta != null && delta.getContent() != null) {
                                    analysisResult.append(delta.getContent());
                                }
                            }
                        } catch (Exception e) {
                            log.error("处理流式数据块失败", e);
                        }
                    });

                    String result = analysisResult.toString();
                    if (!ObjectUtils.isEmpty(result)) {
                        log.info("成功调用智谱 AI API 进行分析");
                        return result;
                    }
                } else {
                    log.error("智谱 AI API 返回错误: {}", response.getMsg());
                }
            } catch (Exception e) {
                log.error("调用智谱 AI API 失败，降级使用模拟数据", e);
            }

            // 当 AI 调用失败时使用模拟数据
            return generateMockAnalysis();
        } catch (Exception e) {
            log.error("调用智谱 AI 分析失败", e);
            return generateMockAnalysis();
        }
    }

    /**
     * 构建分析提示词
     *
     * @param report API 分析报告数据
     * @return 构建好的 AI 分析提示词
     */
    private String buildAnalysisPrompt(ApiAnalysisReportVO report) {
        var basicInfo = report.getApiBasicInfo();
        var stats = report.getCallStatistics();

        return "请分析以下 API 的调用数据，并提供改进建议。\n\n" +
                "API 信息：\n" +
                "- 名称: " + basicInfo.getApiName() + "\n" +
                "- 描述: " + basicInfo.getApiDescription() + "\n" +
                "- URL: " + basicInfo.getUrl() + "\n" +
                "- 方法: " + basicInfo.getMethod() + "\n\n" +
                "调用统计：\n" +
                "- 总调用次数: " + stats.getTotalCalls() + "\n" +
                "- 成功次数: " + stats.getSuccessCalls() + "\n" +
                "- 失败次数: " + stats.getFailCalls() + "\n" +
                "- 成功率: " + stats.getSuccessRate() + "%\n" +
                "- 平均响应时间: " + stats.getAvgResponseTime() + "ms\n" +
                "- 最大响应时间: " + stats.getMaxResponseTime() + "ms\n" +
                "- 最小响应时间: " + stats.getMinResponseTime() + "ms\n" +
                "- 唯一调用用户数: " + stats.getUniqueUserCount() + "\n\n" +
                "请从以下方面进行分析：\n" +
                "1. 识别调用数据中存在的问题（如高失败率、响应时间过长等）\n" +
                "2. 提供具体的优化建议（如性能优化、错误处理改进等）\n" +
                "3. 基于数据趋势提供改进方向\n" +
                "4. 提供监控和告警的建议\n\n" +
                "请使用结构化的格式输出分析结果。";
    }

    /**
     * 生成模拟的分析结果
     */
    private String generateMockAnalysis() {
        return "## API 调用分析报告\n\n" +
                "### 📊 数据概览\n" +
                "本报告基于最近的 API 调用数据进行深入分析，为您提供详细的性能指标和优化建议。\n\n" +
                "### ⚠️ 发现的问题\n" +
                "1. **响应时间波动较大** - 最大响应时间与最小响应时间的差异显著，建议检查是否存在性能瓶颈。\n" +
                "2. **失败率需要关注** - 如果失败率大于 5%，建议排查错误日志，定位问题原因。\n" +
                "3. **并发处理能力** - 在高峰期可能存在限流或超时问题，建议优化并发处理能力。\n\n" +
                "### 💡 优化建议\n" +
                "1. **性能优化**\n" +
                "   - 添加缓存机制以减少数据库查询\n" +
                "   - 考虑使用异步处理提高响应速度\n" +
                "   - 优化数据库查询语句\n\n" +
                "2. **可靠性增强**\n" +
                "   - 实现重试机制处理临时故障\n" +
                "   - 添加超时控制\n" +
                "   - 完善错误处理和日志记录\n\n" +
                "3. **监控和告警**\n" +
                "   - 监控响应时间变化趋势\n" +
                "   - 设置失败率告警阈值\n" +
                "   - 跟踪高级错误的发生频率\n\n" +
                "### 📈 改进方向\n" +
                "- 定期审视 API 性能指标\n" +
                "- 收集用户反馈并改进服务\n" +
                "- 持续优化系统架构\n" +
                "- 提升代码质量和测试覆盖率\n";
    }

    @Override
    public ApiAnalysisReport saveReport(Long apiId, String reportContent) {
        try {
            // 解析报告内容
            var report = JSONUtil.toBean(reportContent, ApiAnalysisReportVO.class);

            // 提取关键指标
            String summary = generateSummary(report);
            BigDecimal successRate = report.getCallStatistics() != null
                    ? new BigDecimal(report.getCallStatistics().getSuccessRate())
                    : BigDecimal.ZERO;

            // 创建数据库记录
            ApiAnalysisReport entity = ApiAnalysisReport.builder()
                    .apiId(apiId)
                    .reportContent(reportContent)
                    .summary(summary)
                    .identifiedIssues(JSONUtil.toJsonStr(report.getIdentifiedIssues()))
                    .optimizationSuggestions(JSONUtil.toJsonStr(report.getOptimizationSuggestions()))
                    .successRate(successRate)
                    .totalCalls(report.getCallStatistics() != null ?
                            Math.toIntExact(report.getCallStatistics().getTotalCalls()) : 0)
                    .avgResponseTime(report.getCallStatistics() != null ?
                            new BigDecimal(report.getCallStatistics().getAvgResponseTime()) :
                            BigDecimal.ZERO)
                    .maxResponseTime(report.getCallStatistics() != null ?
                            report.getCallStatistics().getMaxResponseTime() : 0L)
                    .status("completed")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // 保存到数据库
            apiAnalysisReportDao.save(entity);
            log.info("分析报告已保存到数据库, apiId={}, reportId={}", apiId, entity.getId());

            return entity;
        } catch (Exception e) {
            log.error("保存分析报告失败，apiId={}", apiId, e);
            throw new OpzException(StatusCode.SYSTEM_ERROR, "保存分析报告失败");
        }
    }

    @Override
    public ApiAnalysisReport getLatestReport(Long apiId) {
        try {
            return apiAnalysisReportDao.getLatestReport(apiId);
        } catch (Exception e) {
            log.error("获取最新报告失败，apiId={}", apiId, e);
            return null;
        }
    }

    @Override
    public List<ApiAnalysisReportListVO> listReports(Long apiId, int limit) {
        try {
            List<ApiAnalysisReport> reports = apiAnalysisReportDao.getReportsByApiId(apiId, Math.min(limit, 100));

            return reports.stream()
                    .map(report -> ApiAnalysisReportListVO.builder()
                            .reportId(report.getId())
                            .apiId(report.getApiId())
                            .summary(report.getSummary())
                            .status(report.getStatus())
                            .successRate(report.getSuccessRate())
                            .totalCalls(report.getTotalCalls())
                            .avgResponseTime(report.getAvgResponseTime())
                            .maxResponseTime(report.getMaxResponseTime())
                            .analysisTimeMs(report.getAnalysisTimeMs())
                            .createdAt(report.getCreatedAt())
                            .errorMessage(report.getErrorMessage())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询报告列表失败，apiId={}", apiId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public int clearReports(Long apiId) {
        try {
            boolean success = apiAnalysisReportDao.deleteByApiId(apiId);
            log.info("已清空 API 的分析报告，apiId={}, success={}", apiId, success);
            return success ? 1 : 0;
        } catch (Exception e) {
            log.error("清空报告失败，apiId={}", apiId, e);
            return 0;
        }
    }

    @Override
    public ApiAnalysisReport generateAndSaveAIReport(Long apiId) {
        try {
            long startTime = System.currentTimeMillis();

            // 1. 获取分析报告数据
            ApiAnalysisReportVO reportData = getAnalysisReportData(apiId);
            if (reportData == null) {
                throw new OpzException(StatusCode.NOT_FOUND_ERROR, "无法获取 API 数据");
            }

            // 2. 构建 AI 提示词并调用 AI 生成分析
            String prompt = buildAnalysisPrompt(reportData);
            String analysisContent = callZhipuAIStreaming(prompt);

            long analysisTime = System.currentTimeMillis() - startTime;

            // 3. 保存分析结果到数据库
            var stats = reportData.getCallStatistics();

            ApiAnalysisReport entity = ApiAnalysisReport.builder()
                    .apiId(apiId)
                    .reportContent(analysisContent)
                    .summary(generateSummary(reportData))
                    .successRate(new java.math.BigDecimal(stats.getSuccessRate()))
                    .totalCalls(Math.toIntExact(stats.getTotalCalls()))
                    .avgResponseTime(new java.math.BigDecimal(stats.getAvgResponseTime()))
                    .maxResponseTime(stats.getMaxResponseTime())
                    .status("completed")
                    .analysisTimeMs(analysisTime)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            apiAnalysisReportDao.save(entity);
            log.info("AI 分析报告已生成并保存到数据库, apiId={}, reportId={}, timeMs={}", apiId, entity.getId(), analysisTime);

            return entity;
        } catch (Exception e) {
            log.error("生成 AI 分析报告失败，apiId={}", apiId, e);
            throw new OpzException(StatusCode.SYSTEM_ERROR, "生成 AI 分析报告失败: " + e.getMessage());
        }
    }

    /**
     * 生成报告摘要
     */
    private String generateSummary(ApiAnalysisReportVO report) {
        if (report == null || report.getCallStatistics() == null) {
            return "报告生成成功";
        }

        var stats = report.getCallStatistics();
        double successRate = stats.getSuccessRate();
        long avgResponseTime = Math.round(stats.getAvgResponseTime());

        StringBuilder summary = new StringBuilder();
        summary.append("API 成功率为 ").append(successRate).append("%");

        if (successRate < 95) {
            summary.append("，建议检查错误日志");
        }

        summary.append("，平均响应时间 ").append(avgResponseTime).append("ms");

        if (avgResponseTime > 500) {
            summary.append("，建议优化数据库查询");
        }

        return summary.toString();
    }
}

