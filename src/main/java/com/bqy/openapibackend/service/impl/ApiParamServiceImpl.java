package com.bqy.openapibackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.bqy.openapibackend.common.StatusCode;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.dao.ApiParamDao;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.entity.ApiParam;
import com.bqy.openapibackend.model.entity.User;
import com.bqy.openapibackend.model.request.api.ApiParamAddRequest;
import com.bqy.openapibackend.model.request.api.ApiParamDeleteRequest;
import com.bqy.openapibackend.model.request.api.ApiParamUpdateRequest;
import com.bqy.openapibackend.service.IApiParamService;
import com.bqy.openapibackend.util.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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
public class ApiParamServiceImpl implements IApiParamService {

    @Resource
    private ApiParamDao apiParamDao;

    @Resource
    private ApiInfoDao apiInfoDao;

    @Override
    public Boolean addApiParam(ApiParamAddRequest request) {
        ApiInfo apiInfo = apiInfoDao.getApiInfoById(request.getApiId());
        ThrowUtils.throwIf(ObjectUtil.isEmpty(apiInfo), "apiId无效");
        ApiParam apiParam = new ApiParam();
        BeanUtil.copyProperties(request, apiParam);
        boolean result = apiParamDao.save(apiParam);
        ThrowUtils.throwIf(!result, "保存失败，数据库发生异常");
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateApiParam(ApiParamUpdateRequest request) {
        ApiParam apiParam = apiParamDao.getById(request.getId());
        ThrowUtils.throwIf(ObjectUtil.isEmpty(apiParam), "数据不存在");
        BeanUtil.copyProperties(request, apiParam);
        boolean result = apiParamDao.updateById(apiParam);
        ThrowUtils.throwIf(!result, "更新失败，数据库发生异常");
        return Boolean.TRUE;
    }

    @Override
    public Boolean deleteApiParam(ApiParamDeleteRequest request) {
        boolean result = apiParamDao.removeById(request.getId());
        ThrowUtils.throwIf(!result, "删除失败，数据库发生异常");
        return Boolean.TRUE;
    }

}
