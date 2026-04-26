package com.bqy.openapibackend.service;

import com.bqy.openapibackend.model.entity.ApiResponseParam;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bqy.openapibackend.model.request.api.ApiResponseParamAddRequest;
import com.bqy.openapibackend.model.request.api.ApiResponseParamDeleteRequest;
import com.bqy.openapibackend.model.request.api.ApiResponseParamUpdateRequest;
import jakarta.validation.Valid;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
public interface IApiResponseParamService {

    Boolean addApiResponseParam(ApiResponseParamAddRequest request);

    Boolean updateApiResponseParam(ApiResponseParamUpdateRequest request);

    Boolean deleteApiResponseParam(ApiResponseParamDeleteRequest request);
}
