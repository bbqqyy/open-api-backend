package com.bqy.openapibackend.service.impl;

import com.bqy.openapibackend.dao.ApiCallLogDao;
import com.bqy.openapibackend.dao.ApiCategoryDao;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.dao.UserDao;
import com.bqy.openapibackend.model.entity.ApiCategory;
import com.bqy.openapibackend.model.vo.DashboardVO;
import com.bqy.openapibackend.service.IDashboardService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 仪表板服务实现类
 */
@Slf4j
@Service
public class DashboardServiceImpl implements IDashboardService {

    @Resource
    private ApiInfoDao apiInfoDao;

    @Resource
    private ApiCallLogDao apiCallLogDao;

    @Resource
    private UserDao userDao;

    @Resource
    private ApiCategoryDao apiCategoryDao;

    @Override
    public DashboardVO getDashboardData() {
        try {
            // 1. 获取 API 总数
            long totalApiCount = apiInfoDao.getApiCount();

            // 2. 获取调用统计数据
            long totalCallCount = apiCallLogDao.getTotalCallCount();
            long successCallCount = apiCallLogDao.getSuccessCallCount();
            long failCallCount = apiCallLogDao.getFailCallCount();

            // 3. 计算成功率
            double successRate = 0.0;
            if (totalCallCount > 0) {
                successRate = (successCallCount * 100.0) / totalCallCount;
                // 保留两位小数
                successRate = Math.round(successRate * 100.0) / 100.0;
            }

            // 4. 获取在线用户数 (这里用总用户数作为在线用户数的近似值，实际可以根据 Redis session 实现)
            long onlineUserCount = userDao.getUserCount();

            // 5. 获取接口类别数
            long categoryCount = apiCategoryDao.getCategoryCount();

            // 6. 获取各分类的 API 数量统计
            List<ApiCategory> categories = apiCategoryDao.list();
            List<DashboardVO.CategoryStatVO> categoryStats = categories.stream()
                    .map(category -> DashboardVO.CategoryStatVO.builder()
                            .categoryName(category.getName())
                            .apiCount(apiInfoDao.getApiCountByCategory(category.getId()))
                            .build())
                    .collect(Collectors.toList());

            // 7. 构建返回数据
            DashboardVO dashboardVO = DashboardVO.builder()
                    .totalApiCount(totalApiCount)
                    .successRate(successRate)
                    .onlineUserCount(onlineUserCount)
                    .categoryCount(categoryCount)
                    .totalCallCount(totalCallCount)
                    .successCallCount(successCallCount)
                    .failCallCount(failCallCount)
                    .categoryStats(categoryStats)
                    .build();

            log.info("获取仪表板数据成功: 总API数={}, 成功率={}%, 在线用户数={}, 分类数={}",
                    totalApiCount, successRate, onlineUserCount, categoryCount);

            return dashboardVO;
        } catch (Exception e) {
            log.error("获取仪表板数据失败", e);
            // 返回默认值
            return DashboardVO.builder()
                    .totalApiCount(0L)
                    .successRate(0.0)
                    .onlineUserCount(0L)
                    .categoryCount(0L)
                    .totalCallCount(0L)
                    .successCallCount(0L)
                    .failCallCount(0L)
                    .categoryStats(List.of())
                    .build();
        }
    }
}

