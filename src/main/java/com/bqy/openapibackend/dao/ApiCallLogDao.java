package com.bqy.openapibackend.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.openapibackend.mapper.ApiCallLogMapper;
import com.bqy.openapibackend.model.entity.ApiCallLog;
import org.springframework.stereotype.Component;

@Component
public class ApiCallLogDao extends ServiceImpl<ApiCallLogMapper, ApiCallLog> {

    public boolean deleteByApiId(Long apiId) {
        return lambdaUpdate()
                .eq(ApiCallLog::getApiId, apiId)
                .remove();
    }

    public long getTotalCallCount() {
        return this.count();
    }

    public long getSuccessCallCount() {
        return lambdaQuery()
                .eq(ApiCallLog::getStatus, "success")
                .count();
    }

    public long getFailCallCount() {
        return lambdaQuery()
                .eq(ApiCallLog::getStatus, "fail")
                .count();
    }
}
