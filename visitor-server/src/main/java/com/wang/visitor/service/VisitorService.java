package com.wang.visitor.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.PasswordUpdateEmailDTO;
import com.wang.pojo.dto.VisitorDeleteDTO;
import com.wang.pojo.dto.VisitorDTO;
import com.wang.pojo.dto.VisitorRegisterDTO;

import java.util.List;


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
    Result getVisitorInfo(Long visitorId);
    
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
    Result updatePassword(Long visitorId, String oldPassword, String newPassword);

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
    Result getNameAndAvatar(Long visitorId);

    /**
     * 访客退出登录
     * @param visitorId 访客ID
     * @return 退出结果
     */
    Result logout(Long visitorId);

    /**
     * 获取访客头像URL（供其他微服务Feign调用）
     * @param visitorId 访客ID
     * @return 头像URL
     */
    Result getVisitorAvatar(Long visitorId);

    /**
     * 批量获取访客头像URL（供其他微服务Feign调用，解决N+1查询问题）
     * @param visitorIds 访客ID列表
     * @return 访客ID与头像URL的映射
     */
    Result batchGetVisitorAvatars(List<Long> visitorIds);
}