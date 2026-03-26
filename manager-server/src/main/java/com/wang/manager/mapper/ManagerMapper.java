package com.wang.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.Manager;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ManagerMapper extends BaseMapper<Manager> {

    /**
     * 动态更新管理员信息，只更新非 null 的字段
     * @param manager 管理员信息
     * @return 影响行数
     */
    int update(Manager manager);
}