package com.bqy.openapibackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.model.request.review.BatchReviewRequest;
import com.bqy.openapibackend.model.vo.ReviewProgressVO;
import com.bqy.openapibackend.model.vo.ReviewStatsVO;

import java.util.List;

/**
 * API 审核服务接口
 */
public interface IApiReviewService {

    /**
     * 获取审核进度
     *
     * @param apiId API ID
     * @return 审核进度信息
     */
    ReviewProgressVO getReviewProgress(Long apiId);

    /**
     * 获取待审核列表（带进度信息）
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 待审核 API 列表
     */
    Page<ReviewProgressVO> getPendingReviewList(int pageNum, int pageSize);

    /**
     * 批量审核
     *
     * @param request 批量审核请求
     * @return 是否成功
     */
    Boolean batchReview(BatchReviewRequest request);

    /**
     * 获取审核统计数据
     *
     * @return 审核统计信息
     */
    ReviewStatsVO getReviewStats();

    /**
     * 获取指定时间范围内的审核统计
     *
     * @param startDate 开始日期 (yyyy-MM-dd)
     * @param endDate   结束日期 (yyyy-MM-dd)
     * @return 审核统计信息
     */
    ReviewStatsVO getReviewStatsByDateRange(String startDate, String endDate);

    /**
     * 获取审核员绩效数据
     *
     * @param reviewerId 审核员 ID
     * @return 审核员统计数据
     */
    ReviewStatsVO.ReviewerStats getReviewerPerformance(Long reviewerId);

    /**
     * 获取所有审核员绩效排名
     *
     * @return 审核员绩效列表（按评分排序）
     */
    List<ReviewStatsVO.ReviewerStats> getReviewerRankings();

    /**
     * 自动分配待审核 API
     *
     * @param reviewerId 审核员 ID
     * @param count      分配数量
     * @return 分配的 API ID 列表
     */
    List<Long> autoAssignReviews(Long reviewerId, Integer count);

    /**
     * 获取审核历史记录
     *
     * @param apiId    API ID（可选）
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 审核历史记录
     */
    Page<ReviewProgressVO> getReviewHistory(Long apiId, int pageNum, int pageSize);
}

