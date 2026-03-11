package com.wang.visitor.service.impl;

import com.wang.common.enums.FileUploadTypeEnum;
import com.wang.visitor.config.MinioInfo;
import com.wang.visitor.service.CommonService;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
public class CommonServiceImpl implements CommonService {


    private final MinioInfo minioInfo;
    private final MinioClient minioClient;

    public CommonServiceImpl(MinioInfo minioInfo, MinioClient minioClient) {
        this.minioInfo = minioInfo;
        this.minioClient = minioClient;
    }

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
        String newFileName = "visitor/"+ typeName +"/"
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
                return minioInfo.getEndpoint() + "/" + minioInfo.getBucketName() + "/" +newFileName;
            }
        } catch (Exception e) {
            log.error("上传文件异常: " + e.getMessage());
        }
        return null;
    }
}
