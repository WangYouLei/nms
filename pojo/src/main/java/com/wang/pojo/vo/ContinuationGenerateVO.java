package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("续写生成响应")
public class ContinuationGenerateVO {

    @ApiModelProperty("续写内容")
    private String continuationText;

    @ApiModelProperty("大纲列表")
    private List<String> outlines;

    @ApiModelProperty("引用的知识项")
    private List<KnowledgeItemRef> usedKnowledge;

    @ApiModelProperty("质量评分 0-100")
    private Double qualityScore;

    @ApiModelProperty("警告信息")
    private List<String> warnings;

    @ApiModelProperty("内容是否合规安全")
    private Boolean contentSafe;

    @ApiModelProperty("内容合规安全评分 0-100，100=完全安全")
    private Double contentRiskScore;

    @ApiModelProperty("风险等级: safe/mild/warning/blocked")
    private String contentRiskLevel;

    @ApiModelProperty("内容合规问题列表")
    private List<String> contentIssues;

    @Data
    public static class KnowledgeItemRef {
        private Long id;
        private String itemType;
        private String name;
        private String summary;
        private Double relevanceScore;
    }
}
