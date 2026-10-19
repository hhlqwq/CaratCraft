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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.modules.sys.entity.SysUserEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统用户
 *
 * @author 何海龙
 */
@Mapper
public interface SysUserDao extends BaseMapper<SysUserEntity> {

  /**
   * 查询用户的所有权限
   *
   * @param userId 用户ID
   * @return List
   */
  List<String> queryAllPerms(String userId);

  /**
   * 查询用户的所有菜单ID
   *
   * @param userId 用户ID
   * @return List
   */
  List<String> queryAllMenuId(String userId);

  /**
   * 分页查询
   *
   * @param page 分页参数
   * @param params 查询参数
   * @return List
   */
  List<SysUserEntity> selectListPage(
      Page<SysUserEntity> page, @Param("params") Map<String, Object> params);

  /**
   * 查询所有
   *
   * @param params 查询参数
   * @return List
   */
  List<SysUserEntity> selectListPage(@Param("params") Map<String, Object> params);
}
