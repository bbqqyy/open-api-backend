package com.bqy.openapibackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.dao.ApiResponseParamDao;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.entity.ApiResponseParam;
import com.bqy.openapibackend.model.request.api.ApiResponseParamAddRequest;
import com.bqy.openapibackend.model.request.api.ApiResponseParamDeleteRequest;
import com.bqy.openapibackend.model.request.api.ApiResponseParamUpdateRequest;
import com.bqy.openapibackend.service.IApiResponseParamService;
import com.bqy.openapibackend.util.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
@Service
public class ApiResponseParamServiceImpl implements IApiResponseParamService {

    @Resource
    private ApiResponseParamDao apiResponseParamDao;

    @Resource
    private ApiInfoDao apiInfoDao;

    @Override
    public Boolean addApiResponseParam(ApiResponseParamAddRequest request) {
        ApiInfo apiInfo = apiInfoDao.getApiInfoById(request.getApiId());
        ThrowUtils.throwIf(ObjectUtil.isEmpty(apiInfo), "apiId不存在");
        ApiResponseParam apiResponseParam = new ApiResponseParam();
        BeanUtil.copyProperties(request, apiResponseParam);
        boolean result = apiResponseParamDao.save(apiResponseParam);
        ThrowUtils.throwIf(!result, "保存失败,数据库发生异常");
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateApiResponseParam(ApiResponseParamUpdateRequest request) {
        ApiResponseParam apiResponseParam = apiResponseParamDao.getById(request.getId());
        ThrowUtils.throwIf(ObjectUtil.isEmpty(apiResponseParam), "数据不存在");
        BeanUtil.copyProperties(request, apiResponseParam);
        boolean result = apiResponseParamDao.updateById(apiResponseParam);
        ThrowUtils.throwIf(!result, "更新失败,数据库发生异常");
        return Boolean.TRUE;
    }

    @Override
    public Boolean deleteApiResponseParam(ApiResponseParamDeleteRequest request) {
        boolean result = apiResponseParamDao.removeById(request.getId());
        ThrowUtils.throwIf(!result, "删除失败,数据库发生异常");
        return Boolean.TRUE;
    }

}
