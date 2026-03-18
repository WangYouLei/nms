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
    Result uploadFile(MultipartFile file, Integer code,Integer novelId,String oldFileUrl);

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


}