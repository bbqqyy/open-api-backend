package com.bqy.openapibackend.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.openapibackend.mapper.ApiPermissionMapper;
import com.bqy.openapibackend.model.entity.ApiPermission;
import com.bqy.openapibackend.model.request.api.ApplyApiQueryRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ApiPermissionDao extends ServiceImpl<ApiPermissionMapper, ApiPermission> {
    public Page<ApiPermission> getPageByUserId(ApplyApiQueryRequest applyApiQueryRequest, Long userId) {
        return lambdaQuery()
                .eq(ApiPermission::getUserId, userId)
                .eq(StringUtils.isNotBlank(applyApiQueryRequest.getStatus()), ApiPermission::getStatus, applyApiQueryRequest.getStatus())
                .page(new Page<>(applyApiQueryRequest.getCurrent(), applyApiQueryRequest.getPageSize()));
    }

    public Page<ApiPermission> getPageByOwnerId(ApplyApiQueryRequest applyApiQueryRequest, Long userId) {
        return lambdaQuery()
                .eq(ApiPermission::getOwnerId, userId)
                .eq(StringUtils.isNotBlank(applyApiQueryRequest.getStatus()), ApiPermission::getStatus, applyApiQueryRequest.getStatus())
                .page(new Page<>(applyApiQueryRequest.getCurrent(), applyApiQueryRequest.getPageSize()));

    }

    public ApiPermission getByApiIdAndUserId(Long apiId, Long userId) {
        return lambdaQuery()
                .eq(ApiPermission::getApiId, apiId)
                .eq(ApiPermission::getUserId, userId)
                .one();
    }

    public boolean deleteByApiId(Long apiId) {
        return lambdaUpdate()
                .eq(ApiPermission::getApiId, apiId)
                .remove();
    }
}
