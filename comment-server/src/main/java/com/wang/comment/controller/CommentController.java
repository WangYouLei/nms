package com.wang.comment.controller;

import com.wang.common.result.Result;
import com.wang.comment.service.CommentService;
import com.wang.pojo.dto.CommentDTO;
import com.wang.pojo.dto.CommentQueryDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@Slf4j
@RestController
@Api(tags = "评论管理")
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/add")
    @ApiOperation("发表评论")
    public Result addComment(@RequestBody CommentDTO commentDTO) {
        log.info("发表评论请求：userId={}, novelId={}", commentDTO.getUserId(), commentDTO.getNovelId());
        return commentService.addComment(commentDTO);
    }

    @DeleteMapping("/delete/{commentId}")
    @ApiOperation("删除评论")
    public Result deleteComment(
            @PathVariable @ApiParam("评论ID") Long commentId,
            @RequestParam @ApiParam("用户ID") Long userId) {
        log.info("删除评论请求：commentId={}, userId={}", commentId, userId);
        return commentService.deleteComment(commentId, userId);
    }

    @PutMapping("/update")
    @ApiOperation("更新评论")
    public Result updateComment(@RequestBody CommentDTO commentDTO) {
        log.info("更新评论请求：commentId={}", commentDTO.getId());
        return commentService.updateComment(commentDTO);
    }

    @GetMapping("/detail/{commentId}")
    @ApiOperation("获取评论详情")
    public Result getCommentById(@PathVariable @ApiParam("评论ID") Long commentId) {
        log.info("获取评论详情请求：commentId={}", commentId);
        return commentService.getCommentById(commentId);
    }

    @PostMapping("/list")
    @ApiOperation("分页查询评论列表")
    public Result getCommentList(@RequestBody CommentQueryDTO queryDTO) {
        log.info("分页查询评论列表请求：queryDTO={}", queryDTO);
        return commentService.getCommentList(queryDTO);
    }

    @GetMapping("/novel/{novelId}")
    @ApiOperation("获取小说的一级评论列表")
    public Result getNovelComments(
            @PathVariable @ApiParam("小说ID") Long novelId,
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer pageNum,
            @RequestParam(defaultValue = "10") @ApiParam("每页数量") Integer pageSize) {
        log.info("获取小说评论列表请求：novelId={}", novelId);
        return commentService.getNovelComments(novelId, pageNum, pageSize);
    }

    @GetMapping("/replies/{rootId}")
    @ApiOperation("获取评论的回复列表")
    public Result getCommentReplies(
            @PathVariable @ApiParam("根评论ID") Long rootId,
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer pageNum,
            @RequestParam(defaultValue = "10") @ApiParam("每页数量") Integer pageSize) {
        log.info("获取评论回复列表请求：rootId={}", rootId);
        return commentService.getCommentReplies(rootId, pageNum, pageSize);
    }

    @GetMapping("/my")
    @ApiOperation("获取我的评论")
    public Result getMyComments(
            @RequestParam @ApiParam("用户ID") Long userId,
            @RequestParam @ApiParam("用户类型：1-访客，2-作者，3-管理员") Integer userType,
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer pageNum,
            @RequestParam(defaultValue = "10") @ApiParam("每页数量") Integer pageSize) {
        log.info("获取我的评论请求：userId={}, userType={}", userId, userType);
        return commentService.getMyComments(userId, userType, pageNum, pageSize);
    }

    @PutMapping("/audit")
    @ApiOperation("审核评论")
    public Result auditComment(
            @RequestParam @ApiParam("评论ID") Long commentId,
            @RequestParam @ApiParam("审核层级：0-未审核，1-本地过滤通过，2-人工审核通过") Integer auditLevel) {
        log.info("审核评论请求：commentId={}, auditLevel={}", commentId, auditLevel);
        return commentService.auditComment(commentId, auditLevel);
    }

    @GetMapping("/tree/{novelId}")
    @ApiOperation("获取小说的评论树（包含所有回复）")
    public Result getNovelCommentTree(
            @PathVariable @ApiParam("小说ID") Long novelId,
            @RequestParam(required = false) @ApiParam("评论对象类型：1-小说，2-章节，不传则查询全部") Integer targetType,
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer pageNum,
            @RequestParam(defaultValue = "10") @ApiParam("每页数量") Integer pageSize) {
        log.info("获取小说评论树请求：novelId={}, targetType={}", novelId, targetType);
        return commentService.getNovelCommentTree(novelId, targetType, pageNum, pageSize);
    }
}