package com.wang.pojo.dto;

import lombok.Data;

@Data
public class AbilityLevelDTO {

    /**
     * 能力级别
     */
    private String ability;
    /**
     * 小说ID（关联小说表）
     */
    private Integer novelId;
    /**
     * 备注
     */
    private String remark;

}
