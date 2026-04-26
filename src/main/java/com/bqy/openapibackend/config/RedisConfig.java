package com.bqy.openapibackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisConfig {
    @Bean
    public DefaultRedisScript<Long> rateLimitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(
                "local windowKey = KEYS[1]\n" +
                        "local dailyKey = KEYS[2]\n" +
                        "local now = tonumber(ARGV[1])\n" +
                        "local window = tonumber(ARGV[2])\n" +
                        "local qpsLimit = tonumber(ARGV[3])\n" +
                        "local dailyLimit = tonumber(ARGV[4])\n" +
                        "redis.call('ZREMRANGEBYSCORE', windowKey, 0, now - window)\n" +
                        "local count = redis.call('ZCARD', windowKey)\n" +
                        "if count >= qpsLimit then\n" +
                        "    return 0\n" +
                        "end\n" +
                        "local dailyCount = redis.call('INCR', dailyKey)\n" +
                        "if dailyCount == 1 then\n" +
                        "    redis.call('EXPIRE', dailyKey, 86400)\n" +
                        "end\n" +
                        "if dailyCount > dailyLimit then\n" +
                        "    return 0\n" +
                        "end\n" +
                        "redis.call('ZADD', windowKey, now, now)\n" +
                        "redis.call('EXPIRE', windowKey, math.ceil(window / 1000))\n" +
                        "return 1"
        );


        script.setResultType(Long.class);
        return script;
    }
}
