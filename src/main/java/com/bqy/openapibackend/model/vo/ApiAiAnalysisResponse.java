package com.bqy.openapibackend.model.vo;

import lombok.Data;

@Data
public class ApiAiAnalysisResponse {

    /**
     * AI给出的优化建议
     */
    private String suggestions;

    /**
     * 优化后的预期效果
     */
    private String expectedEffects;
}
