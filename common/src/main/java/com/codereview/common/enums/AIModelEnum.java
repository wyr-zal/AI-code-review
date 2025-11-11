package com.codereview.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI模型类型枚举
 * @author CodeReview
 */
@Getter
@AllArgsConstructor
public enum AIModelEnum {

    GPT_4("gpt-4", "GPT-4模型"),
    GPT_35_TURBO("gpt-3.5-turbo", "GPT-3.5-Turbo模型"),
    CLAUDE_3("claude-3-opus", "Claude-3模型");

    private final String model;
    private final String desc;

    public static AIModelEnum getByModel(String model) {
        for (AIModelEnum aiModel : values()) {
            if (aiModel.getModel().equals(model)) {
                return aiModel;
            }
        }
        return GPT_35_TURBO;
    }
}
