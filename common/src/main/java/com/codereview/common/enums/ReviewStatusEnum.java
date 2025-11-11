package com.codereview.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 代码审查状态枚举
 * @author CodeReview
 */
@Getter
@AllArgsConstructor
public enum ReviewStatusEnum {

    PENDING(0, "待审查"),
    REVIEWING(1, "审查中"),
    COMPLETED(2, "已完成"),
    FAILED(3, "审查失败");

    private final Integer code;
    private final String desc;

    public static ReviewStatusEnum getByCode(Integer code) {
        for (ReviewStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
