package com.wang.commonserver.controller;

import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.FileUploadTypeEnum;
import com.wang.common.enums.UserRole;
import com.wang.common.feign.NovelServiceFeign;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.common.utils.RoleContextUtil;
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
    private final NovelServiceFeign novelServiceFeign;
    public FileController(FileService fileServer, NovelServiceFeign novelServiceFeign) {
        this.fileServer = fileServer;
        this.novelServiceFeign = novelServiceFeign;
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
        Result denied = checkFileAccess(fileUrl);
        if (denied != null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
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
        Result denied = checkFileAccess(fileUrl);
        if (denied != null) return denied;
        return fileServer.deleteFile(fileUrl) ? Result.success() : Result.error("删除文件失败");
    }

    @GetMapping("/content")
    @ApiOperation("获取文件内容")
    public Result getFileContent(
            @ApiParam(value = "文件URL", required = true)
            @RequestParam
            String fileUrl) {
        log.info("获取文件内容，文件地址：{}", fileUrl);
        Result denied = checkFileAccess(fileUrl);
        if (denied != null) return denied;
        String content = fileServer.getFileContent(fileUrl);
        if (content != null) {
            return Result.success(content);
        }
        return Result.error("获取文件内容失败");
    }

    /**
     * 校验当前登录用户对目标文件的访问权限，防止越权读取/删除他人文件：
     * MANAGER：允许访问所有文件
     * AUTHOR：允许访问作者头像目录，以及自己名下小说的封面/章节文件（调用小说服务校验归属）
     * VISITOR：仅允许访问访客头像目录
     * @return 校验通过返回null，否则返回拒绝结果
     */
    private Result checkFileAccess(String fileUrl) {
        LoginUser current = RoleContextUtil.getCurrentUser();
        if (current == null) {
            return Result.buildResult(BizCodeEnum.USER_NOT_LOGIN);
        }
        String role = RoleContextUtil.getCurrentUserRole();
        if (UserRole.MANAGER.getCode().equals(role)) {
            return null;
        }

        String objectName = fileServer.extractObjectName(fileUrl);

        // 各角色允许访问与自己角色对应的头像目录
        String avatarPrefix = UserRole.AUTHOR.getCode().equals(role)
                ? FileUploadTypeEnum.AUTHOR_AVATAR.name() + "/"
                : UserRole.VISITOR.getCode().equals(role)
                ? FileUploadTypeEnum.VISITOR_AVATAR.name() + "/"
                : null;
        if (avatarPrefix != null && objectName.startsWith(avatarPrefix)) {
            return null;
        }

        // 作者允许访问自己名下小说的封面/章节文件（对象名格式：类型/novelId/uuid.ext）
        if (UserRole.AUTHOR.getCode().equals(role)
                && (objectName.startsWith(FileUploadTypeEnum.NOVEL_COVER.name() + "/")
                || objectName.startsWith(FileUploadTypeEnum.NOVEL_CHAPTER.name() + "/"))) {
            Long novelId = extractNovelId(objectName);
            if (novelId != null) {
                Result authorIdResult = novelServiceFeign.getNovelAuthorId(novelId);
                if (authorIdResult != null
                        && Integer.valueOf(BizCodeEnum.SUCCESS.getCode()).equals(authorIdResult.getCode())
                        && authorIdResult.getData() != null
                        && current.getId().equals(Long.valueOf(String.valueOf(authorIdResult.getData())))) {
                    return null;
                }
            }
        }

        log.warn("文件访问被拒绝：role={}, userId={}, objectName={}", role, current.getId(), objectName);
        return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
    }

    /**
     * 从对象名中提取小说ID（格式：类型/novelId/uuid.ext）
     */
    private Long extractNovelId(String objectName) {
        String[] parts = objectName.split("/");
        if (parts.length >= 2) {
            try {
                return Long.parseLong(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
