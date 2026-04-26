package com.bqy.openapibackend.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.openapibackend.mapper.ApiResponseParamMapper;
import com.bqy.openapibackend.model.entity.ApiResponseParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiResponseParamDao extends ServiceImpl<ApiResponseParamMapper, ApiResponseParam> {
    public List<ApiResponseParam> getListByApiId(Long apiId) {
        return lambdaQuery()
                .eq(ApiResponseParam::getApiId, apiId)
                .list();
    }
}
