package com.bqy.openapibackend.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.openapibackend.mapper.ApiStatisticsMapper;
import com.bqy.openapibackend.model.entity.ApiStatistics;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ApiStatisticsDao extends ServiceImpl<ApiStatisticsMapper, ApiStatistics> {
    public ApiStatistics getByApiId(Long apiId) {
        return lambdaQuery()
                .eq(ApiStatistics::getApiId, apiId)
                .one();
    }

    public List<ApiStatistics> listByApiIdLastDays(Long apiId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return lambdaQuery()
                .eq(ApiStatistics::getApiId, apiId)
                .ge(ApiStatistics::getStatDate, since)
                .orderByAsc(ApiStatistics::getStatDate)
                .list();
    }
}
