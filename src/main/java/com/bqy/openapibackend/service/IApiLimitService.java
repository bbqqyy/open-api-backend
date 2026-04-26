package com.bqy.openapibackend.service;

import com.bqy.openapibackend.model.entity.ApiLimit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bqy.openapibackend.model.request.api.ApiLimitAddRequest;
import com.bqy.openapibackend.model.request.api.ApiLimitDeleteRequest;
import com.bqy.openapibackend.model.request.api.ApiLimitUpdateRequest;
import jakarta.validation.Valid;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
public interface IApiLimitService {

    Boolean addApiLimit(ApiLimitAddRequest request);

    Boolean updateApiLimit(ApiLimitUpdateRequest request);

    Boolean deleteApiLimit(ApiLimitDeleteRequest request);
}
