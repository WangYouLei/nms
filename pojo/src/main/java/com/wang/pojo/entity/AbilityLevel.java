package com.wang.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 小说能力等级表
* @TableName ability_level
*/
@Data
public class AbilityLevel implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
    * 能力级别
    */
    @NotBlank(message="[能力级别]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("能力级别")
    @Length(max= 20,message="编码长度不能超过20")
    private String ability;
    /**
    * 小说ID（关联小说表）
    */
    @NotNull(message="[小说ID（关联小说表）]不能为空")
    @ApiModelProperty("小说ID（关联小说表）")
    private String  abilityLevel;
    /**
    * 备注
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("备注")
    @Length(max= 255,message="编码长度不能超过255")
    private String remark;

}
