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
import com.platform.common.utils.Constant;
import com.platform.common.utils.StringUtils;
import com.platform.modules.sys.dao.SysMenuDao;
import com.platform.modules.sys.entity.SysMenuEntity;
import com.platform.modules.sys.enums.MenuTypeEnum;
import com.platform.modules.sys.service.BackOfficePermissions;
import com.platform.modules.sys.service.ShiroService;
import com.platform.modules.sys.service.SysMenuService;
import com.platform.modules.sys.service.SysRoleMenuService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author hehailong
 */
@RequiredArgsConstructor
@Service("sysMenuService")
public class SysMenuServiceImpl extends ServiceImpl<SysMenuDao, SysMenuEntity>
        implements SysMenuService {

    private final ShiroService shiroService;
    private final SysRoleMenuService sysRoleMenuService;

    @Override
    public List<SysMenuEntity> queryListParentId(String parentId, List<String> menuIdList) {
        List<SysMenuEntity> menuList = queryListParentId(parentId);
        if (menuIdList == null) {
            return menuList;
        }

        List<SysMenuEntity> userMenuList = new ArrayList<>();
        for (SysMenuEntity menu : menuList) {
            if (menuIdList.contains(menu.getMenuId())) {
                userMenuList.add(menu);
            }
        }
        return userMenuList;
    }

    @Override
    public List<SysMenuEntity> queryListParentId(String parentId) {
        return baseMapper.selectList(
                new QueryWrapper<SysMenuEntity>()
                        .eq("PARENT_ID", parentId)
                        .orderByAsc("ORDER_NUM"));
    }

    @Override
    public List<SysMenuEntity> queryNotButtonList() {
        return baseMapper.queryNotButtonList();
    }

    @Override
    public List<SysMenuEntity> getUserMenuList(String userId) {
        var effective = shiroService.getUserPermissions(userId);
        if (effective.contains(BackOfficePermissions.MANAGE_MERCHANTS)) {
            SysMenuEntity page = new SysMenuEntity();
            page.setMenuId("platform-merchants");
            page.setName("商家管理员");
            page.setUrl("platform/merchant-accounts");
            page.setIcon("admin");
            page.setType(MenuTypeEnum.MENU.getValue());
            return List.of(page);
        }
        Map<String, SysMenuEntity> byId = new HashMap<>();
        list().forEach(menu -> byId.put(menu.getMenuId(), menu));
        var pages = new HashSet<String>();
        for (SysMenuEntity menu : byId.values()) {
            if (menu.getUrl() == null
                    || menu.getUrl().isBlank()
                    || menu.getPerms() == null
                    || !effective.contains(menu.getPerms().split(",", 2)[0].trim())) {
                continue;
            }
            for (SysMenuEntity item = menu;
                    item != null && pages.add(item.getMenuId());
                    item = byId.get(item.getParentId())) {
                // 保留有权页面的上级目录, 避免岗位缺少目录节点时隐藏实际授权的入口.
            }
        }
        return getAllMenuList(new ArrayList<>(pages));
    }

    @Override
    public boolean delete(String menuId) {
        // 删除菜单
        this.removeById(menuId);
        // 删除菜单与角色关联
        Map<String, Object> map = new HashMap<>(2);
        map.put("menu_id", menuId);
        return sysRoleMenuService.removeByMap(map);
    }

    @Override
    public List<SysMenuEntity> queryList() {
        return baseMapper.queryList();
    }

    @Override
    public boolean add(SysMenuEntity menu) {
        String parentId = menu.getParentId();
        String maxId = baseMapper.queryMaxIdByParentId(parentId);

        menu.setMenuId(StringUtils.addOne(parentId, maxId));
        return this.save(menu);
    }

    /** 获取所有菜单列表 */
    private List<SysMenuEntity> getAllMenuList(List<String> menuIdList) {
        // 查询根菜单列表
        List<SysMenuEntity> menuList = queryListParentId(Constant.STR_ZERO, menuIdList);
        // 递归获取子菜单
        getMenuTreeList(menuList, menuIdList);

        return menuList;
    }

    /** 递归 */
    private List<SysMenuEntity> getMenuTreeList(
            List<SysMenuEntity> menuList, List<String> menuIdList) {
        List<SysMenuEntity> subMenuList = new ArrayList<>();

        for (SysMenuEntity entity : menuList) {
            // 目录
            if (Objects.equals(entity.getType(), MenuTypeEnum.CATALOG.getValue())) {
                entity.setList(
                        getMenuTreeList(
                                queryListParentId(entity.getMenuId(), menuIdList), menuIdList));
            }
            subMenuList.add(entity);
        }

        return subMenuList;
    }
}
