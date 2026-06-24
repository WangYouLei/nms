package com.wang.common.enums;

import lombok.*;

import java.util.Objects;
import java.util.Optional;

/**
 * 业务状态码枚举类
 * 统一管理系统中的所有状态码，便于维护和扩展
 */

@Getter
public enum BizCodeEnum {
    // =================== 通用状态码 ===================
    /**
     * 成功
     */
    SUCCESS(10000, "操作成功"),
    /**
     * 失败
     */
    FAIL(10001, "操作失败"),
    /**
     * 参数错误
     */
    PARAM_INVALID(10002, "参数错误"),
    /**
     * 系统错误
     */
    SYSTEM_ERROR(10003, "系统内部错误"),
    /**
     * 文件上传失败
     */
    FILE_UPLOAD_FAIL(10004, "文件上传失败"),
    /**
     * 资源不存在
     */
    RESOURCE_NOT_FOUND(10005, "请求的资源不存在"),
    /**
     * 权限不足
     */
    PERMISSION_DENIED(10006, "权限不足"),
    /**
     * 数据重复
     */
    DATA_DUPLICATE(10007, "数据已存在"),

    // =================== 用户相关状态码 ===================
    /**
     * 用户不存在
     */
    USER_NOT_FOUND(20001, "用户不存在"),
    /**
     * 用户已存在
     */
    USER_EXIST(20002, "用户名称或账号已存在"),
    /**
     * 账号或密码错误
     */
    USER_ACCOUNT_ERROR(20003, "账号或密码错误"),
    /**
     * 用户未登录
     */
    USER_NOT_LOGIN(20004, "用户未登录"),
    /**
     * 用户登录过期
     */
    USER_LOGIN_EXPIRED(20005, "登录已过期，请重新登录"),
    /**
     * 用户被禁用
     */
    USER_DISABLED(20006, "用户已被禁用"),

    /**
     * 用户账号与邮箱不匹配
     */
    USER_EMAIL_ERROR(20007, "用户账号与邮箱不匹配"),

    // =================== 小说相关状态码 ===================
    /**
     * 小说不存在
     */
    NOVEL_NOT_FOUND(30001, "小说不存在"),
    /**
     * 小说标题已存在
     */
    NOVEL_TITLE_EXIST(30002, "小说标题已存在"),
    /**
     * 小说分类不存在
     */
    NOVEL_CATEGORY_NOT_FOUND(30003, "小说分类不存在"),
    /**
     * 小说分类已存在
     */
    NOVEL_CATEGORY_EXIST(30004, "小说分类已存在"),
    /**
     * 小说章节不存在
     */
    NOVEL_CHAPTER_NOT_FOUND(30005, "小说章节不存在"),
    /**
     * 章节标题已存在
     */
    NOVEL_CHAPTER_TITLE_EXIST(30006, "小说章节标题已存在"),
    /**
     * 保存章节记录失败
     */
    NOVEL_CHAPTER_SAVE_FAIL(30007, "保存章节记录失败"),

    // =================== 访客相关状态码 ===================
    /**
     * 访客不存在
     */
    VISITOR_NOT_FOUND(40001, "访客不存在"),
    /**
     * 访客账号已存在
     */
    VISITOR_ACCOUNT_EXIST(40002, "访客账号已存在"),

    // =================== 访客相关状态码 ===================
    /**
     * 涉及敏感词
     */
    SENSITIVE_WORD(50003, "涉及敏感词"),
    /**
     * 涉及高危敏感次，禁止发布
     */
    HIGH_RISK_SENSITIVE_WORD(50004, "涉及高危敏感次，禁止发布"),
    /**
     * AI审核服务异常
     */
    AI_AUDIT_ERROR(50005, "AI审核服务异常");


    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态描述
     */
    private final String message;

    // 构造函数应接受对应的参数
    private BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据状态码获取对应的枚举
     *
     * @param code 状态码
     * @return 对应的枚举，如果不存在则返回null
     */
    public static BizCodeEnum getByCode(int code) {
        for (BizCodeEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

    /**
     * 安全地根据状态码获取对应的枚举
     *
     * @param code 状态码
     * @return Optional包装的枚举
     */
    public static Optional<BizCodeEnum> findByCode(int code) {
        return Optional.ofNullable(getByCode(code));
    }

    /**
     * 判断是否为成功状态
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return Objects.equals(this, SUCCESS);
    }

    /**
     * 判断是否为失败状态
     *
     * @return 是否失败
     */
    public boolean isFailure() {
        return !isSuccess();
    }
}