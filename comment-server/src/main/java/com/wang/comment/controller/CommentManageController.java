package com.wang.comment.controller;

import com.wang.common.result.Result;
import com.wang.comment.service.CommentService;
import com.wang.pojo.dto.CommentQueryDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论管理控制器（管理端）
 */
@Slf4j
@RestController
@Api(tags = "评论管理（管理端）")
@RequestMapping("/manager/comment")
public class CommentManageController {

    private final CommentService commentService;

    @Autowired
    public CommentManageController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/page")
    @ApiOperation("分页查询评论列表")
    public Result getCommentPage(@RequestBody CommentQueryDTO queryDTO) {
        log.info("管理端分页查询评论请求：queryDTO={}", queryDTO);
        return commentService.getCommentList(queryDTO);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("获取评论详情")
    public Result getCommentDetail(@PathVariable @ApiParam("评论ID") Long id) {
        log.info("管理端获取评论详情请求：id={}", id);
        return commentService.getCommentById(id);
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除评论")
    public Result deleteComment(@PathVariable @ApiParam("评论ID") Long id) {
        log.info("管理端删除评论请求：id={}", id);
        return commentService.managerDeleteComment(id);
    }

    @PutMapping("/audit/{id}")
    @ApiOperation("审核评论")
    public Result auditComment(
            @PathVariable @ApiParam("评论ID") Long id,
            @RequestParam @ApiParam("审核层级：0-未审核，1-本地过滤通过，2-人工审核通过") Integer auditLevel) {
        log.info("管理端审核评论请求：id={}, auditLevel={}", id, auditLevel);
        return commentService.auditComment(id, auditLevel);
    }

    @DeleteMapping("/batch-delete")
    @ApiOperation("批量删除评论")
    public Result batchDeleteComment(@RequestBody @ApiParam("评论ID列表") List<Long> ids) {
        log.info("管理端批量删除评论请求：count={}", ids.size());
        return commentService.managerBatchDeleteComment(ids);
    }

    @PutMapping("/batch-audit")
    @ApiOperation("批量审核评论")
    public Result batchAuditComment(
            @RequestBody @ApiParam("评论ID列表") List<Long> ids,
            @RequestParam @ApiParam("审核层级：0-未审核，1-本地过滤通过，2-人工审核通过") Integer auditLevel) {
        log.info("管理端批量审核评论请求：count={}, auditLevel={}", ids.size(), auditLevel);
        return commentService.managerBatchAuditComment(ids, auditLevel);
    }
}