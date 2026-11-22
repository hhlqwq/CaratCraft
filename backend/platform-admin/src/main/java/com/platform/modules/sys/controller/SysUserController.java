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
package com.platform.modules.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.common.annotation.SysLog;
import com.platform.common.utils.Constant;
import com.platform.common.utils.RestResponse;
import com.platform.common.validator.ValidatorUtils;
import com.platform.common.validator.group.AddGroup;
import com.platform.common.validator.group.UpdateGroup;
import com.platform.datascope.StaffAuthority;
import com.platform.modules.sys.entity.SysUserEntity;
import com.platform.modules.sys.form.PasswordForm;
import com.platform.modules.sys.service.SysUserRoleService;
import com.platform.modules.sys.service.SysUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang.ArrayUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统用户
 *
 * @author hehailong
 */
@Tag(name = "SysUserController|系统用户")
@RequiredArgsConstructor
@RestController
@RequestMapping("/sys/user")
public class SysUserController extends AbstractController {

    private final SysUserService sysUserService;
    private final SysUserRoleService sysUserRoleService;
    private final StaffAuthority authority;
    private final com.platform.modules.sys.service.EmployeeDirectGrants directGrants;
    private final com.platform.modules.sys.service.ShiroService shiroService;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    /**
     * 查看所有列表
     *
     * @param params 查询参数
     * @return RestResponse
     */
    @Operation(summary = "查看所有列表", description = "查看所有列表")
    @GetMapping("/queryAll")
    @RequiresPermissions("sys:user:list")
    public RestResponse<List<SysUserEntity>> queryAll(@RequestParam Map<String, Object> params) {
        applyStaffScope(params);
        List<SysUserEntity> list = sysUserService.queryAll(params);

        return RestResponse.ok(list);
    }

    /**
     * 所有用户列表
     *
     * @param params 查询参数
     * @return RestResponse
     */
    @Operation(summary = "分页查询", description = "分页查询")
    @GetMapping("/list")
    @RequiresPermissions("sys:user:list")
    public RestResponse<Page<SysUserEntity>> list(@RequestParam Map<String, Object> params) {

        // 如需数据权限，在参数中添加DataScope
        applyStaffScope(params);

        Page<SysUserEntity> page = sysUserService.queryPage(params);

        return RestResponse.ok(page);
    }

    /**
     * 获取登录的用户信息
     *
     * @return RestResponse
     */
    @Operation(summary = "获取登录的用户信息", description = "获取登录的用户信息")
    @GetMapping("/info")
    public RestResponse<SysUserEntity> info() {
        return RestResponse.ok(getUser());
    }

    /**
     * 修改登录用户密码
     *
     * @param form form
     * @return RestResponse
     */
    @Operation(summary = "修改密码", description = "修改密码")
    @SysLog("修改密码")
    @PostMapping("/password")
    public RestResponse<String> password(@RequestBody PasswordForm form) {
        if (form.getPassword() == null || form.getPassword().isEmpty()) {
            throw new IllegalArgumentException("请输入当前密码");
        }
        boolean flag =
                sysUserService.updatePassword(
                        getUserId(), form.getPassword(), form.getNewPassword());
        if (!flag) {
            return RestResponse.fail("原密码不正确");
        }

        return RestResponse.ok();
    }

    /**
     * 根据主键查询详情
     *
     * @param userId 主键
     * @return RestResponse
     */
    @Operation(summary = "根据主键查询详情", description = "根据主键查询详情")
    @GetMapping("/info/{userId}")
    @RequiresPermissions("sys:user:info")
    public RestResponse<SysUserEntity> info(@PathVariable("userId") String userId) {
        SysUserEntity user = sysUserService.getById(userId);
        authority.requireEmployee("sys:user:info", user, false);

        // 获取用户所属的角色列表
        List<String> roleIdList = sysUserRoleService.queryRoleIdList(userId);
        String direct = directGrants.roleId(userId);
        user.setRoleIdList(roleIdList.stream().filter(id -> !id.equals(direct)).toList());
        user.setPermissionMenuIds(directGrants.menus(userId));

        return RestResponse.ok(user);
    }

    /**
     * 保存用户
     *
     * @param user user
     * @return RestResponse
     */
    @Operation(summary = "新增", description = "新增")
    @SysLog("保存用户")
    @PostMapping("/save")
    @RequiresPermissions("sys:user:save")
    public RestResponse<String> save(@RequestBody SysUserEntity user) {
        ValidatorUtils.validateEntity(user, AddGroup.class);

        Map<String, Object> params = new HashMap<>(2);

        user.setCreateUserId(getUserId());
        user.setCreateUserOrgNo(getOrgNo());
        sysUserService.add(user, params);

        return RestResponse.ok();
    }

    /**
     * 修改用户
     *
     * @param user user
     * @return RestResponse
     */
    @Operation(summary = "修改", description = "修改")
    @SysLog("修改用户")
    @PostMapping("/update")
    @RequiresPermissions("sys:user:update")
    public RestResponse<String> update(@RequestBody SysUserEntity user) {
        ValidatorUtils.validateEntity(user, UpdateGroup.class);

        Map<String, Object> params = new HashMap<>(2);

        user.setCreateUserId(getUserId());
        user.setCreateUserOrgNo(getOrgNo());
        sysUserService.update(user, params);

        return RestResponse.ok();
    }

    /**
     * 删除用户
     *
     * @param userIds userIds
     * @return RestResponse
     */
    @Operation(summary = "删除", description = "删除")
    @SysLog("删除用户")
    @PostMapping("/delete")
    @RequiresPermissions("sys:user:delete")
    public RestResponse<String> delete(@RequestBody String[] userIds) {
        if (ArrayUtils.contains(userIds, Constant.SUPER_ADMIN)) {
            return RestResponse.fail("系统管理员不能删除");
        }

        if (ArrayUtils.contains(userIds, getUserId())) {
            return RestResponse.fail("当前用户不能删除");
        }

        sysUserService.deleteBatch(userIds);

        return RestResponse.ok();
    }

    /** 使用专用入口为单个员工重置密码, 请求内容不进入审计日志. */
    @PostMapping("/resetPw")
    @RequiresPermissions("sys:user:resetPw")
    public RestResponse<String> resetPw(@RequestBody PasswordReset form) {
        sysUserService.resetPw(form.userId(), form.password());
        return RestResponse.ok();
    }

    /** 查询范围内员工的角色、机构和权限来源, 不包含认证材料. */
    @GetMapping("/permissions/{userId}")
    @RequiresPermissions("sys:user:info")
    public RestResponse<List<Map<String, Object>>> permissions(@PathVariable String userId) {
        SysUserEntity employee = sysUserService.getById(userId);
        authority.requireEmployee("sys:user:info", employee, false);
        var effective = shiroService.getUserPermissions(userId);
        var rows =
                jdbc.queryForList(
                        "SELECT DISTINCT r.ROLE_ID AS roleId,r.ROLE_NAME AS roleName,ro.ORG_NO AS"
                            + " orgNo,m.PERMS AS permissions FROM SYS_USER_ROLE ur JOIN SYS_ROLE r"
                            + " ON r.ROLE_ID=ur.ROLE_ID JOIN SYS_ROLE_ORG ro ON"
                            + " ro.ROLE_ID=r.ROLE_ID JOIN SYS_ROLE_MENU rm ON rm.ROLE_ID=r.ROLE_ID"
                            + " JOIN SYS_MENU m ON m.MENU_ID=rm.MENU_ID WHERE ur.USER_ID=? AND"
                            + " ro.ORG_NO=? AND m.PERMS IS NOT NULL AND (r.DIRECT_USER_ID IS NULL"
                            + " OR r.DIRECT_USER_ID=ur.USER_ID) ORDER BY"
                            + " r.ROLE_ID,ro.ORG_NO,m.PERMS",
                        userId,
                        employee.getOrgNo());
        var result = new java.util.ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : rows) {
            String granted =
                    java.util.Arrays.stream(String.valueOf(row.get("permissions")).split(","))
                            .map(String::trim)
                            .filter(effective::contains)
                            .collect(java.util.stream.Collectors.joining(","));
            if (!granted.isEmpty()) {
                row.put("permissions", granted);
                result.add(row);
            }
        }
        return RestResponse.ok(result);
    }

    /** 替换客户端提供的范围参数, 员工列表与详情同样按动作计算. */
    private void applyStaffScope(Map<String, Object> params) {
        if (!authority.merchant()) {
            throw new org.apache.shiro.authz.UnauthorizedException("仅商家管理员可以查看员工");
        }
        params.remove("staffOrgNos");
        var scope = authority.scope("sys:user:list");
        if (!scope.emergency()) {
            params.put("staffOrgNos", scope.organizations());
        }
    }

    /** 员工密码重置只接受目标和管理员明确设置的新密码. */
    public record PasswordReset(String userId, String password) {}
}
