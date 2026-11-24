package com.platform.datascope;

import com.platform.common.utils.Constant;
import com.platform.common.utils.ShiroUtils;
import com.platform.modules.sys.entity.AccountTier;
import com.platform.modules.sys.entity.SysUserEntity;
import com.platform.modules.sys.service.BackOfficePermissions;
import com.platform.modules.sys.service.BusinessOrganization;
import com.platform.modules.sys.service.OrgActionScope;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** 从现有角色、动作和机构绑定计算独立范围, 供管理端请求使用. */
@Service
@RequiredArgsConstructor
public class StaffAuthority {
    private final JdbcTemplate jdbc;
    private final BusinessOrganization organization;

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

    /** 仅固定平台账号具备商家管理员管理身份. */
    public boolean emergency() {
        var user = ShiroUtils.getUserEntity();
        return Constant.SUPER_ADMIN.equals(user.getUserId())
                && AccountTier.PLATFORM_ADMIN.equals(user.getAccountType());
    }

    /** 判断认证主体是否为默认工作室的商家管理员. */
    public boolean merchant() {
        var user = ShiroUtils.getUserEntity();
        return AccountTier.MERCHANT_ADMIN.equals(user.getAccountType())
                && user.getOrgNo() != null
                && Objects.equals(user.getOrgNo(), organization.defaultOrganization());
    }

    /** 限制单工作室共享设置的访问范围. */
    public void requireWorkspace(String action) {
        if (!scope(action).permits(organization.defaultOrganization())) {
            throw new UnauthorizedException("不在当前工作室授权范围内");
        }
    }

    /** 确保员工具体操作落在商家管理员的本工作室权限, 目标必须为员工. */
    public void requireEmployee(String action, SysUserEntity target, boolean write) {
        if (!merchant()
                || target == null
                || !AccountTier.EMPLOYEE.equals(target.getAccountType())
                || !scope(action).permits(target.getOrgNo())) {
            throw new UnauthorizedException("员工不在该动作的机构范围内");
        }
    }

    /** 员工管理入口必须属于当前单工作室. */
    public void requireEmployeeOrganization(String action, String targetOrganization) {
        if (!merchant() || !scope(action).permits(targetOrganization)) {
            throw new UnauthorizedException("没有此机构的员工管理权限");
        }
    }

    /** 仅分配当前工作室内的共享经营岗位, 个人授权角色不可转绑. */
    public boolean canAssign(String roleId) {
        return merchant()
                && Boolean.TRUE.equals(
                        jdbc.queryForObject(
                                "SELECT COUNT(*)=1 FROM SYS_ROLE WHERE ROLE_ID=? AND DELEGABLE=1"
                                    + " AND DIRECT_USER_ID IS NULL AND ROLE_ID<>'cc-job-system'",
                                Boolean.class,
                                roleId))
                && withinOrganizations(roleId, scope("sys:user:assign"))
                && jdbc
                        .queryForList(
                                "SELECT m.PERMS FROM SYS_ROLE_MENU rm JOIN SYS_MENU m ON"
                                    + " m.MENU_ID=rm.MENU_ID WHERE rm.ROLE_ID=? AND m.PERMS IS NOT"
                                    + " NULL AND m.PERMS<>''",
                                String.class,
                                roleId)
                        .stream()
                        .allMatch(this::businessActions);
    }

    /** 校验岗位或个人授权中的所有节点, 拒绝系统动作与通配授权. */
    public List<String> assignableMenus(List<String> menuIds) {
        if (!merchant()
                || menuIds == null
                || menuIds.size() > 200
                || menuIds.stream().anyMatch(id -> id == null || id.isBlank())) {
            throw new UnauthorizedException("功能权限列表无效");
        }
        Set<String> allowed = new HashSet<>();
        for (var entry : jdbc.queryForList("SELECT MENU_ID,PERMS,TYPE FROM SYS_MENU")) {
            Object permission = entry.get("PERMS");
            if (permission instanceof String value && !value.isBlank() && businessActions(value)) {
                allowed.add(String.valueOf(entry.get("MENU_ID")));
            }
        }
        if (!allowed.containsAll(menuIds)) {
            throw new UnauthorizedException("只能分配明确的经营功能");
        }
        return menuIds.stream().distinct().toList();
    }

    /** 检查一个权限节点中的全部精确经营动作. */
    private boolean businessActions(String node) {
        return Arrays.stream(node.split(",", -1))
                .map(String::trim)
                .allMatch(BackOfficePermissions::isBusiness);
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
        var user = ShiroUtils.getUserEntity();
        if (merchant() && org.apache.shiro.SecurityUtils.getSubject().isPermitted(action)) {
            return new OrgActionScope(false, List.of(user.getOrgNo()));
        }
        if (!AccountTier.EMPLOYEE.equals(user.getAccountType())
                || !BackOfficePermissions.isBusiness(action)) {
            return new OrgActionScope(false, List.of());
        }
        String userId = user.getUserId();
        List<String> organizations =
                jdbc.query(
                        "SELECT DISTINCT o.ORG_NO FROM SYS_USER_ROLE ur JOIN SYS_ROLE r ON"
                            + " r.ROLE_ID=ur.ROLE_ID JOIN SYS_ROLE_MENU rm ON rm.ROLE_ID=r.ROLE_ID"
                            + " JOIN SYS_MENU m ON m.MENU_ID=rm.MENU_ID JOIN SYS_ROLE_ORG ro ON"
                            + " ro.ROLE_ID=r.ROLE_ID JOIN SYS_ORG o ON o.ORG_NO=ro.ORG_NO WHERE"
                            + " ur.USER_ID=? AND o.ORG_NO=? AND o.STATUS=1 AND (r.DIRECT_USER_ID IS"
                            + " NULL OR r.DIRECT_USER_ID=ur.USER_ID) AND FIND_IN_SET(?,"
                            + " REPLACE(m.PERMS,' ',''))>0",
                        (row, index) -> row.getString(1),
                        userId,
                        user.getOrgNo(),
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
        throw new UnauthorizedException("无法确定本次操作的数据归属机构");
    }
}
