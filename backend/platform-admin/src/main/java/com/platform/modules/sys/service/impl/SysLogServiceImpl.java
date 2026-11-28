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
import com.platform.modules.sys.dao.SysLogDao;
import com.platform.modules.sys.entity.SysLogEntity;
import com.platform.modules.sys.service.SysLogService;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author hehailong
 */
@Service("sysLogService")
public class SysLogServiceImpl extends ServiceImpl<SysLogDao, SysLogEntity>
        implements SysLogService {

    @Override
    public Page<SysLogEntity> queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        String ip = (String) params.get("ip");

        var pagination = new java.util.HashMap<String, Object>(params);
        pagination.remove("sidx");
        pagination.remove("asc");
        Page<SysLogEntity> page = new Query<SysLogEntity>(pagination).getPage();
        String module = (String) params.get("module");
        String type = (String) params.get("type");
        String result = (String) params.get("result");
        String start = (String) params.get("start");
        String end = (String) params.get("end");
        var scope = (com.platform.modules.sys.service.OrgActionScope) params.get("auditScope");

        return baseMapper.selectPage(
                page,
                new QueryWrapper<SysLogEntity>()
                        .select(
                                "ID",
                                "USER_NAME",
                                "OPERATION",
                                "TIME",
                                "IP",
                                "CREATE_TIME",
                                "SCOPE_ORG_NO",
                                "actor_id",
                                "actor_type",
                                "module",
                                "operation_type",
                                "target_id",
                                "result",
                                "summary",
                                "error_code")
                        .apply(
                                !scope.emergency(),
                                scope.mybatisCondition("SCOPE_ORG_NO"),
                                scope.organizations().toArray())
                        .eq(StringUtils.isNotBlank(module), "module", module)
                        .eq(StringUtils.isNotBlank(type), "operation_type", type)
                        .eq(StringUtils.isNotBlank(result), "result", result)
                        .ge(
                                StringUtils.isNotBlank(start),
                                "CREATE_TIME",
                                StringUtils.isNotBlank(start) ? java.sql.Date.valueOf(start) : null)
                        .lt(
                                StringUtils.isNotBlank(end),
                                "CREATE_TIME",
                                StringUtils.isNotBlank(end)
                                        ? java.sql.Date.valueOf(
                                                java.time.LocalDate.parse(end).plusDays(1))
                                        : null)
                        .like(StringUtils.isNotBlank(ip), "IP", ip)
                        .and(
                                StringUtils.isNotBlank(key),
                                queryWrapper ->
                                        queryWrapper
                                                .like("USER_NAME", key)
                                                .or()
                                                .like("OPERATION", key))
                        .orderByDesc("CREATE_TIME"));
    }
}
