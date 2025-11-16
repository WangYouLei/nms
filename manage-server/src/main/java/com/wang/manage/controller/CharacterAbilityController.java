package com.wang.manage.controller;

import com.wang.common.result.Result;
import com.wang.manage.service.CharacterAbilityService;
import com.wang.pojo.dto.AbilityLevelDTO;
import com.wang.pojo.dto.CharacterAbilityDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wang/AL")
@Slf4j
@Api(tags = "角色技能及技能等级相关管理")
public class CharacterAbilityController {

    @Autowired
    private CharacterAbilityService characterAbilityService;


    @PostMapping("addAL")
    @ApiOperation("添加技能等级")
    public Result addLevel(List<AbilityLevelDTO> list){
        log.info("添加技能等级请求");
        return characterAbilityService.addLevel(list);
    }

    @PostMapping("addA")
    @ApiOperation("批量插入角色技能")
    public Result addAbility(List<CharacterAbilityDTO>  list,Integer CharacterId){
        log.info("批量插入角色技能请求");
        return characterAbilityService.addAbility(list,CharacterId);
    }
}
