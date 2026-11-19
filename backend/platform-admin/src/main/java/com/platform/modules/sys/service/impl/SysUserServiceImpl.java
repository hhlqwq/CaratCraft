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
    private static final String TEMPORARY_PASSWORD = "123456";
    private static final String MOBILE_PATTERN = "1[3-9][0-9]{9}";

    private final SysUserRoleService sysUserRoleService;
    private final SysUserTokenService tokens;
    private final StaffAuthority authority;
    private final com.platform.modules.sys.service.BusinessOrganization businessOrganization;
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
        String organization = businessOrganization.defaultOrganization();
        if (organization == null) {
            throw new IllegalArgumentException("请先配置工作室业务机构 CARATCRAFT_DEFAULT_BUSINESS_ORG");
        }
        rejectOrganizationOverride(user.getOrgNo(), organization);
        authority.requireEmployeeOrganization("sys:user:save", organization);
        if (!roles(user.getRoleIdList()).isEmpty()) {
            authority.requireEmployeeOrganization("sys:user:assign", organization);
        }
        validateRoles(user.getRoleIdList());
        SysUserEntity employee = profile(user);
        employee.setOrgNo(organization);
        employee.setCreateUserId(user.getCreateUserId());
        employee.setCreateUserOrgNo(user.getCreateUserOrgNo());
        validateContacts(employee, true);
        if (user.getPassword() != null && !TEMPORARY_PASSWORD.equals(user.getPassword())) {
            throw new IllegalArgumentException("新员工初始密码由系统设置");
        }
        replacePassword(employee, TEMPORARY_PASSWORD, true);
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
        rejectOrganizationOverride(user.getOrgNo(), current.getOrgNo());
        if (user.getPassword() != null) {
            throw new IllegalArgumentException("请使用专用的密码重置入口");
        }
        List<String> previousRoles = sysUserRoleService.queryRoleIdList(current.getUserId());
        if (user.getRoleIdList() != null
                && !new java.util.HashSet<>(previousRoles)
                        .equals(new java.util.HashSet<>(user.getRoleIdList()))) {
            authority.requireEmployeeOrganization("sys:user:assign", current.getOrgNo());
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
        validateContacts(employee, false);
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
        validateNewPassword(newPassword);
        SysUserEntity employee = lockUser(userId);
        if (employee == null
                || !Integer.valueOf(1).equals(employee.getStatus())
                || !new Sha256Hash(password, employee.getSalt())
                        .toHex()
                        .equals(employee.getPassword())) {
            return false;
        }
        replacePassword(employee, newPassword, false);
        updateCredential(employee);
        tokens.offlineBatch(new String[] {userId});
        authority.audit("修改本人密码", userId, "撤销现有会话");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPw(String userId, String password) {
        SysUserEntity employee = lockUser(userId);
        authority.requireEmployee("sys:user:resetPw", employee, true);
        replacePassword(employee, password == null ? TEMPORARY_PASSWORD : password, true);
        updateCredential(employee);
        tokens.offlineBatch(new String[] {userId});
        authority.audit("重置员工密码", userId, "撤销现有会话");
    }

    /** 以发码时的密码哈希和联系方式校验恢复上下文, 串行化改密和停用操作. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recoverPassword(
            SysUserEntity snapshot, boolean byEmail, String contact, String newPassword) {
        SysUserEntity employee = lockUser(snapshot.getUserId());
        if (!canRecoverPassword(employee)
                || !Objects.equals(employee.getPassword(), snapshot.getPassword())
                || !Objects.equals(byEmail ? employee.getEmail() : employee.getMobile(), contact)) {
            return false;
        }
        replacePassword(employee, newPassword, false);
        updateCredential(employee);
        tokens.offlineBatch(new String[] {employee.getUserId()});
        jdbc.update(
                "INSERT INTO"
                    + " SYS_LOG(ID,USER_NAME,OPERATION,METHOD,PARAMS,TIME,CREATE_TIME,SCOPE_ORG_NO)"
                    + " VALUES(REPLACE(UUID(),'-',''),?,'员工验证码找回','员工凭据找回','已撤销旧会话',0,NOW(),?)",
                employee.getUserName(),
                employee.getOrgNo());
        return true;
    }

    /** 紧急管理员和持有系统管理或资金写操作权限的员工由授权管理员办理重置. */
    @Override
    public boolean canRecoverPassword(SysUserEntity employee) {
        return employee != null
                && Integer.valueOf(1).equals(employee.getStatus())
                && !com.platform.common.utils.Constant.SUPER_ADMIN.equals(employee.getUserId())
                && Boolean.TRUE.equals(
                        jdbc.queryForObject(
                                "SELECT NOT EXISTS (SELECT 1 FROM SYS_USER_ROLE ur JOIN"
                                    + " SYS_ROLE_MENU rm ON ur.ROLE_ID=rm.ROLE_ID JOIN SYS_MENU m"
                                    + " ON rm.MENU_ID=m.MENU_ID WHERE ur.USER_ID=? AND"
                                    + " (INSTR(m.PERMS,'*')>0 OR LOWER(REPLACE(m.PERMS,' ',''))"
                                    + " REGEXP"
                                    + " '(^|,)(sys:(user|role|menu):|mall:refund:(execute|review|retry|writeback|reconcile)(,|$))'))",
                                Boolean.class,
                                employee.getUserId()));
    }

    /** 校验中国大陆手机号, 开户至少登记一个联系方式. */
    private void validateContacts(SysUserEntity employee, boolean creating) {
        String mobile = employee.getMobile();
        String email = employee.getEmail();
        if (mobile != null && !mobile.isBlank() && !mobile.matches(MOBILE_PATTERN)) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (creating
                && (mobile == null || mobile.isBlank())
                && (email == null || email.isBlank())) {
            throw new IllegalArgumentException("请至少填写手机号或邮箱");
        }
    }

    /** 只变更密码相关字段, 并检查写入是否成功. */
    private void updateCredential(SysUserEntity employee) {
        SysUserEntity credential = new SysUserEntity();
        credential.setUserId(employee.getUserId());
        credential.setPassword(employee.getPassword());
        credential.setSalt(employee.getSalt());
        credential.setPasswordChangeRequired(employee.getPasswordChangeRequired());
        if (!updateById(credential)) {
            throw new IllegalStateException("员工密码更新失败");
        }
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
        employee.setStatus(input.getStatus() == null ? 1 : input.getStatus());
        return employee;
    }

    /** 拒绝用表单提交跨机构参数, 员工资料编辑保持原有业务归属. */
    private void rejectOrganizationOverride(String requested, String organization) {
        if (requested != null && !requested.isBlank() && !Objects.equals(requested, organization)) {
            throw new UnauthorizedException("员工所属机构由服务端维护");
        }
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

    /** 采用6到32位密码并重新生成独立的盐, 临时密码须本人更换. */
    private void replacePassword(SysUserEntity employee, String password, boolean temporary) {
        if (temporary) {
            validatePassword(password);
        } else {
            validateNewPassword(password);
            if (new Sha256Hash(password, employee.getSalt())
                    .toHex()
                    .equals(employee.getPassword())) {
                throw new IllegalArgumentException("新密码不能与当前密码相同");
            }
        }
        employee.setPasswordChangeRequired(temporary);
        byte[] bytes = new byte[16];
        new java.security.SecureRandom().nextBytes(bytes);
        employee.setSalt(
                java.util.Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes)
                        .substring(0, 20));
        employee.setPassword(new Sha256Hash(password, employee.getSalt()).toHex());
    }

    /** 校验本人新密码, 禁止保留统一初始密码. */
    @Override
    public void validateNewPassword(String password) {
        validatePassword(password);
        if (TEMPORARY_PASSWORD.equals(password)) {
            throw new IllegalArgumentException("新密码不能继续使用统一初始密码");
        }
    }

    /** 校验新设密码为6到32位不含空格的可打印ASCII字符. */
    private void validatePassword(String password) {
        if (password == null || !password.matches("[!-~]{6,32}")) {
            throw new IllegalArgumentException("请设置6到32位密码, 不含空格");
        }
    }
}
