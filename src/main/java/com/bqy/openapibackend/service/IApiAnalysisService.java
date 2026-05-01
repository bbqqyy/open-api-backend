package com.bqy.openapibackend.service;

import com.bqy.openapibackend.model.entity.ApiAnalysisReport;
import com.bqy.openapibackend.model.vo.ApiAnalysisReportListVO;
import com.bqy.openapibackend.model.vo.ApiAnalysisReportVO;

import java.util.List;

/**
 * API 分析服务接口
 * 基于 AI 对 API 调用数据进行分析，生成分析报告
 */
public interface IApiAnalysisService {

    /**
     * 获取 API 分析报告数据
     * @param apiId API ID
     * @return 分析报告数据对象
     */
    ApiAnalysisReportVO getAnalysisReportData(Long apiId);

    /**
     * 保存分析报告到数据库
     * @param apiId API ID
     * @param reportContent 报告内容
     * @return 保存的报告对象
     */
    ApiAnalysisReport saveReport(Long apiId, String reportContent);

    /**
     * 获取某个 API 的最近报告
     * @param apiId API ID
     * @return 最近的报告，如果没有则返回 null
     */
    ApiAnalysisReport getLatestReport(Long apiId);

    /**
     * 获取某个 API 的报告列表
     * @param apiId API ID
     * @param limit 查询数量，默认 10
     * @return 报告摘要列表
     */
    List<ApiAnalysisReportListVO> listReports(Long apiId, int limit);

    /**
     * 生成 AI 分析报告并保存到数据库
     * @param apiId API ID
     * @return 保存的报告对象
     */
    ApiAnalysisReport generateAndSaveAIReport(Long apiId);
}

