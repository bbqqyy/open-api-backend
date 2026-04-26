package com.bqy.openapibackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.model.request.api.*;
import com.bqy.openapibackend.model.vo.ApiApplyVO;
import com.bqy.openapibackend.model.vo.ApiInfoDetailVO;
import com.bqy.openapibackend.model.vo.ApiInfoVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
public interface IApiInfoService {

    Boolean addApiInfo(ApiInfoAddRequest apiInfoAddRequest, HttpServletRequest request);

    Boolean updateApiInfo(ApiInfoUpdateRequest request, HttpServletRequest servletRequest);

    Page<ApiInfoVO> getApiPage(ApiInfoQueryRequest request);

    Page<ApiInfoVO> getReleasingApiPage(ApiInfoQueryRequest request);

    Boolean approveApiInfo(ApiInfoReviewRequest request, HttpServletRequest httpServletRequest);

    Boolean rejectApiInfo(ApiInfoReviewRequest request, HttpServletRequest httpServletRequest);

    Boolean changeApiLineStatus(ApiInfoLineRequest apiInfoLineRequest, HttpServletRequest request);

    Page<ApiInfoVO> getMyApiPage(ApiInfoQueryRequest request, HttpServletRequest httpServletRequest);

    ApiInfoDetailVO getDetailedApiInfo(Long apiId);

    Boolean applyApiInfo(Long apiId, HttpServletRequest request);

    Boolean approveApiApply(ApiApplyRequest request);

    Boolean rejectApiApply(ApiApplyRequest request);

    Page<ApiApplyVO> getMyApiApply(ApplyApiQueryRequest applyApiQueryRequest, HttpServletRequest request);

    Page<ApiApplyVO> getMyReceivedApiApply(ApplyApiQueryRequest applyApiQueryRequest, HttpServletRequest request);

    Object invokeApi(Long apiId, Map<String, Object> params, HttpServletRequest request);
}
