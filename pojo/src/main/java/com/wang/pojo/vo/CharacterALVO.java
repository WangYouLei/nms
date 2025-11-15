package com.wang.pojo.vo;

import lombok.Data;

@Data
public class CharacterALVO {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 能力名称
     */
    private String ability;

    /**
     * 能力级别ID（关联能力等级表）
     */
    private String abilityLevel;//这里就将数据转为字符串，省前端转换
    // 端再转换

    /**
     * 备注
     */
    private String remark;
}
