package com.wang.visitor.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.PasswordUpdateEmailDTO;
import com.wang.pojo.dto.VisitorDeleteDTO;
import com.wang.pojo.dto.VisitorDTO;
import com.wang.pojo.dto.VisitorRegisterDTO;


public interface VisitorService {
    /**
     * 访客注册（带验证码）
     * @param registerDTO 注册信息（包含验证码）
     * @return 注册结果
     */
    Result register(VisitorRegisterDTO registerDTO);

    
    /**
     * 访客登录
     * @param account 账号
     * @param password 密码
     * @return 登录结果
     */
    Result login(String account, String password);
    
    /**
     * 获取访客信息
     * @param visitorId 访客ID
     * @return 访客信息
     */
    Result getVisitorInfo(Integer visitorId);
    
    /**
     * 修改访客信息
     * @param visitor 访客信息
     * @return 修改结果
     */
    Result updateVisitor(VisitorDTO visitor);


    /**
     * 修改访客密码
     * @param visitorId 访客ID
     * @param oldPassword  旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    Result updatePassword(Integer visitorId, String oldPassword, String newPassword);

    /**
     * 修改访客密码（通过邮箱）
     * @param dto 修改密码信息
     * @return
     */
    Result updatePasswordByEmail(PasswordUpdateEmailDTO dto);

    /**
     * 删除访客账号（通过邮箱验证码验证，真删除）
     * @param dto 删除信息
     * @return 删除结果
     */
    Result deleteVisitor(VisitorDeleteDTO dto);

    /**
     * 获取访客名称和头像
     * @param visitorId 访客ID
     * @return 名称和头像
     */
    Result getNameAndAvatar(Integer visitorId);
}