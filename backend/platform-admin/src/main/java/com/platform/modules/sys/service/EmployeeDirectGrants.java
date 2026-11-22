package com.platform.modules.sys.service;

import com.platform.common.utils.ShiroUtils;
import com.platform.datascope.StaffAuthority;
import com.platform.modules.sys.entity.SysUserEntity;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** 将员工单独选择的功能沿用现有岗位关联表保存. */
@Service
@RequiredArgsConstructor
public class EmployeeDirectGrants {
    private final JdbcTemplate jdbc;
    private final StaffAuthority authority;

    /** 读取指定员工个人授权角色, 共享岗位列表排除此角色. */
    public String roleId(String userId) {
        return jdbc
                .queryForList(
                        "SELECT ROLE_ID FROM SYS_ROLE WHERE DIRECT_USER_ID=?", String.class, userId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    /** 返回个人直接选择的功能节点. */
    public List<String> menus(String userId) {
        return jdbc.queryForList(
                "SELECT rm.MENU_ID FROM SYS_ROLE_MENU rm JOIN SYS_ROLE r"
                        + " ON r.ROLE_ID=rm.ROLE_ID WHERE r.DIRECT_USER_ID=? ORDER BY rm.MENU_ID",
                String.class,
                userId);
    }

    /** 在已锁定员工行的事务内替换个人授权, 空选择删除原角色. */
    public String replace(SysUserEntity employee, List<String> requested) {
        if (requested == null) {
            return roleId(employee.getUserId());
        }
        var allowed = authority.assignableMenus(requested);
        String roleId = roleId(employee.getUserId());
        if (allowed.isEmpty()) {
            remove(employee.getUserId());
            return null;
        }
        if (roleId == null) {
            roleId = UUID.randomUUID().toString().replace("-", "");
            jdbc.update(
                    "INSERT INTO"
                        + " SYS_ROLE(ROLE_ID,ROLE_NAME,DELEGABLE,DIRECT_USER_ID,CREATE_USER_ID,CREATE_USER_ORG_NO,CREATE_TIME)"
                        + " VALUES(?,'员工个人权限',0,?,?,?,NOW())",
                    roleId,
                    employee.getUserId(),
                    ShiroUtils.getUserEntity().getUserId(),
                    employee.getOrgNo());
            jdbc.update(
                    "INSERT INTO SYS_ROLE_ORG(ID,ROLE_ID,ORG_NO)"
                            + " VALUES(REPLACE(UUID(),'-',''),?,?)",
                    roleId,
                    employee.getOrgNo());
        }
        jdbc.update("DELETE FROM SYS_ROLE_MENU WHERE ROLE_ID=?", roleId);
        for (String menuId : allowed) {
            jdbc.update(
                    "INSERT INTO SYS_ROLE_MENU(ID,ROLE_ID,MENU_ID)"
                            + " VALUES(REPLACE(UUID(),'-',''),?,?)",
                    roleId,
                    menuId);
        }
        authority.audit("变更员工个人授权", employee.getUserId(), "功能节点=" + allowed);
        return roleId;
    }

    /** 移除仅绑定于当前员工的个人角色及关联. */
    public void remove(String userId) {
        String roleId = roleId(userId);
        if (roleId == null) {
            return;
        }
        jdbc.update("DELETE FROM SYS_USER_ROLE WHERE ROLE_ID=?", roleId);
        jdbc.update("DELETE FROM SYS_ROLE_MENU WHERE ROLE_ID=?", roleId);
        jdbc.update("DELETE FROM SYS_ROLE_ORG WHERE ROLE_ID=?", roleId);
        jdbc.update("DELETE FROM SYS_ROLE WHERE ROLE_ID=? AND DIRECT_USER_ID=?", roleId, userId);
    }
}
