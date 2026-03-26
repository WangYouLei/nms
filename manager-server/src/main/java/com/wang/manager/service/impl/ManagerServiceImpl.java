package com.wang.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.config.DefaultUrlConfig;
import com.wang.common.utils.RoleContextUtil;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.UserRole;
import com.wang.common.model.LoginUser;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.common.utils.Argon2idUtil;
import org.springframework.beans.BeanUtils;
import com.wang.common.utils.JWTUtil;
import com.wang.manager.mapper.ManagerMapper;
import com.wang.manager.service.ManagerService;
import com.wang.pojo.dto.ManagerDTO;
import com.wang.pojo.dto.ManagerQueryDTO;
import com.wang.pojo.entity.Manager;
import com.wang.pojo.vo.ManagerVO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ManagerServiceImpl implements ManagerService {

    private final ManagerMapper managerMapper;
    private final DefaultUrlConfig defaultUrlConfig;

    public ManagerServiceImpl(ManagerMapper managerMapper, DefaultUrlConfig defaultUrlConfig) {
        this.managerMapper = managerMapper;
        this.defaultUrlConfig = defaultUrlConfig;
    }

    /**
     * 管理员登录
     */
    @Override
    public Result login(String account, String password) {
        log.info("管理员登录：账号={}", account);

        // 查询管理员信息
        LambdaQueryWrapper<Manager> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Manager::getAccount, account);
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
                .id(manager.getId())
                .name(manager.getName())
                .avatar(manager.getAvatar())
                .account(manager.getAccount())
                .role(UserRole.MANAGER)
                .build();
        String token = JWTUtil.geneJsonWebToken(loginUser);

        return Result.success(token);
    }

    /**
     * 添加管理员（需要管理员登录）
     */
    @Override
    public Result addManager(ManagerDTO managerDTO) {
        // 获取当前登录用户
        LoginUser loginUser = RoleContextUtil.getCurrentUser();

        // 验证是否登录
        if (loginUser == null) {
            log.warn("未登录用户尝试添加管理员");
            return Result.buildResult(BizCodeEnum.USER_NOT_LOGIN);
        }

        // 验证是否为管理员
        if (!UserRole.MANAGER.equals(loginUser.getRole())) {
            log.warn("非管理员用户尝试添加管理员：账号={}", loginUser.getAccount());
            return Result.buildResult(BizCodeEnum.PERMISSION_DENIED);
        }

        log.info("添加管理员：账号={}, 操作者={}", managerDTO.getAccount(), loginUser.getAccount());

        // 检查账号是否已存在
        LambdaQueryWrapper<Manager> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Manager::getAccount, managerDTO.getAccount());
        Manager existManager = managerMapper.selectOne(queryWrapper);
        if (existManager != null) {
            log.warn("账号已存在：账号={}", managerDTO.getAccount());
            return Result.buildResult(BizCodeEnum.USER_EXIST);
        }

        // 构建实体对象
        Manager manager = new Manager();
        BeanUtils.copyProperties(managerDTO, manager);
        manager.setCreateTime(LocalDateTime.now());
        // 设置创建者为当前登录管理员的ID
        manager.setCreateId(loginUser.getId());
        manager.setUpdateTime(LocalDateTime.now());
        manager.setAvatar(defaultUrlConfig.getManagerAvatarUrl());

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
    public Result deleteManager(Integer id) {
        log.info("删除管理员：ID={}", id);

        // 检查管理员是否存在
        Manager manager = managerMapper.selectById(id);
        if (manager == null) {
            log.warn("管理员不存在：ID={}", id);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        LoginUser loginUser = RoleContextUtil.getCurrentUser();
        // 执行删除
        int result = managerMapper.deleteById(id);
        if (result == 1) {
            log.info("删除管理员成功：被删ID={},操作者ID={}", id, loginUser.getId());
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

        // 使用工具类复制非空属性，忽略 password、id、createTime、createId
        BeanUtils.copyProperties(managerDTO, existingManager, "password", "id", "createTime", "createId");
        
        // 设置更新时间
        existingManager.setUpdateTime(LocalDateTime.now());

        int result = managerMapper.update(existingManager);
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
     * 多条件查询管理员（支持id、姓名、账号，条件可为空）
     */
    @Override
    public Result getManagerList(ManagerQueryDTO queryDTO) {
        log.info("多条件查询管理员：queryDTO={}", queryDTO);

        LambdaQueryWrapper<Manager> queryWrapper = new LambdaQueryWrapper<>();

        // 根据ID精确查询
        if (queryDTO.getId() != null) {
            queryWrapper.eq(Manager::getId, queryDTO.getId());
        }

        // 根据姓名模糊查询
        if (StringUtils.hasText(queryDTO.getName())) {
            queryWrapper.like(Manager::getName, queryDTO.getName());
        }

        // 根据账号模糊查询
        if (StringUtils.hasText(queryDTO.getAccount())) {
            queryWrapper.like(Manager::getAccount, queryDTO.getAccount());
        }

        queryWrapper.orderByDesc(Manager::getCreateTime);

        List<Manager> managers = managerMapper.selectList(queryWrapper);

        List<ManagerVO> voList = managers.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    /**
     * 分页查询管理员信息
     */
    @Override
    public Result getManagerPage(Integer pageNum, Integer pageSize) {
        log.info("分页查询管理员：页码={}, 每页数量={}", pageNum, pageSize);

        // 参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        // 创建分页对象
        Page<Manager> page = new Page<>(pageNum, pageSize);

        // 创建查询条件
        LambdaQueryWrapper<Manager> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Manager::getCreateTime);

        // 执行分页查询
        Page<Manager> result = managerMapper.selectPage(page, queryWrapper);

        // 转换为VO
        List<ManagerVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<ManagerVO> pageResult = PageResult.build(
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getTotal(),
                voList
        );

        log.info("分页查询管理员成功，总记录数：{}", result.getTotal());
        return Result.success(pageResult);
    }

    /**
     * 转换为VO
     */
    private ManagerVO convertToVO(Manager manager) {
        ManagerVO vo = new ManagerVO();
        BeanUtils.copyProperties(manager, vo);
        return vo;
    }

    /**
     * 修改管理员密码
     */
    @Override
    public Result updatePassword(Integer id, String newPassword) {
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
        LoginUser loginUser = RoleContextUtil.getCurrentUser();

        int result = managerMapper.update(manager);
        if (result == 1) {
            log.info("修改管理员密码成功：被修改ID={}，修改者ID={}", id,loginUser.getId());
            return Result.success("密码修改成功");
        } else {
            log.error("修改管理员密码失败：ID={}", id);
            return Result.error("密码修改失败");
        }
    }

    @Override
    public Result getNameAndAvatar(Integer id) {
        log.info("获取管理员名称和头像：ID={}", id);
        Map<String, String> map = new HashMap<>();
        Manager manager = managerMapper.selectById(id);
        if (manager == null) {
            log.warn("管理员不存在：ID={}", id);
            return Result.error("管理员不存在");
        }
        map.put("name", manager.getName());
        map.put("avatar", manager.getAvatar());
        return Result.success(map);
    }
}