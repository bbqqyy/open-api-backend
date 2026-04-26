package com.bqy.openapibackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.dao.ApiLimitDao;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.entity.ApiLimit;
import com.bqy.openapibackend.model.request.api.ApiLimitAddRequest;
import com.bqy.openapibackend.model.request.api.ApiLimitDeleteRequest;
import com.bqy.openapibackend.model.request.api.ApiLimitUpdateRequest;
import com.bqy.openapibackend.service.IApiLimitService;
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
public class ApiLimitServiceImpl implements IApiLimitService {

    @Resource
    private ApiLimitDao apiLimitDao;

    @Resource
    private ApiInfoDao apiInfoDao;

    @Override
    public Boolean addApiLimit(ApiLimitAddRequest request) {
        ApiInfo apiInfo = apiInfoDao.getApiInfoById(request.getApiId());
        ThrowUtils.throwIf(ObjectUtil.isEmpty(apiInfo), "apiId无效");
        ApiLimit apiLimit = new ApiLimit();
        BeanUtil.copyProperties(request, apiLimit);
        boolean result = apiLimitDao.save(apiLimit);
        ThrowUtils.throwIf(!result, "保存失败,数据库发生异常");
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateApiLimit(ApiLimitUpdateRequest request) {
        ApiLimit apiLimit = apiLimitDao.getById(request.getId());
        ThrowUtils.throwIf(ObjectUtil.isEmpty(apiLimit), "数据不存在");
        BeanUtil.copyProperties(request, apiLimit);
        boolean result = apiLimitDao.updateById(apiLimit);
        ThrowUtils.throwIf(!result, "更新失败,数据库发生异常");
        return Boolean.TRUE;
    }

    @Override
    public Boolean deleteApiLimit(ApiLimitDeleteRequest request) {
        boolean result = apiLimitDao.removeById(request.getId());
        ThrowUtils.throwIf(!result, "删除失败,数据库发生异常");
        return Boolean.TRUE;
    }
}
