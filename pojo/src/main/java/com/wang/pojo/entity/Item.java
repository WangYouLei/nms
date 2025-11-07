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
* 小说物品表
* @TableName item
*/
@Data
public class Item implements Serializable {

    /**
    * 主键ID
    */
    @NotNull(message="[主键ID]不能为空")
    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
    * 物品名称
    */
    @NotBlank(message="[物品名称]不能为空")
    @Size(max= 20,message="编码长度不能超过20")
    @ApiModelProperty("物品名称")
    @Length(max= 20,message="编码长度不能超过20")
    private String name;
    /**
    * 小说ID（关联小说表）
    */
    @NotNull(message="[小说ID（关联小说表）]不能为空")
    @ApiModelProperty("小说ID（关联小说表）")
    private Integer novelId;
    /**
    * 拥有角色ID（关联角色表）
    */
    @ApiModelProperty("拥有角色ID（关联角色表）")
    private Integer characterId;
    /**
    * 物品数量
    */
    @NotNull(message="[物品数量]不能为空")
    @ApiModelProperty("物品数量")
    private Integer quantity;
    /**
    * 重要程度：0-普通,1-稀缺,2-罕见,3-珍品,4-孤品,5-传说,6-未定义
    */
    @NotNull(message="[重要程度：0-普通,1-稀缺,2-罕见,3-珍品,4-孤品,5-传说,6-未定义]不能为空")
    @ApiModelProperty("重要程度：0-普通,1-稀缺,2-罕见,3-珍品,4-孤品,5-传说,6-未定义")
    private Integer importance;
    /**
    * 备注
    */
    @Size(max= 255,message="编码长度不能超过255")
    @ApiModelProperty("备注")
    @Length(max= 255,message="编码长度不能超过255")
    private String remark;

}
