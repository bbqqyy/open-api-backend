package com.bqy.openapibackend.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.openapibackend.mapper.ApiInfoMapper;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.enums.ApiStatusEnum;
import com.bqy.openapibackend.model.request.api.ApiInfoQueryRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiInfoDao extends ServiceImpl<ApiInfoMapper, ApiInfo> {

    public List<ApiInfo> getApiInfoListByCategoryId(Long categoryId) {
        return this.lambdaQuery()
                .eq(ApiInfo::getCategoryId, categoryId)
                .list();
    }

    public ApiInfo getApiInfoById(Long id) {
        return this.lambdaQuery()
                .eq(ApiInfo::getId, id)
                .one();
    }

    public Page<ApiInfo> getApiPage(ApiInfoQueryRequest request) {
        return this.lambdaQuery()
                .eq(ObjectUtils.isNotEmpty(request.getCategoryId()), ApiInfo::getCategoryId, request.getCategoryId())
                .like(StringUtils.isNotBlank(request.getApiName()), ApiInfo::getApiName, request.getApiName())
                .like(StringUtils.isNotBlank(request.getApiDescription()), ApiInfo::getApiDescription, request.getApiDescription())
                .eq(ObjectUtils.isNotEmpty(request.getIsOnline()), ApiInfo::getIsOnline, request.getIsOnline())
                .eq(ApiInfo::getStatus, ApiStatusEnum.RELEASE_SUCCESS.getCode())
                .page(new Page<>(request.getCurrent(), request.getPageSize()));
    }

    public Page<ApiInfo> getReleasingApiPage(ApiInfoQueryRequest request) {
        return this.lambdaQuery()
                .like(StringUtils.isNotBlank(request.getApiName()), ApiInfo::getApiName, request.getApiName())
                .like(StringUtils.isNotBlank(request.getApiDescription()), ApiInfo::getApiDescription, request.getApiDescription())
                .eq(ObjectUtils.isNotEmpty(request.getIsOnline()), ApiInfo::getIsOnline, request.getIsOnline())
                .eq(ApiInfo::getStatus, ApiStatusEnum.RELEASING.getCode())
                .page(new Page<>(request.getCurrent(), request.getPageSize()));
    }

    public Page<ApiInfo> getMyApiPage(ApiInfoQueryRequest request, Long userId) {
        return this.lambdaQuery()
                .eq(ApiInfo::getUserId, userId)
                .like(StringUtils.isNotBlank(request.getApiName()), ApiInfo::getApiName, request.getApiName())
                .like(StringUtils.isNotBlank(request.getApiDescription()), ApiInfo::getApiDescription, request.getApiDescription())
                .eq(ObjectUtils.isNotEmpty(request.getIsOnline()), ApiInfo::getIsOnline, request.getIsOnline())
                .eq(ObjectUtils.isNotEmpty(request.getStatus()),ApiInfo::getStatus,request.getStatus())
                .page(new Page<>(request.getCurrent(), request.getPageSize()));
    }

    public long getApiCount() {
        return this.count();
    }

    public long getApiCountByCategory(Long categoryId) {
        return this.lambdaQuery()
                .eq(ApiInfo::getCategoryId, categoryId)
                .count();
    }
}
