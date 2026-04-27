package com.bqy.openapibackend.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.openapibackend.mapper.ApiCategoryMapper;
import com.bqy.openapibackend.model.entity.ApiCategory;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryQueryRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ApiCategoryDao extends ServiceImpl<ApiCategoryMapper, ApiCategory> {
    public Page<ApiCategory> getApiCategoryPage(ApiCategoryQueryRequest request) {
        return this.lambdaQuery()
                .like(StringUtils.isNotBlank(request.getName()), ApiCategory::getName, request.getName())
                .like(StringUtils.isNotBlank(request.getDescription()), ApiCategory::getDescription, request.getDescription())
                .page(new Page<>(request.getCurrent(), request.getPageSize()));
    }

    public long getCategoryCount() {
        return this.count();
    }
}
