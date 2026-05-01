package com.bqy.openapibackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.model.vo.ApiAlertVO;

import java.util.List;

/**
 * API 告警服务接口
 */
public interface IApiAlertService {

    /**
     * 分页查询当前登录用户的告警列表
     *
     * @param ownerId  用户 ID
     * @param status   过滤状态（null 表示查全部）
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页告警列表
     */
    Page<ApiAlertVO> getMyAlerts(Long ownerId, String status, int pageNum, int pageSize);

    /**
     * 确认告警（将状态改为 ACKNOWLEDGED）
     *
     * @param alertId 告警 ID
     * @param userId  操作用户 ID（鉴权用）
     */
    void acknowledgeAlert(Long alertId, Long userId);

    /**
     * 忽略告警（将状态改为 IGNORED）
     *
     * @param alertId 告警 ID
     * @param userId  操作用户 ID（鉴权用）
     */
    void ignoreAlert(Long alertId, Long userId);

    /**
     * 统计当前用户未处理的告警数量
     *
     * @param ownerId 用户 ID
     * @return 未处理告警数量
     */
    long countActiveAlerts(Long ownerId);

    /**
     * 查询指定 API 的活跃告警
     *
     * @param apiId API ID
     * @return 告警列表
     */
    List<ApiAlertVO> getActiveAlertsByApiId(Long apiId);
}

