package com.platform.modules.sys.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.platform.common.utils.Constant;
import com.platform.modules.sys.entity.AccountTier;
import com.platform.modules.sys.entity.SysUserEntity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/** 仅从运维私有挂载为封存的唯一超管预置一次性凭据. */
@Component
public class PlatformAccountBootstrap implements ApplicationRunner {
    private final SysUserService users;
    private final SysUserTokenService tokens;
    private final JdbcTemplate jdbc;
    private final String secretFile;

    /** 配置私有凭据文件路径, 文件内容不进入应用配置或日志. */
    public PlatformAccountBootstrap(
            SysUserService users,
            SysUserTokenService tokens,
            JdbcTemplate jdbc,
            @Value("${caratcraft.platform-bootstrap-file:}") String secretFile) {
        this.users = users;
        this.tokens = tokens;
        this.jdbc = jdbc;
        this.secretFile = secretFile;
    }

    /** 仅可用一个有效的临时密码启用当前封存账号, 并限制首登时效. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments arguments) throws IOException {
        SysUserEntity platform = users.getById(Constant.SUPER_ADMIN);
        if (!AccountTier.valid(platform)
                || Boolean.TRUE.equals(
                        jdbc.queryForObject(
                                "SELECT EXISTS(SELECT 1 FROM SYS_USER WHERE ACCOUNT_TYPE IS NULL)",
                                Boolean.class))) {
            throw new IllegalStateException("管理端账号层级尚未完成初始化或受控升级");
        }
        // 确认角色表也已经完成同批结构升级, 旧结构及时失败.
        jdbc.queryForObject("SELECT COUNT(DIRECT_USER_ID) FROM SYS_ROLE", Long.class);
        if (secretFile.isBlank()
                || platform.getPassword() != null
                || !Integer.valueOf(0).equals(platform.getStatus())) {
            return;
        }
        Path file = Path.of(secretFile);
        if (!Files.isRegularFile(file) || Files.size(file) > 64) {
            throw new IllegalStateException("超管引导凭据文件无效");
        }
        String password = Files.readString(file).strip();
        if (!password.matches("[A-Za-z0-9_-]{24,32}")) {
            throw new IllegalStateException("超管引导需要24到32位随机凭据");
        }
        AdminCredentials.replace(
                platform, password, true, Date.from(Instant.now().plus(30, ChronoUnit.MINUTES)));
        boolean changed =
                users.update(
                        new UpdateWrapper<SysUserEntity>()
                                .eq("USER_ID", Constant.SUPER_ADMIN)
                                .eq("ACCOUNT_TYPE", AccountTier.PLATFORM_ADMIN)
                                .eq("STATUS", 0)
                                .isNull("PASSWORD")
                                .set("PASSWORD", platform.getPassword())
                                .set("SALT", platform.getSalt())
                                .set("STATUS", 1)
                                .set("PASSWORD_CHANGE_REQUIRED", true)
                                .set(
                                        "TEMPORARY_PASSWORD_EXPIRES_AT",
                                        platform.getTemporaryPasswordExpiresAt()));
        if (changed) {
            tokens.offlineBatch(new String[] {Constant.SUPER_ADMIN});
        }
    }
}
