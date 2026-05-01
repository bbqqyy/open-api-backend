package com.bqy.openapibackend.model.enums;

import lombok.Getter;

/**
 * 告警类型枚举
 */
@Getter
public enum AlertTypeEnum {

    HIGH_FAIL_RATE("HIGH_FAIL_RATE", "高失败率",
            "API 调用失败率超过阈值，说明接口存在稳定性问题"),

    HIGH_RESPONSE_TIME("HIGH_RESPONSE_TIME", "高响应时间",
            "API 平均响应时间超过阈值，说明接口存在性能问题"),

    NO_CALLS("NO_CALLS", "长时间无调用",
            "已上线的 API 在统计周期内无任何调用记录");

    private final String code;
    private final String label;
    private final String description;

    AlertTypeEnum(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }
}

