package com.wang.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 审核结果枚举类
 * 用于表示人工审核的状态和结果
 */
@Getter
public enum AuditResultEnum {
    /**
     * 待审核
     */
    PENDING(0, "待审核"),

    /**
     * 通过
     */
    APPROVED(1, "通过"),

    /**
     * 拒绝
     */
    REJECTED(2, "拒绝");

    /**
     * 结果值
     */
    private final Integer value;

    /**
     * 结果描述
     */
    private final String description;

    /**
     * 静态映射实现O(1)查找
     */
    private static final Map<Integer, AuditResultEnum> VALUE_MAP = new HashMap<>();

    static {
        for (AuditResultEnum result : values()) {
            VALUE_MAP.put(result.value, result);
        }
    }

    AuditResultEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据值获取结果 (O(1)时间复杂度)
     *
     * @param value 结果值
     * @return 对应的结果枚举，如果不匹配则返回null
     */
    public static AuditResultEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        return VALUE_MAP.get(value);
    }

    /**
     * 获取结果描述
     *
     * @param value 结果值
     * @return 结果描述，如果不匹配则返回"未知"
     */
    public static String getDescription(Integer value) {
        AuditResultEnum result = fromValue(value);
        return result != null ? result.description : "未知";
    }

    /**
     * 判断是否待审核
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * 判断是否通过
     */
    public boolean isApproved() {
        return this == APPROVED;
    }

    /**
     * 判断是否拒绝
     */
    public boolean isRejected() {
        return this == REJECTED;
    }

    /**
     * 判断审核是否已完成（通过或拒绝）
     */
    public boolean isCompleted() {
        return this == APPROVED || this == REJECTED;
    }
}