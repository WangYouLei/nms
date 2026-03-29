package com.wang.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.common.config.DefaultUrlConfig;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.UserRole;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.common.utils.Argon2idUtil;
import com.wang.common.utils.JWTUtil;
import com.wang.common.service.TokenService;
import com.wang.common.service.CacheService;
import com.wang.common.constants.CacheConstants;
import com.wang.visitor.feign.CaptchaServiceFeign;
import com.wang.visitor.feign.EmailServiceFeign;
import com.wang.visitor.mapper.VisitorMapper;
import com.wang.visitor.service.VisitorService;
import com.wang.pojo.dto.PasswordUpdateEmailDTO;
import com.wang.pojo.dto.VisitorDeleteDTO;
import com.wang.pojo.dto.VisitorDTO;
import com.wang.pojo.dto.VisitorRegisterDTO;
import com.wang.pojo.entity.Visitor;
import com.wang.pojo.vo.VisitorVO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 访客服务实现类
 */
@Slf4j
@Service
public class VisitorServiceImpl implements VisitorService {

    private final EmailServiceFeign emailServiceFeign;
    private final CaptchaServiceFeign captchaServiceFeign;
    private final VisitorMapper visitorMapper;
    private final TokenService tokenService;
    private final CacheService cacheService;

    public VisitorServiceImpl(VisitorMapper visitorMapper, 
                              EmailServiceFeign emailServiceFeign, 
                              CaptchaServiceFeign captchaServiceFeign,
                              TokenService tokenService,
                              CacheService cacheService) {
        this.visitorMapper = visitorMapper;
        this.emailServiceFeign = emailServiceFeign;
        this.captchaServiceFeign = captchaServiceFeign;
        this.tokenService = tokenService;
        this.cacheService = cacheService;
    }

    /**
     * 访客注册（带验证码）
     *
     * @param registerDTO 注册信息
     */
    @Override
    public Result register(VisitorRegisterDTO registerDTO) {
        log.info("访客注册：账号={}", registerDTO.getAccount());

        // 1. 验证图形验证码
        Result captchaResult = captchaServiceFeign.verify(registerDTO.getCaptchaToken(), registerDTO.getCaptchaCode());
        if (!"success".equals(captchaResult.getMsg())) {
            log.warn("图形验证码验证失败：token={},captchaCode={}", registerDTO.getCaptchaToken(), registerDTO.getCaptchaCode());
            return Result.error("图形验证码错误或已过期");
        }

        // 2. 验证邮箱验证码
        Result emailResult = emailServiceFeign.verifyCode(registerDTO.getEmail(), registerDTO.getEmailCode());
        if (!"success".equals(emailResult.getMsg())) {
            log.warn("邮箱验证码验证失败：email={}", registerDTO.getEmail());
            return Result.error("邮箱验证码错误或已过期");
        }

        // 3. 处理数据
        Visitor visitor = new Visitor();
        visitor.setName(registerDTO.getName());
        visitor.setAccount(registerDTO.getAccount());
        visitor.setEmail(registerDTO.getEmail());
        visitor.setPassword(Argon2idUtil.hash(registerDTO.getPassword()));
        // 默认为普通用户
        visitor.setVipLevel(0);
        // 设置默认头像
        visitor.setAvatar(DefaultUrlConfig.VISITOR_AVATAR_URL);
        visitor.setCreateTime(LocalDateTime.now());
        visitor.setUpdateTime(LocalDateTime.now());

        // 4. 执行插入操作
        try {
            int result = visitorMapper.insert(visitor);
            if (result == 1) {
                log.info("访客注册成功：ID={}", visitor.getId());
                VisitorVO vo = new VisitorVO();
                BeanUtils.copyProperties(visitor, vo);
                vo.setVipLevelName(getVipLevelName(visitor.getVipLevel()));
                return Result.success(vo);
            } else {
                return Result.buildResult(BizCodeEnum.FAIL);
            }
        } catch (Exception e) {
            //当用户名、用户账号、邮箱相同时，会抛异常，这里捕获
            log.error("访客注册失败：{}", e.getMessage());
            return Result.buildResult(BizCodeEnum.USER_EXIST);
        }
    }


    /**
     * 访客登录
     *
     * @param account  账号
     * @param password 密码
     * @return 登录结果
     */
    @Override
    public Result login(String account, String password) {
        log.info("访客登录：账号={}", account);

        //查询访客信息
        LambdaQueryWrapper<Visitor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Visitor::getAccount, account);
        Visitor visitor = visitorMapper.selectOne(queryWrapper);

        //判断访客是否存在
        if (visitor == null || Boolean.TRUE.equals(visitor.getIsDel())) {
            log.warn("访客不存在：账号={}", account);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        //验证密码
        boolean ok = Argon2idUtil.verify(visitor.getPassword(), password);

        if (!ok) {
            log.warn("密码错误：账号={}", account);
            return Result.buildResult(BizCodeEnum.USER_ACCOUNT_ERROR);
        }

        log.info("访客登录成功：账号={}, 姓名={}", account, visitor.getName());
        //生成token
        LoginUser loginUser = LoginUser.builder()
                .id(visitor.getId())
                .name(visitor.getName())
                .avatar(visitor.getAvatar())
                .account(visitor.getAccount())
                .role(UserRole.VISITOR)
                .build();
        String token = JWTUtil.geneJsonWebToken(loginUser);
        
        // 删除之前的 token（如果有）
        tokenService.deleteUserTokens(UserRole.VISITOR.getCode(), visitor.getId());
        // 存储新 token 到 Redis（24小时过期）
        String tokenKey = tokenService.generateTokenKey(UserRole.VISITOR.getCode(), visitor.getId());
        tokenService.saveToken(tokenKey, token, 24 * 60 * 60);
        
        return Result.success(token);
    }

    /**
     * 访客退出登录
     *
     * @param visitorId 访客ID
     * @return 退出结果
     */
    @Override
    public Result logout(Integer visitorId) {
        log.info("访客退出登录：ID={}", visitorId);
        
        if (visitorId == null) {
            return Result.error("访客ID不能为空");
        }
        
        // 删除 token
        tokenService.deleteUserTokens(UserRole.VISITOR.getCode(), visitorId);
        
        return Result.success("退出成功");
    }

    /**
     * 获取访客信息
     *
     * @param visitorId 访客ID
     * @return 访客信息
     */
    @Override
    public Result getVisitorInfo(Integer visitorId) {
        log.info("获取访客信息：ID={}", visitorId);

        // 先从缓存获取
        String cacheKey = CacheConstants.buildVisitorDetailKey(visitorId);
        VisitorVO cachedVo = cacheService.get(cacheKey, VisitorVO.class);
        if (cachedVo != null) {
            log.info("从缓存获取访客信息：ID={}", visitorId);
            return Result.success(cachedVo);
        }

        // 缓存未命中，查询数据库
        Visitor visitor = visitorMapper.selectById(visitorId);
        if (visitor == null) {
            log.warn("访客不存在：ID={}", visitorId);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        VisitorVO vo = new VisitorVO();
        BeanUtils.copyProperties(visitor, vo);
        vo.setVipLevelName(getVipLevelName(visitor.getVipLevel()));
        
        // 存入缓存
        cacheService.set(cacheKey, vo, CacheConstants.USER_DETAIL_TTL);
        log.info("访客信息已缓存：key={}", cacheKey);
        
        return Result.success(vo);
    }

    /**
     * 修改访客信息(不可以修改密码)
     *
     * @param visitorDTO 访客信息
     * @return 修改结果
     */
    @Override
    public Result updateVisitor(VisitorDTO visitorDTO) {
        log.info("修改访客信息：ID={}", visitorDTO.getId());

        // 检查访客是否存在
        Visitor existingVisitor = visitorMapper.selectById(visitorDTO.getId());
        if (existingVisitor == null) {
            log.warn("访客不存在：ID={}", visitorDTO.getId());
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        // 使用工具类复制非空属性，忽略 password、id、createTime、vipLevel
        BeanUtils.copyProperties(visitorDTO, existingVisitor, "password", "id", "createTime", "vipLevel");
        // 设置更新时间
        existingVisitor.setUpdateTime(LocalDateTime.now());

        int result = visitorMapper.update(existingVisitor);
        if (result > 0) {
            // 删除缓存
            String detailKey = CacheConstants.buildVisitorDetailKey(visitorDTO.getId());
            String nameAvatarKey = CacheConstants.buildVisitorNameAvatarKey(visitorDTO.getId());
            cacheService.delete(detailKey);
            cacheService.delete(nameAvatarKey);
            log.info("访客信息更新成功，已删除缓存：ID={}", visitorDTO.getId());
        }
        if (result == 1) {
            log.info("修改访客信息成功：ID={}", existingVisitor.getId());
            return Result.success("修改成功");
        } else {
            log.error("修改访客信息失败：ID={}", existingVisitor.getId());
            return Result.error("修改失败");
        }
    }


    /**
     * 修改访客密码
     *
     * @param visitorId 访客ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @Override
    public Result updatePassword(Integer visitorId, String oldPassword, String newPassword) {
        log.info("修改访客密码：ID={}", visitorId);

        // 1. 检查访客是否存在
        Visitor visitor = visitorMapper.selectById(visitorId);
        if (visitor == null) {
            log.warn("访客不存在：ID={}", visitorId);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        // 2. 验证旧密码是否正确
        boolean isOldPasswordValid = Argon2idUtil.verify(visitor.getPassword(), oldPassword);
        if (!isOldPasswordValid) {
            log.warn("旧密码验证失败：ID={}", visitorId);
            return Result.error("旧密码错误");
        }

        // 3. 加密新密码并更新
        String hashedPassword = Argon2idUtil.hash(newPassword);
        visitor.setPassword(hashedPassword);
        visitor.setUpdateTime(LocalDateTime.now());

        int result = visitorMapper.update(visitor);
        if (result == 1) {
            log.info("修改访客密码成功：ID={}", visitorId);
            return Result.success("密码修改成功");
        } else {
            log.error("修改访客密码失败：ID={}", visitorId);
            return Result.error("密码修改失败");
        }
    }

    @Override
    public Result updatePasswordByEmail(PasswordUpdateEmailDTO passwordUpdateEmailDTO) {
        // 验证邮箱验证码
        Result result = emailServiceFeign.verifyCode(passwordUpdateEmailDTO.getEmail(), passwordUpdateEmailDTO.getCode());
        if(!"success".equals(result.getMsg())){
            return result;
        }
        
        // 获取用户ID：优先使用ID，否则通过账号查询
        Integer userId = passwordUpdateEmailDTO.getId();
        if (userId == null && passwordUpdateEmailDTO.getAccount() != null) {
            // 忘记密码场景：通过账号查找用户
            LambdaQueryWrapper<Visitor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Visitor::getAccount, passwordUpdateEmailDTO.getAccount());
            Visitor visitor = visitorMapper.selectOne(queryWrapper);
            if (visitor == null) {
                log.warn("账号不存在：account={}", passwordUpdateEmailDTO.getAccount());
                return Result.error("账号不存在");
            }
            // 验证邮箱是否匹配
            if (!passwordUpdateEmailDTO.getEmail().equals(visitor.getEmail())) {
                log.warn("邮箱与账号不匹配：account={}", passwordUpdateEmailDTO.getAccount());
                return Result.error("邮箱与账号不匹配");
            }
            userId = visitor.getId();
        }
        
        if (userId == null) {
            log.warn("缺少用户ID或账号");
            return Result.error("缺少用户信息");
        }
        
        Visitor visitor = new Visitor();
        visitor.setId(userId);
        visitor.setPassword(Argon2idUtil.hash(passwordUpdateEmailDTO.getNewPassword()));
        visitor.setUpdateTime(LocalDateTime.now());

        int number = visitorMapper.update(visitor);

        if(number != 1){
            log.info("访客密码修改失败，id={}", userId);
            return Result.error("密码修改失败");
        }else{
            log.info("修改访客密码成功：ID={}", userId);
            return Result.success("访客密码修改成功");
        }
    }

    /**
     * 删除访客账号（通过邮箱验证码验证，真删除）
     *
     * @param dto 删除信息
     * @return 删除结果
     */
    @Override
    public Result deleteVisitor(VisitorDeleteDTO dto) {
        log.info("删除访客账号：ID={}", dto.getId());

        // 1. 检查访客是否存在
        Visitor visitor = visitorMapper.selectById(dto.getId());
        if (visitor == null) {
            log.warn("访客不存在：ID={}", dto.getId());
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        // 2. 验证邮箱是否匹配
        if (!dto.getEmail().equals(visitor.getEmail())) {
            log.warn("邮箱不匹配：ID={}, 请求邮箱={}, 实际邮箱={}", dto.getId(), dto.getEmail(), visitor.getEmail());
            return Result.error("邮箱不匹配");
        }

        // 3. 验证邮箱验证码
        Result emailResult = emailServiceFeign.verifyCode(dto.getEmail(), dto.getCode());
        if (!"success".equals(emailResult.getMsg())) {
            log.warn("邮箱验证码验证失败：email={}", dto.getEmail());
            return Result.error("邮箱验证码错误或已过期");
        }

        // 4. 执行真删除
        int result = visitorMapper.deleteById(dto.getId());
        if (result == 1) {
            log.info("访客账号删除成功：ID={}", dto.getId());
            return Result.success("账号删除成功");
        } else {
            log.error("访客账号删除失败：ID={}", dto.getId());
            return Result.error("账号删除失败");
        }
    }

    /**
     * 获取VIP级别名称
     */
    private String getVipLevelName(Integer vipLevel) {
        if (vipLevel == null) {
            return "普通用户";
        }
        return switch (vipLevel) {
            case 1 -> "VIP1";
            case 2 -> "VIP2";
            case 3 -> "VIP3";
            case 4 -> "金主";
            default -> "普通用户";
        };
    }

    @Override
    public Result getNameAndAvatar(Integer visitorId) {
        log.info("获取访客名称和头像：ID={}", visitorId);
        
        // 先从缓存获取
        String cacheKey = CacheConstants.buildVisitorNameAvatarKey(visitorId);
        Map<String, String> cachedMap = cacheService.get(cacheKey, Map.class);
        if (cachedMap != null) {
            log.info("从缓存获取访客名称和头像：ID={}", visitorId);
            return Result.success(cachedMap);
        }
        
        // 缓存未命中，查询数据库
        Map<String, String> map = new HashMap<>();
        Visitor visitor = visitorMapper.selectById(visitorId);
        if (visitor == null) {
            log.warn("访客不存在：ID={}", visitorId);
            return Result.error("访客不存在");
        }
        map.put("name", visitor.getName());
        map.put("avatar", visitor.getAvatar());
        
        // 存入缓存
        cacheService.set(cacheKey, map, CacheConstants.USER_NAME_AVATAR_TTL);
        log.info("访客名称和头像已缓存：key={}", cacheKey);
        
        return Result.success(map);
    }
}