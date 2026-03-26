package com.wang.manager.controller;

import com.wang.common.result.Result;
import com.wang.manager.service.ManagerService;
import com.wang.pojo.dto.ManagerDTO;
import com.wang.pojo.dto.ManagerQueryDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(tags = "管理员管理")
@RequestMapping("/manager")
public class ManagerController {

    private final ManagerService managerService;

    @Autowired
    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @PostMapping("/login")
    @ApiOperation("管理员登录")
    public Result login(
            @ApiParam("账号") @RequestParam String account,
            @ApiParam("密码") @RequestParam String password) {
        log.info("管理员登录请求：账号={}", account);
        return managerService.login(account, password);
    }

    @PostMapping("/add")
    @ApiOperation("添加管理员")
    public Result addManager(@RequestBody ManagerDTO managerDTO) {
        log.info("添加管理员请求：账号={}", managerDTO.getAccount());
        return managerService.addManager(managerDTO);
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除管理员")
    public Result deleteManager(@PathVariable Integer id) {
        log.info("删除管理员请求：ID={}", id);
        return managerService.deleteManager(id);
    }

    @PutMapping("/update")
    @ApiOperation("修改管理员信息")
    public Result updateManager(@RequestBody ManagerDTO managerDTO) {
        log.info("修改管理员信息请求：ID={}", managerDTO.getId());
        return managerService.updateManager(managerDTO);
    }

    @GetMapping("/list")
    @ApiOperation("多条件查询管理员（支持id、姓名、账号，条件可为空）")
    public Result getManagerList(@RequestBody ManagerQueryDTO queryDTO) {
        log.info("多条件查询管理员请求：queryDTO={}", queryDTO);
        return managerService.getManagerList(queryDTO);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询管理员信息")
    public Result getManagerPage(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("分页查询管理员请求：页码={}, 每页数量={}", pageNum, pageSize);
        return managerService.getManagerPage(pageNum, pageSize);
    }

    @PostMapping("/updatePassword")
    @ApiOperation("修改管理员密码")
    public Result updatePassword(
            @ApiParam("管理员ID") @RequestParam Integer id,
            @ApiParam("新密码") @RequestParam String newPassword) {
        log.info("修改管理员密码请求：ID={}", id);
        return managerService.updatePassword(id, newPassword);
    }

    @PostMapping("/logout")
    @ApiOperation("管理员登出")
    public Result logout() {
        log.info("管理员登出");
        return Result.success("登出成功");
    }

    @GetMapping("/getNameAndAvatar/{id}")
    @ApiOperation("获取管理员名称和头像")
    public Result getNameAndAvatar(@PathVariable Integer id) {
        log.info("获取管理员名称和头像请求：ID={}", id);
        return managerService.getNameAndAvatar(id);
    }
}