/*
 *
 *      Copyright (c) 2018-2099, hehailong All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the openwtai.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: hehailong(845367567@qq.com)
 *
 */
package com.platform.modules.sys.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.modules.sys.entity.SysUserRoleEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户与角色对应关系
 *
 * @author 何海龙
 */
@Mapper
public interface SysUserRoleDao extends BaseMapper<SysUserRoleEntity> {

  /**
   * 根据用户ID，获取角色ID列表
   *
   * @param userId 用户ID
   * @return List
   */
  List<String> queryRoleIdList(String userId);

  /**
   * 根据角色ID数组，批量删除
   *
   * @param roleIds 角色ids
   * @return int
   */
  int deleteBatch(String[] roleIds);
}
