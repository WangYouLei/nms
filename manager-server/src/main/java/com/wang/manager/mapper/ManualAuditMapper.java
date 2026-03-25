package com.wang.manager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.ManualAudit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 人工审核Mapper接口
 */
@Mapper
public interface ManualAuditMapper extends BaseMapper<ManualAudit> {

    /**
     * 根据审核目标查询审核记录
     * @param aimId 审核目标对象ID
     * @param aimType 审核目标对象类型
     * @return 审核记录
     */
    @Select("SELECT * FROM manual_audit WHERE aim_id = #{aimId} AND aim_type = #{aimType}")
    ManualAudit selectByAimId(@Param("aimId") Long aimId, @Param("aimType") Integer aimType);

    /**
     * 查询待审核记录数量
     * @return 待审核记录数量
     */
    @Select("SELECT COUNT(*) FROM manual_audit WHERE result = 0")
    int countPending();

    /**
     * 根据审核结果查询记录数量
     * @param result 审核结果
     * @return 记录数量
     */
    @Select("SELECT COUNT(*) FROM manual_audit WHERE result = #{result}")
    int countByResult(@Param("result") Integer result);

    /**
     * 查询指定管理员的审核记录
     * @param managerId 管理员ID
     * @return 审核记录列表
     */
    @Select("SELECT * FROM manual_audit WHERE manager_id = #{managerId} ORDER BY create_time DESC")
    List<ManualAudit> selectByManagerId(@Param("managerId") Long managerId);
}