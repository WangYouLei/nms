package com.wang.manager.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.ManagerDTO;

public interface ManagerService {

    /**
     * 管理员登录
     * @param account 账号
     * @param password 密码
     * @return 登录结果
     */
    Result login(String account, String password);

    /**
     * 添加管理员
     * @param managerDTO 管理员信息
     * @return 添加结果
     */
    Result addManager(ManagerDTO managerDTO);

    /**
     * 删除管理员
     * @param id 管理员ID
     * @return 删除结果
     */
    Result deleteManager(Long id);

    /**
     * 修改管理员信息
     * @param managerDTO 管理员信息
     * @return 修改结果
     */
    Result updateManager(ManagerDTO managerDTO);

    /**
     * 根据ID查询管理员
     * @param id 管理员ID
     * @return 管理员信息
     */
    Result getManagerById(Long id);

    /**
     * 修改管理员密码
     * @param id 管理员ID
     * @param newPassword 新密码
     * @return 修改结果
     */
    Result updatePassword(Long id, String newPassword);
}