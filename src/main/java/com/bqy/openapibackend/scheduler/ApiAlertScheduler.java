package com.bqy.openapibackend.scheduler;

import com.bqy.openapibackend.config.AlertConfig;
import com.bqy.openapibackend.dao.ApiAlertDao;
import com.bqy.openapibackend.dao.ApiCallLogDao;
import com.bqy.openapibackend.dao.ApiInfoDao;
import com.bqy.openapibackend.model.entity.ApiAlert;
import com.bqy.openapibackend.model.entity.ApiCallLog;
import com.bqy.openapibackend.model.entity.ApiInfo;
import com.bqy.openapibackend.model.enums.AlertLevelEnum;
import com.bqy.openapibackend.model.enums.AlertStatusEnum;
import com.bqy.openapibackend.model.enums.AlertTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

/**
 * API 告警定时扫描器
 *
 * <p>每隔一段时间（默认 5 分钟）扫描所有上线的 API，
 * 计算其在统计窗口内的调用数据，若触发阈值则写入告警记录。</p>
 *
 * <p>支持三类告警：
 * <ul>
 *   <li>HIGH_FAIL_RATE：失败率超过阈值</li>
 *   <li>HIGH_RESPONSE_TIME：平均响应时间超过阈值</li>
 *   <li>NO_CALLS：上线 API 长时间无调用</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class ApiAlertScheduler {

    @Resource
    private ApiCallLogDao apiCallLogDao;

    @Resource
    private ApiInfoDao apiInfoDao;

    @Resource
    private ApiAlertDao apiAlertDao;

    @Resource
    private AlertConfig alertConfig;

    /**
     * 定时扫描任务，默认每 5 分钟执行一次。
     * cron 表达式可通过 alert.scheduler-cron 配置项覆盖。
     */
    @Scheduled(cron = "${alert.scheduler-cron:0 */5 * * * *}")
    public void scanAndGenerateAlerts() {
        log.info("[告警扫描] 开始执行 API 告警扫描任务 ...");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(alertConfig.getWindowMinutes());
        LocalDateTime deduplicationSince = now.minusMinutes(alertConfig.getDeduplicationWindowMinutes());

        // 获取所有上线中的 API（is_online = 1）
        List<ApiInfo> onlineApis = apiInfoDao.lambdaQuery()
                .eq(ApiInfo::getIsOnline, 1)
                .list();

        if (onlineApis.isEmpty()) {
            log.info("[告警扫描] 暂无上线 API，跳过扫描");
            return;
        }

        int newAlertCount = 0;
        for (ApiInfo api : onlineApis) {
            try {
                newAlertCount += checkApi(api, windowStart, now, deduplicationSince);
            } catch (Exception e) {
                log.error("[告警扫描] 扫描 API[{}] 时发生异常: {}", api.getId(), e.getMessage(), e);
            }
        }

        log.info("[告警扫描] 扫描完成，共发现 {} 个新告警，扫描 {} 个上线 API", newAlertCount, onlineApis.size());
    }

    /**
     * 检查单个 API，返回本次产生的告警数量
     */
    private int checkApi(ApiInfo api, LocalDateTime windowStart, LocalDateTime now,
                         LocalDateTime deduplicationSince) {
        Long apiId = api.getId();
        int count = 0;

        // 获取统计窗口内的所有调用日志
        List<ApiCallLog> logs = apiCallLogDao.lambdaQuery()
                .eq(ApiCallLog::getApiId, apiId)
                .ge(ApiCallLog::getCallTime, windowStart)
                .le(ApiCallLog::getCallTime, now)
                .list();

        int totalCalls = logs.size();

        // ── 检查 1：长时间无调用 ──────────────────────────────────
        // 使用更长的 no_calls 窗口
        LocalDateTime noCallsWindowStart = now.minusMinutes(alertConfig.getNoCallsWindowMinutes());
        long callsInNoCallsWindow = apiCallLogDao.lambdaQuery()
                .eq(ApiCallLog::getApiId, apiId)
                .ge(ApiCallLog::getCallTime, noCallsWindowStart)
                .le(ApiCallLog::getCallTime, now)
                .count();

        if (callsInNoCallsWindow == 0) {
            boolean alreadyAlerted = apiAlertDao.existsActiveAlert(
                    apiId, AlertTypeEnum.NO_CALLS.getCode(), deduplicationSince);
            if (!alreadyAlerted) {
                String message = String.format(
                        "API「%s」已上线，但在过去 %d 分钟内没有任何调用记录，请检查 API 可用性或推广情况。",
                        api.getApiName(), alertConfig.getNoCallsWindowMinutes());
                saveAlert(api, AlertTypeEnum.NO_CALLS, AlertLevelEnum.WARNING,
                        message, null, null, 0, now);
                count++;
            }
        }

        // 以下检查需要足够的样本量
        if (totalCalls < alertConfig.getMinCallsForAlert()) {
            return count;
        }

        // ── 检查 2：高失败率 ─────────────────────────────────────
        long failCount = logs.stream()
                .filter(l -> "fail".equalsIgnoreCase(l.getStatus()))
                .count();
        double failRate = (double) failCount / totalCalls * 100.0;

        if (failRate >= alertConfig.getFailRateCriticalThreshold()) {
            // CRITICAL 级别
            boolean alreadyAlerted = apiAlertDao.existsActiveAlert(
                    apiId, AlertTypeEnum.HIGH_FAIL_RATE.getCode(), deduplicationSince);
            if (!alreadyAlerted) {
                String message = String.format(
                        "API「%s」在过去 %d 分钟内调用失败率达 %.1f%%（共 %d 次调用，%d 次失败），已超过严重阈值 %.0f%%，请立即排查！",
                        api.getApiName(), alertConfig.getWindowMinutes(), failRate,
                        totalCalls, failCount, alertConfig.getFailRateCriticalThreshold());
                saveAlert(api, AlertTypeEnum.HIGH_FAIL_RATE, AlertLevelEnum.CRITICAL,
                        message, BigDecimal.valueOf(failRate).setScale(2, RoundingMode.HALF_UP),
                        null, totalCalls, now);
                count++;
            }
        } else if (failRate >= alertConfig.getFailRateWarningThreshold()) {
            // WARNING 级别
            boolean alreadyAlerted = apiAlertDao.existsActiveAlert(
                    apiId, AlertTypeEnum.HIGH_FAIL_RATE.getCode(), deduplicationSince);
            if (!alreadyAlerted) {
                String message = String.format(
                        "API「%s」在过去 %d 分钟内调用失败率达 %.1f%%（共 %d 次调用，%d 次失败），超过警告阈值 %.0f%%，请关注。",
                        api.getApiName(), alertConfig.getWindowMinutes(), failRate,
                        totalCalls, failCount, alertConfig.getFailRateWarningThreshold());
                saveAlert(api, AlertTypeEnum.HIGH_FAIL_RATE, AlertLevelEnum.WARNING,
                        message, BigDecimal.valueOf(failRate).setScale(2, RoundingMode.HALF_UP),
                        null, totalCalls, now);
                count++;
            }
        }

        // ── 检查 3：高响应时间 ───────────────────────────────────
        OptionalDouble avgOptional = logs.stream()
                .filter(l -> l.getResponseTime() != null && l.getResponseTime() > 0)
                .mapToLong(ApiCallLog::getResponseTime)
                .average();

        if (avgOptional.isPresent()) {
            double avgResponseTime = avgOptional.getAsDouble();

            if (avgResponseTime >= alertConfig.getResponseTimeCriticalThreshold()) {
                boolean alreadyAlerted = apiAlertDao.existsActiveAlert(
                        apiId, AlertTypeEnum.HIGH_RESPONSE_TIME.getCode(), deduplicationSince);
                if (!alreadyAlerted) {
                    String message = String.format(
                            "API「%s」在过去 %d 分钟内平均响应时间为 %.0f ms，已超过严重阈值 %d ms，接口性能严重下降，请立即排查！",
                            api.getApiName(), alertConfig.getWindowMinutes(), avgResponseTime,
                            alertConfig.getResponseTimeCriticalThreshold());
                    saveAlert(api, AlertTypeEnum.HIGH_RESPONSE_TIME, AlertLevelEnum.CRITICAL,
                            message, null,
                            BigDecimal.valueOf(avgResponseTime).setScale(2, RoundingMode.HALF_UP),
                            totalCalls, now);
                    count++;
                }
            } else if (avgResponseTime >= alertConfig.getResponseTimeWarningThreshold()) {
                boolean alreadyAlerted = apiAlertDao.existsActiveAlert(
                        apiId, AlertTypeEnum.HIGH_RESPONSE_TIME.getCode(), deduplicationSince);
                if (!alreadyAlerted) {
                    String message = String.format(
                            "API「%s」在过去 %d 分钟内平均响应时间为 %.0f ms，超过警告阈值 %d ms，请关注接口性能。",
                            api.getApiName(), alertConfig.getWindowMinutes(), avgResponseTime,
                            alertConfig.getResponseTimeWarningThreshold());
                    saveAlert(api, AlertTypeEnum.HIGH_RESPONSE_TIME, AlertLevelEnum.WARNING,
                            message, null,
                            BigDecimal.valueOf(avgResponseTime).setScale(2, RoundingMode.HALF_UP),
                            totalCalls, now);
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * 构建并保存告警记录
     */
    private void saveAlert(ApiInfo api, AlertTypeEnum type, AlertLevelEnum level,
                           String message, BigDecimal failRate, BigDecimal avgResponseTime,
                           int totalCalls, LocalDateTime now) {
        ApiAlert alert = ApiAlert.builder()
                .apiId(api.getId())
                .apiName(api.getApiName())
                .ownerId(api.getUserId())
                .alertType(type.getCode())
                .alertLevel(level.getCode())
                .alertMessage(message)
                .failRate(failRate)
                .avgResponseTime(avgResponseTime)
                .totalCalls(totalCalls)
                .status(AlertStatusEnum.ACTIVE.getCode())
                .createdAt(now)
                .updatedAt(now)
                .build();

        apiAlertDao.save(alert);
        log.warn("[告警扫描] 新增告警 => API[{}]「{}」类型[{}] 级别[{}]: {}",
                api.getId(), api.getApiName(), type.getLabel(), level.getLabel(), message);
    }
}

