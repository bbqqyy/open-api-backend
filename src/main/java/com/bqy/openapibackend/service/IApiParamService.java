package com.bqy.openapibackend.service;

import com.bqy.openapibackend.model.entity.ApiParam;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bqy.openapibackend.model.request.api.ApiParamAddRequest;
import com.bqy.openapibackend.model.request.api.ApiParamDeleteRequest;
import com.bqy.openapibackend.model.request.api.ApiParamUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
public interface IApiParamService {

    Boolean addApiParam(ApiParamAddRequest request);

    Boolean updateApiParam(ApiParamUpdateRequest request);

    Boolean deleteApiParam(ApiParamDeleteRequest request);
}
