package com.platform.modules.sys.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.datascope.StaffAuthority;
import com.platform.modules.sys.entity.AccountTier;
import com.platform.modules.sys.entity.SysUserEntity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/** 负责唯一超管对商家管理员的开户及凭据生命周期. */
@Service
@RequiredArgsConstructor
public class MerchantAccountService {
    private final SysUserService users;
    private final SysUserTokenService tokens;
    private final StaffAuthority authority;
    private final BusinessOrganization businessOrganization;
    private final PasswordPolicy passwordPolicy;

    /** 分页读取商家管理员基本资料. */
    public Page<SysUserEntity> list(
            int page, int size, String account, String name, Integer status) {
        requirePlatform();
        return users.page(
                new Page<>(Math.max(page, 1), Math.max(1, Math.min(100, size))),
                new QueryWrapper<SysUserEntity>()
                        .eq("ACCOUNT_TYPE", AccountTier.MERCHANT_ADMIN)
                        .like(account != null && !account.isBlank(), "USER_NAME", account)
                        .like(name != null && !name.isBlank(), "REAL_NAME", name)
                        .eq(status != null, "STATUS", status)
                        .orderByDesc("CREATE_TIME"));
    }

    /** 在默认工作室创建商家管理员, 仅通过本次响应交付随机临时凭据. */
    @Transactional(rollbackFor = Exception.class)
    public Issued create(Create request) {
        requirePlatform();
        String org = businessOrganization.defaultOrganization();
        if (org == null) {
            throw new IllegalArgumentException("请先配置已启用的默认工作室");
        }
        SysUserEntity user = new SysUserEntity();
        user.setUserName(request.userName().strip());
        user.setRealName(request.realName().strip());
        user.setSex(0);
        user.setMobile(request.mobile());
        user.setEmail(request.email());
        user.setOrgNo(org);
        user.setAccountType(AccountTier.MERCHANT_ADMIN);
        user.setCreateUserId("1");
        user.setStatus(1);
        String password = issue(user);
        if (!users.save(user)) {
            throw new IllegalStateException("商家管理员开户失败");
        }
        authority.audit("创建商家管理员", user.getUserId(), "工作室=" + org);
        return new Issued(user.getUserId(), password);
    }

    /** 停用或恢复商家管理员, 每次变化均撤销旧会话. */
    @Transactional(rollbackFor = Exception.class)
    public void status(String userId, boolean active) {
        SysUserEntity user = merchant(userId);
        if (!users.update(
                new UpdateWrapper<SysUserEntity>()
                        .eq("USER_ID", user.getUserId())
                        .set("STATUS", active ? 1 : 0))) {
            throw new IllegalStateException("商家管理员状态更新失败");
        }
        tokens.offlineBatch(new String[] {userId});
        authority.audit(active ? "恢复商家管理员" : "停用商家管理员", userId, "撤销旧会话");
    }

    /** 为商家管理员签发新的限时随机密码, 并撤销旧令牌. */
    @Transactional(rollbackFor = Exception.class)
    public Issued reset(String userId) {
        SysUserEntity current = merchant(userId);
        SysUserEntity credential = new SysUserEntity();
        credential.setUserId(current.getUserId());
        String password = issue(credential);
        if (!users.updateById(credential)) {
            throw new IllegalStateException("商家管理员密码更新失败");
        }
        tokens.offlineBatch(new String[] {userId});
        authority.audit("重置商家管理员密码", userId, "撤销旧会话");
        return new Issued(userId, password);
    }

    /** 对管理员目标加锁并检查层级, 禁止这个入口写入员工或超管. */
    private SysUserEntity merchant(String userId) {
        requirePlatform();
        SysUserEntity target =
                users.getOne(
                        new QueryWrapper<SysUserEntity>()
                                .eq("USER_ID", userId)
                                .eq("ACCOUNT_TYPE", AccountTier.MERCHANT_ADMIN)
                                .last("FOR UPDATE"));
        if (!AccountTier.valid(target)) {
            throw new UnauthorizedException("商家管理员不可访问");
        }
        return target;
    }

    /** 在服务层确保账号生命周期管理仅能由平台超管调用. */
    private void requirePlatform() {
        if (!authority.emergency()) {
            throw new UnauthorizedException("仅平台超级管理员可以管理商家管理员");
        }
    }

    /** 为管理员签发有效期 24 小时的独立凭据. */
    private String issue(SysUserEntity account) {
        String password = passwordPolicy.temporary();
        AdminCredentials.replace(
                account, password, true, Date.from(Instant.now().plus(24, ChronoUnit.HOURS)));
        return password;
    }

    /** 编辑基本账号资料, 不接收层级、角色、机构或密码参数. */
    @Transactional(rollbackFor = Exception.class)
    public void update(Edit request) {
        SysUserEntity user = merchant(request.userId());
        if (!users.update(
                new UpdateWrapper<SysUserEntity>()
                        .eq("USER_ID", user.getUserId())
                        .set("REAL_NAME", request.realName().strip())
                        .set("MOBILE", request.mobile())
                        .set("EMAIL", request.email()))) {
            throw new IllegalStateException("商家管理员资料保存失败");
        }
        authority.audit("编辑商家管理员", user.getUserId(), "已更新基本联系资料");
    }

    /** 仅接受可编辑的基本资料. */
    public record Edit(
            @NotBlank String userId,
            @NotBlank @Size(max = 64) String realName,
            @Pattern(regexp = "^$|1[3-9][0-9]{9}") String mobile,
            @Email @Size(max = 100) String email) {}

    /** 接收最少的开户资料, 角色和机构由服务端决定. */
    public record Create(
            @NotBlank @Size(max = 50) String userName,
            @NotBlank @Size(max = 64) String realName,
            @Pattern(regexp = "^$|1[3-9][0-9]{9}") String mobile,
            @Email @Size(max = 100) String email) {}

    /** 临时密码只在创建或重置响应中返回一次. */
    public record Issued(String userId, String temporaryPassword) {}
}
