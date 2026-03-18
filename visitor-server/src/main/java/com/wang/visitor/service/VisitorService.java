package com.wang.visitor.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.VisitorDTO;
import com.wang.pojo.dto.VisitorRegisterDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 访客服务接口
 */
public interface VisitorService {

    /**
     * 访客注册（带验证码）
     * @param registerDTO 注册信息（包含验证码）
     * @return 操作结果
     */
    Result register(VisitorRegisterDTO registerDTO);

    /**
     * 访客注册（无验证码，保留兼容）
     * @param visitorDTO 访客信息
     * @return 操作结果
     */
    Result register(VisitorDTO visitorDTO);

    /**
     * 访客登录
     * @param account 账号
     * @param password 密码
     * @return 登录结果（包含token）
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
     * @param visitorId 访客ID
     * @param name  姓名
     * @return 操作结果
     */
    Result updateVisitor(Integer visitorId, String name);


    /**
     * 修改密码
     * @param visitorId 访客ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 操作结果
     */
    Result updatePassword(Integer visitorId, String oldPassword, String newPassword);
}