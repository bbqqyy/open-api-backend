package com.bqy.openapibackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 告警配置类
 * 通过 application.properties 中的 alert.* 前缀配置
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "alert")
public class AlertConfig {

    /**
     * 扫描统计窗口（分钟），默认 30 分钟
     * 定时任务会统计最近 X 分钟内的调用数据来判断是否需要告警
     */
    private int windowMinutes = 30;

    /**
     * 触发 WARNING 级别的失败率阈值（百分比），默认 30%
     */
    private double failRateWarningThreshold = 30.0;

    /**
     * 触发 CRITICAL 级别的失败率阈值（百分比），默认 60%
     */
    private double failRateCriticalThreshold = 60.0;

    /**
     * 触发 WARNING 级别的平均响应时间阈值（ms），默认 2000ms
     */
    private long responseTimeWarningThreshold = 2000L;

    /**
     * 触发 CRITICAL 级别的平均响应时间阈值（ms），默认 5000ms
     */
    private long responseTimeCriticalThreshold = 5000L;

    /**
     * 触发 NO_CALLS 告警所需的最短无调用时长（分钟），默认 60 分钟
     * 即：上线的 API 在过去 X 分钟内没有任何调用，则触发该告警
     */
    private int noCallsWindowMinutes = 60;

    /**
     * 统计窗口内至少需要多少次调用，才进行失败率/响应时间判断（防止样本量过小误报）
     * 默认至少 5 次
     */
    private int minCallsForAlert = 5;

    /**
     * 防重复告警窗口（分钟）：同一 API 同一类型的告警在该时间内不重复触发，默认 60 分钟
     */
    private int deduplicationWindowMinutes = 60;
}

