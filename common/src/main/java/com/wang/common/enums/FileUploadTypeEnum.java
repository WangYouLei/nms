package com.wang.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum FileUploadTypeEnum {
    /**
     * 作者头像上传
     */
    AUTHOR_AVATAR(0, "作者头像上传"),

    /**
     * 访客头像上传
     */
    VISITOR_AVATAR(1, "访客头像上传"),

    /**
     * 管理员头像上传
     */
    MANAGER_AVATAR(2, "管理员头像上传"),

    /**
     * 小说封面
     */
    NOVEL_COVER(3, "小说封面"),

    /**
     * 小说章节
     */
    NOVEL_CHAPTER(4, "小说章节");


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
     */
    public static FileUploadTypeEnum getMessageByCode(int code) {
        FileUploadTypeEnum type = CODE_MAP.get(code);
        if (type == null) {
            return null;
        }
        return type;
    }

}
