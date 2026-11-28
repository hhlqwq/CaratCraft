package com.platform.modules.sys.service;

import com.platform.common.utils.IpUtils;
import com.platform.modules.sys.entity.SysUserEntity;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.SecurityUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** 统一写入现有SYS_LOG, 只接受业务明确提供的安全摘要, 不序列化请求或凭据. */
@Service
@RequiredArgsConstructor
public class OperationAudit {
    public static final String RECORDED = "caratcraft.audit.recorded";
    private final JdbcTemplate jdbc;

    /** 在业务事务内记录成功变更, 并标识同请求已记录以避免切面重复. */
    public void success(String action, String target, String summary) {
        write(action, "账户与权限", "CHANGE", target, "SUCCESS", summary, null, 0, null);
        HttpServletRequest request = currentRequest();
        if (request != null) {
            request.setAttribute(RECORDED, true);
        }
    }

    /** 写入一个明确结果的事件, 查询方只取得结构化安全字段. */
    public void write(
            String action,
            String module,
            String type,
            String target,
            String result,
            String summary,
            String code,
            long time,
            String attemptedUser) {
        SysUserEntity actor = null;
        try {
            Object principal = SecurityUtils.getSubject().getPrincipal();
            if (principal instanceof SysUserEntity user) {
                actor = user;
            }
        } catch (org.apache.shiro.UnavailableSecurityManagerException ignored) {
            // 独立维护任务使用明确标记的平台身份, 不依赖Web会话.
        }
        if (actor == null && attemptedUser != null && "SUCCESS".equals(result)) {
            var actors =
                    jdbc.query(
                            "SELECT USER_ID,USER_NAME,ACCOUNT_TYPE,ORG_NO FROM SYS_USER WHERE"
                                    + " USER_NAME=?",
                            (row, index) -> {
                                var user = new SysUserEntity();
                                user.setUserId(row.getString(1));
                                user.setUserName(row.getString(2));
                                user.setAccountType(row.getString(3));
                                user.setOrgNo(row.getString(4));
                                return user;
                            },
                            attemptedUser);
            if (actors.size() == 1) {
                actor = actors.getFirst();
            }
        }
        HttpServletRequest request = currentRequest();
        jdbc.update(
                "INSERT INTO"
                    + " SYS_LOG(ID,USER_NAME,OPERATION,METHOD,PARAMS,TIME,IP,CREATE_TIME,SCOPE_ORG_NO,"
                    + "actor_id,actor_type,module,operation_type,target_id,result,summary,error_code)"
                    + " VALUES(REPLACE(UUID(),'-',''),?,?,'操作审计',NULL,?,?,NOW(),?,?,?,?,?,?,?,?,?)",
                text(actor == null ? attemptedUser : actor.getUserName(), 50),
                text(action, 50),
                time,
                request == null ? null : text(IpUtils.getIpAddr(request), 64),
                actor == null ? null : actor.getOrgNo(),
                actor == null ? null : actor.getUserId(),
                actor == null ? "ANONYMOUS" : actor.getAccountType(),
                text(module, 50),
                text(type, 32),
                text(target, 128),
                text(result, 16),
                text(summary, 1000),
                text(code, 40));
    }

    /** 非Web任务没有请求上下文, 审计仍可保存明确的系统事件. */
    private HttpServletRequest currentRequest() {
        var attributes =
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        return attributes
                        instanceof
                        org.springframework.web.context.request.ServletRequestAttributes servlet
                ? servlet.getRequest()
                : null;
    }

    /** 限制可读摘要长度, 清除控制字符, 不接收异常堆栈. */
    private String text(String value, int maximum) {
        if (value == null) {
            return null;
        }
        String safe = value.replaceAll("[\\p{Cntrl}]", " ");
        return safe.substring(0, Math.min(safe.length(), maximum));
    }
}
