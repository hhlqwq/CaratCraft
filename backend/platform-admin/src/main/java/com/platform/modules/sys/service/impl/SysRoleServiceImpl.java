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
import com.platform.common.utils.Query;
import com.platform.common.utils.ShiroUtils;
import com.platform.datascope.StaffAuthority;
import com.platform.modules.sys.dao.SysRoleDao;
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
        prepareStudioRole(role, "sys:role:save");
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
        prepareStudioRole(role, "sys:role:update");
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

    /** 只允许修改同工作室内可分配的共享经营岗位. */
    private SysRoleEntity validateExistingRole(String roleId, String action) {
        SysRoleEntity target =
                lambdaQuery().eq(SysRoleEntity::getRoleId, roleId).last("FOR UPDATE").one();
        if (target == null
                || !authority.canAssign(roleId)
                || !authority.withinOrganizations(roleId, authority.scope(action))) {
            throw new org.apache.shiro.authz.UnauthorizedException("岗位不在本工作室管理范围内");
        }
        return target;
    }

    /** 岗位角色只关联商家管理员所在工作室并保留明确选中的经营动作. */
    private void prepareStudioRole(SysRoleEntity role, String action) {
        String studio = ShiroUtils.getUserEntity().getOrgNo();
        authority.requireEmployeeOrganization(action, studio);
        if (role.getOrgNoList() != null
                && role.getOrgNoList().stream().anyMatch(value -> !studio.equals(value))) {
            throw new org.apache.shiro.authz.UnauthorizedException("岗位机构由服务端维护");
        }
        role.setOrgNoList(List.of(studio));
        role.setMenuIdList(authority.assignableMenus(role.getMenuIdList()));
        role.setDelegable(true);
    }
}
