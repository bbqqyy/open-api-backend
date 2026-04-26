package com.bqy.openapibackend.util;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisRateLimiter {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private DefaultRedisScript<Long> rateLimitScript;

    /**
     * 限流（滑动窗口 + 每日配额）
     *
     * @param baseKey       业务key
     * @param userId        调用用户id（用来隔离每个用户的使用次数）
     * @param windowSeconds 滑动窗口大小（秒）
     * @param maxQps        QPS限制
     * @param dailyLimit    每日调用上限
     */
    public boolean tryAcquire(String baseKey, Long userId, int windowSeconds, int maxQps, int dailyLimit) {

        long now = System.currentTimeMillis();

        String windowKey = baseKey + ":window";

        // 按天分key
        String date = java.time.LocalDate.now().toString();
        String dailyKey = baseKey + ":daily:" + userId + date;

        Long result = stringRedisTemplate.execute(
                rateLimitScript,
                java.util.Arrays.asList(windowKey, dailyKey),
                String.valueOf(now),
                String.valueOf(windowSeconds * 1000L),
                String.valueOf(maxQps),
                String.valueOf(dailyLimit)
        );

        return result != null && result == 1;
    }
}
