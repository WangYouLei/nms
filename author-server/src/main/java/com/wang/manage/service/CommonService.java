package com.wang.manage.service;

import org.springframework.web.multipart.MultipartFile;

public interface CommonService {
    /**
     * 文件上传
     * @param file 文件
     * @param code 文件类型编码
     * @return 文件访问URL
     */
    String fileUpload(MultipartFile file, Integer code);
    
    /**
     * 删除文件
     * @param fileUrl 文件完整URL
     * @return 是否删除成功
     */
    boolean deleteFile(String fileUrl);
}
