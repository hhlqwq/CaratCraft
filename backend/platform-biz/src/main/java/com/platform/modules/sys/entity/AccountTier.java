package com.platform.modules.sys.entity;

import com.platform.common.utils.Constant;

/** 管理端账号层级独立于可授予的岗位角色. */
public final class AccountTier {
    public static final String PLATFORM_ADMIN = "PLATFORM_ADMIN";
    public static final String MERCHANT_ADMIN = "MERCHANT_ADMIN";
    public static final String EMPLOYEE = "EMPLOYEE";

    /** 静态层级定契约不对外实例化. */
    private AccountTier() {}

    /** 仅允许固定标识的账号拥有平台层级, 拒绝未映射账号. */
    public static boolean valid(SysUserEntity user) {
        if (user == null) {
            return false;
        }
        if (Constant.SUPER_ADMIN.equals(user.getUserId())) {
            return PLATFORM_ADMIN.equals(user.getAccountType());
        }
        return MERCHANT_ADMIN.equals(user.getAccountType())
                || EMPLOYEE.equals(user.getAccountType());
    }
}
