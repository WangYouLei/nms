package com.wang.manage.service.impl;

import com.wang.common.enums.FileUploadTypeEnum;
import com.wang.manage.config.MinioInfo;
import com.wang.manage.service.CommonService;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
public class CommonServiceImpl implements CommonService {


    @Autowired
    private MinioInfo minioInfo;

    @Autowired
    private MinioClient minioClient;

    /**
     * 文件上传
     *
     * @param file 文件
     * @return 上传结果
     */
    @Override
    public String fileUpload(MultipartFile file,Integer code) {
        //重命名名称   命名规则：manager/传递过来的type/UUID生成随机字符串+文件后缀
        String typeName = FileUploadTypeEnum.getMessageByCode(code).name();
        String newFileName = "manager/"+ typeName +"/"
                + UUID.randomUUID().toString()
                + file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf("."));

        try {
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioInfo.getBucketName())
                    .object(newFileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());

            if(objectWriteResponse != null){
                String url = minioInfo.getEndpoint() + "/" + minioInfo.getBucketName() + "/" +newFileName;
                return url;
            }
        } catch (Exception e) {
            log.error("上传文件异常: " + e.getMessage());
        }
        return null;
    }
}
