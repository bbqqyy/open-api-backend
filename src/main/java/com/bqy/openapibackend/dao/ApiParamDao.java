package com.bqy.openapibackend.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.openapibackend.mapper.ApiParamMapper;
import com.bqy.openapibackend.model.entity.ApiParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiParamDao extends ServiceImpl<ApiParamMapper, ApiParam> {

    public List<ApiParam> getListByApiId(Long apiId) {
        return lambdaQuery()
                .eq(ApiParam::getApiId, apiId)
                .list();
    }

    public boolean deleteByApiId(Long apiId) {
        return lambdaUpdate()
                .eq(ApiParam::getApiId, apiId)
                .remove();
    }
}
