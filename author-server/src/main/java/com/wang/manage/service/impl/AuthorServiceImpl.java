package com.wang.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.enums.FileUploadTypeEnum;
import com.wang.common.model.LoginUser;
import com.wang.common.result.Result;
import com.wang.common.untils.Argon2idUtil;
import com.wang.common.untils.JWTUtil;
import com.wang.manage.mapper.AuthorMapper;
import com.wang.manage.service.CommonService;
import com.wang.manage.service.AuthorService;
import com.wang.pojo.dto.AuthorDTO;
import com.wang.pojo.entity.Author;
import com.wang.pojo.vo.AuthorVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;


@Slf4j
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorMapper authorMapper;
    private final CommonService commonService;

    public AuthorServiceImpl(AuthorMapper authorMapper, CommonService commonService) {
        this.authorMapper = authorMapper;
        this.commonService = commonService;
    }

    /**
     * 添加作者
     *
     * @param authorDTO 作者信息
     */
    @Override
    public Result addAuthor(AuthorDTO authorDTO) {
        //处理数据  密码进行argon2加密
        Author author = new Author();
        BeanUtils.copyProperties(authorDTO, author);
        author.setCreateTime(LocalDateTime.now());
        author.setUpdateTime(LocalDateTime.now());
        // 设置默认等级为1（执笔者）
        if (author.getRank() == null) {
            author.setRank(1);
        }

        //argon2加密
        String hashedPassword = Argon2idUtil.hash(author.getPassword());
        author.setPassword(hashedPassword);

        //执行插入操作
        try {
            int result = authorMapper.insert(author);
            if (result == 1) {
                AuthorVO vo = new AuthorVO();
                BeanUtils.copyProperties(author, vo);
                return Result.success(vo);
            } else {
                return Result.buildResult(BizCodeEnum.FAIL);
            }
        } catch (Exception e) {//当用户名和用户账号相同时，会抛异常，这里捕获
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
        QueryWrapper<Author> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        Author author = authorMapper.selectOne(queryWrapper);

        //判断作者是否存在
        if (author == null) {
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
                .build();
        String token = JWTUtil.geneJsonWebToken(loginUser);
        AuthorVO vo = new AuthorVO();
        BeanUtils.copyProperties(author, vo);
        return Result.success(token);
    }

    /**
     * 作者退出登入
     */
    @PostMapping("logout")
    @ApiOperation("作者退出登入")
    public Result logout() {
        log.info("作者退出登入");
        return Result.success("退出登入成功");
    }

    /**
     * 删除作者（逻辑删除）
     * 由于使用了@TableLogic注解，MyBatis-Plus会自动将DELETE转为UPDATE
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

        //执行逻辑删除操作（MyBatis-Plus会自动转为UPDATE is_del=1）
        int result = authorMapper.deleteById(id);
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
        log.info("修改作者信息：ID={}, 姓名={}", authorDTO.getId(), authorDTO.getName());

        //检查作者是否存在
        Author existingAuthor = authorMapper.selectById(authorDTO.getId());
        if (existingAuthor == null) {
            log.warn("作者不存在：ID={}", authorDTO.getId());
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        //执行更新操作
        //将DTO中的数据复制到实体对象中
        BeanUtils.copyProperties(authorDTO, existingAuthor);
        existingAuthor.setUpdateTime(LocalDateTime.now());
        int result = authorMapper.updateById(existingAuthor);
        if (result == 1) {
            log.info("修改作者信息成功：ID={}", existingAuthor.getId());
            return Result.success("修改成功");
        } else {
            log.error("修改作者信息失败：ID={}", existingAuthor.getId());
            return Result.error("修改失败");
        }
    }

    /**
     * 更新作者头像
     * 上传新头像后删除旧头像
     *
     * @param authorId 作者ID
     * @param file      头像文件
     * @return 更新结果
     */
    @Override
    public Result updateAvatar(Integer authorId, MultipartFile file) {
        log.info("更新作者头像：ID={}", authorId);

        // 1. 检查作者是否存在
        Author author = authorMapper.selectById(authorId);
        if (author == null) {
            log.warn("作者不存在：ID={}", authorId);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        // 保存旧头像URL，用于删除
        String oldAvatarUrl = author.getAvatar();

        try {
            // 2. 上传新头像
            String newAvatarUrl = commonService.fileUpload(file, FileUploadTypeEnum.ADMIN_AVATAR.getCode());
            if (newAvatarUrl == null) {
                log.error("上传头像失败：ID={}", authorId);
                return Result.error("上传头像失败");
            }

            // 3. 更新数据库
            author.setAvatar(newAvatarUrl);
            author.setUpdateTime(LocalDateTime.now());
            int result = authorMapper.updateById(author);

            if (result == 1) {
                log.info("头像更新成功：ID={}, 新头像URL={}", authorId, newAvatarUrl);

                // 4. 删除旧头像（数据库更新成功后）
                if (StringUtils.hasText(oldAvatarUrl)) {
                    boolean deleted = commonService.deleteFile(oldAvatarUrl);
                    if (deleted) {
                        log.info("旧头像删除成功：{}", oldAvatarUrl);
                    }
                }

                return Result.success(newAvatarUrl);
            } else {
                log.error("头像更新失败：ID={}", authorId);
                return Result.error("头像更新失败");
            }
        } catch (Exception e) {
            log.error("更新头像异常：ID={}, 错误={}", authorId, e.getMessage(), e);
            return Result.error("更新头像失败：" + e.getMessage());
        }
    }

    /**
     * 修改作者密码
     *
     * @param id 作者ID
     * @param newPassword 新密码
     * @return 修改结果
     */
    @Override
    public Result updatePassword(Integer id, String newPassword) {
        log.info("修改作者密码：ID={}", id);

        //检查作者是否存在
        Author author = authorMapper.selectById(id);
        if (author == null) {
            log.warn("作者不存在：ID={}", id);
            return Result.buildResult(BizCodeEnum.USER_NOT_FOUND);
        }

        //加密新密码
        String hashedPassword = Argon2idUtil.hash(newPassword);
        author.setPassword(hashedPassword);
        author.setUpdateTime(LocalDateTime.now());

        int result = authorMapper.updateById(author);
        if (result == 1) {
            log.info("修改作者密码成功：ID={}", id);
            return Result.success("密码修改成功");
        } else {
            log.error("修改作者密码失败：ID={}", id);
            return Result.error("密码修改失败");
        }
    }

}