package com.wang.manage.controller;

import com.wang.common.result.Result;
import com.wang.manage.service.AuthorService;
import com.wang.pojo.dto.AuthorDTO;
import com.wang.pojo.dto.AuthorRegisterDTO;
import com.wang.pojo.dto.PasswordUpdateEmailDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Api(tags = "作者管理")
@RequestMapping("/author")
public class AuthorController {

    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }


    @PostMapping("login")
    @ApiOperation("作者登录")
    public Result login(@RequestParam String account, @RequestParam String password){
        log.info("作者登录请求：账号={}", account);
        return authorService.login(account, password);
    }


    @PostMapping("register")
    @ApiOperation("作者注册（带验证码）")
    public Result register(@RequestBody AuthorRegisterDTO registerDTO){
        log.info("作者注册请求：账号={}", registerDTO.getAccount());
        return authorService.register(registerDTO);
    }


    @DeleteMapping("delete/{id}")
    @ApiOperation("作者注销（逻辑删除）")
    public Result deleteAuthor(@PathVariable Integer id){
        log.info("作者注销请求：ID={}", id);
        return authorService.deleteAuthor(id);
    }


    @PutMapping("update")
    @ApiOperation("修改作者信息（密码这里不提供修改）")
    public Result updateAuthor(@RequestBody AuthorDTO author){
        log.info("修改作者信息请求：ID={}", author.getId());
        return authorService.updateAuthor(author);
    }


    @PostMapping("/updatePassword")
    @ApiOperation("修改作者密码")
    public Result updatePassword(
            @ApiParam("作者ID") @RequestParam Integer id,
            @ApiParam("旧密码") @RequestParam String oldPassword,
            @ApiParam("新密码") @RequestParam String newPassword) {
        log.info("修改作者密码请求：ID={}", id);
        return authorService.updatePassword(id, oldPassword,newPassword);
    }

    @PostMapping("/updatePasswordByEmail")
    @ApiOperation("通过邮箱短信验证码修改密码")
    public Result updatePasswordByEmail(@RequestBody @ApiParam("密码修改邮箱类") PasswordUpdateEmailDTO dto) {
        log.info("通过邮箱短信验证码修改密码请求：ID={}", dto.getId());
        return authorService.updatePasswordByEmail(dto);
    }
}