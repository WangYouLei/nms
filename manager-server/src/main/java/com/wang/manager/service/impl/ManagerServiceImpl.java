package com.wang.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.common.untils.Argon2idUtil;
import com.wang.common.untils.JWTUtil;
import com.wang.manager.mapper.ManagerMapper;
import com.wang.manager.service.ManagerService;
import com.wang.pojo.dto.ManagerDTO;
import com.wang.pojo.entity.Manager;
import com.wang.pojo.vo.ManagerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ManagerServiceImpl implements ManagerService {

    private final ManagerMapper managerMapper;

    public ManagerServiceImpl(ManagerMapper managerMapper) {
        this.managerMapper = managerMapper;
    }

    /**
     * 管理员登录
     */
    @Override
    public Result login(String account, String password) {
        log.info("管理员登录：账号={}", account);

        // 查询管理员信息
        QueryWrapper<Manager> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        Manager manager = managerMapper.selectOne(queryWrapper);

        // 判断管理员是否存在
        if (manager == null) {
            log.warn("管理员不存在：账号={}", account);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        // 验证密码
        boolean ok = Argon2idUtil.verify(manager.getPassword(), password);
        if (!ok) {
            log.warn("密码错误：账号={}", account);
            return Result.buildResult(BizCodeEnum.USER_ACCOUNT_ERROR);
        }

        log.info("管理员登录成功：账号={}, 姓名={}", account, manager.getName());

        // 生成token
        LoginUser loginUser = LoginUser.builder()
                .id(manager.getId().intValue())
                .name(manager.getName())
                .account(manager.getAccount())
                .build();
        String token = JWTUtil.geneJsonWebToken(loginUser);

        return Result.success(token);
    }

    /**
     * 添加管理员
     */
    @Override
    public Result addManager(ManagerDTO managerDTO) {
        log.info("添加管理员：账号={}", managerDTO.getAccount());

        // 检查账号是否已存在
        QueryWrapper<Manager> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", managerDTO.getAccount());
        Manager existManager = managerMapper.selectOne(queryWrapper);
        if (existManager != null) {
            log.warn("账号已存在：账号={}", managerDTO.getAccount());
            return Result.buildResult(BizCodeEnum.USER_EXIST);
        }

        // 构建实体对象
        Manager manager = new Manager();
        BeanUtils.copyProperties(managerDTO, manager);
        manager.setCreateTime(LocalDateTime.now());

        // 密码加密
        String hashedPassword = Argon2idUtil.hash(managerDTO.getPassword());
        manager.setPassword(hashedPassword);

        // 执行插入
        try {
            int result = managerMapper.insert(manager);
            if (result == 1) {
                log.info("添加管理员成功：ID={}", manager.getId());
                ManagerVO vo = new ManagerVO();
                BeanUtils.copyProperties(manager, vo);
                return Result.success(vo);
            } else {
                return Result.buildResult(BizCodeEnum.FAIL);
            }
        } catch (Exception e) {
            log.error("添加管理员失败：{}", e.getMessage());
            return Result.buildResult(BizCodeEnum.FAIL);
        }
    }

    /**
     * 删除管理员
     */
    @Override
    public Result deleteManager(Long id) {
        log.info("删除管理员：ID={}", id);

        // 检查管理员是否存在
        Manager manager = managerMapper.selectById(id);
        if (manager == null) {
            log.warn("管理员不存在：ID={}", id);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        // 执行删除
        int result = managerMapper.deleteById(id);
        if (result == 1) {
            log.info("删除管理员成功：ID={}", id);
            return Result.success("删除成功");
        } else {
            log.error("删除管理员失败：ID={}", id);
            return Result.error("删除失败");
        }
    }

    /**
     * 修改管理员信息
     */
    @Override
    public Result updateManager(ManagerDTO managerDTO) {
        log.info("修改管理员信息：ID={}", managerDTO.getId());

        // 检查管理员是否存在
        Manager existingManager = managerMapper.selectById(managerDTO.getId());
        if (existingManager == null) {
            log.warn("管理员不存在：ID={}", managerDTO.getId());
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        // 更新信息（不更新密码）
        if (managerDTO.getName() != null) {
            existingManager.setName(managerDTO.getName());
        }
        if (managerDTO.getAccount() != null) {
            existingManager.setAccount(managerDTO.getAccount());
        }

        int result = managerMapper.updateById(existingManager);
        if (result == 1) {
            log.info("修改管理员信息成功：ID={}", existingManager.getId());
            ManagerVO vo = new ManagerVO();
            BeanUtils.copyProperties(existingManager, vo);
            return Result.success(vo);
        } else {
            log.error("修改管理员信息失败：ID={}", existingManager.getId());
            return Result.error("修改失败");
        }
    }

    /**
     * 根据ID查询管理员
     */
    @Override
    public Result getManagerById(Long id) {
        log.info("查询管理员：ID={}", id);

        Manager manager = managerMapper.selectById(id);
        if (manager == null) {
            log.warn("管理员不存在：ID={}", id);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        ManagerVO vo = new ManagerVO();
        BeanUtils.copyProperties(manager, vo);
        return Result.success(vo);
    }

    /**
     * 修改管理员密码
     */
    @Override
    public Result updatePassword(Long id, String newPassword) {
        log.info("修改管理员密码：ID={}", id);

        // 检查管理员是否存在
        Manager manager = managerMapper.selectById(id);
        if (manager == null) {
            log.warn("管理员不存在：ID={}", id);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        // 加密新密码
        String hashedPassword = Argon2idUtil.hash(newPassword);
        manager.setPassword(hashedPassword);

        int result = managerMapper.updateById(manager);
        if (result == 1) {
            log.info("修改管理员密码成功：ID={}", id);
            return Result.success("密码修改成功");
        } else {
            log.error("修改管理员密码失败：ID={}", id);
            return Result.error("密码修改失败");
        }
    }
}