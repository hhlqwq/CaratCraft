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
package com.platform.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.modules.sys.dao.SysMenuDao;
import com.platform.modules.sys.dao.SysUserDao;
import com.platform.modules.sys.dao.SysUserTokenDao;
import com.platform.modules.sys.entity.AccountTier;
import com.platform.modules.sys.entity.SysMenuEntity;
import com.platform.modules.sys.entity.SysUserEntity;
import com.platform.modules.sys.entity.SysUserTokenEntity;
import com.platform.modules.sys.service.BackOfficePermissions;
import com.platform.modules.sys.service.ShiroService;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author hehailong
 */
@RequiredArgsConstructor
@Service("shiroService")
public class ShiroServiceImpl extends ServiceImpl<SysUserTokenDao, SysUserTokenEntity>
        implements ShiroService {

    private final SysMenuDao sysMenuDao;

    private final SysUserDao sysUserDao;

    @Override
    public Set<String> getUserPermissions(String userId) {
        SysUserEntity user = queryUser(userId);
        if (!AccountTier.valid(user) || !Integer.valueOf(1).equals(user.getStatus())) {
            return Set.of();
        }
        if (AccountTier.PLATFORM_ADMIN.equals(user.getAccountType())) {
            return Set.of(BackOfficePermissions.MANAGE_MERCHANTS);
        }
        boolean merchant = AccountTier.MERCHANT_ADMIN.equals(user.getAccountType());
        List<String> permsList =
                merchant
                        ? sysMenuDao
                                .selectList(
                                        new QueryWrapper<SysMenuEntity>()
                                                .select("PERMS")
                                                .isNotNull("PERMS"))
                                .stream()
                                .map(SysMenuEntity::getPerms)
                                .toList()
                        : sysUserDao.queryAllPerms(userId);
        Set<String> permsSet = new HashSet<>();
        for (String perms : permsList) {
            if (StringUtils.isBlank(perms)) {
                continue;
            }
            Arrays.stream(perms.split(","))
                    .map(String::trim)
                    .filter(BackOfficePermissions::isBusiness)
                    .forEach(permsSet::add);
        }
        if (merchant) {
            permsSet.addAll(BackOfficePermissions.MERCHANT_ACCOUNT_MANAGEMENT);
        }
        return permsSet;
    }

    @Override
    public SysUserTokenEntity queryByToken(String token) {
        return baseMapper.selectOne(new QueryWrapper<SysUserTokenEntity>().eq("TOKEN", token));
    }

    @Override
    public SysUserEntity queryUser(String userId) {
        return sysUserDao.selectById(userId);
    }
}
