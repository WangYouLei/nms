package com.wang.manage.controller;

import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.manage.service.ManagerServer;
import com.wang.pojo.dto.ManagerDTO;
import com.wang.pojo.entity.Manager;
import com.wang.pojo.vo.ManagerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@RestController
@Api(tags = "管理员管理")
@RequestMapping("wang/manager")
public class ManagerController {
    @Autowired
    private ManagerServer managerServer;


    @PostMapping("login")
    @ApiOperation("管理员登录")
    public Result login(@RequestParam String account, @RequestParam String password){
        log.info("管理员登录请求：账号={}", account);
        return managerServer.login(account, password);
    }


    //这个不是注册功能，而是要管理员登入后才有添加功能
    @PostMapping("addManager")
    @ApiOperation("添加管理员")
    public Result addManager(@RequestBody ManagerDTO  manager){
        log.info("添加管理员");
        return managerServer.addManager(manager);
    }

    @PostMapping ("upload")
    @ApiOperation("头像上传")
    public Result upload(
            @ApiParam(value = "上传文件", required = true)
            @RequestPart("file") MultipartFile file
    ){
        log.info("头像上传");
       return managerServer.fileUpload(file);
    }


    @DeleteMapping("delete/{id}")
    @ApiOperation("删除管理员")
    public Result deleteManager(@PathVariable Integer id){
        log.info("删除管理员请求：ID={}", id);
        return managerServer.deleteManager(id);
    }
    

    @GetMapping("list")
    @ApiOperation("分页查询管理员列表")
    public Result getManagerList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize){
        log.info("分页查询管理员列表请求：页码={}, 每页数量={}", pageNum, pageSize);
        return managerServer.getManagerList(pageNum, pageSize);
    }
    

    @PutMapping("update")
    @ApiOperation("修改管理员信息")
    public Result updateManager(@RequestBody Manager manager){
        log.info("修改管理员信息请求：ID={}", manager.getId());
        return managerServer.updateManager(manager);
    }
    

    @GetMapping("/query")
    @ApiOperation("多条件查询管理员")
    public Result queryManagers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String account) {
        log.info("接收到多条件查询管理员请求，名称: " + name + "，账号: " + account);
        return managerServer.queryManagers(name, account);
    }
}
