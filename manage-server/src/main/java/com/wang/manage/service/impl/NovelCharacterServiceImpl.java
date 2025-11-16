package com.wang.manage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.common.enums.CharacterCategoryEnum;
import com.wang.manage.mapper.CharacterAbilityMapper;
import com.wang.manage.service.CharacterAbilityService;
import com.wang.pojo.dto.NovelCharacterDTO;
import com.wang.manage.mapper.NovelCharacterMapper;
import com.wang.manage.service.NovelCharacterService;
import com.wang.pojo.vo.CharacterAbilityVO;
import com.wang.pojo.vo.NovelCharacterVO;
import com.wang.pojo.entity.NovelCharacter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 小说角色Service实现类
 */
@Slf4j
@Service
public class NovelCharacterServiceImpl implements NovelCharacterService {
    
    @Autowired
    private NovelCharacterMapper novelCharacterMapper;

    @Autowired
    private CharacterAbilityMapper characterAbilityMapper;

    @Autowired
    private CharacterAbilityService characterAbilityService;

    @Transactional//开启事务，涉及多个表的操作
    @Override
    public boolean addNovelCharacter(NovelCharacterDTO novelCharacterDTO) {

        try {
            NovelCharacter novelCharacter = new NovelCharacter();
            BeanUtils.copyProperties(novelCharacterDTO, novelCharacter);
            novelCharacterMapper.insertAndGetId(novelCharacter);

            Integer id = novelCharacter.getId();//获取角色id,insertAndGetId方法不需要返回值，id会自动封装到实体中（sql写好了）
            // 添加角色和角色技能关联
            //不建议这里使用批量插入，
            // 如果没有 MyBatis-Plus 批量操作插件这里就使用不了（我去github中查看了源码，确实没有批量插入的insert方法）
            //所以这里建议自己写一个批量插入的方法或者使用循环单个插入（这样可以保证不同环境下都可以使用）
            //characterAbilityMapper.insert(novelCharacterDTO.getCharacterAbilityList());
            novelCharacterDTO.getCharacterAbilityList().forEach(characterAbility -> {
                characterAbility.setCharacterId(id);
            });
            characterAbilityService.insertAbilityBatch(novelCharacterDTO.getCharacterAbilityList());
            return true;
        } catch (Exception e) {
            log.error("添加角色信息失败: {}", e.getMessage());
            //TODO 自定义错误
            throw new RuntimeException("添加角色信息失败", e);
        }

    }

    @Transactional
    @Override
    public boolean updateNovelCharacter(NovelCharacterDTO novelCharacterDTO) {
        try {
            NovelCharacter novelCharacter = new NovelCharacter();
            BeanUtils.copyProperties(novelCharacterDTO, novelCharacter);
            novelCharacterMapper.updateById(novelCharacter);
            //修改技能
            characterAbilityService.updateAbility(novelCharacterDTO.getCharacterAbilityList());
            return true;
        } catch (Exception e) {
            log.error("修改角色信息失败: {}", e.getMessage());
            throw new RuntimeException("修改角色信息失败", e);
        }
    }
    
    @Override
    public IPage<NovelCharacterVO> getNovelCharacterList(Integer pageNum, Integer pageSize, Integer novelId) {
        try {
            Page<NovelCharacter> page = new Page<>(pageNum, pageSize);
            
            // 按照角色类别进行排序，数字越小越靠前
            QueryWrapper<NovelCharacter> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("novel_id", novelId)
                       .orderByAsc("category");
            
            IPage<NovelCharacter> novelCharacterPage = novelCharacterMapper.selectPage(page, queryWrapper);
            
            // 转换为VO
            return novelCharacterPage.convert(this::convertToVO);
        } catch (Exception e) {
            log.error("分页查询角色列表失败: {}", e.getMessage());
            return new Page<>();
        }
    }
    
    @Override
    public NovelCharacterVO getNovelCharacterById(Integer id) {
        try {
            NovelCharacter novelCharacter = novelCharacterMapper.selectById(id);
            List<CharacterAbilityVO> abilityList = characterAbilityService.getAbilityList(id);
            if (novelCharacter != null) {
                NovelCharacterVO novelCharacterVO = convertToVO(novelCharacter);
                novelCharacterVO.setList(abilityList);
                return novelCharacterVO;
            }
            return null;
        } catch (Exception e) {
            log.error("根据ID查询角色信息失败: {}", e.getMessage());
            return null;
        }
    }
    
    @Override
    public IPage<NovelCharacterVO> searchNovelCharacters(Integer pageNum, Integer pageSize, String name, Integer novelId) {
        try {
            Page<NovelCharacter> page = new Page<>(pageNum, pageSize);
            
            QueryWrapper<NovelCharacter> queryWrapper = new QueryWrapper<>();
            queryWrapper.like("name", name)
                       .eq("novel_id", novelId)
                       .orderByAsc("category");
            
            IPage<NovelCharacter> novelCharacterPage = novelCharacterMapper.selectPage(page, queryWrapper);
            
            // 转换为VO
            return novelCharacterPage.convert(this::convertToVO);
        } catch (Exception e) {
            log.error("模糊查询角色信息失败: {}", e.getMessage());
            return new Page<>();
        }
    }
    
    @Override
    public boolean deleteNovelCharacter(Integer id) {
        try {
            int result = novelCharacterMapper.deleteById(id);
            return result > 0;
        } catch (Exception e) {
            log.error("删除角色信息失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 将实体转换为VO
     */
    private NovelCharacterVO convertToVO(NovelCharacter novelCharacter) {
        NovelCharacterVO vo = new NovelCharacterVO();
        BeanUtils.copyProperties(novelCharacter, vo);
        
        // 设置角色类别名称
        vo.setCategory(CharacterCategoryEnum.getMessageByCode(novelCharacter.getCategory()).getMessage());
        
        // TODO 这里可以添加一些逻辑
        List<CharacterAbilityVO> list = characterAbilityService.getAbilityList(novelCharacter.getId());
        return vo;
    }
}