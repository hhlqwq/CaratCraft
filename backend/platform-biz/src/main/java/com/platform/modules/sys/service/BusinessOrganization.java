package com.platform.modules.sys.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** 为无管理端操作员的单商城入口提供明确配置的业务归属. */
@Component
public class BusinessOrganization {
    private final JdbcTemplate jdbc;
    private final String defaultOrganization;

    /** 保存部署端配置, 归属不从客户端、地址或客户身份推断. */
    public BusinessOrganization(
            JdbcTemplate jdbc,
            @Value("${caratcraft.default-business-org:}") String defaultOrganization) {
        this.jdbc = jdbc;
        this.defaultOrganization = defaultOrganization.trim();
    }

    /** 未配置时保留未知归属, 无效配置时终止新增. */
    public String defaultOrganization() {
        String organization = defaultOrganization;
        if (organization.isEmpty()) {
            var configured =
                    jdbc.queryForList(
                            "SELECT PARAM_VALUE FROM SYS_CONFIG WHERE"
                                    + " PARAM_KEY='caratcraft.business.org'",
                            String.class);
            if (configured.size() != 1
                    || configured.get(0) == null
                    || configured.get(0).isBlank()) {
                return null;
            }
            organization = configured.get(0).trim();
        }
        Long count =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM SYS_ORG WHERE ORG_NO=? AND STATUS=1",
                        Long.class,
                        organization);
        if (count == null || count != 1) {
            throw new IllegalStateException("商城默认业务归属机构不可用");
        }
        return organization;
    }
}
