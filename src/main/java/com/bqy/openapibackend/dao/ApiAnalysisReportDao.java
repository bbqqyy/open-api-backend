package com.bqy.openapibackend.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.openapibackend.mapper.ApiAnalysisReportMapper;
import com.bqy.openapibackend.model.entity.ApiAnalysisReport;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * API 分析报告数据访问对象
 * </p>
 *
 * @author bianqingyun
 * @since 2026-04-27
 */
@Component
public class ApiAnalysisReportDao extends ServiceImpl<ApiAnalysisReportMapper, ApiAnalysisReport> {

    /**
     * 获取某个 API 最近的分析报告
     *
     * @param apiId API ID
     * @return 最近的分析报告，如果没有则返回 null
     */
    public ApiAnalysisReport getLatestReport(Long apiId) {
        return lambdaQuery()
                .eq(ApiAnalysisReport::getApiId, apiId)
                .eq(ApiAnalysisReport::getStatus, "completed")
                .orderByDesc(ApiAnalysisReport::getCreatedAt)
                .last("LIMIT 1")
                .one();
    }

    /**
     * 获取某个 API 的报告列表
     *
     * @param apiId API ID
     * @param limit 查询数量
     * @return 报告列表
     */
    public List<ApiAnalysisReport> getReportsByApiId(Long apiId, int limit) {
        return lambdaQuery()
                .eq(ApiAnalysisReport::getApiId, apiId)
                .orderByDesc(ApiAnalysisReport::getCreatedAt)
                .last("LIMIT " + limit)
                .list();
    }

    /**
     * 删除某个 API 的所有分析报告
     *
     * @param apiId API ID
     * @return 是否删除成功
     */
    public boolean deleteByApiId(Long apiId) {
        return lambdaUpdate()
                .eq(ApiAnalysisReport::getApiId, apiId)
                .remove();
    }
}

