package com.wang.commonserver.controller;

import com.wang.common.result.Result;
import com.wang.commonserver.service.FileService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/file")
@ApiOperation("文件服务")
public class FileController {

    private final FileService fileServer;
    public FileController(FileService fileServer) {
        this.fileServer = fileServer;
    }

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public Result upload(
            @ApiParam(value = "上传文件")
            @RequestParam
            MultipartFile  file,
            @ApiParam(value = "上传文件类型", required = true)
            @RequestParam
            Integer code,
            @ApiParam(value = "如果是上传小说章节，就需要上传小说id")
            @RequestParam(required = false)
            Long novelId,
            @ApiParam(value = "如果是修改操作，需要上传原文件地址")
            @RequestParam(required = false)
            String oldFileUrl
            ) {
        log.info("文件上传，上传类型为：code={}",code);
        return fileServer.uploadFile(file,code,novelId,oldFileUrl);
    }


    @GetMapping("/download")
    @ApiOperation("下载文件")
    public void downloadFile(
            @ApiParam(value = "文件URL", required = true)
            @RequestParam
            String fileUrl,
            HttpServletResponse response
    ) {
        log.info("下载文件，文件地址：{}", fileUrl);
        fileServer.downloadFile(fileUrl, response);
    }

    @GetMapping("/presigned-url")
    @ApiOperation("获取预签名URL（用于前端直接访问私有bucket中的文件）")
    public Result getPresignedUrl(
            @ApiParam(value = "文件URL", required = true)
            @RequestParam
            String fileUrl,
            @ApiParam(value = "过期时间（秒），默认1小时")
            @RequestParam(required = false, defaultValue = "3600")
            Integer expireSeconds
    ) {
        log.info("获取预签名URL，文件地址：{}", fileUrl);
        String presignedUrl = fileServer.getPresignedUrl(fileUrl, expireSeconds);
        if (presignedUrl != null) {
            return Result.success(presignedUrl);
        }
        return Result.error("获取预签名URL失败");
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除文件")
    public Result deleteFile(
            @ApiParam(value = "文件URL", required = true)
            @RequestParam
            String fileUrl){
        log.info("删除文件，文件地址：{}", fileUrl);
        return fileServer.deleteFile(fileUrl) ? Result.success() : Result.error("删除文件失败");
    }

    @GetMapping("/content")
    @ApiOperation("获取文件内容")
    public Result getFileContent(
            @ApiParam(value = "文件URL", required = true)
            @RequestParam
            String fileUrl) {
        log.info("获取文件内容，文件地址：{}", fileUrl);
        String content = fileServer.getFileContent(fileUrl);
        if (content != null) {
            return Result.success(content);
        }
        return Result.error("获取文件内容失败");
    }
}