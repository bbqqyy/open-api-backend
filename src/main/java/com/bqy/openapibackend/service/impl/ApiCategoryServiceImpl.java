package com.bqy.openapibackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.common.CommonConstant;
import com.bqy.openapibackend.dao.ApiCategoryDao;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.model.entity.ApiCategory;
import com.bqy.openapibackend.mapper.ApiCategoryMapper;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryAddRequest;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryDeleteRequest;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryQueryRequest;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryUpdateRequest;
import com.bqy.openapibackend.model.vo.ApiCategoryVO;
import com.bqy.openapibackend.service.IApiCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bqy.openapibackend.util.ThrowUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
@Service
public class ApiCategoryServiceImpl implements IApiCategoryService {

    @Resource
    private ApiCategoryDao apiCategoryDao;

    @Resource
    private ApiInfoDao apiInfoDao;

    @Override
    public Boolean addAppCategory(ApiCategoryAddRequest request) {
        ApiCategory apiCategory = new ApiCategory();
        BeanUtil.copyProperties(request, apiCategory);
        boolean result = apiCategoryDao.save(apiCategory);
        ThrowUtils.throwIf(!result, "添加失败，数据库异常");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteApiCategory(ApiCategoryDeleteRequest request) {
        List<ApiInfo> apiInfoList = apiInfoDao.getApiInfoListByCategoryId(request.getId());
        if (CollectionUtil.isNotEmpty(apiInfoList)) {
            List<ApiInfo> updateApiInfoList = apiInfoList.stream().peek(apiInfo -> apiInfo.setCategoryId(CommonConstant.NOT_CLASSIFIED)).toList();
            boolean updateResult = apiInfoDao.updateBatchById(updateApiInfoList);
            ThrowUtils.throwIf(!updateResult, "更新失败，数据库异常");
        }
        boolean result = apiCategoryDao.removeById(request.getId());
        ThrowUtils.throwIf(!result, "删除API分类失败，数据库异常");
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateApiCategory(ApiCategoryUpdateRequest request) {
        ApiCategory apiCategory = new ApiCategory();
        BeanUtil.copyProperties(request, apiCategory);
        boolean result = apiCategoryDao.updateById(apiCategory);
        ThrowUtils.throwIf(!result, "更新失败，数据库异常");
        return Boolean.TRUE;
    }

    @Override
    public Page<ApiCategoryVO> getApiCategoryPage(ApiCategoryQueryRequest request) {
        Page<ApiCategory> apiInfoPage = apiCategoryDao.getApiCategoryPage(request);
        List<ApiCategory> apiCategoryList = apiInfoPage.getRecords();
        List<ApiCategoryVO> apiCategoryVOList = apiCategoryList.stream().map(apiCategory -> {
            ApiCategoryVO apiCategoryVO = new ApiCategoryVO();
            BeanUtil.copyProperties(apiCategory, apiCategoryVO);
            return apiCategoryVO;
        }).toList();
        Page<ApiCategoryVO> apiCategoryVOPage = new Page<>(apiInfoPage.getCurrent(), apiInfoPage.getSize(), apiInfoPage.getTotal());
        apiCategoryVOPage.setRecords(apiCategoryVOList);
        return apiCategoryVOPage;
    }
}
