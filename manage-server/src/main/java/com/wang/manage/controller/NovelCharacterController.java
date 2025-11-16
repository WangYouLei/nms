package com.wang.manage.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wang.common.result.Result;
import com.wang.pojo.dto.NovelCharacterDTO;
import com.wang.manage.service.NovelCharacterService;
import com.wang.pojo.vo.NovelCharacterVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 小说角色Controller
 */
@Slf4j
@RestController
@RequestMapping("/wang/novel-character")
@Api(tags = "小说角色管理")
public class NovelCharacterController {
    
    @Autowired
    private NovelCharacterService novelCharacterService;
    
    /**
     * 添加角色信息
     */
    @PostMapping("/add")
    @ApiOperation("添加角色信息")
    public Result addNovelCharacter(@RequestBody NovelCharacterDTO novelCharacterDTO) {
        log.info("添加角色信息: {}", novelCharacterDTO);
        boolean result = novelCharacterService.addNovelCharacter(novelCharacterDTO);
        return result ? Result.success("添加成功") : Result.error("添加失败");
    }
    
    /**
     * 修改角色信息
     */
    @PutMapping("/update")
    @ApiOperation("修改角色信息")
    public Result updateNovelCharacter(@RequestBody NovelCharacterDTO novelCharacterDTO) {
        log.info("修改角色信息: {}", novelCharacterDTO);
        boolean result = novelCharacterService.updateNovelCharacter(novelCharacterDTO);
        return result ? Result.success("修改成功") : Result.error("修改失败");
    }
    
    /**
     * 分页查询本小说角色（按照角色类别进行排序，数字越小越靠前）
     */
    @GetMapping("/list")
    @ApiOperation("分页查询本小说角色")
    public Result getNovelCharacterList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam Integer novelId) {
        log.info("分页查询角色列表: pageNum={}, pageSize={}, novelId={}", pageNum, pageSize, novelId);
        IPage<NovelCharacterVO> result = novelCharacterService.getNovelCharacterList(pageNum, pageSize, novelId);
        return Result.success(result);
    }
    
    /**
     * 根据角色id查询角色信息  TODO 这个功能应该是不需要的
     */
    @GetMapping("/{id}")
    @ApiOperation("根据角色ID查询角色信息")
    public Result getNovelCharacterById(@PathVariable Integer id) {
        log.info("根据ID查询角色信息: id={}", id);
        NovelCharacterVO result = novelCharacterService.getNovelCharacterById(id);
        return result != null ? Result.success(result) : Result.error("角色不存在");
    }
    
    /**
     * 根据角色名称进行模糊查询
     */
    @GetMapping("/search")
    @ApiOperation("根据角色名称模糊查询")
    public Result searchNovelCharacters(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam String name,
            @RequestParam Integer novelId) {
        log.info("模糊查询角色信息: pageNum={}, pageSize={}, name={}, novelId={}", pageNum, pageSize, name, novelId);
        IPage<NovelCharacterVO> result = novelCharacterService.searchNovelCharacters(pageNum, pageSize, name, novelId);
        return Result.success(result);
    }
    
    /**
     * 删除角色信息
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除角色信息")
    public Result deleteNovelCharacter(@PathVariable Integer id) {
        log.info("删除角色信息: id={}", id);
        boolean result = novelCharacterService.deleteNovelCharacter(id);
        return result ? Result.success("删除成功") : Result.error("删除失败");
    }
    
}