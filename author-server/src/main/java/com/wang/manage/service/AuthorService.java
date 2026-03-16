package com.wang.manage.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.AuthorDTO;
import org.springframework.web.multipart.MultipartFile;

public interface AuthorService {
    /**
     * 添加作者
     * @param authorDTO
     */
    Result addAuthor(AuthorDTO authorDTO);
    
    /**
     * 作者登录
     * @param account 账号
     * @param password 密码
     * @return 登录结果
     */
    Result login(String account, String password);
    
/**
     * 删除作者
     * @param id 作者ID
     * @return 删除结果
     */
    Result deleteAuthor(Integer id);
    
    /**
     * 修改作者信息
     * @param author 作者信息
     * @return 修改结果
     */
    Result updateAuthor(AuthorDTO author);
    
    /**
     * 更新作者头像
     * @param authorId 作者ID
     * @param file 头像文件
     * @return 更新结果
     */
    Result updateAvatar(Integer authorId, MultipartFile file);

    /**
     * 修改作者密码
     * @param id 作者ID
     * @param newPassword 新密码
     * @return 修改结果
     */
    Result updatePassword(Integer id, String newPassword);
}