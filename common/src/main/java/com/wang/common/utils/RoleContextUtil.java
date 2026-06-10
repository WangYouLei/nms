package com.wang.common.utils;

import com.wang.common.model.LoginUser;

/**
 * 用户上下文工具类
 * 用于在请求线程中存储和获取当前登录用户信息
 */
public class RoleContextUtil {
    
    private static final ThreadLocal<LoginUser> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 私有构造方法，工具类不能实例化
     */
    private RoleContextUtil() {
        throw new UnsupportedOperationException("工具类不能被实例化");
    }

    /**
     * 设置当前登录用户
     * @param loginUser 登录用户信息
     */
    public static void setCurrentUser(LoginUser loginUser) {
        THREAD_LOCAL.set(loginUser);
    }

    /**
     * 获取当前登录用户
     * @return 当前登录用户，未登录返回 null
     */
    public static LoginUser getCurrentUser() {
        return THREAD_LOCAL.get();
    }

    /**
     * 获取当前登录用户ID
     * @return 用户ID，未登录返回 null
     */
    public static Long getCurrentUserId() {
        LoginUser user = THREAD_LOCAL.get();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前登录用户角色
     * @return 用户角色，未登录返回 null
     */
    public static String getCurrentUserRole() {
        LoginUser user = THREAD_LOCAL.get();
        return user != null && user.getRole() != null ? user.getRole().getCode() : null;
    }

    /**
     * 清除当前登录用户信息
     * 应在请求结束时调用，防止内存泄漏
     */
    public static void clear() {
        THREAD_LOCAL.remove();
    }

    /**
     * 判断当前用户是否已登录
     * @return true 已登录，false 未登录
     */
    public static boolean isLoggedIn() {
        return THREAD_LOCAL.get() != null;
    }
}