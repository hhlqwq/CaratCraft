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
package com.platform.modules.sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.modules.sys.entity.SysUserTokenEntity;

import java.util.Map;

/**
 * 用户Token
 *
 * @author hehailong
 */
public interface SysUserTokenService extends IService<SysUserTokenEntity> {

    /**
     * 账号仍有效且已校验的密码版本未变化时签发令牌.
     *
     * @param userId 用户Id
     * @param verifiedPasswordHash 已校验的密码版本
     * @return 令牌, 员工状态或凭据发生变化时为空
     */
    String createToken(String userId, String verifiedPasswordHash);

    /**
     * 退出，修改token值
     *
     * @param userId 用户Id
     */
    void logout(String userId);

    /**
     * 分页查询
     *
     * @param params 查询参数
     * @return Page
     */
    Page<SysUserTokenEntity> queryPage(Map<String, Object> params);

    /**
     * 根据主键删除
     *
     * @param userId 用户Id
     */
    void delete(String userId);

    /**
     * 批量下线用户(删除用户token记录)
     *
     * @param userIds 用户Ids
     */
    void offlineBatch(String[] userIds);
}
