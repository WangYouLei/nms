package com.wang.manage.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.AuthorDTO;
import com.wang.pojo.dto.AuthorRegisterDTO;
import com.wang.pojo.dto.PasswordUpdateEmailDTO;

import java.util.List;


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
     * 获取作者信息
     * @param id 作者ID
     * @return 作者信息
     */
    Result getAuthorInfo(Long id);
    
    /**
     * 逻辑删除作者
     * @param id 作者ID
     * @return 删除结果
     */
    Result deleteAuthor(Long id);
    
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
    Result updatePassword(Long id, String oldPassword,String newPassword);

    /**
     * 修改作者密码（通过邮箱）
     * @param dto 修改密码信息
     * @return
     */
    Result updatePasswordByEmail(PasswordUpdateEmailDTO dto);

    /**
     * 获取作者名称和头像
     * @param id 作者ID
     * @return 名称和头像
     */
    Result getNameAndAvatar(Long id);

    /**
     * 作者退出登录
     * @param authorId 作者ID
     * @return 退出结果
     */
    Result logout(Long authorId);

    /**
     * 获取作者头像URL（供其他微服务Feign调用）
     * @param authorId 作者ID
     * @return 头像URL
     */
    Result getAuthorAvatar(Long authorId);

    /**
     * 批量获取作者头像URL（供其他微服务Feign调用，解决N+1查询问题）
     * @param authorIds 作者ID列表
     * @return 作者ID与头像URL的映射
     */
    Result batchGetAuthorAvatars(List<Long> authorIds);

    /**
     * 获取作者基本信息（供其他微服务Feign调用）
     * @param authorId 作者ID
     * @return 作者基本信息（名称、头像、等级、简介、作品数）
     */
    Result getAuthorBasicInfo(Long authorId);
}