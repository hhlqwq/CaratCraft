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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.common.exception.BusinessException;
import com.platform.common.utils.Query;
import com.platform.common.utils.ShiroUtils;
import com.platform.datascope.StaffAuthority;
import com.platform.modules.sys.dao.SysRoleDao;
import com.platform.modules.sys.dao.SysUserDao;
import com.platform.modules.sys.entity.SysRoleEntity;
import com.platform.modules.sys.service.SysRoleMenuService;
import com.platform.modules.sys.service.SysRoleOrgService;
import com.platform.modules.sys.service.SysRoleService;
import com.platform.modules.sys.service.SysUserRoleService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author hehailong
 */
@RequiredArgsConstructor
@Service("sysRoleService")
public class SysRoleServiceImpl extends ServiceImpl<SysRoleDao, SysRoleEntity>
        implements SysRoleService {

    private final SysRoleMenuService sysRoleMenuService;
    private final SysUserDao sysUserDao;
    private final com.platform.modules.sys.dao.SysMenuDao menuDao;
    private final SysUserRoleService sysUserRoleService;
    private final SysRoleOrgService sysRoleOrgService;
    private final StaffAuthority authority;

    @Override
    public Page<SysRoleEntity> queryPage(Map<String, Object> params) {
        // 排序
        params.put("sidx", "T.CREATE_TIME");
        params.put("asc", false);
        Page<SysRoleEntity> page = new Query<SysRoleEntity>(params).getPage();
        return page.setRecords(baseMapper.selectSysRolePage(page, params));
    }

    @Override
    public List<SysRoleEntity> selectListByMap(Map<String, Object> params) {
        return baseMapper.selectSysRolePage(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SysRoleEntity role) {
        role.setRoleId(null);
        checkRoleOrganizations(role, "sys:role:save");
        checkPrems(role);
        role.setDelegable(authority.emergency() && Boolean.TRUE.equals(role.getDelegable()));
        this.save(role);

        // 保存角色与菜单关系
        sysRoleMenuService.saveOrUpdate(role.getRoleId(), role.getMenuIdList());

        // 保存角色与机构关系
        sysRoleOrgService.saveOrUpdate(role.getRoleId(), role.getOrgNoList());
        authority.audit(
                "保存角色权限",
                role.getRoleId(),
                "菜单节点="
                        + role.getMenuIdList()
                        + ", 机构="
                        + role.getOrgNoList()
                        + ", 可委派="
                        + role.getDelegable());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysRoleEntity role) {
        SysRoleEntity current = validateExistingRole(role.getRoleId(), "sys:role:update");
        role.setCreateTime(current.getCreateTime());
        role.setCreateUserId(current.getCreateUserId());
        role.setCreateUserOrgNo(current.getCreateUserOrgNo());
        if (!authority.emergency()) {
            role.setDelegable(current.getDelegable());
        }
        checkRoleOrganizations(role, "sys:role:update");
        checkPrems(role);
        this.updateById(role);

        // 更新角色与菜单关系
        sysRoleMenuService.saveOrUpdate(role.getRoleId(), role.getMenuIdList());
        // 保存角色与机构关系
        sysRoleOrgService.saveOrUpdate(role.getRoleId(), role.getOrgNoList());
        authority.audit(
                "保存角色权限",
                role.getRoleId(),
                "菜单节点="
                        + role.getMenuIdList()
                        + ", 机构="
                        + role.getOrgNoList()
                        + ", 可委派="
                        + role.getDelegable());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(String[] roleIds) {
        for (String roleId : roleIds) {
            validateExistingRole(roleId, "sys:role:delete");
        }
        // 删除角色
        this.removeByIds(Arrays.asList(roleIds));

        // 删除角色与菜单关联
        sysRoleMenuService.deleteBatch(roleIds);

        // 删除角色与用户关联
        sysUserRoleService.deleteBatch(roleIds);

        // 删除角色与机构关联
        sysRoleOrgService.deleteBatch(roleIds);
    }

    @Override
    public List<String> queryRoleIdList(Map<String, Object> params) {
        return baseMapper.queryRoleIdList(params);
    }

    /** 普通管理员只能编辑自己创建的非委派角色, 且角色当前未分派给本人. */
    private SysRoleEntity validateExistingRole(String roleId, String action) {
        SysRoleEntity target =
                lambdaQuery().eq(SysRoleEntity::getRoleId, roleId).last("FOR UPDATE").one();
        String actor = ShiroUtils.getUserEntity().getUserId();
        if (target == null
                || (!authority.emergency()
                        && (!actor.equals(target.getCreateUserId())
                                || Boolean.TRUE.equals(target.getDelegable())
                                || sysUserRoleService.queryRoleIdList(actor).contains(roleId)
                                || !sysRoleOrgService.queryOrgNoList(roleId).stream()
                                        .allMatch(authority.scope(action)::permits)))) {
            throw new org.apache.shiro.authz.UnauthorizedException("该角色由权限管理员维护");
        }
        return target;
    }

    /** 拟配置的机构必须落在对应角色管理动作范围内. */
    private void checkRoleOrganizations(SysRoleEntity role, String action) {
        var scope = authority.scope(action);
        if (role.getOrgNoList() != null && !role.getOrgNoList().stream().allMatch(scope::permits)) {
            throw new org.apache.shiro.authz.UnauthorizedException("机构超出角色管理范围");
        }
    }

    /** 检查权限是否越权 */
    private void checkPrems(SysRoleEntity role) {
        // 如果不是超级管理员，则需要判断角色的权限是否超过自己的权限
        if (authority.emergency()) {
            return;
        }

        // 查询用户所拥有的菜单列表
        List<String> menuIdList = sysUserDao.queryAllMenuId(ShiroUtils.getUserEntity().getUserId());

        // 判断是否越权
        if (role.getMenuIdList() == null
                || !menuIdList.containsAll(role.getMenuIdList())
                || !role.getMenuIdList().stream().allMatch(id -> transferableMenu(id, role))) {
            throw new BusinessException("新增角色的权限，已超出你的权限范围");
        }
    }

    /** 管理面权限由应急管理员签发, 非业务菜单目录可加入普通自定义角色. */
    private boolean transferableMenu(String menuId, SysRoleEntity role) {
        var menu = menuDao.selectById(menuId);
        if (menu == null) {
            return false;
        }
        if (menu.getPerms() == null || menu.getPerms().isBlank()) {
            return true;
        }
        List<String> organizations = role.getOrgNoList() == null ? List.of() : role.getOrgNoList();
        return Arrays.stream(menu.getPerms().split(","))
                .map(String::trim)
                .allMatch(
                        action ->
                                !action.startsWith("sys:")
                                        && !action.contains("*")
                                        && organizations.stream()
                                                .allMatch(authority.scope(action)::permits));
    }
}
