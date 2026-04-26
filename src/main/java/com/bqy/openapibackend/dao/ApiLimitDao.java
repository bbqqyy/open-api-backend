package com.bqy.openapibackend.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.openapibackend.mapper.ApiLimitMapper;
import com.bqy.openapibackend.model.entity.ApiLimit;
import org.springframework.stereotype.Component;

@Component
public class ApiLimitDao extends ServiceImpl<ApiLimitMapper, ApiLimit> {
    public ApiLimit getByApiId(Long apiId) {
        return lambdaQuery()
                .eq(ApiLimit::getApiId, apiId)
                .one();
    }
}
