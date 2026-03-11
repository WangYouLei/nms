package com.wang.manage.controller;

import com.wang.common.result.Result;
import com.wang.manage.service.ManagerService;
import com.wang.pojo.dto.ManagerDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@Api(tags = "管理员管理")
@RequestMapping("/manager")
public class ManagerController {

    private final ManagerService managerServer;

    @Autowired
    public ManagerController(ManagerService managerServer) {
        this.managerServer = managerServer;
    }


    @PostMapping("login")
    @ApiOperation("管理员登录")
    public Result login(@RequestParam String account, @RequestParam String password){
        log.info("管理员登录请求：账号={}", account);
        return managerServer.login(account, password);
    }



    @PostMapping("addManager")
    @ApiOperation("添加管理员")
    public Result addManager(@RequestBody ManagerDTO  manager){
        log.info("添加管理员");
        return managerServer.addManager(manager);
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
    @ApiOperation("修改管理员信息（密码不提供修改）")
    public Result updateManager(@RequestBody ManagerDTO manager){
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
