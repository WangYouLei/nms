package com.wang.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 审核目标类型枚举类
 * 用于区分不同类型的审核对象
 */
@Getter
public enum AuditAimTypeEnum {
    /**
     * 评论
     */
    COMMENT(1, "评论"),

    /**
     * 小说
     */
    NOVEL(2, "小说"),

    /**
     * 章节
     */
    CHAPTER(3, "章节");

    /**
     * 类型值
     */
    private final Integer value;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 静态映射实现O(1)查找
     */
    private static final Map<Integer, AuditAimTypeEnum> VALUE_MAP = new HashMap<>();

    static {
        for (AuditAimTypeEnum type : values()) {
            VALUE_MAP.put(type.value, type);
        }
    }

    AuditAimTypeEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据值获取类型 (O(1)时间复杂度)
     *
     * @param value 类型值
     * @return 对应的类型枚举，如果不匹配则返回null
     */
    public static AuditAimTypeEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        return VALUE_MAP.get(value);
    }

    /**
     * 获取类型描述
     *
     * @param value 类型值
     * @return 类型描述，如果不匹配则返回"未知"
     */
    public static String getDescription(Integer value) {
        AuditAimTypeEnum type = fromValue(value);
        return type != null ? type.description : "未知";
    }

    /**
     * 判断是否为评论
     */
    public boolean isComment() {
        return this == COMMENT;
    }

    /**
     * 判断是否为小说
     */
    public boolean isNovel() {
        return this == NOVEL;
    }

    /**
     * 判断是否为章节
     */
    public boolean isChapter() {
        return this == CHAPTER;
    }
}