package com.wang.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户角色枚举类
 * 用于区分不同类型的用户权限
 */
@Getter
public enum UserRole {
    /**
     * 管理员 - 可以管理所有小说和分类，但不能添加/修改小说和章节
     */
    MANAGER("MANAGER", "管理员"),

    /**
     * 作者 - 只能管理自己的小说和章节
     */
    AUTHOR("AUTHOR", "作者"),

    /**
     * 访客 - 普通用户，只能浏览内容
     */
    VISITOR("VISITOR", "访客");

    /**
     * 角色编码
     */
    private final String code;

    /**
     * 角色描述
     */
    private final String description;

    /**
     * 静态映射实现O(1)查找
     */
    private static final Map<String, UserRole> CODE_MAP = new HashMap<>();

    static {
        for (UserRole role : values()) {
            CODE_MAP.put(role.code.toUpperCase(), role);
        }
    }

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取角色 (O(1)时间复杂度)
     *
     * @param code 角色编码
     * @return 对应的角色枚举，如果不匹配则返回null
     */
    public static UserRole fromCode(String code) {
        UserRole userRole = CODE_MAP.get(code.toUpperCase());
        if (userRole == null) {
            return null;
        }
        return userRole;
    }

    /**
     * 判断是否为管理员
     */
    public boolean isManager() {
        return this == MANAGER;
    }

    /**
     * 判断是否为作者
     */
    public boolean isAuthor() {
        return this == AUTHOR;
    }

    /**
     * 判断是否为访客
     */
    public boolean isVisitor() {
        return this == VISITOR;
    }
}