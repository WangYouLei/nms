package com.wang.commonserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.pojo.entity.ManualAudit;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人工审核Mapper接口
 */
@Mapper
public interface ManualAuditMapper extends BaseMapper<ManualAudit> {

}