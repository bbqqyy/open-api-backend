package com.bqy.openapibackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryAddRequest;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryDeleteRequest;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryQueryRequest;
import com.bqy.openapibackend.model.request.apicategory.ApiCategoryUpdateRequest;
import com.bqy.openapibackend.model.vo.ApiCategoryVO;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author bianqingyun
 * @since 2026-03-16
 */
public interface IApiCategoryService {

    Boolean addAppCategory(ApiCategoryAddRequest request);

    Boolean deleteApiCategory(ApiCategoryDeleteRequest request);

    Boolean updateApiCategory(ApiCategoryUpdateRequest request);

    Page<ApiCategoryVO> getApiCategoryPage(ApiCategoryQueryRequest request);

    List<ApiCategoryVO> listApiCategories();
}
