package com.bqy.openapibackend.service;

import com.bqy.openapibackend.model.entity.ApiStatistics;

import java.util.List;

/**
 * <p>
 * API 统计服务接口
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
public interface IApiStatisticsService {

    /**
     * 根据 API ID 获取统计数据（单条汇总）
     *
     * @param apiId API ID
     * @return 统计实体，不存在则返回 null
     */
    ApiStatistics getByApiId(Long apiId);

    /**
     * 获取指定 API 最近 N 天的统计数据列表
     *
     * @param apiId API ID
     * @param days  天数
     * @return 按日期升序排列的统计列表
     */
    List<ApiStatistics> listByApiIdLastDays(Long apiId, int days);

    /**
     * 记录一次 API 调用结果（成功或失败），若无统计记录则自动创建
     *
     * @param apiId   API ID
     * @param success 是否成功
     */
    void recordCall(Long apiId, boolean success);

    /**
     * 删除指定 API 的所有统计数据（用于 API 删除时级联清理）
     *
     * @param apiId API ID
     * @return 是否删除成功
     */
    boolean deleteByApiId(Long apiId);
}

