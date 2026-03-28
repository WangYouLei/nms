package com.wang.novel.feign;

import com.wang.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "common-server")
public interface FileServiceFeign {

    @PostMapping("/file/upload")
    Result uploadFile(@RequestParam("file") MultipartFile file,
                      @RequestParam("code") Integer code,
                      @RequestParam(value = "novelId", required = false) Integer novelId,
                      @RequestParam(value = "oldFileUrl", required = false) String oldFileUrl);

    @DeleteMapping("/file/delete")
    Result deleteFile(@RequestParam("fileUrl") String fileUrl);

    @GetMapping("/file/presigned-url")
    Result getPresignedUrl(@RequestParam("fileUrl") String fileUrl,
                           @RequestParam(value = "expireSeconds", required = false, defaultValue = "3600") Integer expireSeconds);

    @GetMapping("/file/content")
    Result getFileContent(@RequestParam("fileUrl") String fileUrl);
}
