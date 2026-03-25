package com.wang.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 启用状态枚举类
 * 用于表示通用启用/禁用状态
 */
@Getter
public enum EnableStatusEnum {
    /**
     * 禁用
     */
    DISABLED(0, "禁用"),

    /**
     * 启用
     */
    ENABLED(1, "启用");

    /**
     * 状态值
     */
    private final Integer value;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 静态映射实现O(1)查找
     */
    private static final Map<Integer, EnableStatusEnum> VALUE_MAP = new HashMap<>();

    static {
        for (EnableStatusEnum status : values()) {
            VALUE_MAP.put(status.value, status);
        }
    }

    EnableStatusEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据值获取状态 (O(1)时间复杂度)
     *
     * @param value 状态值
     * @return 对应的状态枚举，如果不匹配则返回null
     */
    public static EnableStatusEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        return VALUE_MAP.get(value);
    }

    /**
     * 获取状态描述
     *
     * @param value 状态值
     * @return 状态描述，如果不匹配则返回"未知"
     */
    public static String getDescription(Integer value) {
        EnableStatusEnum status = fromValue(value);
        return status != null ? status.description : "未知";
    }

    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return this == ENABLED;
    }

    /**
     * 判断是否禁用
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }
}