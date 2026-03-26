package com.wang.manager.service;

import com.wang.common.result.Result;
import com.wang.pojo.dto.ManagerDTO;
import com.wang.pojo.dto.ManagerQueryDTO;

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
    Result deleteManager(Integer id);

    /**
     * 修改管理员信息
     * @param managerDTO 管理员信息
     * @return 修改结果
     */
    Result updateManager(ManagerDTO managerDTO);

    /**
     * 多条件查询管理员（支持id、姓名、账号，条件可为空）
     * @param queryDTO 查询条件
     * @return 管理员信息列表
     */
    Result getManagerList(ManagerQueryDTO queryDTO);

    /**
     * 分页查询管理员信息
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Result getManagerPage(Integer pageNum, Integer pageSize);

    /**
     * 修改管理员密码
     * @param id 管理员ID
     * @param newPassword 新密码
     * @return 修改结果
     */
    Result updatePassword(Integer id, String newPassword);

    /**
     * 获取管理员名称和头像
     * @param id 管理员ID
     * @return 名称和头像
     */
    Result getNameAndAvatar(Integer id);
}