package com.platform.datascope;

import com.platform.common.utils.Constant;
import com.platform.common.utils.ShiroUtils;
import com.platform.modules.sys.entity.SysUserEntity;
import com.platform.modules.sys.service.OrgActionScope;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** 从现有角色、动作和机构绑定计算独立范围, 供管理端请求使用. */
@Service
@RequiredArgsConstructor
public class StaffAuthority {
    private final JdbcTemplate jdbc;

    public enum Resource {
        GOODS("mall_goods", "id"),
        ORDER("mall_order", "id"),
        FEEDBACK("mall_feedback", "msg_id"),
        COMMUNITY("CC_COMMUNITY_POST", "id");

        private final String table;
        private final String id;

        /** 注册允许检查业务归属的根表和主键. */
        Resource(String table, String id) {
            this.table = table;
            this.id = id;
        }
    }

    /** 仅根据服务端认证主体识别应急管理员. */
    public boolean emergency() {
        return Constant.SUPER_ADMIN.equals(ShiroUtils.getUserEntity().getUserId());
    }

    /** 检查操作人员对目标员工的独立机构范围, 修改途径统一禁止自授权和接管管理账号. */
    public void requireEmployee(String action, SysUserEntity target, boolean write) {
        if (target == null) {
            throw new UnauthorizedException("员工不可访问");
        }
        if (!write && emergency()) {
            return;
        }
        if (write
                && (Constant.SUPER_ADMIN.equals(target.getUserId())
                        || ShiroUtils.getUserEntity().getUserId().equals(target.getUserId()))) {
            throw new UnauthorizedException("不能通过员工管理修改本人或应急账号");
        }
        if (emergency()) {
            return;
        }
        if (Constant.SUPER_ADMIN.equals(target.getUserId())
                || !scope(action).permits(target.getOrgNo())) {
            throw new UnauthorizedException("员工不在该动作的机构范围内");
        }
        if (write) {
            Long protectedRoles =
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM SYS_USER_ROLE ur JOIN SYS_ROLE r ON"
                                + " r.ROLE_ID=ur.ROLE_ID WHERE ur.USER_ID=? AND (r.DELEGABLE=0 OR"
                                + " r.ROLE_ID='cc-job-system' OR EXISTS (SELECT 1 FROM"
                                + " SYS_ROLE_MENU rm JOIN SYS_MENU m ON m.MENU_ID=rm.MENU_ID WHERE"
                                + " rm.ROLE_ID=r.ROLE_ID AND "
                                    + incompatibleDelegation()
                                    + "))",
                            Long.class,
                            target.getUserId());
            if (protectedRoles != null && protectedRoles > 0) {
                throw new UnauthorizedException("该员工含受保护的角色");
            }
        }
    }

    /** 员工所属机构必须在对应员工动作范围内且为已启用机构. */
    public void requireEmployeeOrganization(String action, String organization) {
        if (organization == null
                || organization.isBlank()
                || !scope(action).permits(organization)
                || !Boolean.TRUE.equals(
                        jdbc.queryForObject(
                                "SELECT COUNT(*)=1 FROM SYS_ORG WHERE ORG_NO=? AND STATUS=1",
                                Boolean.class,
                                organization))) {
            throw new UnauthorizedException("没有此机构的员工管理权限");
        }
    }

    /** 检验受委派角色的权限来源和全部机构, 避免代分配时扩大范围. */
    public boolean canAssign(String roleId) {
        if (emergency()) {
            return true;
        }
        OrgActionScope grantScope = scope("sys:user:assign");
        return !"cc-job-system".equals(roleId)
                && Boolean.TRUE.equals(
                        jdbc.queryForObject(
                                "SELECT COUNT(*)=1 FROM SYS_ROLE r WHERE ROLE_ID=? AND DELEGABLE=1"
                                        + " AND NOT EXISTS (SELECT 1 FROM SYS_ROLE_MENU rm JOIN"
                                        + " SYS_MENU m ON m.MENU_ID=rm.MENU_ID WHERE"
                                        + " rm.ROLE_ID=r.ROLE_ID AND "
                                        + incompatibleDelegation()
                                        + ")",
                                Boolean.class,
                                roleId))
                && withinOrganizations(roleId, grantScope);
    }

    /** 系统权限和通配节点不能通过普通员工管理转授. */
    private String incompatibleDelegation() {
        return "(LOWER(REPLACE(m.PERMS,' ','')) REGEXP '(^|,)sys:' OR INSTR(m.PERMS,'*')>0)";
    }

    /** 当且仅当角色有明确的机构且全部落在允许范围内时可委派. */
    public boolean withinOrganizations(String roleId, OrgActionScope scope) {
        List<String> organizations =
                jdbc.queryForList(
                        "SELECT ORG_NO FROM SYS_ROLE_ORG WHERE ROLE_ID=?", String.class, roleId);
        return !organizations.isEmpty() && organizations.stream().allMatch(scope::permits);
    }

    /** 在变更同一事务写入无凭据审计, 包含对象和通过服务端校验的角色. */
    public void audit(String action, String target, String change) {
        var actor = ShiroUtils.getUserEntity();
        jdbc.update(
                "INSERT INTO"
                    + " SYS_LOG(ID,USER_NAME,OPERATION,METHOD,PARAMS,TIME,CREATE_TIME,SCOPE_ORG_NO)"
                    + " VALUES(REPLACE(UUID(),'-',''),?,?,?,?,0,NOW(),?)",
                actor.getUserName(),
                action,
                "员工与岗位授权",
                "对象=" + target + "; " + change,
                actor.getOrgNo());
    }

    /** 仅合并明确持有本动作的现存角色所配置的启用机构, 不在此展开旧通配. */
    public OrgActionScope scope(String action) {
        if (emergency()) {
            return new OrgActionScope(true, List.of());
        }
        String userId = ShiroUtils.getUserEntity().getUserId();
        List<String> organizations =
                jdbc.query(
                        "SELECT DISTINCT o.ORG_NO FROM SYS_USER_ROLE ur JOIN SYS_ROLE r ON"
                            + " r.ROLE_ID=ur.ROLE_ID JOIN SYS_ROLE_MENU rm ON rm.ROLE_ID=r.ROLE_ID"
                            + " JOIN SYS_MENU m ON m.MENU_ID=rm.MENU_ID JOIN SYS_ROLE_ORG ro ON"
                            + " ro.ROLE_ID=r.ROLE_ID JOIN SYS_ORG o ON o.ORG_NO=ro.ORG_NO WHERE"
                            + " ur.USER_ID=? AND o.STATUS=1 AND FIND_IN_SET(?, REPLACE(m.PERMS,'"
                            + " ',''))>0",
                        (row, index) -> row.getString(1),
                        userId,
                        action);
        return new OrgActionScope(false, organizations);
    }

    /** 校验单笔或批量请求的所有业务记录, 防止忽略部分权限后写入. */
    public void require(String action, Resource resource, List<? extends Number> ids) {
        if (ids.isEmpty() || ids.stream().anyMatch(id -> id == null || id.longValue() <= 0)) {
            throw new IllegalArgumentException("业务记录编号无效");
        }
        OrgActionScope scope = scope(action);
        if (scope.emergency()) {
            return;
        }
        List<Object> parameters = new java.util.ArrayList<>(ids);
        String condition = scope.andWhere("scope_org_no", parameters);
        Long count =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM "
                                + resource.table
                                + " WHERE "
                                + resource.id
                                + " IN ("
                                + String.join(",", Collections.nCopies(ids.size(), "?"))
                                + ")"
                                + condition,
                        Long.class,
                        parameters.toArray());
        if (count == null || count != ids.stream().distinct().count()) {
            throw new UnauthorizedException("目标不在该操作的数据范围内");
        }
    }

    /** 校验指定业务记录的写入或明细权限. */
    public void require(String action, Resource resource, Number id) {
        require(action, resource, Arrays.asList(id));
    }

    /** 新建记录优先选择员工所在且有权创建的机构, 多个候选无法确定时要求明确授权配置. */
    public String organizationForCreation(String action, String defaultOrganization) {
        OrgActionScope scope = scope(action);
        String own = ShiroUtils.getUserEntity().getOrgNo();
        if (own != null && scope.permits(own)) {
            return own;
        }
        if (defaultOrganization != null && scope.permits(defaultOrganization)) {
            return defaultOrganization;
        }
        if (scope.organizations().size() == 1) {
            return scope.organizations().get(0);
        }
        if (scope.emergency()) {
            return defaultOrganization;
        }
        throw new UnauthorizedException("无法确定本次操作的数据归属机构");
    }
}
