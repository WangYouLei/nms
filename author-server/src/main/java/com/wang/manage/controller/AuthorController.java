package com.wang.manage.controller;

import com.wang.common.result.Result;
import com.wang.manage.service.AuthorService;
import com.wang.pojo.dto.AuthorDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

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



    @PostMapping("addAuthor")
    @ApiOperation("添加作者")
    public Result addAuthor(@RequestBody AuthorDTO author){
        log.info("添加作者");
        return authorService.addAuthor(author);
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


    @PostMapping("/avatar/{id}")
    @ApiOperation("更新作者头像")
    public Result updateAvatar(
            @PathVariable Integer id,
            @RequestPart("file") MultipartFile file) {
        log.info("更新作者头像请求：ID={}", id);
        return authorService.updateAvatar(id, file);
    }

    //TODO 如果用户忘记密码，添加邮箱验证码修改密码功能
    @PostMapping("/updatePassword")
    @ApiOperation("修改作者密码")
    public Result updatePassword(
            @ApiParam("作者ID") @RequestParam Integer id,
            @ApiParam("新密码") @RequestParam String newPassword) {
        log.info("修改作者密码请求：ID={}", id);
        return authorService.updatePassword(id, newPassword);
    }



}