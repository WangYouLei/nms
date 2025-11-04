package com.wang.manage.service;

import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.pojo.dto.ManagerDTO;
import com.wang.pojo.vo.ManagerVO;

public interface ManagerServer {
    /**
     * 添加管理员
     * @param managerDTO
     */
    Result addManager(ManagerDTO managerDTO);
    
    /**
     * 管理员登录
     * @param account 账号
     * @param password 密码
     * @return 登录结果
     */
    Result login(String account, String password);
    
    /**
     * 删除管理员
     * @param id 管理员ID
     * @return 删除结果
     */
    Result deleteManager(Integer id);
    
    /**
     * 分页查询管理员列表
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Result getManagerList(Integer pageNum, Integer pageSize);
    
    /**
     * 修改管理员信息
     * @param manager 管理员信息
     * @return 修改结果
     */
    Result updateManager(ManagerDTO manager);
    
    /**
     * 根据名称和账号进行多条件查询
     * @param name 管理员名称（支持模糊查询）
     * @param account 管理员账号
     * @return 查询结果（最多返回一个管理员信息）
     */
    Result queryManagers(String name, String account);
}
