package com.platform.modules.sys.service;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** 管理已明确选择的单工作室基本名称, 不修改历史归属或账号绑定. */
@Service
@RequiredArgsConstructor
public class PlatformWorkspace {
    private final BusinessOrganization organization;
    private final JdbcTemplate jdbc;
    private final OperationAudit audit;

    /** 返回当前已配置的工作室基本资料. */
    public Map<String, Object> get() {
        String code = organization.defaultOrganization();
        if (code == null) {
            throw new IllegalArgumentException("默认工作室未确定, 请核对已有工作室配置和增量执行结果");
        }
        return jdbc.queryForMap(
                "SELECT ORG_NO AS code, ORG_NAME AS name FROM SYS_ORG WHERE ORG_NO=? AND STATUS=1",
                code);
    }

    /** 仅更新名称, 编码与归属不由页面参数变更. */
    @Transactional(rollbackFor = Exception.class)
    public void rename(String name) {
        if (name == null || name.isBlank() || name.strip().length() > 50) {
            throw new IllegalArgumentException("请填写1到50字的工作室名称");
        }
        String code = organization.defaultOrganization();
        if (code == null
                || jdbc.update(
                                "UPDATE SYS_ORG SET ORG_NAME=? WHERE ORG_NO=? AND STATUS=1",
                                name.strip(),
                                code)
                        != 1) {
            throw new IllegalArgumentException("默认工作室不可用");
        }
        audit.success("修改工作室基本信息", code, "更新工作室名称");
    }
}
