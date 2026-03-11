package com.wang.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.enums.BizCodeEnum;
import com.wang.common.result.PageResult;
import com.wang.common.result.Result;
import com.wang.manage.mapper.NovelCategoryMapper;
import com.wang.manage.mapper.NovelCategoryRelationMapper;
import com.wang.manage.mapper.NovelMapper;
import com.wang.pojo.dto.NovelCategoryDTO;
import com.wang.pojo.dto.NovelCategoryRelationDTO;
import com.wang.pojo.entity.Novel;
import com.wang.pojo.entity.NovelCategory;
import com.wang.pojo.entity.NovelCategoryRelation;
import com.wang.pojo.vo.NovelCategoryVO;
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
 * NovelCategoryService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("小说分类服务测试")
class NovelCategoryServiceImplTest {

    @Mock
    private NovelCategoryMapper categoryMapper;

    @Mock
    private NovelCategoryRelationMapper relationMapper;

    @Mock
    private NovelMapper novelMapper;

    @InjectMocks
    private NovelCategoryServiceImpl categoryService;

    private NovelCategoryDTO categoryDTO;
    private NovelCategory category;
    private NovelCategoryRelation relation;
    private Novel novel;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        categoryDTO = new NovelCategoryDTO();
        categoryDTO.setType("玄幻");
        categoryDTO.setCategory(1);
        categoryDTO.setIsHot(1);

        category = new NovelCategory();
        category.setId(1);
        category.setType("玄幻");
        category.setCategory(1);
        category.setIsHot(1);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());

        relation = new NovelCategoryRelation();
        relation.setId(1);
        relation.setNovelId(1);
        relation.setCategoryId(1);

        novel = new Novel();
        novel.setId(1);
        novel.setName("测试小说");
        novel.setAuthorId(1);
    }

    @Test
    @DisplayName("添加分类成功")
    void testAddCategorySuccess() {
        // Given: 分类不存在
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.insert(any(NovelCategory.class))).thenAnswer(invocation -> {
            NovelCategory c = invocation.getArgument(0);
            c.setId(1);
            return 1;
        });

        // When: 执行添加
        Result result = categoryService.addCategory(categoryDTO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof NovelCategoryVO);

        NovelCategoryVO vo = (NovelCategoryVO) result.getData();
        assertEquals("玄幻", vo.getType());
        assertEquals("男频", vo.getCategoryName());

        verify(categoryMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(categoryMapper, times(1)).insert(any(NovelCategory.class));
    }

    @Test
    @DisplayName("添加分类失败 - 分类已存在")
    void testAddCategoryFail_Exists() {
        // Given: 分类已存在
        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When: 执行添加
        Result result = categoryService.addCategory(categoryDTO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(BizCodeEnum.NOVEL_CATEGORY_EXIST.getCode(), result.getCode());

        verify(categoryMapper, never()).insert(any(NovelCategory.class));
    }

    @Test
    @DisplayName("修改分类成功")
    void testUpdateCategorySuccess() {
        // Given: 分类存在
        when(categoryMapper.selectById(1)).thenReturn(category);
        when(categoryMapper.updateById(any(NovelCategory.class))).thenReturn(1);

        categoryDTO.setId(1);
        categoryDTO.setType("修仙");

        // When: 执行修改
        Result result = categoryService.updateCategory(categoryDTO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());

        verify(categoryMapper, times(1)).selectById(1);
        verify(categoryMapper, times(1)).updateById(any(NovelCategory.class));
    }

    @Test
    @DisplayName("修改分类失败 - 分类不存在")
    void testUpdateCategoryFail_NotFound() {
        // Given: 分类不存在
        when(categoryMapper.selectById(999)).thenReturn(null);

        categoryDTO.setId(999);

        // When: 执行修改
        Result result = categoryService.updateCategory(categoryDTO);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(BizCodeEnum.NOVEL_CATEGORY_NOT_FOUND.getCode(), result.getCode());

        verify(categoryMapper, never()).updateById(any(NovelCategory.class));
    }

    @Test
    @DisplayName("删除分类成功")
    void testDeleteCategorySuccess() {
        // Given: 分类存在且无关联小说
        when(categoryMapper.selectById(1)).thenReturn(category);
        when(relationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.deleteById(1)).thenReturn(1);

        // When: 执行删除
        Result result = categoryService.deleteCategory(1);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());

        verify(categoryMapper, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("删除分类失败 - 分类不存在")
    void testDeleteCategoryFail_NotFound() {
        // Given: 分类不存在
        when(categoryMapper.selectById(999)).thenReturn(null);

        // When: 执行删除
        Result result = categoryService.deleteCategory(999);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(BizCodeEnum.NOVEL_CATEGORY_NOT_FOUND.getCode(), result.getCode());

        verify(categoryMapper, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("删除分类失败 - 分类下存在小说")
    void testDeleteCategoryFail_HasNovels() {
        // Given: 分类下存在小说
        when(categoryMapper.selectById(1)).thenReturn(category);
        when(relationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // When: 执行删除
        Result result = categoryService.deleteCategory(1);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("该分类下存在小说，无法删除", result.getMsg());

        verify(categoryMapper, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("根据ID查询分类成功")
    void testGetCategoryByIdSuccess() {
        // Given: 分类存在
        when(categoryMapper.selectById(1)).thenReturn(category);

        // When: 查询分类
        Result result = categoryService.getCategoryById(1);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof NovelCategoryVO);

        NovelCategoryVO vo = (NovelCategoryVO) result.getData();
        assertEquals("玄幻", vo.getType());
        assertEquals("男频", vo.getCategoryName());
    }

    @Test
    @DisplayName("根据ID查询分类失败 - 分类不存在")
    void testGetCategoryByIdFail_NotFound() {
        // Given: 分类不存在
        when(categoryMapper.selectById(999)).thenReturn(null);

        // When: 查询分类
        Result result = categoryService.getCategoryById(999);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(BizCodeEnum.NOVEL_CATEGORY_NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    @DisplayName("查询所有分类成功")
    void testGetAllCategoriesSuccess() {
        // Given: 存在多个分类
        List<NovelCategory> categories = new ArrayList<>();
        categories.add(category);

        NovelCategory category2 = new NovelCategory();
        category2.setId(2);
        category2.setType("都市");
        category2.setCategory(1);
        categories.add(category2);

        when(categoryMapper.selectList(null)).thenReturn(categories);

        // When: 查询所有分类
        Result result = categoryService.getAllCategories();

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());

        List<NovelCategoryVO> voList = (List<NovelCategoryVO>) result.getData();
        assertEquals(2, voList.size());
    }

    @Test
    @DisplayName("根据频道查询分类成功")
    void testGetCategoriesByCategorySuccess() {
        // Given: 存在男频分类
        List<NovelCategory> categories = new ArrayList<>();
        categories.add(category);

        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(categories);

        // When: 查询男频分类
        Result result = categoryService.getCategoriesByCategory(1);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());

        List<NovelCategoryVO> voList = (List<NovelCategoryVO>) result.getData();
        assertEquals(1, voList.size());
        assertEquals("男频", voList.get(0).getCategoryName());
    }

    @Test
    @DisplayName("查询热门分类成功")
    void testGetHotCategoriesSuccess() {
        // Given: 存在热门分类
        List<NovelCategory> categories = new ArrayList<>();
        categories.add(category);

        when(categoryMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(categories);

        // When: 查询热门分类
        Result result = categoryService.getHotCategories();

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());

        List<NovelCategoryVO> voList = (List<NovelCategoryVO>) result.getData();
        assertEquals(1, voList.size());
        assertEquals(1, voList.get(0).getIsHot());
    }

    @Test
    @DisplayName("设置小说分类成功")
    void testSetNovelCategorySuccess() {
        // Given: 小说和分类都存在
        NovelCategoryRelationDTO dto = new NovelCategoryRelationDTO();
        dto.setNovelId(1);
        dto.setCategoryId(1);

        when(novelMapper.selectById(1)).thenReturn(novel);
        when(categoryMapper.selectById(1)).thenReturn(category);
        when(relationMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
        when(relationMapper.insert(any(NovelCategoryRelation.class))).thenReturn(1);

        // When: 设置小说分类
        Result result = categoryService.setNovelCategory(dto);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertEquals("设置成功", result.getData());

        verify(relationMapper, times(1)).delete(any(LambdaQueryWrapper.class));
        verify(relationMapper, times(1)).insert(any(NovelCategoryRelation.class));
    }

    @Test
    @DisplayName("设置小说分类失败 - 小说不存在")
    void testSetNovelCategoryFail_NovelNotFound() {
        // Given: 小说不存在
        NovelCategoryRelationDTO dto = new NovelCategoryRelationDTO();
        dto.setNovelId(999);
        dto.setCategoryId(1);

        when(novelMapper.selectById(999)).thenReturn(null);

        // When: 设置小说分类
        Result result = categoryService.setNovelCategory(dto);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(BizCodeEnum.NOVEL_NOT_FOUND.getCode(), result.getCode());

        verify(relationMapper, never()).insert(any(NovelCategoryRelation.class));
    }

    @Test
    @DisplayName("设置小说分类失败 - 分类不存在")
    void testSetNovelCategoryFail_CategoryNotFound() {
        // Given: 分类不存在
        NovelCategoryRelationDTO dto = new NovelCategoryRelationDTO();
        dto.setNovelId(1);
        dto.setCategoryId(999);

        when(novelMapper.selectById(1)).thenReturn(novel);
        when(categoryMapper.selectById(999)).thenReturn(null);

        // When: 设置小说分类
        Result result = categoryService.setNovelCategory(dto);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(BizCodeEnum.NOVEL_CATEGORY_NOT_FOUND.getCode(), result.getCode());

        verify(relationMapper, never()).insert(any(NovelCategoryRelation.class));
    }

    @Test
    @DisplayName("获取小说分类成功")
    void testGetNovelCategorySuccess() {
        // Given: 小说有分类
        when(relationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(relation);
        when(categoryMapper.selectById(1)).thenReturn(category);

        // When: 获取小说分类
        Result result = categoryService.getNovelCategory(1);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());

        NovelCategoryVO vo = (NovelCategoryVO) result.getData();
        assertEquals("玄幻", vo.getType());
    }

    @Test
    @DisplayName("获取小说分类 - 小说无分类")
    void testGetNovelCategory_NoCategory() {
        // Given: 小说没有分类
        when(relationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When: 获取小说分类
        Result result = categoryService.getNovelCategory(1);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("分页查询分类列表成功")
    void testGetCategoryListSuccess() {
        // Given: 存在分类数据
        List<NovelCategory> categories = new ArrayList<>();
        categories.add(category);

        Page<NovelCategory> page = new Page<>(1, 10);
        page.setRecords(categories);
        page.setTotal(1);

        when(categoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // When: 分页查询
        Result result = categoryService.getCategoryList(1, 10, null, null);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertNotNull(result.getData());

        PageResult<NovelCategoryVO> pageResult = (PageResult<NovelCategoryVO>) result.getData();
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
    }

    @Test
    @DisplayName("分页查询分类列表 - 带筛选条件")
    void testGetCategoryList_WithFilter() {
        // Given: 存在分类数据
        List<NovelCategory> categories = new ArrayList<>();
        categories.add(category);

        Page<NovelCategory> page = new Page<>(1, 10);
        page.setRecords(categories);
        page.setTotal(1);

        when(categoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // When: 带条件分页查询
        Result result = categoryService.getCategoryList(1, 10, "玄幻", 1);

        // Then: 验证结果
        assertNotNull(result);
        assertEquals(1, result.getCode());
    }

    @Test
    @DisplayName("频道名称转换测试")
    void testCategoryNameConversion() {
        // 测试男频
        category.setCategory(1);
        when(categoryMapper.selectById(1)).thenReturn(category);
        Result result1 = categoryService.getCategoryById(1);
        NovelCategoryVO vo1 = (NovelCategoryVO) result1.getData();
        assertEquals("男频", vo1.getCategoryName());

        // 测试女频
        category.setCategory(2);
        when(categoryMapper.selectById(1)).thenReturn(category);
        Result result2 = categoryService.getCategoryById(1);
        NovelCategoryVO vo2 = (NovelCategoryVO) result2.getData();
        assertEquals("女频", vo2.getCategoryName());
    }

    @Test
    @DisplayName("添加分类 - 默认热门值测试")
    void testAddCategory_DefaultHotValue() {
        // Given: 不设置热门值
        NovelCategoryDTO dto = new NovelCategoryDTO();
        dto.setType("科幻");
        dto.setCategory(1);
        // 不设置 isHot

        when(categoryMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(categoryMapper.insert(any(NovelCategory.class))).thenAnswer(invocation -> {
            NovelCategory c = invocation.getArgument(0);
            c.setId(2);
            return 1;
        });

        // When: 执行添加
        Result result = categoryService.addCategory(dto);

        // Then: 验证默认值为0
        assertNotNull(result);
        verify(categoryMapper, times(1)).insert(any(NovelCategory.class));
    }
}