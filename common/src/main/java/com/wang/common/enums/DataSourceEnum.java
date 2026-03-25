package com.wang.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据来源枚举类
 * 用于表示数据的创建来源
 */
@Getter
public enum DataSourceEnum {
    /**
     * 系统内置
     */
    SYSTEM(1, "系统内置"),

    /**
     * 管理员添加
     */
    MANAGER(2, "管理员添加");

    /**
     * 来源值
     */
    private final Integer value;

    /**
     * 来源描述
     */
    private final String description;

    /**
     * 静态映射实现O(1)查找
     */
    private static final Map<Integer, DataSourceEnum> VALUE_MAP = new HashMap<>();

    static {
        for (DataSourceEnum source : values()) {
            VALUE_MAP.put(source.value, source);
        }
    }

    DataSourceEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据值获取来源 (O(1)时间复杂度)
     *
     * @param value 来源值
     * @return 对应的来源枚举，如果不匹配则返回null
     */
    public static DataSourceEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        return VALUE_MAP.get(value);
    }

    /**
     * 获取来源描述
     *
     * @param value 来源值
     * @return 来源描述，如果不匹配则返回"未知"
     */
    public static String getDescription(Integer value) {
        DataSourceEnum source = fromValue(value);
        return source != null ? source.description : "未知";
    }

    /**
     * 判断是否为系统内置
     */
    public boolean isSystem() {
        return this == SYSTEM;
    }

    /**
     * 判断是否为管理员添加
     */
    public boolean isManager() {
        return this == MANAGER;
    }
}