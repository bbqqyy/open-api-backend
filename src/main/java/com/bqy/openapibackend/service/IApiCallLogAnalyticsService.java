package com.bqy.openapibackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.model.vo.ApiCallAnalyticsVO;
import com.bqy.openapibackend.model.vo.ApiCallLogVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 调用日志分析服务接口
 */
public interface IApiCallLogAnalyticsService {

    /**
     * 分页查询 API 调用日志
     *
     * @param apiId    API ID（可选）
     * @param userId   用户 ID（可选）
     * @param status   调用状态（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 调用日志列表
     */
    Page<ApiCallLogVO> queryCallLogs(Long apiId, Long userId, String status,
                                      LocalDateTime startTime, LocalDateTime endTime,
                                      int pageNum, int pageSize);

    /**
     * 获取整体分析数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分析数据
     */
    ApiCallAnalyticsVO getOverallAnalytics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取特定 API 的分析数据
     *
     * @param apiId     API ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分析数据
     */
    ApiCallAnalyticsVO getApiAnalytics(Long apiId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取按时间分组的数据（用于趋势图）
     *
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param intervalMinutes 时间间隔（分钟）
     * @return 时间序列数据
     */
    List<ApiCallAnalyticsVO.TimeSeriesData> getTimeSeriesData(LocalDateTime startTime,
                                                               LocalDateTime endTime,
                                                               int intervalMinutes);
}

