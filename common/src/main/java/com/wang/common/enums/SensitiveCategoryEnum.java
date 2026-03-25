package com.wang.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词类别枚举类
 * 用于区分不同类型的敏感词
 */
@Getter
public enum SensitiveCategoryEnum {
    /**
     * 涉政
     */
    POLITICS(1, "涉政"),

    /**
     * 涉黄
     */
    PORNOGRAPHY(2, "涉黄"),

    /**
     * 涉暴
     */
    VIOLENCE(3, "涉暴"),

    /**
     * 广告
     */
    ADVERTISEMENT(4, "广告"),

    /**
     * 其他
     */
    OTHER(5, "其他");

    /**
     * 类别值
     */
    private final Integer value;

    /**
     * 类别描述
     */
    private final String description;

    /**
     * 静态映射实现O(1)查找
     */
    private static final Map<Integer, SensitiveCategoryEnum> VALUE_MAP = new HashMap<>();

    static {
        for (SensitiveCategoryEnum category : values()) {
            VALUE_MAP.put(category.value, category);
        }
    }

    SensitiveCategoryEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据值获取类别 (O(1)时间复杂度)
     *
     * @param value 类别值
     * @return 对应的类别枚举，如果不匹配则返回null
     */
    public static SensitiveCategoryEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        return VALUE_MAP.get(value);
    }

    /**
     * 获取类别描述
     *
     * @param value 类别值
     * @return 类别描述，如果不匹配则返回"未知"
     */
    public static String getDescription(Integer value) {
        SensitiveCategoryEnum category = fromValue(value);
        return category != null ? category.description : "未知";
    }

    /**
     * 判断是否为涉政
     */
    public boolean isPolitics() {
        return this == POLITICS;
    }

    /**
     * 判断是否为涉黄
     */
    public boolean isPornography() {
        return this == PORNOGRAPHY;
    }

    /**
     * 判断是否为涉暴
     */
    public boolean isViolence() {
        return this == VIOLENCE;
    }

    /**
     * 判断是否为广告
     */
    public boolean isAdvertisement() {
        return this == ADVERTISEMENT;
    }

    /**
     * 判断是否为其他
     */
    public boolean isOther() {
        return this == OTHER;
    }
}