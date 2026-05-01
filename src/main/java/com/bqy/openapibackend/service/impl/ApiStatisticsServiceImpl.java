package com.bqy.openapibackend.service.impl;

import com.bqy.openapibackend.dao.ApiStatisticsDao;
import com.bqy.openapibackend.model.entity.ApiStatistics;
import com.bqy.openapibackend.service.IApiStatisticsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * API 统计服务实现类
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
@Slf4j
@Service
public class ApiStatisticsServiceImpl implements IApiStatisticsService {

    @Resource
    private ApiStatisticsDao apiStatisticsDao;

    @Override
    public ApiStatistics getByApiId(Long apiId) {
        return apiStatisticsDao.getByApiId(apiId);
    }

    @Override
    public List<ApiStatistics> listByApiIdLastDays(Long apiId, int days) {
        return apiStatisticsDao.listByApiIdLastDays(apiId, days);
    }

    @Override
    public void recordCall(Long apiId, boolean success) {
        try {
            ApiStatistics statistics = apiStatisticsDao.getByApiId(apiId);
            if (statistics == null) {
                statistics = new ApiStatistics();
                statistics.setApiId(apiId);
                statistics.setCallCount(0);
                statistics.setSuccessCount(0);
                statistics.setFailCount(0);
            }
            statistics.setCallCount(statistics.getCallCount() + 1);
            if (success) {
                statistics.setSuccessCount(statistics.getSuccessCount() + 1);
            } else {
                statistics.setFailCount(statistics.getFailCount() + 1);
            }
            apiStatisticsDao.saveOrUpdate(statistics);
        } catch (Exception e) {
            log.error("记录 API 统计失败, apiId={}, success={}", apiId, success, e);
        }
    }

    @Override
    public boolean deleteByApiId(Long apiId) {
        return apiStatisticsDao.deleteByApiId(apiId);
    }
}

