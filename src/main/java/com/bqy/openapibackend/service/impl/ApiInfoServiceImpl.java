package com.bqy.openapibackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.common.*;
import com.bqy.openapibackend.dao.*;
import com.bqy.openapibackend.exception.OpzException;
import com.bqy.openapibackend.model.entity.*;
import com.bqy.openapibackend.model.enums.ApiOnlineEnum;
import com.bqy.openapibackend.model.enums.ApiStatusEnum;
import com.bqy.openapibackend.model.enums.MethodEnum;
import com.bqy.openapibackend.model.request.api.*;
import com.bqy.openapibackend.model.vo.ApiApplyVO;
import com.bqy.openapibackend.model.vo.ApiInfoDetailVO;
import com.bqy.openapibackend.model.vo.ApiInfoVO;
import com.bqy.openapibackend.service.IApiInfoService;
import com.bqy.openapibackend.service.IApiStatisticsService;
import com.bqy.openapibackend.util.DynamicApiUtil;
import com.bqy.openapibackend.util.LoginUserUtils;
import com.bqy.openapibackend.util.RedisRateLimiter;
import com.bqy.openapibackend.util.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
@Slf4j
@Service
public class ApiInfoServiceImpl implements IApiInfoService {

    private static final String BASE_KEY = "rate_limit:open-api:";

    @Resource
    private ApiInfoDao apiInfoDao;

    @Resource
    private UserDao userDao;

    @Resource
    private ApiReviewDao apiReviewDao;

    @Resource
    private ApiParamDao apiParamDao;

    @Resource
    private ApiResponseParamDao apiResponseParamDao;

    @Resource
    private ApiLimitDao apiLimitDao;

    @Resource
    private ApiPermissionDao apiPermissionDao;

    @Resource
    private RedisRateLimiter redisRateLimiter;

    @Resource
    private DynamicApiUtil dynamicApiUtil;

    @Resource
    private ApiCallLogDao apiCallLogDao;

    @Resource
    private IApiStatisticsService apiStatisticsService;

    @Resource
    private ApiAnalysisReportDao apiAnalysisReportDao;

    @Resource
    private ApiCategoryDao apiCategoryDao;

    @Resource
    private LoginUserUtils loginUserUtils;

    @Value("${api.url}")
    private String apiUrl;

    @Override
    public Boolean addApiInfo(ApiInfoAddRequest apiInfoAddRequest, HttpServletRequest request) {
        User user = loginUserUtils.getLoginUser(request);
        ApiInfo apiInfo = new ApiInfo();
        apiInfo.setApiName(apiInfoAddRequest.getApiName());
        apiInfo.setApiDescription(apiInfoAddRequest.getApiDescription());
        if (ObjectUtils.isEmpty(apiInfoAddRequest.getCategoryId())) {
            apiInfo.setCategoryId(CommonConstant.NOT_CLASSIFIED);
        }
        apiInfo.setCategoryId(apiInfoAddRequest.getCategoryId());
        apiInfo.setUrl(apiInfoAddRequest.getUrl());
        apiInfo.setMethod(apiInfoAddRequest.getMethod());
        apiInfo.setStatus(ApiStatusEnum.WAITING_RELEASE.getCode());
        apiInfo.setIsOnline(ApiOnlineEnum.OFFLINE.getCode());
        apiInfo.setUserId(user.getId());
        boolean result = apiInfoDao.save(apiInfo);
        ThrowUtils.throwIf(!result, "保存失败，数据库发生异常");
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateApiInfo(ApiInfoUpdateRequest request, HttpServletRequest servletRequest) {
        User user = loginUserUtils.getLoginUser(servletRequest);
        ApiInfo apiInfo = apiInfoDao.getApiInfoById(request.getId());
        ThrowUtils.throwIf(ObjectUtils.isEmpty(apiInfo), "api不存在");
        ThrowUtils.throwIf(ObjectUtils.notEqual(user.getId(), apiInfo.getUserId()), StatusCode.NO_AUTH_ERROR);
        BeanUtil.copyProperties(request, apiInfo);
        if (apiInfo.getCategoryId() == null) {
            apiInfo.setCategoryId(CommonConstant.NOT_CLASSIFIED);
        }
        boolean result = apiInfoDao.updateById(apiInfo);
        ThrowUtils.throwIf(!result, "更新失败，数据库发生异常");
        return Boolean.TRUE;
    }

    @Override
    public Page<ApiInfoVO> getApiPage(ApiInfoQueryRequest request) {
        Page<ApiInfo> apiInfoPage = apiInfoDao.getApiPage(request);
        List<ApiInfo> apiInfoList = apiInfoPage.getRecords();
        List<ApiInfoVO> apiInfoVOList = apiInfoList.stream().map(apiInfo -> {
            ApiInfoVO apiInfoVO = new ApiInfoVO();
            BeanUtil.copyProperties(apiInfo, apiInfoVO);
            User user = userDao.getUserById(apiInfo.getUserId());
            apiInfoVO.setUserName(user.getUserName());
            apiInfoVO.setUrl(apiUrl + apiInfo.getId());
            fillCategoryInfo(apiInfoVO, apiInfo.getCategoryId());
            return apiInfoVO;
        }).toList();
        Page<ApiInfoVO> apiInfoVOPage = new Page<>(apiInfoPage.getCurrent(), apiInfoPage.getSize(), apiInfoPage.getTotal());
        apiInfoVOPage.setRecords(apiInfoVOList);
        return apiInfoVOPage;
    }

    @Override
    public Page<ApiInfoVO> getReleasingApiPage(ApiInfoQueryRequest request) {
        Page<ApiInfo> apiInfoPage = apiInfoDao.getReleasingApiPage(request);
        List<ApiInfo> apiInfoList = apiInfoPage.getRecords();
        List<ApiInfoVO> apiInfoVOList = apiInfoList.stream().map(apiInfo -> {
            ApiInfoVO apiInfoVO = new ApiInfoVO();
            BeanUtil.copyProperties(apiInfo, apiInfoVO);
            User user = userDao.getUserById(apiInfo.getUserId());
            apiInfoVO.setUserName(user.getUserName());
            apiInfoVO.setUrl(apiUrl + apiInfo.getId());
            fillCategoryInfo(apiInfoVO, apiInfo.getCategoryId());
            return apiInfoVO;
        }).toList();
        Page<ApiInfoVO> apiInfoVOPage = new Page<>(apiInfoPage.getCurrent(), apiInfoPage.getSize(), apiInfoPage.getTotal());
        apiInfoVOPage.setRecords(apiInfoVOList);
        return apiInfoVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveApiInfo(ApiInfoReviewRequest request, HttpServletRequest httpServletRequest) {
        User user = loginUserUtils.getLoginUser(httpServletRequest);
        ApiInfo apiInfo = apiInfoDao.getApiInfoById(request.getApiId());
        ThrowUtils.throwIf(ObjectUtils.isEmpty(apiInfo), StatusCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(ObjectUtils.notEqual(apiInfo.getStatus(), ApiStatusEnum.RELEASING.getCode()), "该接口未完善,不能进行审批");
        apiInfo.setStatus(ApiStatusEnum.RELEASE_SUCCESS.getCode());
        boolean result = apiInfoDao.updateById(apiInfo);
        ThrowUtils.throwIf(!result, "api状态更新失败，数据库发生异常");
        ApiReview apiReview = new ApiReview();
        apiReview.setApiId(request.getApiId());
        apiReview.setAdminId(user.getId());
        apiReview.setResult(ApiStatusEnum.RELEASE_SUCCESS.getMessage());
        apiReview.setComment(request.getComment());
        boolean save = apiReviewDao.save(apiReview);
        ThrowUtils.throwIf(!save, "添加审批记录失败，数据库发生异常");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rejectApiInfo(ApiInfoReviewRequest request, HttpServletRequest httpServletRequest) {
        User user = loginUserUtils.getLoginUser(httpServletRequest);
        ApiInfo apiInfo = apiInfoDao.getApiInfoById(request.getApiId());
        ThrowUtils.throwIf(ObjectUtils.isEmpty(apiInfo), StatusCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(ObjectUtils.notEqual(apiInfo.getStatus(), ApiStatusEnum.RELEASING.getCode()), "该接口未完善,不能进行审批");
        apiInfo.setStatus(ApiStatusEnum.RELEASE_FAIL.getCode());
        boolean result = apiInfoDao.updateById(apiInfo);
        ThrowUtils.throwIf(!result, "api状态更新失败，数据库发生异常");
        ApiReview apiReview = new ApiReview();
        apiReview.setApiId(request.getApiId());
        apiReview.setAdminId(user.getId());
        apiReview.setResult(ApiStatusEnum.RELEASE_FAIL.getMessage());
        apiReview.setComment(request.getComment());
        boolean save = apiReviewDao.save(apiReview);
        ThrowUtils.throwIf(!save, "添加审批记录失败，数据库发生异常");
        return Boolean.TRUE;
    }

    @Override
    public Boolean changeApiLineStatus(ApiInfoLineRequest apiInfoLineRequest, HttpServletRequest request) {
        User user = loginUserUtils.getLoginUser(request);
        ApiInfo apiInfo = apiInfoDao.getApiInfoById(apiInfoLineRequest.getApiId());
        ThrowUtils.throwIf(ObjectUtils.isEmpty(apiInfo), StatusCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(ObjectUtils.notEqual(user.getId(), apiInfo.getUserId()), StatusCode.NO_AUTH_ERROR);
        if (ObjectUtil.equal(apiInfo.getIsOnline(), ApiOnlineEnum.ONLINE.getCode())) {
            apiInfo.setIsOnline(ApiOnlineEnum.OFFLINE.getCode());
            boolean result = apiInfoDao.updateById(apiInfo);
            ThrowUtils.throwIf(!result, "api上线状态更新失败，数据库发生异常");
        } else if (ObjectUtil.equal(apiInfo.getIsOnline(), ApiOnlineEnum.OFFLINE.getCode())) {
            apiInfo.setIsOnline(ApiOnlineEnum.ONLINE.getCode());
            boolean result = apiInfoDao.updateById(apiInfo);
            ThrowUtils.throwIf(!result, "api上线状态更新失败，数据库发生异常");
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean releaseApiInfo(ApiInfoLineRequest request, HttpServletRequest servletRequest) {
        User user = loginUserUtils.getLoginUser(servletRequest);
        ApiInfo apiInfo = apiInfoDao.getApiInfoById(request.getApiId());
        ThrowUtils.throwIf(ObjectUtils.isEmpty(apiInfo), StatusCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(ObjectUtils.notEqual(user.getId(), apiInfo.getUserId()), StatusCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(
                ObjectUtils.notEqual(apiInfo.getStatus(), ApiStatusEnum.WAITING_RELEASE.getCode())
                && ObjectUtils.notEqual(apiInfo.getStatus(), ApiStatusEnum.RELEASE_FAIL.getCode()),
                "只有待发布或发布失败的API才能提交发布");
        apiInfo.setStatus(ApiStatusEnum.RELEASING.getCode());
        boolean result = apiInfoDao.updateById(apiInfo);
        ThrowUtils.throwIf(!result, "状态更新失败，数据库发生异常");
        return Boolean.TRUE;
    }

    @Override
    public Page<ApiInfoVO> getMyApiPage(ApiInfoQueryRequest request, HttpServletRequest httpServletRequest) {
        User user = loginUserUtils.getLoginUser(httpServletRequest);
        Page<ApiInfo> apiInfoPage = apiInfoDao.getMyApiPage(request, user.getId());
        List<ApiInfo> apiInfoList = apiInfoPage.getRecords();
        List<ApiInfoVO> apiInfoVOList = apiInfoList.stream().map(apiInfo -> {
            ApiInfoVO apiInfoVO = new ApiInfoVO();
            BeanUtil.copyProperties(apiInfo, apiInfoVO);
            apiInfoVO.setUserName(user.getUserName());
            // 保留 ApiInfo.url 原始值，供创建者查看和编辑真实地址
            fillCategoryInfo(apiInfoVO, apiInfo.getCategoryId());
            return apiInfoVO;
        }).toList();
        Page<ApiInfoVO> apiInfoVOPage = new Page<>(apiInfoPage.getCurrent(), apiInfoPage.getSize(), apiInfoPage.getTotal());
        apiInfoVOPage.setRecords(apiInfoVOList);
        return apiInfoVOPage;
    }

    @Override
    public ApiInfoDetailVO getDetailedApiInfo(Long apiId) {
        ApiInfoDetailVO apiInfoDetailVO = new ApiInfoDetailVO();
        ApiInfo apiInfo = apiInfoDao.getApiInfoById(apiId);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(apiInfo), StatusCode.NOT_FOUND_ERROR);
        User user = userDao.getUserById(apiInfo.getUserId());
        ThrowUtils.throwIf(ObjectUtils.isEmpty(user), StatusCode.NOT_FOUND_ERROR);
        List<ApiParam> apiParams = apiParamDao.getListByApiId(apiId);
        if (CollectionUtil.isNotEmpty(apiParams)) {
            apiInfoDetailVO.setParamList(apiParams);
        }
        List<ApiResponseParam> apiResponseParamList = apiResponseParamDao.getListByApiId(apiId);
        if (CollectionUtil.isNotEmpty(apiResponseParamList)) {
            apiInfoDetailVO.setApiResponseParamList(apiResponseParamList);
        }
        ApiLimit apiLimit = apiLimitDao.getByApiId(apiId);
        if (ObjectUtils.isNotEmpty(apiLimit)) {
            apiInfoDetailVO.setApiLimit(apiLimit);
        }
        apiInfoDetailVO.setApiName(apiInfo.getApiName());
        apiInfoDetailVO.setApiDescription(apiInfo.getApiDescription());
        apiInfoDetailVO.setUrl(apiUrl + apiId);
        apiInfoDetailVO.setMethod(apiInfo.getMethod());
        apiInfoDetailVO.setStatus(apiInfo.getStatus());
        apiInfoDetailVO.setIsOnline(apiInfo.getIsOnline());
        apiInfoDetailVO.setUserName(user.getUserName());
        apiInfoDetailVO.setCreateTime(apiInfo.getCreateTime());
        apiInfoDetailVO.setUpdateTime(apiInfo.getUpdateTime());
        apiInfoDetailVO.setCategoryId(apiInfo.getCategoryId());
        if (apiInfo.getCategoryId() != null && apiInfo.getCategoryId() > 0) {
            ApiCategory category = apiCategoryDao.getById(apiInfo.getCategoryId());
            if (category != null) {
                apiInfoDetailVO.setCategoryName(category.getName());
            }
        }
        return apiInfoDetailVO;
    }

    @Override
    public Boolean applyApiInfo(Long apiId, HttpServletRequest request) {
        User loginUser = loginUserUtils.getLoginUser(request);
        ApiInfo apiInfo = apiInfoDao.getApiInfoById(apiId);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(apiInfo), StatusCode.NOT_FOUND_ERROR);

        // 查询是否存在历史申请记录
        ApiPermission existing = apiPermissionDao.getByApiIdAndUserId(apiId, loginUser.getId());
        if (existing != null) {
            String status = existing.getStatus();
            // 已通过或审核中，不允许重复申请
            ThrowUtils.throwIf(ApiApplyConstant.APPROVED.equals(status), "您已获得该接口的调用权限，无需重复申请");
            ThrowUtils.throwIf(ApiApplyConstant.PENDING.equals(status), "您的申请正在审核中，请勿重复申请");
            // 申请被拒绝，重置状态为待审核
            existing.setStatus(ApiApplyConstant.PENDING);
            boolean result = apiPermissionDao.updateById(existing);
            ThrowUtils.throwIf(!result, "申请失败，数据库发生异常");
            return Boolean.TRUE;
        }

        // 不存在历史记录，新建申请
        ApiPermission apiPermission = new ApiPermission();
        apiPermission.setApiId(apiId);
        apiPermission.setUserId(loginUser.getId());
        apiPermission.setOwnerId(apiInfo.getUserId());
        apiPermission.setStatus(ApiApplyConstant.PENDING);
        boolean result = apiPermissionDao.save(apiPermission);
        ThrowUtils.throwIf(!result, "申请失败，数据库发生异常");
        return Boolean.TRUE;
    }

    @Override
    public Boolean approveApiApply(ApiApplyRequest request) {
        ApiPermission apiPermission = apiPermissionDao.getById(request.getId());
        ThrowUtils.throwIf(ObjectUtils.isEmpty(apiPermission), StatusCode.NOT_FOUND_ERROR);
        apiPermission.setStatus(ApiApplyConstant.APPROVED);
        boolean result = apiPermissionDao.updateById(apiPermission);
        ThrowUtils.throwIf(!result, "更新失败，数据库发生异常");
        return Boolean.TRUE;
    }

    @Override
    public Boolean rejectApiApply(ApiApplyRequest request) {
        ApiPermission apiPermission = apiPermissionDao.getById(request.getId());
        ThrowUtils.throwIf(ObjectUtils.isEmpty(apiPermission), StatusCode.NOT_FOUND_ERROR);
        apiPermission.setStatus(ApiApplyConstant.REJECTED);
        boolean result = apiPermissionDao.updateById(apiPermission);
        ThrowUtils.throwIf(!result, "更新失败，数据库发生异常");
        return Boolean.TRUE;
    }

    @Override
    public Page<ApiApplyVO> getMyApiApply(ApplyApiQueryRequest applyApiQueryRequest, HttpServletRequest request) {
        User loginUser = loginUserUtils.getLoginUser(request);
        Page<ApiPermission> apiPermissionPage = apiPermissionDao.getPageByUserId(applyApiQueryRequest, loginUser.getId());
        return transferToVO(apiPermissionPage);
    }

    @Override
    public Page<ApiApplyVO> getMyReceivedApiApply(ApplyApiQueryRequest applyApiQueryRequest, HttpServletRequest request) {
        User loginUser = loginUserUtils.getLoginUser(request);
        Page<ApiPermission> apiPermissionPage = apiPermissionDao.getPageByOwnerId(applyApiQueryRequest, loginUser.getId());
        return transferToVO(apiPermissionPage);
    }

    @Override
    public Object invokeApi(Long apiId, Map<String, Object> params, HttpServletRequest request) {
        String redisKey = BASE_KEY + apiId;
        User loginUser = loginUserUtils.getLoginUser(request);
        ApiInfo apiInfo = apiInfoDao.getById(apiId);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(apiInfo), StatusCode.NOT_FOUND_ERROR);
        // API 拥有者调用自己的接口，无需检查 apiPermission
        boolean isOwner = ObjectUtils.equals(loginUser.getId(), apiInfo.getUserId());
        if (!isOwner) {
            ApiPermission apiPermission = apiPermissionDao.getByApiIdAndUserId(apiId, loginUser.getId());
            ThrowUtils.throwIf(ObjectUtils.isEmpty(apiPermission), StatusCode.NOT_FOUND_ERROR);
            ThrowUtils.throwIf(!StringUtils.equals(ApiApplyConstant.APPROVED, apiPermission.getStatus()), StatusCode.NO_AUTH_ERROR);
        }
        ThrowUtils.throwIf(ObjectUtils.notEqual(ApiOnlineEnum.ONLINE.getCode(), apiInfo.getIsOnline()) || ObjectUtils.notEqual(ApiStatusEnum.RELEASE_SUCCESS.getCode(), apiInfo.getStatus()), "不能调用未上线或者未发布成功的接口");
        ApiLimit apiLimit = apiLimitDao.getByApiId(apiId);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(apiLimit), StatusCode.NOT_FOUND_ERROR);
        boolean result = redisRateLimiter.tryAcquire(redisKey, loginUser.getId(), 1, apiLimit.getQps(), apiLimit.getDailyLimit());
        ThrowUtils.throwIf(!result, "qps过高或每天访问次数上限");
        Map<String, String> headers = new HashMap<>();
        // 示例：透传 Authorization 头
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(authHeader)) {
            headers.put("Authorization", authHeader);
        }
        String userAgent = request.getHeader("User-Agent");
        if(StringUtils.isNotBlank(userAgent)){
            headers.put("User-Agent",userAgent);
        }


        // 3.2 处理请求体/参数
        // 如果 apiInfo 中配置了固定的请求参数（例如 API 密钥），需要与用户传来的 params 合并
        Object requestBody = params;

        // 4. 确定请求方法
        HttpMethod method = HttpMethod.GET;
        try {
            if (ObjectUtils.isNotEmpty(apiInfo.getMethod())) {
                method = HttpMethod.valueOf(MethodEnum.getNameByCode(apiInfo.getMethod()));
            }
        } catch (IllegalArgumentException e) {
            // 如果数据库中的 method 格式不对，默认使用 GET
            method = HttpMethod.GET;
        }

        // 5. 发起 HTTP 请求（根据请求方法区分参数位置）
        //    GET / DELETE / HEAD / OPTIONS：参数追加到 URL QueryString，body 为 null
        //    POST / PUT / PATCH 等：body 传 JSON，不拼 QueryString
        Object response = null;

        // GET 系方法才需要将参数转为 Map<String, String> 拼到 URL 上
        Map<String, String> urlParams = null;
        Object bodyToSend = null;
        if (method == HttpMethod.GET || method == HttpMethod.DELETE
                || method == HttpMethod.HEAD || method == HttpMethod.OPTIONS) {
            urlParams = new HashMap<>();
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (entry.getValue() != null) {
                        urlParams.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                }
            }
        } else {
            // POST / PUT / PATCH：直接把 Map<String,Object> 作为 JSON body
            bodyToSend = params;
        }

        ApiCallLog apiCallLog = new ApiCallLog();
        apiCallLog.setApiId(apiId);
        apiCallLog.setUserId(loginUser.getId());
        if (params != null && !params.isEmpty()) {
            apiCallLog.setRequestParam(JSONUtil.toJsonStr(params));
        }

        StopWatch stopWatch = new StopWatch();
        boolean callSuccess = false;
        try {
            stopWatch.start();
            response = dynamicApiUtil.request(
                    apiInfo.getUrl(),
                    method,
                    urlParams,
                    headers,
                    bodyToSend,
                    Object.class
            );
            stopWatch.stop();
            callSuccess = true;
            apiCallLog.setResponseTime(stopWatch.getTotalTimeMillis());
            apiCallLog.setStatus(ApiInvokeStatus.SUCCESS);
            return response;
        } catch (Exception e) {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            apiCallLog.setStatus(ApiInvokeStatus.FAIL);
            apiCallLog.setResponseTime(stopWatch.getTotalTimeMillis());
            throw new OpzException(StatusCode.SYSTEM_ERROR, "接口调用异常:" + e.getMessage());
        } finally {
            // 通过 Service 层记录统计，职责分离
            apiStatisticsService.recordCall(apiId, callSuccess);
            try {
                apiCallLogDao.save(apiCallLog);
            } catch (Exception e) {
                log.error("保存API调用日志失败, apiId={}", apiId, e);
            }
        }

    }

    private Page<ApiApplyVO> transferToVO(Page<ApiPermission> apiPermissionPage) {
        List<ApiApplyVO> apiApplyVOList = apiPermissionPage.getRecords().stream()
                .map(apiPermission -> {
                    ApiApplyVO apiApplyVO = new ApiApplyVO();
                    apiApplyVO.setId(apiPermission.getId());
                    apiApplyVO.setApiId(apiPermission.getApiId());
                    // API 信息
                    ApiInfo apiInfo = apiInfoDao.getApiInfoById(apiPermission.getApiId());
                    if (apiInfo != null) {
                        apiApplyVO.setApiName(apiInfo.getApiName());
                    }
                    // 申请人信息
                    apiApplyVO.setUserId(apiPermission.getUserId());
                    User applicant = userDao.getUserById(apiPermission.getUserId());
                    if (applicant != null) {
                        apiApplyVO.setApplicantName(applicant.getUserName());
                    }
                    // API 拥有者信息
                    apiApplyVO.setOwnerId(apiPermission.getOwnerId());
                    User owner = userDao.getUserById(apiPermission.getOwnerId());
                    if (owner != null) {
                        apiApplyVO.setOwnerName(owner.getUserName());
                    }
                    // 状态和时间
                    apiApplyVO.setStatus(apiPermission.getStatus());
                    apiApplyVO.setCreateTime(apiPermission.getCreateTime());
                    apiApplyVO.setUpdateTime(apiPermission.getUpdateTime());
                    return apiApplyVO;
                }).toList();
        Page<ApiApplyVO> apiApplyVOPage = new Page<>(apiPermissionPage.getCurrent(), apiPermissionPage.getSize(), apiPermissionPage.getTotal());
        apiApplyVOPage.setRecords(apiApplyVOList);
        return apiApplyVOPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteApiInfo(ApiInfoDeleteRequest request, HttpServletRequest servletRequest) {
        // 1. 参数校验
        ThrowUtils.throwIf(request == null || request.getId() == null, "请求参数不合法");
        Long apiId = request.getId();
        ThrowUtils.throwIf(apiId <= 0, "API ID不合法");

        // 2. 获取当前登录用户
        User loginUser = loginUserUtils.getLoginUser(servletRequest);
        ThrowUtils.throwIf(loginUser == null, StatusCode.NOT_FOUND_ERROR);

        // 3. 获取API信息
        ApiInfo apiInfo = apiInfoDao.getApiInfoById(apiId);
        ThrowUtils.throwIf(ObjectUtils.isEmpty(apiInfo), StatusCode.NOT_FOUND_ERROR);

        // 4. 权限检查：只有API的创建者或管理员可以删除
        if (!StringUtils.equals(UserConstant.ADMIN, loginUser.getUserRole())) {
            ThrowUtils.throwIf(!ObjectUtils.equals(loginUser.getId(), apiInfo.getUserId()), StatusCode.NO_AUTH_ERROR);
        }

        // 5. 删除API的所有关联信息（按外键关系删除）
        try {
            // 5.1 删除API权限记录（api_permission 表关联 apiId）
            apiPermissionDao.deleteByApiId(apiId);

            // 5.2 删除API请求参数定义（api_param 表关联 apiId）
            apiParamDao.deleteByApiId(apiId);

            // 5.3 删除API响应参数定义（api_response_param 表关联 apiId）
            apiResponseParamDao.deleteByApiId(apiId);

            // 5.4 删除API限流规则（api_limit 表关联 apiId）
            apiLimitDao.deleteByApiId(apiId);

            // 5.5 删除API审核记录（api_review 表关联 apiId）
            apiReviewDao.deleteByApiId(apiId);

            // 5.6 删除API调用日志（api_call_log 表关联 apiId）
            apiCallLogDao.deleteByApiId(apiId);

            // 5.7 删除API统计数据（api_statistics 表关联 apiId）
            apiStatisticsService.deleteByApiId(apiId);

            // 5.8 删除API分析报告（api_analysis_report 表关联 apiId）
            apiAnalysisReportDao.deleteByApiId(apiId);

            // 5.9 最后删除API本身（api_info 表）
            boolean result = apiInfoDao.removeById(apiId);
            ThrowUtils.throwIf(!result, "删除API失败，数据库发生异常");

            log.info("API删除成功，apiId={}, 操作用户={}, 已清理所有关联数据", apiId, loginUser.getId());
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("API删除失败，apiId={}, 错误信息：{}", apiId, e.getMessage(), e);
            throw new OpzException(StatusCode.SYSTEM_ERROR, "API删除失败：" + e.getMessage());
        }
    }

    /**
     * 填充分类信息到 ApiInfoVO
     */
    private void fillCategoryInfo(ApiInfoVO vo, Long categoryId) {
        vo.setCategoryId(categoryId);
        if (categoryId != null && categoryId > 0) {
            ApiCategory category = apiCategoryDao.getById(categoryId);
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
    }

}
