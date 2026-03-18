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
            @ApiParam(value = "如果是上传小说章节、作者头像、用户头像，就需要上传小说id")
            @RequestParam(required = false)
            Integer novelId,
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


}