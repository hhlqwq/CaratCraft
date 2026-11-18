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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.utils.Query;
import com.platform.datascope.StaffAuthority;
import com.platform.modules.sys.dao.SysUserDao;
import com.platform.modules.sys.entity.SysUserEntity;
import com.platform.modules.sys.service.SysUserRoleService;
import com.platform.modules.sys.service.SysUserService;
import com.platform.modules.sys.service.SysUserTokenService;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author hehailong
 */
@RequiredArgsConstructor
@Service("sysUserService")
public class SysUserServiceImpl extends ServiceImpl<SysUserDao, SysUserEntity>
        implements SysUserService {

    private final SysUserRoleService sysUserRoleService;
    private final SysUserTokenService tokens;
    private final StaffAuthority authority;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Override
    public List<SysUserEntity> queryAll(Map<String, Object> params) {
        return baseMapper.selectListPage(params);
    }

    @Override
    public Page<SysUserEntity> queryPage(Map<String, Object> params) {
        // 排序
        params.put("sidx", "T.CREATE_TIME");
        params.put("asc", false);
        Page<SysUserEntity> page = new Query<SysUserEntity>(params).getPage();
        return page.setRecords(baseMapper.selectListPage(page, params));
    }

    @Override
    public List<String> queryAllMenuId(String userId) {
        return baseMapper.queryAllMenuId(userId);
    }

    @Override
    public SysUserEntity queryByUserName(String userName) {
        return this.getOne(new QueryWrapper<SysUserEntity>().eq("USER_NAME", userName));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysUserEntity user, Map<String, Object> params) {
        authority.requireEmployeeOrganization("sys:user:save", user.getOrgNo());
        if (!roles(user.getRoleIdList()).isEmpty()) {
            authority.requireEmployeeOrganization("sys:user:assign", user.getOrgNo());
        }
        validateRoles(user.getRoleIdList());
        SysUserEntity employee = profile(user);
        employee.setCreateUserId(user.getCreateUserId());
        employee.setCreateUserOrgNo(user.getCreateUserOrgNo());
        replacePassword(employee, user.getPassword());
        if (!save(employee)) {
            throw new IllegalStateException("员工账号保存失败");
        }
        sysUserRoleService.saveOrUpdate(employee.getUserId(), roles(user.getRoleIdList()));
        authority.audit("创建员工", employee.getUserId(), "角色=" + roles(user.getRoleIdList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysUserEntity user, Map<String, Object> params) {
        SysUserEntity current = lockUser(user.getUserId());
        authority.requireEmployee("sys:user:update", current, true);
        authority.requireEmployeeOrganization("sys:user:update", user.getOrgNo());
        if (user.getPassword() != null) {
            throw new IllegalArgumentException("请使用专用的密码重置入口");
        }
        List<String> previousRoles = sysUserRoleService.queryRoleIdList(current.getUserId());
        if (user.getRoleIdList() != null
                && !new java.util.HashSet<>(previousRoles)
                        .equals(new java.util.HashSet<>(user.getRoleIdList()))) {
            authority.requireEmployeeOrganization("sys:user:assign", current.getOrgNo());
            authority.requireEmployeeOrganization("sys:user:assign", user.getOrgNo());
            validateRoles(user.getRoleIdList());
            if (!authority.emergency() && !previousRoles.stream().allMatch(authority::canAssign)) {
                throw new UnauthorizedException("没有移除该员工现有角色的权限");
            }
            sysUserRoleService.saveOrUpdate(current.getUserId(), roles(user.getRoleIdList()));
            authority.audit(
                    "变更员工角色",
                    current.getUserId(),
                    "原角色=" + previousRoles + ", 新角色=" + roles(user.getRoleIdList()));
        }
        SysUserEntity employee = profile(user);
        employee.setUserId(current.getUserId());
        if (user.getStatus() == null) {
            employee.setStatus(current.getStatus());
        }
        if (!updateById(employee)) {
            throw new IllegalStateException("员工账号更新失败");
        }
        if (!Integer.valueOf(1).equals(employee.getStatus())) {
            tokens.offlineBatch(new String[] {current.getUserId()});
            authority.audit("停用员工", current.getUserId(), "撤销现有会话");
        } else if (!Objects.equals(employee.getStatus(), current.getStatus())) {
            authority.audit("启用员工", current.getUserId(), "重新登录后生效");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(String[] userIds) {
        for (String id : userIds) {
            authority.requireEmployee("sys:user:delete", lockUser(id), true);
        }
        for (String id : userIds) {
            tokens.offlineBatch(new String[] {id});
            sysUserRoleService.saveOrUpdate(id, List.of());
            removeById(id);
            authority.audit("删除员工", id, "解除角色并注销会话");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(String userId, String password, String newPassword) {
        validatePassword(newPassword);
        SysUserEntity employee = lockUser(userId);
        if (employee == null
                || !Integer.valueOf(1).equals(employee.getStatus())
                || !new Sha256Hash(password, employee.getSalt())
                        .toHex()
                        .equals(employee.getPassword())) {
            return false;
        }
        replacePassword(employee, newPassword);
        updateById(employee);
        tokens.offlineBatch(new String[] {userId});
        authority.audit("修改本人密码", userId, "撤销现有会话");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPw(String userId, String password) {
        SysUserEntity employee = lockUser(userId);
        authority.requireEmployee("sys:user:resetPw", employee, true);
        replacePassword(employee, password);
        updateById(employee);
        tokens.offlineBatch(new String[] {userId});
        authority.audit("重置员工密码", userId, "撤销现有会话");
    }

    /** 只写请求允许修改的人员字段, 创建者、标识、密码盐和最近登录由服务端维护. */
    private SysUserEntity profile(SysUserEntity input) {
        if (!List.of(0, 1).contains(input.getStatus() == null ? 1 : input.getStatus())) {
            throw new IllegalArgumentException("员工状态无效");
        }
        SysUserEntity employee = new SysUserEntity();
        employee.setUserName(input.getUserName());
        employee.setRealName(input.getRealName());
        employee.setSex(input.getSex());
        employee.setEmail(input.getEmail());
        employee.setMobile(input.getMobile());
        employee.setOrgNo(input.getOrgNo());
        employee.setStatus(input.getStatus() == null ? 1 : input.getStatus());
        return employee;
    }

    /** 角色必须存在, 普通管理员只能分配显式批准且机构范围受限的角色. */
    private void validateRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return;
        }
        if (roles.size() > 20
                || roles.stream().anyMatch(id -> id == null || id.isBlank())
                || (!authority.emergency()
                        && !org.apache.shiro.SecurityUtils.getSubject()
                                .isPermitted("sys:user:assign"))
                || !roles.stream().allMatch(id -> rolesExist(id) && authority.canAssign(id))) {
            throw new UnauthorizedException("所选角色不可分配");
        }
    }

    /** 查询现有角色标识, 不从请求内容推断角色类型. */
    private boolean rolesExist(String roleId) {
        return jdbc.queryForObject(
                        "SELECT COUNT(*) FROM SYS_ROLE WHERE ROLE_ID=?", Long.class, roleId)
                == 1L;
    }

    /** 对员工变更串行化, 防止凭据重置和停用互相覆盖. */
    private SysUserEntity lockUser(String id) {
        return getOne(new QueryWrapper<SysUserEntity>().eq("USER_ID", id).last("FOR UPDATE"));
    }

    /** 收敛相同角色重复提交, 保持用户角色关系唯一. */
    private List<String> roles(List<String> roleIds) {
        return roleIds == null ? List.of() : roleIds.stream().distinct().toList();
    }

    /** 校验显式提供的独立密码并重新产生盐. */
    private void replacePassword(SysUserEntity employee, String password) {
        validatePassword(password);
        byte[] bytes = new byte[16];
        new java.security.SecureRandom().nextBytes(bytes);
        employee.setSalt(
                java.util.Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes)
                        .substring(0, 20));
        employee.setPassword(new Sha256Hash(password, employee.getSalt()).toHex());
    }

    /** 要求每次建立或重置凭据时使用管理员明确输入的12到128位可打印ASCII字符. */
    private void validatePassword(String password) {
        if (password == null || !password.matches("[!-~]{12,128}")) {
            throw new IllegalArgumentException("请设置12到128位密码, 不含空格");
        }
    }
}
