package com.wang.common.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum CharacterCategoryEnum {
    MAIN_CHARACTER(0, "主角"),
    MAIN_PARTNER(1, "主角伙伴"),
    MAIN_PARTNER_OF_PARTNER(2, "主角伴侣"),
    MAIN_FAMILY(3, "主角家人"),
    CHARACTER_MASTER(4, "角色师傅"),
    ENEMY(5, "反派"),
    WALL_HEAD_CRAW(6, "墙头草"),
    YOU_ARE_BOTH_GOOD_AND_EVIL(7, "亦正亦邪"),
    OTHER(8, "其他");
    private final Integer code;
    private final String message;

    private static final Map<Integer, CharacterCategoryEnum> CODE_MAP = new HashMap<>();

    static {
        for (CharacterCategoryEnum type : values()) {
            CODE_MAP.put(type.code, type);
        }
    }

    CharacterCategoryEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据状态码获取枚举实例 (O(1)时间复杂度)
     * @param code 状态码
     * @return 对应的枚举实例
     * @throws IllegalArgumentException 如果找不到对应的枚举
     */
    public static CharacterCategoryEnum getMessageByCode(Integer code) {
        CharacterCategoryEnum type = CODE_MAP.get(code);
        if (type == null) {
            throw new IllegalArgumentException("无效的角色类别编码: " + code);
        }
        return type;
    }
}
