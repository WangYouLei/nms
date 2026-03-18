package com.wang.manage.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.AuthorDTO;
import com.wang.pojo.dto.AuthorRegisterDTO;
import com.wang.pojo.dto.PasswordUpdateEmailDTO;


public interface AuthorService {
    /**
     * 作者注册（带验证码）
     * @param registerDTO 注册信息（包含验证码）
     * @return 注册结果
     */
    Result register(AuthorRegisterDTO registerDTO);

    
    /**
     * 作者登录
     * @param account 账号
     * @param password 密码
     * @return 登录结果
     */
    Result login(String account, String password);
    
    /**
     * 逻辑删除作者
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
     * 修改作者密码
     * @param id 作者ID
     * @param oldPassword  旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    Result updatePassword(Integer id, String oldPassword,String newPassword);

    /**
     * 修改作者密码（通过邮箱）
     * @param dto 修改密码信息
     * @return
     */
    Result updatePasswordByEmail(PasswordUpdateEmailDTO dto);
}