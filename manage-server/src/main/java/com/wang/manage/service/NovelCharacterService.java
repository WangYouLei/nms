package com.wang.manage.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wang.common.result.Result;
import com.wang.pojo.dto.NovelCharacterDTO;
import com.wang.pojo.vo.NovelCharacterVO;
import com.wang.pojo.entity.NovelCharacter;

/**
 * 小说角色Service接口
 */
public interface NovelCharacterService {
    
    /**
     * 添加角色信息
     * @param novelCharacterDTO 角色DTO
     * @return 是否成功
     */
    boolean addNovelCharacter(NovelCharacterDTO novelCharacterDTO);
    
    /**
     * 修改角色信息
     * @param novelCharacterDTO 角色DTO
     * @return 是否成功
     */
    boolean updateNovelCharacter(NovelCharacterDTO novelCharacterDTO);
    
    /**
     * 分页查询本小说角色（按照角色类别进行排序，数字越小越靠前）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param novelId 小说ID
     * @return 分页结果
     */
    IPage<NovelCharacterVO> getNovelCharacterList(Integer pageNum, Integer pageSize, Integer novelId);
    
    /**
     * 根据角色id查询角色信息
     * @param id 角色ID
     * @return 角色VO
     */
    NovelCharacterVO getNovelCharacterById(Integer id);
    
    /**
     * 根据角色名称进行模糊查询
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param name 角色名称
     * @param novelId 小说ID
     * @return 分页结果
     */
    IPage<NovelCharacterVO> searchNovelCharacters(Integer pageNum, Integer pageSize, String name, Integer novelId);
    
    /**
     * 删除角色信息
     * @param id 角色ID
     * @return 是否成功
     */
    boolean deleteNovelCharacter(Integer id);
}