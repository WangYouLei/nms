package com.wang.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.enums.AuditAimTypeEnum;
import com.wang.common.enums.AuditResultEnum;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import org.springframework.beans.BeanUtils;
import com.wang.manager.mapper.ManualAuditMapper;
import com.wang.manager.service.ManualAuditService;
import com.wang.pojo.dto.ManualAuditDTO;
import com.wang.pojo.dto.ManualAuditQueryDTO;
import com.wang.pojo.entity.ManualAudit;
import com.wang.pojo.vo.ManualAuditVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人工审核服务实现类
 */
@Slf4j
@Service
public class ManualAuditServiceImpl implements ManualAuditService {

    private final ManualAuditMapper manualAuditMapper;

    public ManualAuditServiceImpl(ManualAuditMapper manualAuditMapper) {
        this.manualAuditMapper = manualAuditMapper;
    }

    @Override
    @Transactional
    public Result createAuditRecord(ManualAuditDTO dto) {
        log.info("创建审核记录：aimId={}, aimType={}", dto.getAimId(), dto.getAimType());

        // 检查是否已存在待审核记录
        ManualAudit existing = manualAuditMapper.selectByAimId(dto.getAimId(), dto.getAimType());
        if (existing != null && AuditResultEnum.PENDING.getValue().equals(existing.getResult())) {
            log.warn("审核记录已存在：aimId={}, aimType={}", dto.getAimId(), dto.getAimType());
            return Result.error("该目标已存在待审核记录");
        }

        ManualAudit entity = new ManualAudit();
        entity.setAimId(dto.getAimId());
        entity.setAimType(dto.getAimType());
        entity.setResult(AuditResultEnum.PENDING.getValue()); // 默认待审核
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        int result = manualAuditMapper.insert(entity);
        if (result == 1) {
            log.info("审核记录创建成功：id={}", entity.getId());
            return Result.success(convertToVO(entity));
        } else {
            log.error("审核记录创建失败：aimId={}", dto.getAimId());
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    @Override
    @Transactional
    public Result executeAudit(Long id, Integer result, String refusalReason, Integer managerId, String managerName) {
        log.info("执行审核：id={}, result={}, managerId={}", id, result, managerId);

        ManualAudit entity = manualAuditMapper.selectById(id);
        if (entity == null) {
            log.warn("审核记录不存在：id={}", id);
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }

        if (!AuditResultEnum.PENDING.getValue().equals(entity.getResult())) {
            log.warn("审核记录已完成，无法重复审核：id={}, currentResult={}", id, entity.getResult());
            return Result.error("该记录已完成审核，无法重复操作");
        }

        // 验证审核结果
        if (!AuditResultEnum.APPROVED.getValue().equals(result) && !AuditResultEnum.REJECTED.getValue().equals(result)) {
            return Result.error("审核结果无效");
        }

        // 拒绝时必须填写理由
        if (AuditResultEnum.REJECTED.getValue().equals(result) && (refusalReason == null || refusalReason.trim().isEmpty())) {
            return Result.error("拒绝时必须填写拒绝理由");
        }

        entity.setResult(result);
        entity.setRefusalReason(refusalReason);
        entity.setManagerId(managerId);
        entity.setManagerName(managerName);
        entity.setFirstAuditTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        int updateResult = manualAuditMapper.update(entity);
        if (updateResult == 1) {
            log.info("审核执行成功：id={}, result={}", id, result);
            return Result.success(convertToVO(entity));
        } else {
            log.error("审核执行失败：id={}", id);
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    @Override
    @Transactional
    public Result batchApprove(List<Long> ids, Integer managerId, String managerName) {
        log.info("批量审核通过：ids={}, managerId={}", ids, managerId);

        int successCount = 0;
        int skipCount = 0;

        for (Long id : ids) {
            ManualAudit entity = manualAuditMapper.selectById(id);
            if (entity == null || !AuditResultEnum.PENDING.getValue().equals(entity.getResult())) {
                skipCount++;
                continue;
            }

            entity.setResult(AuditResultEnum.APPROVED.getValue());
            entity.setManagerId(managerId);
            entity.setManagerName(managerName);
            entity.setFirstAuditTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());

            int result = manualAuditMapper.update(entity);
            if (result == 1) {
                successCount++;
            }
        }

        log.info("批量审核通过完成：成功{}个，跳过{}个", successCount, skipCount);
        return Result.success(Map.of(
                "successCount", successCount,
                "skipCount", skipCount
        ));
    }

    @Override
    @Transactional
    public Result batchReject(List<Long> ids, String refusalReason, Integer managerId, String managerName) {
        log.info("批量审核拒绝：ids={}, managerId={}", ids, managerId);

        if (refusalReason == null || refusalReason.trim().isEmpty()) {
            return Result.error("拒绝理由不能为空");
        }

        int successCount = 0;
        int skipCount = 0;

        for (Long id : ids) {
            ManualAudit entity = manualAuditMapper.selectById(id);
            if (entity == null || !AuditResultEnum.PENDING.getValue().equals(entity.getResult())) {
                skipCount++;
                continue;
            }

            entity.setResult(AuditResultEnum.REJECTED.getValue());
            entity.setRefusalReason(refusalReason);
            entity.setManagerId(managerId);
            entity.setManagerName(managerName);
            entity.setFirstAuditTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());

            int result = manualAuditMapper.update(entity);
            if (result == 1) {
                successCount++;
            }
        }

        log.info("批量审核拒绝完成：成功{}个，跳过{}个", successCount, skipCount);
        return Result.success(Map.of(
                "successCount", successCount,
                "skipCount", skipCount
        ));
    }

    @Override
    public Result getAuditById(Long id) {
        log.info("获取审核记录详情：id={}", id);

        ManualAudit entity = manualAuditMapper.selectById(id);
        if (entity == null) {
            log.warn("审核记录不存在：id={}", id);
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }

        return Result.success(convertToVO(entity));
    }

    @Override
    public Result getAuditList(ManualAuditQueryDTO queryDTO) {
        log.info("分页查询审核记录列表：queryDTO={}", queryDTO);

        LambdaQueryWrapper<ManualAudit> queryWrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getAimId() != null) {
            queryWrapper.eq(ManualAudit::getAimId, queryDTO.getAimId());
        }
        if (queryDTO.getAimType() != null) {
            queryWrapper.eq(ManualAudit::getAimType, queryDTO.getAimType());
        }
        if (queryDTO.getResult() != null) {
            queryWrapper.eq(ManualAudit::getResult, queryDTO.getResult());
        }
        if (queryDTO.getManagerId() != null) {
            queryWrapper.eq(ManualAudit::getManagerId, queryDTO.getManagerId());
        }

        queryWrapper.orderByDesc(ManualAudit::getCreateTime);

        Page<ManualAudit> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<ManualAudit> resultPage = manualAuditMapper.selectPage(page, queryWrapper);

        List<ManualAuditVO> voList = new ArrayList<>();
        for (ManualAudit entity : resultPage.getRecords()) {
            voList.add(convertToVO(entity));
        }

        PageResult<ManualAuditVO> pageResult = PageResult.build(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                voList
        );

        return Result.success(pageResult);
    }

    @Override
    public Result getPendingList(Integer pageNum, Integer pageSize) {
        log.info("获取待审核记录列表");

        LambdaQueryWrapper<ManualAudit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ManualAudit::getResult, AuditResultEnum.PENDING.getValue())
                .orderByAsc(ManualAudit::getCreateTime);

        Page<ManualAudit> page = new Page<>(pageNum, pageSize);
        Page<ManualAudit> resultPage = manualAuditMapper.selectPage(page, queryWrapper);

        List<ManualAuditVO> voList = new ArrayList<>();
        for (ManualAudit entity : resultPage.getRecords()) {
            voList.add(convertToVO(entity));
        }

        PageResult<ManualAuditVO> pageResult = PageResult.build(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                voList
        );

        return Result.success(pageResult);
    }

    @Override
    public Result getAuditByManagerId(Integer managerId, Integer pageNum, Integer pageSize) {
        log.info("获取管理员的审核记录：managerId={}", managerId);

        LambdaQueryWrapper<ManualAudit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ManualAudit::getManagerId, managerId)
                .orderByDesc(ManualAudit::getFirstAuditTime);

        Page<ManualAudit> page = new Page<>(pageNum, pageSize);
        Page<ManualAudit> resultPage = manualAuditMapper.selectPage(page, queryWrapper);

        List<ManualAuditVO> voList = new ArrayList<>();
        for (ManualAudit entity : resultPage.getRecords()) {
            voList.add(convertToVO(entity));
        }

        PageResult<ManualAuditVO> pageResult = PageResult.build(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                voList
        );

        return Result.success(pageResult);
    }

    @Override
    public Result getAuditStatistics() {
        log.info("获取审核统计信息");

        int pendingCount = manualAuditMapper.countByResult(AuditResultEnum.PENDING.getValue());
        int approvedCount = manualAuditMapper.countByResult(AuditResultEnum.APPROVED.getValue());
        int rejectedCount = manualAuditMapper.countByResult(AuditResultEnum.REJECTED.getValue());
        int totalCount = pendingCount + approvedCount + rejectedCount;

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", totalCount);
        statistics.put("pending", pendingCount);
        statistics.put("approved", approvedCount);
        statistics.put("rejected", rejectedCount);

        return Result.success(statistics);
    }

    @Override
    @Transactional
    public Result deleteAuditRecord(Long id) {
        log.info("删除审核记录：id={}", id);

        ManualAudit entity = manualAuditMapper.selectById(id);
        if (entity == null) {
            log.warn("审核记录不存在：id={}", id);
            return Result.buildResult(BizCodeEnum.RESOURCE_NOT_FOUND);
        }

        int result = manualAuditMapper.deleteById(id);
        if (result == 1) {
            log.info("审核记录删除成功：id={}", id);
            return Result.success("删除成功");
        } else {
            log.error("审核记录删除失败：id={}", id);
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    @Override
    public boolean hasPendingAudit(Long aimId, Integer aimType) {
        ManualAudit entity = manualAuditMapper.selectByAimId(aimId, aimType);
        return entity != null && AuditResultEnum.PENDING.getValue().equals(entity.getResult());
    }

    /**
     * 转换为VO
     */
    private ManualAuditVO convertToVO(ManualAudit entity) {
        ManualAuditVO vo = new ManualAuditVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setAimTypeName(AuditAimTypeEnum.getDescription(entity.getAimType()));
        vo.setResultName(AuditResultEnum.getDescription(entity.getResult()));
        return vo;
    }
}