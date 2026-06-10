package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("文本润色响应")
public class PolishResultVO {

    @ApiModelProperty("润色后的文本")
    private String polishedText;

    @ApiModelProperty("修改列表")
    private List<PolishChangeItem> changes;

    @ApiModelProperty("修改说明摘要")
    private String summary;

    @ApiModelProperty("内容是否合规安全")
    private Boolean contentSafe;

    @ApiModelProperty("内容合规安全评分 0-100，100=完全安全")
    private Double contentRiskScore;

    @ApiModelProperty("风险等级: safe/mild/warning/blocked")
    private String contentRiskLevel;

    @ApiModelProperty("内容合规问题列表")
    private List<String> contentIssues;

    @Data
    public static class PolishChangeItem {
        private String type;
        private String original;
        private String polished;
        private String description;
    }
}
