package com.wang.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum FileUploadTypeEnum {
    /**
     * 管理员头像上传
     */
    AUTHOR_AVATAR(0, "管理员头像上传"),

    /**
     * 用户头像上传
     */
    USER_AVATAR(1, "用户头像上传"),

    /**
     * 小说封面
     */
    NOVEL_COVER(2, "小说封面"),

    /**
     * 小说章节
     */
    NOVEL_CHAPTER(3, "小说章节");


    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态描述
     */
    private final String message;




    // 静态映射实现O(1)查找
    private static final Map<Integer, FileUploadTypeEnum> CODE_MAP = new HashMap<>();

    static {
        for (FileUploadTypeEnum type : values()) {
            CODE_MAP.put(type.code, type);
        }
    }

    FileUploadTypeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据状态码获取枚举实例 (O(1)时间复杂度)
     * @param code 状态码
     * @return 对应的枚举实例
     * @throws IllegalArgumentException 如果找不到对应的枚举
     */
    public static FileUploadTypeEnum getMessageByCode(int code) {
        FileUploadTypeEnum type = CODE_MAP.get(code);
        if (type == null) {
            throw new IllegalArgumentException("无效的文件上传类型编码: " + code);
        }
        return type;
    }

}
