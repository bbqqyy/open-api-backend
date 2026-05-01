package com.bqy.openapibackend.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.dao.ApiAlertDao;
import com.bqy.openapibackend.exception.OpzException;
import com.bqy.openapibackend.model.entity.ApiAlert;
import com.bqy.openapibackend.model.enums.AlertLevelEnum;
import com.bqy.openapibackend.model.enums.AlertStatusEnum;
import com.bqy.openapibackend.model.enums.AlertTypeEnum;
import com.bqy.openapibackend.model.vo.ApiAlertVO;
import com.bqy.openapibackend.service.IApiAlertService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API 告警服务实现类
 */
@Slf4j
@Service
public class ApiAlertServiceImpl implements IApiAlertService {

    @Resource
    private ApiAlertDao apiAlertDao;

    @Override
    public Page<ApiAlertVO> getMyAlerts(Long ownerId, String status, int pageNum, int pageSize) {
        Page<ApiAlert> alertPage = apiAlertDao.getPageByOwnerId(ownerId, status, pageNum, pageSize);
        Page<ApiAlertVO> voPage = new Page<>(alertPage.getCurrent(), alertPage.getSize(), alertPage.getTotal());
        voPage.setRecords(alertPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public void acknowledgeAlert(Long alertId, Long userId) {
        ApiAlert alert = apiAlertDao.getById(alertId);
        if (alert == null) {
            throw new OpzException(StatusCode.NOT_FOUND_ERROR, "告警不存在");
        }
        if (!alert.getOwnerId().equals(userId)) {
            throw new OpzException(StatusCode.NO_AUTH_ERROR, "无权操作该告警");
        }
        if (!AlertStatusEnum.ACTIVE.getCode().equals(alert.getStatus())) {
            throw new OpzException(StatusCode.OPERATION_ERROR, "该告警已处理，无需重复操作");
        }
        alert.setStatus(AlertStatusEnum.ACKNOWLEDGED.getCode());
        alert.setUpdatedAt(LocalDateTime.now());
        apiAlertDao.updateById(alert);
        log.info("用户 {} 确认了告警 {}", userId, alertId);
    }

    @Override
    public void ignoreAlert(Long alertId, Long userId) {
        ApiAlert alert = apiAlertDao.getById(alertId);
        if (alert == null) {
            throw new OpzException(StatusCode.NOT_FOUND_ERROR, "告警不存在");
        }
        if (!alert.getOwnerId().equals(userId)) {
            throw new OpzException(StatusCode.NO_AUTH_ERROR, "无权操作该告警");
        }
        if (!AlertStatusEnum.ACTIVE.getCode().equals(alert.getStatus())) {
            throw new OpzException(StatusCode.OPERATION_ERROR, "该告警已处理，无需重复操作");
        }
        alert.setStatus(AlertStatusEnum.IGNORED.getCode());
        alert.setUpdatedAt(LocalDateTime.now());
        apiAlertDao.updateById(alert);
        log.info("用户 {} 忽略了告警 {}", userId, alertId);
    }

    @Override
    public long countActiveAlerts(Long ownerId) {
        return apiAlertDao.countActiveAlertsByOwnerId(ownerId);
    }

    @Override
    public List<ApiAlertVO> getActiveAlertsByApiId(Long apiId) {
        return apiAlertDao.getActiveAlertsByApiId(apiId)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 将实体转换为 VO，附加中文标签
     */
    private ApiAlertVO toVO(ApiAlert alert) {
        String typeLabel = resolveTypeLabel(alert.getAlertType());
        String levelLabel = resolveLevelLabel(alert.getAlertLevel());
        String statusLabel = resolveStatusLabel(alert.getStatus());

        return ApiAlertVO.builder()
                .id(alert.getId())
                .apiId(alert.getApiId())
                .apiName(alert.getApiName())
                .alertType(alert.getAlertType())
                .alertTypeLabel(typeLabel)
                .alertLevel(alert.getAlertLevel())
                .alertLevelLabel(levelLabel)
                .alertMessage(alert.getAlertMessage())
                .failRate(alert.getFailRate())
                .avgResponseTime(alert.getAvgResponseTime())
                .totalCalls(alert.getTotalCalls())
                .status(alert.getStatus())
                .statusLabel(statusLabel)
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .build();
    }

    private String resolveTypeLabel(String type) {
        if (type == null) return "";
        for (AlertTypeEnum e : AlertTypeEnum.values()) {
            if (e.getCode().equals(type)) return e.getLabel();
        }
        return type;
    }

    private String resolveLevelLabel(String level) {
        if (level == null) return "";
        for (AlertLevelEnum e : AlertLevelEnum.values()) {
            if (e.getCode().equals(level)) return e.getLabel();
        }
        return level;
    }

    private String resolveStatusLabel(String status) {
        if (status == null) return "";
        for (AlertStatusEnum e : AlertStatusEnum.values()) {
            if (e.getCode().equals(status)) return e.getLabel();
        }
        return status;
    }
}

