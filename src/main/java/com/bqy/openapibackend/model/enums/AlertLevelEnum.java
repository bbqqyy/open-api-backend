package com.bqy.openapibackend.model.enums;

import lombok.Getter;

/**
 * 告警级别枚举
 */
@Getter
public enum AlertLevelEnum {

    WARNING("WARNING", "警告", "指标超出阈值，但尚在可接受范围内，需要关注"),
    CRITICAL("CRITICAL", "严重", "指标严重超出阈值，需要立即处理");

    private final String code;
    private final String label;
    private final String description;

    AlertLevelEnum(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }
}

