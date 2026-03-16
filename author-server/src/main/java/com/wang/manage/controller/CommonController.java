package com.wang.manage.controller;

import com.wang.manage.service.CommonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/manager/common")
@RestController
@Slf4j
@Api(tags = "通用接口")
public class CommonController {

    private final CommonService commonService;
    @Autowired
    public CommonController(CommonService commonService) {
        this.commonService = commonService;
    }



    @PostMapping("upload")
    @ApiOperation("文件上传")
    public String upload(
            @ApiParam(value = "上传文件类型", required = true)
            @RequestParam
            Integer code,
            @ApiParam(value = "上传文件", required = true)
            @RequestPart("file") MultipartFile file
    ){
        log.info("文件上传");
        return commonService.fileUpload(file, code);
    }


    //TODO 当管理员头像、小说封面等文件修改时，要删除之前的文件（这个要在managerController等Controller中操作）

    //TODO 当上传的文件大小过大时，要返回一个信息给前端
}
