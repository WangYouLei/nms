package com.wang.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Set;

/**
 * 审核结果VO类
 * 用于返回内容审核结果
 */
@Data
@ApiModel("审核结果VO")
public class AuditResultVO {

    @ApiModelProperty("是否通过审核")
    private Boolean passed;

    @ApiModelProperty("审核结果：1-通过，2-需人工审核，3-拒绝")
    private Integer result;

    @ApiModelProperty("审核结果描述")
    private String resultDesc;

    @ApiModelProperty("检测到的敏感词列表")
    private Set<String> sensitiveWords;

    @ApiModelProperty("最高敏感等级：1-低，2-高")
    private Integer maxLevel;

    @ApiModelProperty("敏感词数量")
    private Integer wordCount;

    /**
     * 创建通过审核的结果
     */
    public static AuditResultVO passed() {
        AuditResultVO vo = new AuditResultVO();
        vo.setPassed(true);
        vo.setResult(1);
        vo.setResultDesc("审核通过");
        vo.setWordCount(0);
        vo.setMaxLevel(0);
        return vo;
    }

    /**
     * 创建需要人工审核的结果
     */
    public static AuditResultVO needReview(Set<String> sensitiveWords, Integer maxLevel) {
        AuditResultVO vo = new AuditResultVO();
        vo.setPassed(true);
        vo.setResult(2);
        vo.setResultDesc("需人工审核");
        vo.setSensitiveWords(sensitiveWords);
        vo.setWordCount(sensitiveWords.size());
        vo.setMaxLevel(maxLevel);
        return vo;
    }

    /**
     * 创建拒绝的结果
     */
    public static AuditResultVO rejected(Set<String> sensitiveWords) {
        AuditResultVO vo = new AuditResultVO();
        vo.setPassed(false);
        vo.setResult(3);
        vo.setResultDesc("内容包含违规敏感词，禁止发布");
        vo.setSensitiveWords(sensitiveWords);
        vo.setWordCount(sensitiveWords.size());
        vo.setMaxLevel(2);
        return vo;
    }
}