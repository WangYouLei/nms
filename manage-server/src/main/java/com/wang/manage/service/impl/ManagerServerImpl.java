package com.wang.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.interceptor.LoginInterceptor;
import com.wang.common.model.LoginUser;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.common.untils.Argon2idUtil;
import com.wang.common.untils.JWTUtil;
import com.wang.manage.mapper.ManagerMapper;
import com.wang.manage.service.ManagerService;
import com.wang.pojo.dto.ManagerDTO;
import com.wang.pojo.entity.Manager;
import com.wang.pojo.vo.ManagerVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ManagerServerImpl implements ManagerService {
    private final ManagerMapper managerMapper;

    public ManagerServerImpl(ManagerMapper managerMapper) {
        this.managerMapper = managerMapper;
    }

    /**
     * 添加管理员
     *
     * @param manager 管理员信息
     */
    @Override
    public Result addManager(ManagerDTO manager) {
        //处理数据  密码进行argon2加密
        Manager one = new Manager();
        BeanUtils.copyProperties(manager, one);
        one.setCreateTime(LocalDateTime.now());
        one.setUpdateTime(LocalDateTime.now());
        //通过拦截器获取当前登录用户信息
        LoginUser loginUser = LoginInterceptor.threadLocal.get();
        one.setCreateManager(loginUser.getId());
        one.setUpdateManager(loginUser.getId());
       /* //$1$(表示md5加密的) + 8位随机数
        one.setSecret("$1$" + CommonUtil.getRandomString(8));//添加一个随机的盐
        //数据库保存密码=md5（密码 + 盐）
        String s = Md5Crypt.md5Crypt(one.getPassword().getBytes(), one.getSecret());//通过这种方法的加盐方式更好*/


        //md5加盐的加密方式已经过时了，现在使用argon2进行加密
        String s = Argon2idUtil.hash(one.getPassword());
        one.setPassword(s);

        //唯一性校验   这里数据库有唯一性约束，所以不写代码

        //执行插入操作
        try {
            int result = managerMapper.insert(one);
            if (result == 1) {
                ManagerVO vo = new ManagerVO();
                BeanUtils.copyProperties(one, vo);
                return Result.success(vo);
            } else {
                return Result.buildResult(BizCodeEnum.FAIL);
            }
        } catch (Exception e) {//当用户名和用户账号相同时，会抛异常，这里捕获
            return Result.buildResult(BizCodeEnum.USER_EXIST);
        }
    }

    /**
     * 管理员登录
     *
     * @param account  账号
     * @param password 密码
     * @return 登录结果
     */
    @Override
    public Result login(String account, String password) {
        log.info("管理员登录：账号={}", account);

        //查询管理员信息
        QueryWrapper<Manager> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        Manager manager = managerMapper.selectOne(queryWrapper);

        //判断管理员是否存在
        if (manager == null) {
            log.warn("管理员不存在：账号={}", account);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        //验证密码
        boolean ok = Argon2idUtil.verify(manager.getPassword(), password);



        if (!ok) {
            log.warn("密码错误：账号={}", account);
            return Result.buildResult(BizCodeEnum.USER_ACCOUNT_ERROR);
        }

        log.info("管理员登录成功：账号={}, 姓名={}", account, manager.getName());
        //生成token
        LoginUser loginUser = LoginUser.builder()
                .id(manager.getId())
                .name(manager.getName())
                .avatar(manager.getAvatar())
                .account(manager.getAccount())
                .build();
        String token = JWTUtil.geneJsonWebToken(loginUser);
        ManagerVO vo = new ManagerVO();
        BeanUtils.copyProperties(manager, vo);
        return Result.success(token);
    }

    /**
     * 管理员退出登入
     */
    @PostMapping("logout")
    @ApiOperation("管理员退出登入")
    public Result logout() {
        log.info("管理员退出登入");
        return Result.success("退出登入成功");
    }

    /**
     * 删除管理员
     *
     * @param id 管理员ID
     * @return 删除结果
     */
    @Override
    public Result deleteManager(Integer id) {
        log.info("删除管理员：ID={}", id);

        //检查管理员是否存在
        Manager manager = managerMapper.selectById(id);
        if (manager == null) {
            log.info("管理员不存在：ID={}", id);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        //执行删除操作
        int result = managerMapper.deleteById(id);
        if (result == 1) {
            log.info("删除管理员成功：ID={}, 姓名={}", id, manager.getName());
            return Result.success("删除成功");
        } else {
            log.error("删除管理员失败：ID={}", id);
            return Result.error("删除失败");
        }
    }

    /**
     * 分页查询管理员列表
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    @Override
    public Result getManagerList(Integer pageNum, Integer pageSize) {
        log.info("分页查询管理员列表：页码={}, 每页数量={}", pageNum, pageSize);

        //参数校验
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        //创建分页对象
        Page<Manager> page = new Page<>(pageNum, pageSize);

        //执行分页查询
        Page<Manager> result = managerMapper.selectPage(page, null);

        //构建分页结果
        // 修改分页查询部分
        PageResult<ManagerVO> pageResult = PageResult.build(
                (int)result.getCurrent(),
                (int)result.getSize(),
                result.getTotal(),
                result.getRecords().stream().map(item -> {
                    ManagerVO vo = new ManagerVO();
                    BeanUtils.copyProperties(item, vo);
                    return vo;
                }).collect(Collectors.toList())
        );


        log.info("分页查询管理员列表成功，总记录数：{}", result.getTotal());
        return Result.success(pageResult);
    }

    /**
     * 修改管理员信息(不可以修改其他管理员的密码)
     *
     * @param managerDTO 管理员信息
     * @return 修改结果
     */
    @Override
    public Result updateManager(ManagerDTO managerDTO) {
        log.info("修改管理员信息：ID={}, 姓名={}", managerDTO.getId(), managerDTO.getName());

        //检查管理员是否存在
        Manager existingManager = managerMapper.selectById(managerDTO.getId());
        if (existingManager == null) {
            log.warn("管理员不存在：ID={}", managerDTO.getId());
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }


        //执行更新操作
        BeanUtils.copyProperties(managerDTO, existingManager);//将DTO中的数据复制到实体对象中
        existingManager.setUpdateTime(LocalDateTime.now());
        int result = managerMapper.updateById(existingManager);
        if (result == 1) {
            log.info("修改管理员信息成功：ID={}", existingManager.getId());
            return Result.success("修改成功");
        } else {
            log.error("修改管理员信息失败：ID={}", existingManager.getId());
            return Result.error("修改失败");
        }
    }

    /**
     * 根据名称和账号进行多条件查询
     * @param name 管理员名称（支持模糊查询）
     * @param account 管理员账号
     * @return
     */
    @Override
    public Result queryManagers(String name, String account) {
        try {
            log.info("多条件查询管理员，名称: " + name + "，账号: " + account);

            // 创建查询条件
            LambdaQueryWrapper<Manager> queryWrapper = new LambdaQueryWrapper<>();

            // 名称支持模糊查询
            if (StringUtils.hasText(name)) {
                queryWrapper.like(Manager::getName, name);
            }

            // 账号精确查询
            if (StringUtils.hasText(account)) {
                queryWrapper.eq(Manager::getAccount, account);
            }


            // 执行查询
            List<Manager> managerList = managerMapper.selectList(queryWrapper);

            if (managerList == null || managerList.isEmpty()) {
                log.info("未找到符合条件的管理员");
                return Result.error("未找到符合条件的管理员");
            }
            List<ManagerVO> managerVOList = managerList.stream().map(item -> {
                ManagerVO vo = new ManagerVO();
                BeanUtils.copyProperties(item, vo);
                return vo;
            }).collect(Collectors.toList());

            return Result.success(managerVOList);
        } catch (Exception e) {
            log.error("多条件查询管理员异常: " + e.getMessage());
            return Result.error("查询失败，请稍后重试");
        }
    }

}
