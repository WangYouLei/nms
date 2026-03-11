package com.wang.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.common.untils.Argon2idUtil;
import com.wang.common.untils.JWTUtil;
import com.wang.pojo.dto.VisitorDTO;
import com.wang.pojo.entity.Visitor;
import com.wang.pojo.vo.VisitorVO;
import com.wang.visitor.mapper.VisitorMapper;
import com.wang.visitor.service.VisitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 访客服务实现类
 */
@Slf4j
@Service
public class VisitorServiceImpl implements VisitorService {

    private final VisitorMapper visitorMapper;

    public VisitorServiceImpl(VisitorMapper visitorMapper) {
        this.visitorMapper = visitorMapper;
    }

    /**
     * VIP级别名称
     */
    private static final String[] VIP_LEVEL_NAMES = {"普通用户", "VIP1", "VIP2", "VIP3", "金主"};

    @Override
    public Result register(VisitorDTO visitorDTO) {
        log.info("访客注册：账号={}", visitorDTO.getAccount());

        // 检查账号是否已存在
        LambdaQueryWrapper<Visitor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Visitor::getAccount, visitorDTO.getAccount());
        if (visitorMapper.selectCount(queryWrapper) > 0) {
            return Result.error("该账号已存在");
        }

        Visitor visitor = new Visitor();
        BeanUtils.copyProperties(visitorDTO, visitor);
        visitor.setPassword(Argon2idUtil.hash(visitorDTO.getPassword()));
        visitor.setVipLevel(0);
        visitor.setCreateTime(LocalDateTime.now());
        visitor.setUpdateTime(LocalDateTime.now());

        int result = visitorMapper.insert(visitor);
        if (result > 0) {
            log.info("访客注册成功：ID={}", visitor.getId());
            return Result.success(convertToVO(visitor));
        }
        return Result.error("注册失败");
    }

    @Override
    public Result login(String account, String password) {
        log.info("访客登录：账号={}", account);

        // 查询访客
        LambdaQueryWrapper<Visitor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Visitor::getAccount, account);
        Visitor visitor = visitorMapper.selectOne(queryWrapper);

        if (visitor == null) {
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        // 验证密码
        boolean ok = Argon2idUtil.verify(visitor.getPassword(), password);
        if (!ok) {
            return Result.buildResult(BizCodeEnum.USER_ACCOUNT_ERROR);
        }

        // 生成token
        LoginUser loginUser = LoginUser.builder()
                .id(visitor.getId())
                .name(visitor.getName())
                .avatar(visitor.getAvatar())
                .account(visitor.getAccount())
                .build();
        String token = JWTUtil.geneJsonWebToken(loginUser);

        log.info("访客登录成功：ID={},token={}", visitor.getId(),token);
        return Result.success(token);
    }

    @Override
    public Result getVisitorInfo(Integer visitorId) {
        log.info("获取访客信息：ID={}", visitorId);

        Visitor visitor = visitorMapper.selectById(visitorId);
        if (visitor == null) {
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }
        return Result.success(convertToVO(visitor));
    }

    @Override
    public Result updateVisitor(VisitorDTO visitorDTO) {
        log.info("修改访客信息：ID={}", visitorDTO.getId());

        Visitor visitor = visitorMapper.selectById(visitorDTO.getId());
        if (visitor == null) {
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        // 只更新名称和头像
        visitor.setName(visitorDTO.getName());
        if (visitorDTO.getAvatar() != null) {
            visitor.setAvatar(visitorDTO.getAvatar());
        }
        visitor.setUpdateTime(LocalDateTime.now());

        int result = visitorMapper.updateById(visitor);
        if (result > 0) {
            return Result.success(convertToVO(visitor));
        }
        return Result.error("修改失败");
    }

    @Override
    public Result updatePassword(Integer visitorId, String oldPassword, String newPassword) {
        log.info("修改访客密码：ID={}", visitorId);

        Visitor visitor = visitorMapper.selectById(visitorId);
        if (visitor == null) {
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        // 验证旧密码
        boolean ok = Argon2idUtil.verify(visitor.getPassword(), oldPassword);
        if (!ok) {
            return Result.error("旧密码错误");
        }

        visitor.setPassword(Argon2idUtil.hash(newPassword));
        visitor.setUpdateTime(LocalDateTime.now());
        visitorMapper.updateById(visitor);

        return Result.success("密码修改成功");
    }

    /**
     * 转换为VO
     */
    private VisitorVO convertToVO(Visitor visitor) {
        VisitorVO vo = new VisitorVO();
        BeanUtils.copyProperties(visitor, vo);
        if (visitor.getVipLevel() != null && visitor.getVipLevel() >= 0 && visitor.getVipLevel() < VIP_LEVEL_NAMES.length) {
            vo.setVipLevelName(VIP_LEVEL_NAMES[visitor.getVipLevel()]);
        }
        return vo;
    }
}