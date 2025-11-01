package com.wang.pojo.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
* 小说势力表
* @TableName faction
*/
@Data
public class Faction implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    private Integer id;
    /**
    * 势力名称
    */
    @NotBlank(message="[势力名称]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("势力名称")
    @Length(max= 20,message="编码长度不能超过20")
    private String name;
    /**
    * 小说ID（关联小说表）
    */
    @NotNull(message="[小说ID（关联小说表）]不能为空")
    @ApiModelProperty("小说ID（关联小说表）")
    private Integer novelId;

}
