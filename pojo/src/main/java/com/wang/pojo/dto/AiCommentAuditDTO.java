package com.wang.pojo.dto;

import com.wang.pojo.vo.AuditResultVO;
import lombok.Data;

/**
 * ai评论审核DTO类
 */
@Data
public class AiCommentAuditDTO {
    /**
     * 评论内容
     */
    private String content;

    /**
     * 目标对象id
     */
    private Long aimId;

    /**
     * 目标对象类型
     */
    private Integer aimType;

    /**
     * 本地审核结果
     */
    private AuditResultVO localResult;
}
