package com.wang.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wang.common.config.DefaultUrlConfig;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.UserRole;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.common.utils.Argon2idUtil;
import org.springframework.beans.BeanUtils;
import com.wang.common.utils.JWTUtil;
import com.wang.commonserver.service.CaptchaService;
import com.wang.commonserver.service.EmailService;
import com.wang.manage.mapper.AuthorMapper;
import com.wang.manage.service.AuthorService;
import com.wang.pojo.dto.AuthorDTO;
import com.wang.pojo.dto.AuthorRegisterDTO;
import com.wang.pojo.dto.PasswordUpdateEmailDTO;
import com.wang.pojo.entity.Author;
import com.wang.pojo.vo.AuthorVO;
import com.wang.pojo.vo.VisitorAuthorVO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 作者服务实现类
 */
@Slf4j
@Service
public class AuthorServiceImpl implements AuthorService {

    private final EmailService emailService;
    private final CaptchaService captchaService;
    private final AuthorMapper authorMapper;
    private final DefaultUrlConfig  defaultUrlConfig;

    public AuthorServiceImpl(AuthorMapper authorMapper, EmailService emailService, CaptchaService captchaService, DefaultUrlConfig defaultUrlConfig) {
        this.authorMapper = authorMapper;
        this.emailService = emailService;
        this.captchaService = captchaService;
        this.defaultUrlConfig = defaultUrlConfig;
    }

    /**
     * 作者注册（带验证码）
     *
     * @param registerDTO 注册信息
     */
    @Override
    public Result register(AuthorRegisterDTO registerDTO) {
        log.info("作者注册：账号={}", registerDTO.getAccount());

        // 1. 验证图形验证码
        boolean captchaValid = captchaService.verify(registerDTO.getCaptchaToken(), registerDTO.getCaptchaCode());
        if (!captchaValid) {
            log.warn("图形验证码验证失败：token={},captchaCode={}", registerDTO.getCaptchaToken(),registerDTO.getCaptchaCode());
            return Result.error("图形验证码错误或已过期");
        }

        // 2. 验证邮箱验证码
        Result emailResult = emailService.verifyCode(registerDTO.getEmail(), registerDTO.getEmailCode());
        if (!"success".equals(emailResult.getMsg())) {
            log.warn("邮箱验证码验证失败：email={}", registerDTO.getEmail());
            return Result.error("邮箱验证码错误或已过期");
        }

        // 3. 处理数据
        Author author = new Author();
        author.setName(registerDTO.getName());
        author.setAccount(registerDTO.getAccount());
        author.setEmail(registerDTO.getEmail());
        author.setPassword(Argon2idUtil.hash(registerDTO.getPassword()));
        // 默认等级为执笔者
        author.setRank(1);
        // 默认头像
        author.setAvatar(defaultUrlConfig.getAuthorAvatarUrl());
        author.setCreateTime(LocalDateTime.now());
        author.setUpdateTime(LocalDateTime.now());

        // 4. 执行插入操作
        try {
            int result = authorMapper.insert(author);
            if (result == 1) {
                log.info("作者注册成功：ID={}", author.getId());
                AuthorVO vo = new AuthorVO();
                BeanUtils.copyProperties(author, vo);
                return Result.success(vo);
            } else {
                return Result.buildResult(BizCodeEnum.FAIL);
            }
        } catch (Exception e) {
            //当用户名、用户账号、邮箱相同时，会抛异常，这里捕获
            log.error("作者注册失败：{}", e.getMessage());
            return Result.buildResult(BizCodeEnum.USER_EXIST);
        }
    }


    /**
     * 作者登录
     *
     * @param account  账号
     * @param password 密码
     * @return 登录结果
     */
    @Override
    public Result login(String account, String password) {
        log.info("作者登录：账号={}", account);

        //查询作者信息
        LambdaQueryWrapper<Author> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Author::getAccount, account);
        Author author = authorMapper.selectOne(queryWrapper);

        //判断作者是否存在
        if (author == null  || author.getIsDel()) {
            log.warn("作者不存在：账号={}", account);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        //验证密码
        boolean ok = Argon2idUtil.verify(author.getPassword(), password);

        if (!ok) {
            log.warn("密码错误：账号={}", account);
            return Result.buildResult(BizCodeEnum.USER_ACCOUNT_ERROR);
        }

        log.info("作者登录成功：账号={}, 姓名={}", account, author.getName());
        //生成token
        LoginUser loginUser = LoginUser.builder()
                .id(author.getId())
                .name(author.getName())
                .avatar(author.getAvatar())
                .account(author.getAccount())
                .role(UserRole.AUTHOR)
                .build();
        String token = JWTUtil.geneJsonWebToken(loginUser);
        return Result.success(token);
    }

    /**
     * 获取作者信息
     *
     * @param id 作者ID
     * @return 作者信息
     */
    @Override
    public Result getAuthorInfo(Integer id) {
        log.info("获取作者信息：ID={}", id);

        Author author = authorMapper.selectById(id);
        if (author == null || author.getIsDel()) {
            log.warn("作者不存在：ID={}", id);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        AuthorVO vo = new AuthorVO();
        BeanUtils.copyProperties(author, vo);
        return Result.success(vo);
    }


    /**
     * 删除作者（逻辑删除）
     *
     * @param id 作者ID
     * @return 删除结果
     */
    @Override
    public Result deleteAuthor(Integer id) {
        log.info("删除作者：ID={}", id);

        //检查作者是否存在
        Author author = authorMapper.selectById(id);
        if (author == null) {
            log.info("作者不存在：ID={}", id);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        //执行逻辑删除操作
        author.setIsDel(true);
        int result = authorMapper.update(author);
        if (result == 1) {
            log.info("删除作者成功：ID={}, 姓名={}", id, author.getName());
            return Result.success("删除成功");
        } else {
            log.error("删除作者失败：ID={}", id);
            return Result.error("删除失败");
        }
    }

    /**
     * 修改作者信息(不可以修改其他作者的密码)
     *
     * @param authorDTO 作者信息
     * @return 修改结果
     */
    @Override
    public Result updateAuthor(AuthorDTO authorDTO) {
        log.info("修改作者信息：ID={}", authorDTO.getId());

        // 检查作者是否存在
        Author author = authorMapper.selectById(authorDTO.getId());
        if (author == null || author.getIsDel()) {
            log.warn("作者不存在：ID={}", authorDTO.getId());
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        // 使用工具类复制非空属性，忽略 password、id、createTime、isDel、email
        BeanUtils.copyProperties(authorDTO, author, "password", "id", "createTime", "isDel", "email");
        // 设置更新时间
        author.setUpdateTime(LocalDateTime.now());

        int result = authorMapper.update(author);
        if (result == 1) {
            log.info("修改作者信息成功：ID={}", author.getId());
            return Result.success("修改成功");
        } else {
            log.error("修改作者信息失败：ID={}", author.getId());
            return Result.error("修改失败");
        }
    }


    /**
     * 修改作者密码
     *
     * @param id 作者ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @Override
    public Result updatePassword(Integer id, String oldPassword, String newPassword) {
        log.info("修改作者密码：ID={}", id);

        // 1. 检查作者是否存在
        Author author = authorMapper.selectById(id);
        if (author == null || author.getIsDel()) {
            log.warn("作者不存在：ID={}", id);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        // 2. 验证旧密码是否正确
        boolean isOldPasswordValid = Argon2idUtil.verify(author.getPassword(), oldPassword);
        if (!isOldPasswordValid) {
            log.warn("旧密码验证失败：ID={}", id);
            return Result.error("旧密码错误");
        }

        // 3. 加密新密码并更新
        String hashedPassword = Argon2idUtil.hash(newPassword);
        author.setPassword(hashedPassword);
        author.setUpdateTime(LocalDateTime.now());

        int result = authorMapper.update(author);
        if (result == 1) {
            log.info("修改作者密码成功：ID={}", id);
            return Result.success("密码修改成功");
        } else {
            log.error("修改作者密码失败：ID={}", id);
            return Result.error("密码修改失败");
        }
    }

    @Override
    public Result updatePasswordByEmail(PasswordUpdateEmailDTO passwordUpdateEmailDTO) {
        // 验证邮箱验证码
        Result result = emailService.verifyCode(passwordUpdateEmailDTO.getEmail(), passwordUpdateEmailDTO.getCode());
        if(!"success".equals(result.getMsg())){
            return result;
        }
        
        // 获取用户ID：优先使用ID，否则通过账号查询
        Integer userId = passwordUpdateEmailDTO.getId();
        if (userId == null && passwordUpdateEmailDTO.getAccount() != null) {
            // 忘记密码场景：通过账号查找用户
            LambdaQueryWrapper<Author> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Author::getAccount, passwordUpdateEmailDTO.getAccount());
            Author author = authorMapper.selectOne(queryWrapper);
            if (author == null) {
                log.warn("账号不存在：account={}", passwordUpdateEmailDTO.getAccount());
                return Result.error("账号不存在");
            }
            // 验证邮箱是否匹配
            if (!passwordUpdateEmailDTO.getEmail().equals(author.getEmail())) {
                log.warn("邮箱与账号不匹配：account={}", passwordUpdateEmailDTO.getAccount());
                return Result.error("邮箱与账号不匹配");
            }
            userId = author.getId();
        }
        
        if (userId == null) {
            log.warn("缺少用户ID或账号");
            return Result.error("缺少用户信息");
        }
        
        Author author = new Author();
        author.setId(userId);
        author.setPassword(Argon2idUtil.hash(passwordUpdateEmailDTO.getNewPassword()));
        author.setUpdateTime(LocalDateTime.now());

        int number = authorMapper.update(author);

        if(number != 1){
            log.info("作者密码修改失败，id={}", userId);
            return Result.error("密码修改失败");
        }else{
            log.info("修改作者密码成功：ID={}", userId);
            return Result.success("作者密码修改成功");
        }
    }

    @Override
    public Result getNameAndAvatar(Integer id) {
        Author author = authorMapper.selectById(id);
        if (author == null || author.getIsDel()) {
            return Result.error("作者不存在");
        }

        VisitorAuthorVO vo = new VisitorAuthorVO();
        vo.setId(author.getId());
        vo.setName(author.getName());
        vo.setAvatar(author.getAvatar());
        vo.setRank(author.getRank());
        vo.setIntroduction(author.getIntroduction());
        vo.setNovelCount(author.getNovelCount() != null ? author.getNovelCount() : 0);
        
        return Result.success(vo);
    }
}