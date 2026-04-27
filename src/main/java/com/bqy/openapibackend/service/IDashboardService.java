package com.bqy.openapibackend.service;

import com.bqy.openapibackend.model.vo.DashboardVO;

/**
 * 仪表板服务接口
 */
public interface IDashboardService {

    /**
     * 获取首页仪表板数据
     * @return 仪表板数据
     */
    DashboardVO getDashboardData();
}

