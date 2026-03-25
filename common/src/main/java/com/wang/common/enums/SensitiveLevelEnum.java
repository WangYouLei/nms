package com.wang.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 敏感等级枚举类
 * 用于表示敏感词的危险程度
 */
@Getter
public enum SensitiveLevelEnum {
    /**
     * 低（需人工审核）
     */
    LOW(1, "低"),

    /**
     * 高（直接拒绝）
     */
    HIGH(2, "高");

    /**
     * 等级值
     */
    private final Integer value;

    /**
     * 等级描述
     */
    private final String description;

    /**
     * 静态映射实现O(1)查找
     */
    private static final Map<Integer, SensitiveLevelEnum> VALUE_MAP = new HashMap<>();

    static {
        for (SensitiveLevelEnum level : values()) {
            VALUE_MAP.put(level.value, level);
        }
    }

    SensitiveLevelEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据值获取等级 (O(1)时间复杂度)
     *
     * @param value 等级值
     * @return 对应的等级枚举，如果不匹配则返回null
     */
    public static SensitiveLevelEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        return VALUE_MAP.get(value);
    }

    /**
     * 获取等级描述
     *
     * @param value 等级值
     * @return 等级描述，如果不匹配则返回"未知"
     */
    public static String getDescription(Integer value) {
        SensitiveLevelEnum level = fromValue(value);
        return level != null ? level.description : "未知";
    }

    /**
     * 判断是否为低等级
     */
    public boolean isLow() {
        return this == LOW;
    }

    /**
     * 判断是否为高等级
     */
    public boolean isHigh() {
        return this == HIGH;
    }

    /**
     * 判断是否需要人工审核
     */
    public boolean needManualReview() {
        return this == LOW;
    }
}