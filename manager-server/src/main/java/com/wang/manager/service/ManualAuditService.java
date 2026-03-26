package com.wang.manager.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.result.Result;
import com.wang.pojo.dto.ManualAuditDTO;
import com.wang.pojo.dto.ManualAuditQueryDTO;
import com.wang.pojo.vo.ManualAuditVO;

/**
 * 人工审核服务接口
 */
public interface ManualAuditService {

    /**
     * 创建审核记录（提交审核）
     * @param dto 审核信息
     * @return 创建结果
     */
    Result createAuditRecord(ManualAuditDTO dto);

    /**
     * 执行审核（通过/拒绝）
     * @param id 审核记录ID
     * @param result 审核结果：1-通过，2-拒绝
     * @param refusalReason 拒绝理由（拒绝时必填）
     * @param managerId 审核管理员ID
     * @param managerName 审核管理员昵称
     * @return 审核结果
     */
    Result executeAudit(Long id, Integer result, String refusalReason, Integer managerId, String managerName);

    /**
     * 批量审核通过
     * @param ids 审核记录ID列表
     * @param managerId 审核管理员ID
     * @param managerName 审核管理员昵称
     * @return 审核结果
     */
    Result batchApprove(java.util.List<Long> ids, Integer managerId, String managerName);

    /**
     * 批量审核拒绝
     * @param ids 审核记录ID列表
     * @param refusalReason 拒绝理由
     * @param managerId 审核管理员ID
     * @param managerName 审核管理员昵称
     * @return 审核结果
     */
    Result batchReject(java.util.List<Long> ids, String refusalReason, Integer managerId, String managerName);

    /**
     * 获取审核记录详情
     * @param id 审核记录ID
     * @return 审核记录详情
     */
    Result getAuditById(Long id);

    /**
     * 分页查询审核记录列表
     * @param queryDTO 查询条件
     * @return 审核记录列表
     */
    Result getAuditList(ManualAuditQueryDTO queryDTO);

    /**
     * 获取待审核记录列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 待审核记录列表
     */
    Result getPendingList(Integer pageNum, Integer pageSize);

    /**
     * 获取指定管理员的审核记录
     * @param managerId 管理员ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 审核记录列表
     */
    Result getAuditByManagerId(Integer managerId, Integer pageNum, Integer pageSize);

    /**
     * 获取审核统计信息
     * @return 统计信息
     */
    Result getAuditStatistics();

    /**
     * 删除审核记录
     * @param id 审核记录ID
     * @return 删除结果
     */
    Result deleteAuditRecord(Long id);

    /**
     * 检查审核目标是否存在待审核记录
     * @param aimId 审核目标对象ID
     * @param aimType 审核目标对象类型
     * @return 是否存在
     */
    boolean hasPendingAudit(Long aimId, Integer aimType);
}