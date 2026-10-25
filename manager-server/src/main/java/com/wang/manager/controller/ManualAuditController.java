package com.wang.manager.controller;

import com.wang.common.enums.AuditResultEnum;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.UserRole;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.common.utils.RoleContextUtil;
import com.wang.manager.service.ManualAuditService;
import com.wang.pojo.dto.ManualAuditDTO;
import com.wang.pojo.dto.ManualAuditQueryDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 人工审核控制器
 * 仅管理员可访问，审核人身份以当前登录管理员为准，不信任前端传入的管理员信息
 */
@Slf4j
@RestController
@Api(tags = "人工审核管理")
@RequestMapping("/manual-audit")
public class ManualAuditController {

    private final ManualAuditService manualAuditService;

    public ManualAuditController(ManualAuditService manualAuditService) {
        this.manualAuditService = manualAuditService;
    }

    /**
     * 校验当前登录用户是否为管理员（服务层纵深校验，网关已做角色控制）
     * @return 校验通过返回null，否则返回拒绝结果
     */
    private Result denyIfNotManager() {
        if (!UserRole.MANAGER.getCode().equals(RoleContextUtil.getCurrentUserRole())) {
            log.warn("人工审核接口拒绝访问：当前用户非管理员");
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }
        return null;
    }

    @PostMapping("/create")
    @ApiOperation("创建审核记录（提交审核）")
    public Result createAuditRecord(@RequestBody ManualAuditDTO dto) {
        Result denied = denyIfNotManager();
        if (denied != null) return denied;
        log.info("创建审核记录请求：aimId={}, aimType={}", dto.getAimId(), dto.getAimType());
        return manualAuditService.createAuditRecord(dto);
    }

    @PutMapping("/execute")
    @ApiOperation("执行审核（通过/拒绝）")
    public Result executeAudit(
            @RequestParam @ApiParam("审核记录ID") Long id,
            @RequestParam @ApiParam("审核结果：1-通过，2-拒绝") Integer result,
            @RequestParam(required = false) @ApiParam("拒绝理由（拒绝时必填）") String refusalReason,
            @RequestParam @ApiParam("审核管理员ID") Long managerId,
            @RequestParam @ApiParam("审核管理员昵称") String managerName) {
        Result denied = denyIfNotManager();
        if (denied != null) return denied;
        log.info("执行审核请求：id={}, result={}", id, result);
        // 审核人身份以当前登录管理员为准，忽略前端传入值
        LoginUser current = RoleContextUtil.getCurrentUser();
        return manualAuditService.executeAudit(id, result, refusalReason, current.getId(), current.getName());
    }

    @PutMapping("/approve/{id}")
    @ApiOperation("审核通过")
    public Result approve(
            @PathVariable @ApiParam("审核记录ID") Long id,
            @RequestParam @ApiParam("审核管理员ID") Long managerId,
            @RequestParam @ApiParam("审核管理员昵称") String managerName) {
        Result denied = denyIfNotManager();
        if (denied != null) return denied;
        log.info("审核通过请求：id={}", id);
        LoginUser current = RoleContextUtil.getCurrentUser();
        return manualAuditService.executeAudit(id, AuditResultEnum.APPROVED.getValue(), null, current.getId(), current.getName());
    }

    @PutMapping("/reject/{id}")
    @ApiOperation("审核拒绝")
    public Result reject(
            @PathVariable @ApiParam("审核记录ID") Long id,
            @RequestParam @ApiParam("拒绝理由") String refusalReason,
            @RequestParam @ApiParam("审核管理员ID") Long managerId,
            @RequestParam @ApiParam("审核管理员昵称") String managerName) {
        Result denied = denyIfNotManager();
        if (denied != null) return denied;
        log.info("审核拒绝请求：id={}", id);
        LoginUser current = RoleContextUtil.getCurrentUser();
        return manualAuditService.executeAudit(id, AuditResultEnum.REJECTED.getValue(), refusalReason, current.getId(), current.getName());
    }

    @PutMapping("/batch-approve")
    @ApiOperation("批量审核通过")
    public Result batchApprove(
            @RequestBody @ApiParam("审核记录ID列表") List<Long> ids,
            @RequestParam @ApiParam("审核管理员ID") Long managerId,
            @RequestParam @ApiParam("审核管理员昵称") String managerName) {
        Result denied = denyIfNotManager();
        if (denied != null) return denied;
        log.info("批量审核通过请求：count={}", ids.size());
        LoginUser current = RoleContextUtil.getCurrentUser();
        return manualAuditService.batchApprove(ids, current.getId(), current.getName());
    }

    @PutMapping("/batch-reject")
    @ApiOperation("批量审核拒绝")
    public Result batchReject(
            @RequestBody @ApiParam("审核记录ID列表") List<Long> ids,
            @RequestParam @ApiParam("拒绝理由") String refusalReason,
            @RequestParam @ApiParam("审核管理员ID") Long managerId,
            @RequestParam @ApiParam("审核管理员昵称") String managerName) {
        Result denied = denyIfNotManager();
        if (denied != null) return denied;
        log.info("批量审核拒绝请求：count={}", ids.size());
        LoginUser current = RoleContextUtil.getCurrentUser();
        return manualAuditService.batchReject(ids, refusalReason, current.getId(), current.getName());
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("获取审核记录详情")
    public Result getAuditById(@PathVariable @ApiParam("审核记录ID") Long id) {
        Result denied = denyIfNotManager();
        if (denied != null) return denied;
        log.info("获取审核记录详情请求：id={}", id);
        return manualAuditService.getAuditById(id);
    }

    @PostMapping("/list")
    @ApiOperation("分页查询审核记录列表")
    public Result getAuditList(@RequestBody ManualAuditQueryDTO queryDTO) {
        Result denied = denyIfNotManager();
        if (denied != null) return denied;
        log.info("分页查询审核记录列表请求");
        return manualAuditService.getAuditList(queryDTO);
    }

    @GetMapping("/pending")
    @ApiOperation("获取待审核记录列表")
    public Result getPendingList(
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer pageNum,
            @RequestParam(defaultValue = "10") @ApiParam("每页数量") Integer pageSize) {
        Result denied = denyIfNotManager();
        if (denied != null) return denied;
        log.info("获取待审核记录列表请求");
        return manualAuditService.getPendingList(pageNum, pageSize);
    }

    @GetMapping("/manager/{managerId}")
    @ApiOperation("获取指定管理员的审核记录")
    public Result getAuditByManagerId(
            @PathVariable @ApiParam("管理员ID") Long managerId,
            @RequestParam(defaultValue = "1") @ApiParam("页码") Integer pageNum,
            @RequestParam(defaultValue = "10") @ApiParam("每页数量") Integer pageSize) {
        Result denied = denyIfNotManager();
        if (denied != null) return denied;
        log.info("获取管理员审核记录请求：managerId={}", managerId);
        return manualAuditService.getAuditByManagerId(managerId, pageNum, pageSize);
    }

    @GetMapping("/statistics")
    @ApiOperation("获取审核统计信息")
    public Result getAuditStatistics() {
        Result denied = denyIfNotManager();
        if (denied != null) return denied;
        log.info("获取审核统计信息请求");
        return manualAuditService.getAuditStatistics();
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除审核记录")
    public Result deleteAuditRecord(@PathVariable @ApiParam("审核记录ID") Long id) {
        Result denied = denyIfNotManager();
        if (denied != null) return denied;
        log.info("删除审核记录请求：id={}", id);
        return manualAuditService.deleteAuditRecord(id);
    }

    @GetMapping("/check-pending")
    @ApiOperation("检查是否存在待审核记录")
    public Result checkPending(
            @RequestParam @ApiParam("审核目标对象ID") Long aimId,
            @RequestParam @ApiParam("审核目标对象类型") Integer aimType) {
        Result denied = denyIfNotManager();
        if (denied != null) return denied;
        log.info("检查待审核记录请求：aimId={}, aimType={}", aimId, aimType);
        boolean hasPending = manualAuditService.hasPendingAudit(aimId, aimType);
        return Result.success(hasPending);
    }
}
