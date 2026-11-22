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
package com.platform.modules.sys.oauth2;

import com.platform.modules.sys.entity.AccountTier;
import com.platform.modules.sys.entity.SysUserEntity;
import com.platform.modules.sys.entity.SysUserTokenEntity;
import com.platform.modules.sys.service.BusinessOrganization;
import com.platform.modules.sys.service.ShiroService;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

/**
 * 认证
 *
 * @author hehailong
 */
@RequiredArgsConstructor
@Component
public class Oauth2Realm extends AuthorizingRealm {
    private final ShiroService shiroService;
    private final BusinessOrganization businessOrganization;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof Oauth2Token;
    }

    /** 授权(验证权限时调用) */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SysUserEntity user = (SysUserEntity) principals.getPrimaryPrincipal();
        String userId = user.getUserId();

        // 用户权限列表
        Set<String> permsSet = shiroService.getUserPermissions(userId);

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permsSet);
        return info;
    }

    /** 认证(登录时调用) */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
            throws AuthenticationException {
        String accessToken = (String) token.getPrincipal();

        // 根据accessToken，查询用户信息
        SysUserTokenEntity tokenEntity = shiroService.queryByToken(accessToken);
        // 区分令牌被覆盖和自然过期，便于前端给出准确原因 / Distinguishes replacement from expiration.
        if (tokenEntity == null) {
            throw new IncorrectCredentialsException("当前登录令牌无效，可能已在其他标签页重新登录");
        }
        if (tokenEntity.getExpireTime().getTime() < System.currentTimeMillis()) {
            throw new ExpiredCredentialsException("登录令牌已过期，请重新登录");
        }

        // 查询用户信息
        SysUserEntity user = shiroService.queryUser(tokenEntity.getUserId());
        // 账号锁定
        if (!AccountTier.valid(user) || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new LockedAccountException("账号已被锁定,请联系管理员");
        }
        if (AccountTier.MERCHANT_ADMIN.equals(user.getAccountType())
                && (user.getOrgNo() == null
                        || !Objects.equals(
                                user.getOrgNo(), businessOrganization.defaultOrganization()))) {
            throw new LockedAccountException("商家管理员的工作室配置不可用");
        }

        return new SimpleAuthenticationInfo(user, accessToken, getName());
    }
}
