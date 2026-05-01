package com.bqy.openapibackend.model.enums;

import lombok.Getter;

/**
 * 告警状态枚举
 */
@Getter
public enum AlertStatusEnum {

    ACTIVE("ACTIVE", "待处理", "告警已触发，等待用户查看处理"),
    ACKNOWLEDGED("ACKNOWLEDGED", "已确认", "用户已确认告警，正在处理"),
    IGNORED("IGNORED", "已忽略", "用户已忽略该告警");

    private final String code;
    private final String label;
    private final String description;

    AlertStatusEnum(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }
}

