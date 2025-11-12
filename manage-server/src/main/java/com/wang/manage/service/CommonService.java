package com.wang.manage.service;

import org.springframework.web.multipart.MultipartFile;

public interface CommonService {
    /**
     * 文件上传
     * @param file
     * @return
     */
    String fileUpload(MultipartFile file,Integer code);
}
