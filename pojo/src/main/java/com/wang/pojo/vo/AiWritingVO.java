package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("AI写作助手响应VO")
public class AiWritingVO {

    @ApiModelProperty("功能类型：1-续写建议，2-章节摘要，3-角色一致性检查，4-标题/简介优化")
    private Integer type;

    @ApiModelProperty("AI生成结果")
    private String result;

    @ApiModelProperty("备选标题列表（仅type=4时返回）")
    private List<String> titleOptions;

    @ApiModelProperty("优化后的简介（仅type=4时返回）")
    private String optimizedIntroduction;

    public static AiWritingVO of(Integer type, String result) {
        AiWritingVO vo = new AiWritingVO();
        vo.setType(type);
        vo.setResult(result);
        return vo;
    }

    public static AiWritingVO ofTitleAndIntro(List<String> titleOptions, String optimizedIntroduction) {
        AiWritingVO vo = new AiWritingVO();
        vo.setType(4);
        vo.setResult("标题和简介优化完成");
        vo.setTitleOptions(titleOptions);
        vo.setOptimizedIntroduction(optimizedIntroduction);
        return vo;
    }
}
