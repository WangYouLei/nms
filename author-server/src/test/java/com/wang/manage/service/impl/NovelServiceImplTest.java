package com.wang.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.interceptor.LoginInterceptor;
import com.wang.common.model.LoginUser;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.manage.mapper.NovelMapper;
import com.wang.pojo.dto.NovelDTO;
import com.wang.pojo.entity.Novel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NovelService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("小说服务测试")
class NovelServiceImplTest {

    @Mock
    private NovelMapper novelMapper;

    @InjectMocks
    private NovelServiceImpl novelService;

    private NovelDTO novelDTO;
    private Novel novel;
    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        // 准备登录用户
        loginUser = LoginUser.builder()
                .id(1)
                .name("测试作者")
                .account("author001")
                .build();

        // 设置 ThreadLocal
        LoginInterceptor.THREAD_LOCAL.set(loginUser);

        // 准备测试数据
        novelDTO = new NovelDTO();
        novelDTO.setName("测试小说");
        novelDTO.setSubName("副标题");
        novelDTO.setTags("玄幻,热血");
        novelDTO.setIntroduction("这是一本测试小说");
        novelDTO.setUrl("https://cover.example.com/test.jpg");

        novel = new Novel();
        novel.setId(1);
        novel.setName("测试小说");
        novel.setSubName("副标题");
        novel.setTags("玄幻,热血");
        novel.setIntroduction("这是一本测试小说");
        novel.setAuthorId(1);
        novel.setUrl("https://cover.example.com/test.jpg");
        novel.setCreateTime(LocalDateTime.now());
        novel.setUpdateTime(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        // 清理 ThreadLocal
        LoginInterceptor.THREAD_LOCAL.remove();
    }

    @Test
    @DisplayName("新增小说成功")
    void testAddNovelSuccess() {
        // Given: 登录用户且小说名称不存在
        when(novelMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(novelMapper.insert(any(Novel.class))).thenAnswer(invocation -> {
            Novel n = invocation.getArgument(0);
            n.setId(1);
            return 1;
        });

        // When: 执行新增
        Result result = novelService.addNovel(novelDTO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof Novel);

        Novel insertedNovel = (Novel) result.getData();
        assertEquals("测试小说", insertedNovel.getName());
        assertEquals(1, insertedNovel.getAuthorId());

        verify(novelMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(novelMapper, times(1)).insert(any(Novel.class));
    }

    @Test
    @DisplayName("新增小说失败 - 名称已存在")
    void testAddNovelFail_NameExists() {
        // Given: 登录用户但小说名称已存在
        when(novelMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When: 执行新增
        Result result = novelService.addNovel(novelDTO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(BizCodeEnum.NOVEL_TITLE_EXIST.getCode(), result.getCode());

        verify(novelMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(novelMapper, never()).insert(any(Novel.class));
    }

    @Test
    @DisplayName("删除小说成功")
    void testDeleteNovelSuccess() {
        // Given: 小说存在且属于当前用户
        when(novelMapper.selectById(1)).thenReturn(novel);
        when(novelMapper.deleteById(1)).thenReturn(1);

        // When: 执行删除
        Result result = novelService.deleteNovel(1);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());

        verify(novelMapper, times(1)).selectById(1);
        verify(novelMapper, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("删除小说失败 - 小说不存在")
    void testDeleteNovelFail_NotFound() {
        // Given: 小说不存在
        when(novelMapper.selectById(999)).thenReturn(null);

        // When: 执行删除
        Result result = novelService.deleteNovel(999);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(BizCodeEnum.NOVEL_NOT_FOUND.getCode(), result.getCode());

        verify(novelMapper, times(1)).selectById(999);
        verify(novelMapper, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("删除小说失败 - 无权限删除他人小说")
    void testDeleteNovelFail_NoPermission() {
        // Given: 小说存在但不属于当前用户
        Novel otherUserNovel = new Novel();
        otherUserNovel.setId(1);
        otherUserNovel.setAuthorId(999); // 其他用户

        when(novelMapper.selectById(1)).thenReturn(otherUserNovel);

        // When: 执行删除
        Result result = novelService.deleteNovel(1);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(BizCodeEnum.PERMISSION_DENIED.getCode(), result.getCode());

        verify(novelMapper, times(1)).selectById(1);
        verify(novelMapper, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("分页查询小说列表成功")
    void testGetNovelListSuccess() {
        // Given: 登录用户
        List<Novel> novelList = new ArrayList<>();
        novelList.add(novel);

        Page<Novel> page = new Page<>(1, 10);
        page.setRecords(novelList);
        page.setTotal(1);

        when(novelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // When: 执行查询
        Result result = novelService.getNovelList(1, 10);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof PageResult);

        PageResult<Novel> pageResult = (PageResult<Novel>) result.getData();
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());

        verify(novelMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询小说列表 - 默认参数处理")
    void testGetNovelList_DefaultParams() {
        // Given: 登录用户，传入非法参数
        Page<Novel> page = new Page<>(1, 10);
        page.setRecords(new ArrayList<>());
        page.setTotal(0);

        when(novelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // When: 传入 null 或非法参数
        Result result = novelService.getNovelList(null, null);

        // Then: 应使用默认值
        assertNotNull(result);
        assertEquals(1, result.getCode());

        verify(novelMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("模糊查询小说成功")
    void testSearchNovelsSuccess() {
        // Given: 登录用户
        List<Novel> novelList = new ArrayList<>();
        novelList.add(novel);

        Page<Novel> page = new Page<>(1, 10);
        page.setRecords(novelList);
        page.setTotal(1);

        when(novelMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // When: 执行模糊查询
        Result result = novelService.searchNovels("测试", null, 1, 10);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());

        verify(novelMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("修改小说成功")
    void testUpdateNovelSuccess() {
        // Given: 小说存在且属于当前用户
        NovelDTO updateDTO = new NovelDTO();
        updateDTO.setId(1);
        updateDTO.setName("新名字");
        updateDTO.setIntroduction("新的简介");

        when(novelMapper.selectById(1)).thenReturn(novel);
        when(novelMapper.updateById(any(Novel.class))).thenReturn(1);

        // When: 执行修改
        Result result = novelService.updateNovel(updateDTO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());

        verify(novelMapper, times(1)).selectById(1);
        verify(novelMapper, times(1)).updateById(any(Novel.class));
    }

    @Test
    @DisplayName("修改小说失败 - 小说不存在")
    void testUpdateNovelFail_NotFound() {
        // Given: 小说不存在
        NovelDTO updateDTO = new NovelDTO();
        updateDTO.setId(999);
        updateDTO.setName("新名字");

        when(novelMapper.selectById(999)).thenReturn(null);

        // When: 执行修改
        Result result = novelService.updateNovel(updateDTO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(BizCodeEnum.NOVEL_NOT_FOUND.getCode(), result.getCode());

        verify(novelMapper, times(1)).selectById(999);
        verify(novelMapper, never()).updateById(any(Novel.class));
    }

    @Test
    @DisplayName("修改小说失败 - 无权限修改他人小说")
    void testUpdateNovelFail_NoPermission() {
        // Given: 小说存在但不属于当前用户
        Novel otherUserNovel = new Novel();
        otherUserNovel.setId(1);
        otherUserNovel.setAuthorId(999);

        NovelDTO updateDTO = new NovelDTO();
        updateDTO.setId(1);
        updateDTO.setName("新名字");

        when(novelMapper.selectById(1)).thenReturn(otherUserNovel);

        // When: 执行修改
        Result result = novelService.updateNovel(updateDTO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("无权修改他人的小说", result.getMsg());

        verify(novelMapper, times(1)).selectById(1);
        verify(novelMapper, never()).updateById(any(Novel.class));
    }

    @Test
    @DisplayName("修改小说失败 - 新名称已被其他小说使用")
    void testUpdateNovelFail_NameConflict() {
        // Given: 小说存在但新名称已被其他小说使用
        NovelDTO updateDTO = new NovelDTO();
        updateDTO.setId(1);
        updateDTO.setName("已存在的名字");

        when(novelMapper.selectById(1)).thenReturn(novel);
        when(novelMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When: 执行修改
        Result result = novelService.updateNovel(updateDTO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(BizCodeEnum.NOVEL_TITLE_EXIST.getCode(), result.getCode());

        verify(novelMapper, times(1)).selectById(1);
        verify(novelMapper, never()).updateById(any(Novel.class));
    }
}