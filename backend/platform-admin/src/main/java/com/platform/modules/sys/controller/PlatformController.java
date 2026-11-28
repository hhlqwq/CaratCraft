package com.platform.modules.sys.controller;

import com.platform.common.annotation.SysLog;
import com.platform.common.utils.RestResponse;
import com.platform.modules.sys.service.BackOfficePermissions;
import com.platform.modules.sys.service.MaintenanceClient;
import com.platform.modules.sys.service.PlatformConsole;
import com.platform.modules.sys.service.PlatformWorkspace;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/** 超管固定操作入口, 各接口独立鉴权且不接受任意维护命令. */
@RestController
@RequestMapping("/platform")
@RequiredArgsConstructor
public class PlatformController {
    private final PlatformConsole console;
    private final PlatformWorkspace workspace;
    private final MaintenanceClient maintenance;
    private final com.platform.modules.sys.service.HostMonitorService hostMonitor;
    private final com.fasterxml.jackson.databind.ObjectMapper json;

    /** 获取平台系统总览. */
    @GetMapping("/overview")
    @RequiresPermissions(BackOfficePermissions.PLATFORM_OVERVIEW)
    public RestResponse<?> overview() {
        return RestResponse.ok(console.overview());
    }

    /** 获取默认工作室基本资料. */
    @GetMapping("/workspace")
    @RequiresPermissions(BackOfficePermissions.MANAGE_MERCHANTS)
    public RestResponse<?> workspace() {
        console.requirePlatform();
        return RestResponse.ok(workspace.get());
    }

    /** 只更新明确允许的工作室名称. */
    @PostMapping("/workspace")
    @RequiresPermissions(BackOfficePermissions.MANAGE_MERCHANTS)
    @SysLog("修改工作室基本信息")
    public RestResponse<?> workspace(@RequestBody Map<String, String> request) {
        console.requirePlatform();
        if (!request.keySet().equals(java.util.Set.of("name"))) {
            throw new IllegalArgumentException("仅允许修改工作室名称");
        }
        workspace.rename(request.get("name"));
        return RestResponse.ok();
    }

    /** 获取真实宿主及容器监控. */
    @GetMapping("/monitor")
    @RequiresPermissions(BackOfficePermissions.PLATFORM_MONITOR)
    public RestResponse<?> monitor() {
        console.requirePlatform();
        var result = (com.fasterxml.jackson.databind.node.ObjectNode) maintenance.read("monitor");
        result.set("jvm", json.valueToTree(hostMonitor.currentJvm()));
        return RestResponse.ok(result);
    }

    /** 获取独立的软件发行记录. */
    @GetMapping("/release")
    @RequiresPermissions(BackOfficePermissions.PLATFORM_RELEASE)
    public RestResponse<?> release() {
        console.requirePlatform();
        return RestResponse.ok(maintenance.read("release"));
    }

    /** 获取持久升级进度并同步重要结果到统一审计. */
    @GetMapping("/jobs")
    @RequiresPermissions(BackOfficePermissions.PLATFORM_RELEASE)
    public RestResponse<?> jobs() {
        return RestResponse.ok(console.jobs());
    }

    /** 流式上传发行包, 校验通过后仍需明确确认才执行. */
    @PostMapping(value = "/packages", consumes = "application/octet-stream")
    @RequiresPermissions(BackOfficePermissions.PLATFORM_RELEASE)
    @SysLog("校验离线发行包")
    public RestResponse<?> upload(HttpServletRequest request) throws IOException {
        console.requirePlatform();
        return RestResponse.ok(
                maintenance.upload(request.getInputStream(), request.getContentLengthLong()));
    }

    /** 确认升级持久任务, 不在Web请求中执行部署. */
    @PostMapping("/confirm")
    @RequiresPermissions(BackOfficePermissions.PLATFORM_RELEASE)
    @SysLog("确认离线升级")
    public RestResponse<?> confirm(@RequestBody Task request) {
        console.requirePlatform();
        return RestResponse.ok(maintenance.action("confirm", request.id()));
    }

    /** 明确请求恢复失败任务的一致备份. */
    @PostMapping("/restore")
    @RequiresPermissions(BackOfficePermissions.PLATFORM_RELEASE)
    @SysLog("恢复失败升级")
    public RestResponse<?> restore(@RequestBody Task request) {
        console.requirePlatform();
        return RestResponse.ok(maintenance.action("restore", request.id()));
    }

    /** 按账号、模块、结果和日期分页查询安全平台审计. */
    @GetMapping("/logs")
    @RequiresPermissions(BackOfficePermissions.PLATFORM_AUDIT)
    public RestResponse<?> logs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String account,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        return RestResponse.ok(console.logs(page, account, module, result, type, start, end));
    }

    /** 任务标识不携带容器名或执行参数. */
    public record Task(String id) {}
}
