package com.bqy.openapibackend.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.openapibackend.mapper.ApiAlertMapper;
import com.bqy.openapibackend.model.entity.ApiAlert;
import com.bqy.openapibackend.model.enums.AlertStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 告警 DAO
 */
@Component
public class ApiAlertDao extends ServiceImpl<ApiAlertMapper, ApiAlert> {

    /**
     * 分页查询指定用户的告警（按 owner_id），支持按状态过滤
     */
    public Page<ApiAlert> getPageByOwnerId(Long ownerId, String status, int pageNum, int pageSize) {
        return lambdaQuery()
                .eq(ApiAlert::getOwnerId, ownerId)
                .eq(StringUtils.isNotBlank(status), ApiAlert::getStatus, status)
                .orderByDesc(ApiAlert::getCreatedAt)
                .page(new Page<>(pageNum, pageSize));
    }

    /**
     * 查询指定 API 的活跃告警列表
     */
    public List<ApiAlert> getActiveAlertsByApiId(Long apiId) {
        return lambdaQuery()
                .eq(ApiAlert::getApiId, apiId)
                .eq(ApiAlert::getStatus, AlertStatusEnum.ACTIVE.getCode())
                .orderByDesc(ApiAlert::getCreatedAt)
                .list();
    }

    /**
     * 统计指定用户未处理（ACTIVE）的告警数量
     */
    public long countActiveAlertsByOwnerId(Long ownerId) {
        return lambdaQuery()
                .eq(ApiAlert::getOwnerId, ownerId)
                .eq(ApiAlert::getStatus, AlertStatusEnum.ACTIVE.getCode())
                .count();
    }

    /**
     * 检查某 API 在指定时间之后是否已存在相同类型的活跃告警（防止重复告警）
     */
    public boolean existsActiveAlert(Long apiId, String alertType, LocalDateTime since) {
        return lambdaQuery()
                .eq(ApiAlert::getApiId, apiId)
                .eq(ApiAlert::getAlertType, alertType)
                .eq(ApiAlert::getStatus, AlertStatusEnum.ACTIVE.getCode())
                .ge(ApiAlert::getCreatedAt, since)
                .exists();
    }

    /**
     * 查询所有需要管理员关注的活跃告警
     */
    public List<ApiAlert> getAllActiveAlerts() {
        return lambdaQuery()
                .eq(ApiAlert::getStatus, AlertStatusEnum.ACTIVE.getCode())
                .orderByDesc(ApiAlert::getCreatedAt)
                .list();
    }
}

