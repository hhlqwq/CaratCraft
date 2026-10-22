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

import com.platform.common.utils.RestResponse;
import com.platform.modules.sys.service.HostMonitorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 服务器监控接口, 使用项目内采集服务替代旧监控包装库. */
@Tag(name = "SysMonitorController|服务器监控")
@RestController
@RequestMapping("/sys/monitor")
public class SysMonitorController {
    @Resource private HostMonitorService hostMonitorService;

    /** 在原权限边界内返回兼容的服务器快照. */
    @Operation(summary = "服务器监控", description = "服务器监控")
    @PostMapping("/server")
    @RequiresPermissions("sys:monitor:server")
    public RestResponse<Map<String, Object>> monitor() {
        return RestResponse.ok(hostMonitorService.collect());
    }
}
