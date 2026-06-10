package com.wang.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.common.config.DefaultUrlConfig;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.UserRole;
import com.wang.common.event.AuthorUpdatedEvent;
import com.wang.manage.event.AuthorEventPublisher;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.common.utils.Argon2idUtil;
import com.wang.common.utils.JWTUtil;
import com.wang.common.service.TokenService;
import com.wang.common.service.CacheService;
import com.wang.common.constants.CacheConstants;
import com.wang.common.feign.CaptchaServiceFeign;
import com.wang.common.feign.EmailServiceFeign;
import com.wang.manage.mapper.AuthorMapper;
import com.wang.manage.service.AuthorService;
import com.wang.pojo.dto.AuthorDTO;
import com.wang.pojo.dto.AuthorRegisterDTO;
import com.wang.pojo.dto.PasswordUpdateEmailDTO;
import com.wang.pojo.entity.Author;
import com.wang.pojo.vo.AuthorVO;
import com.wang.pojo.vo.VisitorAuthorVO;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.LocalDateTime;

/**
 * 作者服务实现类
 */
@Slf4j
@Service
public class AuthorServiceImpl implements AuthorService {

    private final EmailServiceFeign emailServiceFeign;
    private final CaptchaServiceFeign captchaServiceFeign;
    private final AuthorMapper authorMapper;
    private final TokenService tokenService;
    private final CacheService cacheService;
    private final AuthorEventPublisher authorEventPublisher;

    public AuthorServiceImpl(AuthorMapper authorMapper, 
                             EmailServiceFeign emailServiceFeign,
                             CaptchaServiceFeign captchaServiceFeign,
                             TokenService tokenService,
                             CacheService cacheService,
                             AuthorEventPublisher authorEventPublisher) {
        this.authorMapper = authorMapper;
        this.emailServiceFeign = emailServiceFeign;
        this.captchaServiceFeign = captchaServiceFeign;
        this.tokenService = tokenService;
        this.cacheService = cacheService;
        this.authorEventPublisher = authorEventPublisher;
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
        Result captchaResult = captchaServiceFeign.verify(registerDTO.getCaptchaToken(), registerDTO.getCaptchaCode());
        if (!"success".equals(captchaResult.getMsg())) {
            log.warn("图形验证码验证失败：token={},captchaCode={}", registerDTO.getCaptchaToken(),registerDTO.getCaptchaCode());
            return Result.error("图形验证码错误或已过期");
        }

        // 2. 验证邮箱验证码
        Result emailResult = emailServiceFeign.verifyCode(registerDTO.getEmail(), registerDTO.getEmailCode());
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
        author.setAvatar(DefaultUrlConfig.AUTHOR_AVATAR_URL);
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
        
        // 删除之前的 token（如果有）
        tokenService.deleteUserTokens(UserRole.AUTHOR.getCode(), author.getId());
        // 存储新 token 到 Redis（24小时过期）
        String tokenKey = tokenService.generateTokenKey(UserRole.AUTHOR.getCode(), author.getId());
        tokenService.saveToken(tokenKey, token, 24 * 60 * 60);
        
        return Result.success(token);
    }

    /**
     * 作者退出登录
     *
     * @param authorId 作者ID
     * @return 退出结果
     */
    @Override
    public Result logout(Long authorId) {
        log.info("作者退出登录：ID={}", authorId);
        
        if (authorId == null) {
            return Result.error("作者ID不能为空");
        }
        
        // 删除 token
        tokenService.deleteUserTokens(UserRole.AUTHOR.getCode(), authorId);
        
        return Result.success("退出成功");
    }

    /**
     * 获取作者信息
     *
     * @param id 作者ID
     * @return 作者信息
     */
    @Override
    public Result getAuthorInfo(Long id) {
        log.info("获取作者信息：ID={}", id);

        // 先从缓存获取
        String cacheKey = CacheConstants.buildAuthorDetailKey(id);
        AuthorVO cachedVo = cacheService.get(cacheKey, AuthorVO.class);
        if (cachedVo != null) {
            log.info("从缓存获取作者信息：ID={}", id);
            return Result.success(cachedVo);
        }

        // 缓存未命中，查询数据库
        Author author = authorMapper.selectById(id);
        if (author == null || author.getIsDel()) {
            log.warn("作者不存在：ID={}", id);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        AuthorVO vo = new AuthorVO();
        BeanUtils.copyProperties(author, vo);
        
        // 存入缓存
        cacheService.set(cacheKey, vo, CacheConstants.USER_DETAIL_TTL);
        log.info("作者信息已缓存：key={}", cacheKey);
        
        return Result.success(vo);
    }


    /**
     * 删除作者（逻辑删除）
     *
     * @param id 作者ID
     * @return 删除结果
     */
    @Override
    public Result deleteAuthor(Long id) {
        log.info("删除作者：ID={}", id);

        //检查作者是否存在
        Author author = authorMapper.selectById(id);
        if (author == null) {
            log.info("作者不存在：ID={}", id);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        //执行逻辑删除操作
        author.setIsDel(true);
        int result = authorMapper.updateSelective(author);
        if (result == 1) {
            log.info("删除作者成功：ID={}, 姓名={}", id, author.getName());

            // 发布作者删除事件，通知 search-server 同步 ES 索引
            authorEventPublisher.publishAuthorUpdated(
                    new AuthorUpdatedEvent(author.getId(), "DELETE")
            );

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

        int result = authorMapper.updateSelective(author);
        if (result == 1) {
            // 删除缓存
            String detailKey = CacheConstants.buildAuthorDetailKey(authorDTO.getId());
            String nameAvatarKey = CacheConstants.buildAuthorNameAvatarKey(authorDTO.getId());
            cacheService.delete(detailKey);
            cacheService.delete(nameAvatarKey);
            log.info("修改作者信息成功，已删除缓存：ID={}", author.getId());

            // 发布作者信息变更事件，通知其他服务同步冗余字段
            authorEventPublisher.publishAuthorUpdated(
                    new AuthorUpdatedEvent(author.getId(), author.getName(), author.getAvatar(), author.getRank())
            );

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
    public Result updatePassword(Long id, String oldPassword, String newPassword) {
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

        int result = authorMapper.updateSelective(author);
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
        Result result = emailServiceFeign.verifyCode(passwordUpdateEmailDTO.getEmail(), passwordUpdateEmailDTO.getCode());
        if(!"success".equals(result.getMsg())){
            return result;
        }
        
        // 获取用户ID：优先使用ID，否则通过账号查询
        Long userId = passwordUpdateEmailDTO.getId();
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

        int number = authorMapper.updateSelective(author);

        if(number != 1){
            log.info("作者密码修改失败，id={}", userId);
            return Result.error("密码修改失败");
        }else{
            log.info("修改作者密码成功：ID={}", userId);
            return Result.success("作者密码修改成功");
        }
    }

    @Override
    public Result getNameAndAvatar(Long id) {
        // 先从缓存获取
        String cacheKey = CacheConstants.buildAuthorNameAvatarKey(id);
        VisitorAuthorVO cachedVo = cacheService.get(cacheKey, VisitorAuthorVO.class);
        if (cachedVo != null) {
            log.info("从缓存获取作者名称和头像：ID={}", id);
            return Result.success(cachedVo);
        }

        // 缓存未命中，查询数据库
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
        
        // 存入缓存
        cacheService.set(cacheKey, vo, CacheConstants.USER_NAME_AVATAR_TTL);
        log.info("作者名称和头像已缓存：key={}", cacheKey);
        
        return Result.success(vo);
    }

    @Override
    public Result getAuthorAvatar(Long authorId) {
        log.info("[内部调用] 获取作者头像：authorId={}", authorId);
        Author author = authorMapper.selectById(authorId);
        if (author == null || author.getIsDel()) {
            log.warn("[内部调用] 作者不存在：authorId={}", authorId);
            return Result.error("作者不存在");
        }
        return Result.success(author.getAvatar());
    }

    @Override
    public Result batchGetAuthorAvatars(List<Long> authorIds) {
        log.info("[内部调用] 批量获取作者头像：count={}", authorIds.size());
        if (authorIds.isEmpty()) {
            return Result.success(new HashMap<>());
        }
        List<Author> authors = authorMapper.selectBatchIds(authorIds);
        Map<Long, String> avatarMap = new HashMap<>();
        for (Author author : authors) {
            if (author != null && !author.getIsDel()) {
                avatarMap.put(author.getId(), author.getAvatar());
            }
        }
        log.info("[内部调用] 批量获取作者头像完成：请求{}个，返回{}个", authorIds.size(), avatarMap.size());
        return Result.success(avatarMap);
    }

    @Override
    public Result getAuthorBasicInfo(Long authorId) {
        log.info("[内部调用] 获取作者基本信息：authorId={}", authorId);
        Author author = authorMapper.selectById(authorId);
        if (author == null || author.getIsDel()) {
            log.warn("[内部调用] 作者不存在：authorId={}", authorId);
            return Result.error("作者不存在");
        }

        Map<String, Object> basicInfo = new HashMap<>();
        basicInfo.put("id", author.getId());
        basicInfo.put("name", author.getName());
        basicInfo.put("avatar", author.getAvatar());
        basicInfo.put("rank", author.getRank());
        basicInfo.put("introduction", author.getIntroduction());
        basicInfo.put("novelCount", author.getNovelCount() != null ? author.getNovelCount() : 0);
        return Result.success(basicInfo);
    }
}