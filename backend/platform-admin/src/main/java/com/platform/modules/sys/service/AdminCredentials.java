package com.platform.modules.sys.service;

import com.platform.modules.sys.entity.SysUserEntity;

import org.apache.shiro.crypto.hash.Sha256Hash;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

/** 生成管理账号的独立临时密码与存储凭据. */
public final class AdminCredentials {
    private static final SecureRandom RANDOM = new SecureRandom();

    /** 工具类不对外实例化. */
    private AdminCredentials() {}

    /** 为单个账号签发 24 位随机密码. */
    public static String randomPassword() {
        return randomText(18);
    }

    /** 使用现有认证器兼容的加盐格式保存凭据. */
    public static void replace(
            SysUserEntity account, String password, boolean temporary, Date expiresAt) {
        account.setSalt(randomText(15));
        account.setPassword(new Sha256Hash(password, account.getSalt()).toHex());
        account.setPasswordChangeRequired(temporary);
        account.setTemporaryPasswordExpiresAt(expiresAt);
    }

    /** 从系统安全随机源生成适合账号凭据的文本. */
    private static String randomText(int count) {
        byte[] bytes = new byte[count];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
