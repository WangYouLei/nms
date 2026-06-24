package com.wang.common.feign;

import com.wang.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 文件服务 Feign 客户端
 * 注意：uploadFile 方法已移除，因为 Feign 不支持 MultipartFile 传输。
 * 如需上传文件，请使用 @LoadBalanced RestTemplate 调用 common-server 的 /file/upload 接口。
 */
@FeignClient(name = "common-server")
public interface FileServiceFeign {

    @DeleteMapping("/file/delete")
    Result deleteFile(@RequestParam("fileUrl") String fileUrl);

    @GetMapping("/file/presigned-url")
    Result getPresignedUrl(@RequestParam("fileUrl") String fileUrl,
                           @RequestParam(value = "expireSeconds", required = false, defaultValue = "3600") Integer expireSeconds);

    @GetMapping("/file/content")
    Result getFileContent(@RequestParam("fileUrl") String fileUrl);
}
