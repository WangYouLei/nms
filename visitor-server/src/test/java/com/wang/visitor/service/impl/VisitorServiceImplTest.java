package com.wang.visitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.result.Result;
import com.wang.common.untils.Argon2idUtil;
import com.wang.pojo.dto.VisitorDTO;
import com.wang.pojo.entity.Visitor;
import com.wang.pojo.vo.VisitorVO;
import com.wang.visitor.mapper.VisitorMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * VisitorService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("访客服务测试")
class VisitorServiceImplTest {

    @Mock
    private VisitorMapper visitorMapper;

    @InjectMocks
    private VisitorServiceImpl visitorService;

    private VisitorDTO visitorDTO;
    private Visitor visitor;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        visitorDTO = new VisitorDTO();
        visitorDTO.setName("测试用户");
        visitorDTO.setAvatar("https://avatar.example.com/test.jpg");
        visitorDTO.setAccount("13800138001");
        visitorDTO.setPassword("123456");

        visitor = new Visitor();
        visitor.setId(1);
        visitor.setName("测试用户");
        visitor.setAvatar("https://avatar.example.com/test.jpg");
        visitor.setAccount("13800138001");
        visitor.setPassword(Argon2idUtil.hash("123456"));
        visitor.setVipLevel(0);
        visitor.setCreateTime(LocalDateTime.now());
        visitor.setUpdateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("注册成功 - 新用户注册")
    void testRegisterSuccess() {
        // Given: 账号不存在
        when(visitorMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(visitorMapper.insert(any(Visitor.class))).thenAnswer(invocation -> {
            Visitor v = invocation.getArgument(0);
            v.setId(1);
            return 1;
        });

        // When: 执行注册
        Result result = visitorService.register(visitorDTO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof VisitorVO);
        
        VisitorVO vo = (VisitorVO) result.getData();
        assertEquals("测试用户", vo.getName());
        assertEquals("13800138001", vo.getAccount());
        assertEquals(0, vo.getVipLevel());

        // 验证方法调用
        verify(visitorMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(visitorMapper, times(1)).insert(any(Visitor.class));
    }

    @Test
    @DisplayName("注册失败 - 账号已存在")
    void testRegisterFail_AccountExists() {
        // Given: 账号已存在
        when(visitorMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When: 执行注册
        Result result = visitorService.register(visitorDTO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("该账号已被注册", result.getMsg());

        // 验证 insert 未被调用
        verify(visitorMapper, never()).insert(any(Visitor.class));
    }

    @Test
    @DisplayName("登录成功")
    void testLoginSuccess() {
        // Given: 用户存在
        when(visitorMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(visitor);

        // When: 执行登录（使用静态 mock 来 mock Argon2idUtil.verify）
        try (MockedStatic<Argon2idUtil> mockedStatic = mockStatic(Argon2idUtil.class)) {
            mockedStatic.when(() -> Argon2idUtil.verify(anyString(), anyString())).thenReturn(true);
            
            Result result = visitorService.login("13800138001", "123456");

            // Then: 验证结果
            assertNotNull(result);
            assertEquals(1, result.getCode());
            assertNotNull(result.getData());
            assertTrue(result.getData() instanceof String); // 返回 token
        }

        verify(visitorMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void testLoginFail_UserNotFound() {
        // Given: 用户不存在
        when(visitorMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When: 执行登录
        Result result = visitorService.login("13800138099", "123456");

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(BizCodeEnum.USER_NOT_FOUND.getCode(), result.getCode());
        assertEquals(BizCodeEnum.USER_NOT_FOUND.getMessage(), result.getMsg());

        verify(visitorMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void testLoginFail_WrongPassword() {
        // Given: 用户存在
        when(visitorMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(visitor);

        // When: 执行登录（密码错误）
        try (MockedStatic<Argon2idUtil> mockedStatic = mockStatic(Argon2idUtil.class)) {
            mockedStatic.when(() -> Argon2idUtil.verify(anyString(), anyString())).thenReturn(false);
            
            Result result = visitorService.login("13800138001", "wrong_password");

            // Then: 验证结果
            assertNotNull(result);
            assertEquals(BizCodeEnum.USER_ACCOUNT_ERROR.getCode(), result.getCode());
        }
    }

    @Test
    @DisplayName("获取访客信息成功")
    void testGetVisitorInfoSuccess() {
        // Given: 用户存在
        when(visitorMapper.selectById(1)).thenReturn(visitor);

        // When: 获取访客信息
        Result result = visitorService.getVisitorInfo(1);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof VisitorVO);

        VisitorVO vo = (VisitorVO) result.getData();
        assertEquals(1, vo.getId());
        assertEquals("测试用户", vo.getName());
        assertEquals("普通用户", vo.getVipLevelName());

        verify(visitorMapper, times(1)).selectById(1);
    }

    @Test
    @DisplayName("获取访客信息失败 - 用户不存在")
    void testGetVisitorInfoFail_UserNotFound() {
        // Given: 用户不存在
        when(visitorMapper.selectById(999)).thenReturn(null);

        // When: 获取访客信息
        Result result = visitorService.getVisitorInfo(999);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(BizCodeEnum.USER_NOT_FOUND.getCode(), result.getCode());

        verify(visitorMapper, times(1)).selectById(999);
    }

    @Test
    @DisplayName("修改访客信息成功")
    void testUpdateVisitorSuccess() {
        // Given: 用户存在
        VisitorDTO updateDTO = new VisitorDTO();
        updateDTO.setId(1);
        updateDTO.setName("新名字");
        updateDTO.setAvatar("https://avatar.example.com/new.jpg");

        when(visitorMapper.selectById(1)).thenReturn(visitor);
        when(visitorMapper.updateById(any(Visitor.class))).thenReturn(1);

        // When: 修改访客信息
        Result result = visitorService.updateVisitor(updateDTO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());

        verify(visitorMapper, times(1)).selectById(1);
        verify(visitorMapper, times(1)).updateById(any(Visitor.class));
    }

    @Test
    @DisplayName("修改密码成功")
    void testUpdatePasswordSuccess() {
        // Given: 用户存在且旧密码正确
        when(visitorMapper.selectById(1)).thenReturn(visitor);
        when(visitorMapper.updateById(any(Visitor.class))).thenReturn(1);

        // When: 修改密码
        try (MockedStatic<Argon2idUtil> mockedStatic = mockStatic(Argon2idUtil.class)) {
            mockedStatic.when(() -> Argon2idUtil.verify(anyString(), eq("123456"))).thenReturn(true);
            mockedStatic.when(() -> Argon2idUtil.hash(anyString())).thenReturn("new_hashed_password");

            Result result = visitorService.updatePassword(1, "123456", "newpassword");

            // Then: 验证结果
            assertNotNull(result);
            assertEquals(1, result.getCode());
            assertEquals("密码修改成功", result.getData());
        }

        verify(visitorMapper, times(1)).selectById(1);
        verify(visitorMapper, times(1)).updateById(any(Visitor.class));
    }

    @Test
    @DisplayName("修改密码失败 - 旧密码错误")
    void testUpdatePasswordFail_WrongOldPassword() {
        // Given: 用户存在但旧密码错误
        when(visitorMapper.selectById(1)).thenReturn(visitor);

        // When: 修改密码
        try (MockedStatic<Argon2idUtil> mockedStatic = mockStatic(Argon2idUtil.class)) {
            mockedStatic.when(() -> Argon2idUtil.verify(anyString(), eq("wrong_old_password"))).thenReturn(false);

            Result result = visitorService.updatePassword(1, "wrong_old_password", "newpassword");

            // Then: 验证结果
            assertNotNull(result);
            assertEquals(0, result.getCode());
            assertEquals("旧密码错误", result.getMsg());
        }

        // 验证 updateById 未被调用
        verify(visitorMapper, never()).updateById(any(Visitor.class));
    }

    @Test
    @DisplayName("VIP级别名称转换测试")
    void testVipLevelNameConversion() {
        // 测试各个VIP级别的名称
        String[] expectedNames = {"普通用户", "VIP1", "VIP2", "VIP3", "金主"};
        
        for (int i = 0; i < expectedNames.length; i++) {
            visitor.setVipLevel(i);
            when(visitorMapper.selectById(1)).thenReturn(visitor);
            
            Result result = visitorService.getVisitorInfo(1);
            VisitorVO vo = (VisitorVO) result.getData();
            
            assertEquals(expectedNames[i], vo.getVipLevelName(), 
                "VIP级别 " + i + " 的名称应为: " + expectedNames[i]);
        }
    }
}