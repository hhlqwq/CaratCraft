package com.platform.modules.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.modules.sys.entity.SysRegionEntity;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author hehailong
 * @email 939961241@qq.com
 * @date 2017-11-04 11:19:31
 */
@Mapper
public interface SysRegionDao extends BaseMapper<SysRegionEntity> {

    /** 按给定条件查询当前领域记录列表. */
    List<SysRegionEntity> queryList(Map<String, Object> map);

    /** 统计符合当前查询条件的记录数量. */
    int queryTotal(Map<String, Object> map);
}
