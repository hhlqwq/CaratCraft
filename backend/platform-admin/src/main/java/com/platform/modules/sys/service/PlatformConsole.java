package com.platform.modules.sys.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.platform.datascope.StaffAuthority;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

/** 平台工作区仅聚合账号、系统和维护状态, 不访问商家的经营数据. */
@Service
@org.springframework.scheduling.annotation.EnableScheduling
@RequiredArgsConstructor
public class PlatformConsole {
    private final StaffAuthority authority;
    private final JdbcTemplate jdbc;
    private final MaintenanceClient maintenance;

    @Value("${caratcraft.environment:production}")
    private String environment;

    /** 身份及唯一平台账号双重核对, 角色不能赋予平台身份. */
    public void requirePlatform() {
        if (!authority.emergency()) {
            throw new UnauthorizedException("仅平台超级管理员可访问");
        }
    }

    /** 展示实际运行版本和服务状态, 不混入订单、收入和客户信息. */
    public Map<String, Object> overview() {
        requirePlatform();
        var value = new HashMap<String, Object>();
        value.put("environment", environment);
        value.put("uptimeMillis", ManagementFactory.getRuntimeMXBean().getUptime());
        value.put(
                "merchants",
                jdbc.queryForMap(
                        "SELECT COUNT(*) AS total, COALESCE(SUM(STATUS=1),0) AS active,"
                                + " COALESCE(SUM(STATUS<>1),0) AS disabled FROM SYS_USER WHERE"
                                + " ACCOUNT_TYPE='MERCHANT_ADMIN'"));
        value.put("release", maintenance.read("release"));
        value.put("monitor", maintenance.read("monitor"));
        value.put("jobs", jobs());
        value.put(
                "events",
                jdbc.queryForList(
                        "SELECT USER_NAME AS actor, OPERATION AS operation, result, CREATE_TIME AS"
                                + " time FROM SYS_LOG WHERE actor_type='PLATFORM_ADMIN' OR"
                                + " module='系统维护' ORDER BY CREATE_TIME DESC LIMIT 8"));
        return value;
    }

    /** 将持久维护任务的重要结果去重同步到同一个SYS_LOG, 不建立第二套审计表. */
    public JsonNode jobs() {
        requirePlatform();
        JsonNode jobs = maintenance.read("jobs");
        recordTasks(jobs);
        return jobs;
    }

    /** 后台定时同步终态, 任务审计不依赖用户是否打开页面. */
    @org.springframework.scheduling.annotation.Scheduled(initialDelay = 30000, fixedDelay = 30000)
    public void synchronizeAudit() {
        try {
            recordTasks(maintenance.read("jobs"));
        } catch (RuntimeException error) {
            org.slf4j.LoggerFactory.getLogger(PlatformConsole.class).warn("维护结果审计同步暂不可用, 将在下次重试");
        }
    }

    /** 以任务与结果的稳定标识防止轮询重复入库. */
    private void recordTasks(JsonNode jobs) {
        for (JsonNode job : jobs) {
            String status = job.path("status").asText();
            if (!java.util.Set.of("SUCCEEDED", "FAILED", "FAILED_RECOVERED", "RESTORED")
                    .contains(status)) {
                continue;
            }
            String id = job.path("id").asText();
            String key =
                    java.util
                            .UUID
                            .nameUUIDFromBytes(
                                    (id + status).getBytes(java.nio.charset.StandardCharsets.UTF_8))
                            .toString()
                            .replace("-", "");
            jdbc.update(
                    "INSERT IGNORE INTO"
                        + " SYS_LOG(ID,USER_NAME,OPERATION,METHOD,PARAMS,TIME,CREATE_TIME,actor_id,actor_type,module,operation_type,target_id,result,summary)"
                        + " SELECT ?,USER_NAME,'离线版本维护','操作审计',NULL,0,?,"
                        + " USER_ID,ACCOUNT_TYPE,'系统维护','UPDATE',?,?,? FROM SYS_USER WHERE"
                        + " USER_ID='1' AND ACCOUNT_TYPE='PLATFORM_ADMIN'",
                    key,
                    java.sql.Timestamp.from(
                            java.time.Instant.parse(job.path("updatedAt").asText())),
                    id,
                    status.equals("SUCCEEDED") || status.equals("RESTORED") ? "SUCCESS" : "FAILED",
                    "维护任务" + status + ", 目标版本" + job.path("version").asText());
        }
    }

    /** 使用绑定参数和固定筛选项只读取安全审计字段, 不返回历史参数原文. */
    public Map<String, Object> logs(
            int page,
            String account,
            String module,
            String result,
            String type,
            String start,
            String end) {
        requirePlatform();
        if (page < 1 || page > 100000) {
            throw new IllegalArgumentException("页码须为1到100000");
        }
        var values = new java.util.ArrayList<Object>();
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        for (var filter :
                java.util.List.of(
                        new String[] {"USER_NAME", account},
                        new String[] {"module", module},
                        new String[] {"result", result},
                        new String[] {"operation_type", type})) {
            if (filter[1] != null && !filter[1].isBlank()) {
                where.append(" AND ").append(filter[0]).append("=?");
                values.add(filter[1]);
            }
        }
        if (start != null && !start.isBlank()) {
            where.append(" AND CREATE_TIME>=?");
            values.add(java.sql.Date.valueOf(start));
        }
        if (end != null && !end.isBlank()) {
            where.append(" AND CREATE_TIME<?");
            values.add(java.sql.Date.valueOf(java.time.LocalDate.parse(end).plusDays(1)));
        }
        long total =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM SYS_LOG" + where, Long.class, values.toArray());
        values.add(Math.max(0, page - 1) * 20);
        var records =
                jdbc.queryForList(
                        "SELECT ID AS id,USER_NAME AS userName,OPERATION AS operation,CREATE_TIME"
                                + " AS createTime,IP AS ip,actor_type AS actorType,SCOPE_ORG_NO AS"
                                + " scopeOrgNo,module,operation_type AS operationType,target_id AS"
                                + " targetId,result,summary,error_code AS errorCode FROM SYS_LOG"
                                + where
                                + " ORDER BY CREATE_TIME DESC LIMIT 20 OFFSET ?",
                        values.toArray());
        return Map.of("total", total, "records", records);
    }
}
