package com.platform.modules.sys.service;

import java.util.Set;
import java.util.regex.Pattern;

/** 维护账号层级与岗位可授予动作的边界, 不展开任何通配项. */
public final class BackOfficePermissions {
    public static final String MANAGE_MERCHANTS = "platform:merchant:manage";
    public static final String PLATFORM_OVERVIEW = "platform:overview:view";
    public static final String PLATFORM_MONITOR = "platform:monitor:view";
    public static final String PLATFORM_RELEASE = "platform:release:manage";
    public static final String PLATFORM_AUDIT = "platform:audit:view";
    public static final Set<String> PLATFORM =
            Set.of(
                    MANAGE_MERCHANTS,
                    PLATFORM_OVERVIEW,
                    PLATFORM_MONITOR,
                    PLATFORM_RELEASE,
                    PLATFORM_AUDIT);
    public static final Set<String> MERCHANT_ACCOUNT_MANAGEMENT =
            Set.of(
                    "sys:user:list",
                    "sys:user:info",
                    "sys:user:save",
                    "sys:user:update",
                    "sys:user:assign",
                    "sys:user:resetPw",
                    "sys:role:list",
                    "sys:role:select",
                    "sys:role:info",
                    "sys:role:save",
                    "sys:role:update",
                    "sys:role:delete",
                    "sys:log:list",
                    "sys:menu:list");
    private static final Pattern BUSINESS =
            Pattern.compile("(?:mall|custom|community|merchant):[a-zA-Z0-9-]+(?::[a-zA-Z0-9-]+)+");
    private static final Set<String> IMAGE_PERMISSIONS =
            Set.of(
                    "sys:oss:list",
                    "sys:oss:info",
                    "sys:oss:save",
                    "sys:oss:upload",
                    "sys:oss:delete");

    /** 静态权限边界不对外实例化. */
    private BackOfficePermissions() {}

    /** 仅接纳明确的经营动作, 拒绝系统管理节点、通配及隐式权限. */
    public static boolean isBusiness(String action) {
        return action != null
                && (BUSINESS.matcher(action).matches() || IMAGE_PERMISSIONS.contains(action));
    }
}
