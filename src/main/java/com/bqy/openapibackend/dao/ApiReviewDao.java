package com.bqy.openapibackend.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.openapibackend.mapper.ApiReviewMapper;
import com.bqy.openapibackend.model.entity.ApiReview;
import org.springframework.stereotype.Component;

@Component
public class ApiReviewDao extends ServiceImpl<ApiReviewMapper, ApiReview> {

    public boolean deleteByApiId(Long apiId) {
        return lambdaUpdate()
                .eq(ApiReview::getApiId, apiId)
                .remove();
    }
}
