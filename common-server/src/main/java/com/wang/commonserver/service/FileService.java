package com.wang.commonserver.service;

import com.wang.common.result.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件存储服务接口
 */
public interface FileService {

    /**
     * 上传文件
     * @param file 文件
     * @param code 文件类型编码
     * @param novelId 小说名(如果上传的是小说章节就需要上传小说id)
     * @param oldFileUrl 旧文件URL(如果是修改操作，就要上传)
     * @return 文件访问URL
     */
    Result uploadFile(MultipartFile file, Integer code,Long novelId,String oldFileUrl);


    /**
     * 获取文件内容为字符串
     * @param fileUrl 文件完整URL
     * @return 文件内容字符串
     */
    String getFileContent(String fileUrl);


    /**
     * 删除文件
     * @param fileUrl 文件完整URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);

    /**
     * 下载文件
     * @param fileUrl 文件完整URL
     * @param response HTTP响应
     */
    void downloadFile(String fileUrl, HttpServletResponse response);

    /**
     * 从URL中提取对象名称
     * @param url 文件URL
     * @return 对象名称
     */
    String extractObjectName(String url);

    /**
     * 构建完整的访问URL
     * @param objectName 存储路径
     * @return 完整URL
     */
    String buildFileUrl(String objectName);

    /**
     * 获取预签名URL（用于前端直接访问私有bucket中的文件）
     * @param fileUrl 文件完整URL
     * @param expireSeconds 过期时间（秒），默认1小时
     * @return 预签名URL
     */
    String getPresignedUrl(String fileUrl, Integer expireSeconds);

}