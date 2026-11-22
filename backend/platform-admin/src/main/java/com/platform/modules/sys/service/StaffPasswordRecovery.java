package com.platform.modules.sys.service;

import com.platform.modules.sys.entity.SysUserEntity;
import com.platform.modules.wx.service.MobileVerificationSmsService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

/** 按员工档案发送和消费找回密码的验证码, 不接收任意收件地址. */
@Service
@RequiredArgsConstructor
public class StaffPasswordRecovery {
    private static final String PREFIX = "caratcraft:staff-recovery:";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DefaultRedisScript<Long> LIMIT =
            new DefaultRedisScript<>(
                    "local n=redis.call('INCR',KEYS[1]); if n==1 then"
                            + " redis.call('EXPIRE',KEYS[1],ARGV[1]) end; return n",
                    Long.class);
    private static final DefaultRedisScript<Long> STORE =
            new DefaultRedisScript<>(
                    "redis.call('SET',KEYS[1],ARGV[1],'EX',300); redis.call('DEL',KEYS[2]); return"
                            + " 1",
                    Long.class);
    private static final DefaultRedisScript<Long> CONSUME =
            new DefaultRedisScript<>(
                    "local v=redis.call('GET',KEYS[1]); if not v then return 0 end; local"
                            + " n=redis.call('INCR',KEYS[2]); if n==1 then"
                            + " redis.call('EXPIRE',KEYS[2],300) end; if v==ARGV[1] and n<=5 then"
                            + " redis.call('DEL',KEYS[1],KEYS[2]); return 1 end; if n>=5 then"
                            + " redis.call('DEL',KEYS[1],KEYS[2]) end; return 0",
                    Long.class);

    private final SysUserService users;
    private final StringRedisTemplate redis;
    private final MailService mail;
    private final MobileVerificationSmsService sms;

    @Value("${caratcraft.staff-recovery.email-enabled:false}")
    private boolean emailEnabled;

    @Value("${caratcraft.staff-recovery.sms-enabled:false}")
    private boolean smsEnabled;

    /** 限制单个来源、员工和联系方式的发送次数, 对不同账号返回同样的提示. */
    public boolean send(String userName, String channel, String source) {
        boolean byEmail = selectEmail(channel);
        if (!(byEmail ? emailEnabled : smsEnabled)) {
            return false;
        }
        if (!withinLimit("send:" + source, 40, 600)) {
            return true;
        }
        SysUserEntity employee = eligibleEmployee(userName);
        if (employee == null) {
            return true;
        }
        String contact = contact(employee, byEmail);
        if (contact == null
                || !Boolean.TRUE.equals(
                        redis.opsForValue()
                                .setIfAbsent(
                                        PREFIX + "recent:" + digest(employee.getUserId()),
                                        "1",
                                        Duration.ofMinutes(1)))
                || !withinLimit("employee:" + employee.getUserId(), 5, 3600)
                || !withinLimit("contact:" + contact, 5, 3600)) {
            return true;
        }
        String code = String.valueOf(100000 + RANDOM.nextInt(900000));
        if (deliver(byEmail, contact, code)) {
            redis.execute(STORE, challengeKeys(employee), proof(employee, byEmail, contact, code));
        }
        return true;
    }

    /** 消费与当前档案及密码版本绑定的验证码, 成功后交由员工服务更新凭据. */
    public boolean reset(
            String userName, String channel, String code, String password, String source) {
        boolean byEmail = selectEmail(channel);
        if (!(byEmail ? emailEnabled : smsEnabled)
                || code == null
                || !code.matches("[0-9]{6}")
                || !withinLimit("reset:" + source, 100, 600)) {
            return false;
        }
        users.validateNewPassword(password);
        SysUserEntity employee = eligibleEmployee(userName);
        if (employee == null) {
            return false;
        }
        String contact = contact(employee, byEmail);
        return contact != null
                && Long.valueOf(1)
                        .equals(
                                redis.execute(
                                        CONSUME,
                                        challengeKeys(employee),
                                        proof(employee, byEmail, contact, code)))
                && users.recoverPassword(employee, byEmail, contact, password);
    }

    /** 校验找回渠道, 仅接受手机号或邮箱. */
    private boolean selectEmail(String channel) {
        if ("email".equals(channel)) {
            return true;
        }
        if ("mobile".equals(channel)) {
            return false;
        }
        throw new IllegalArgumentException("请选择手机号或邮箱");
    }

    /** 查找允许自助找回的员工, 无法找回时以同样的方式反馈. */
    private SysUserEntity eligibleEmployee(String userName) {
        if (userName == null || userName.isBlank() || userName.length() > 50) {
            return null;
        }
        SysUserEntity employee = users.queryByUserName(userName.strip());
        return users.canRecoverPassword(employee) ? employee : null;
    }

    /** 只采用已登记的单个地址, 防止旧资料中的多收件人或非法字符用于发码. */
    private String contact(SysUserEntity employee, boolean byEmail) {
        String value = byEmail ? employee.getEmail() : employee.getMobile();
        String pattern = byEmail ? "[^@,;<>\\s]+@[^@,;<>\\s]+" : "1[3-9][0-9]{9}";
        return value != null && value.length() <= 254 && value.matches(pattern) ? value : null;
    }

    /** 通过已配置的邮件或短信投递验证码, 供应商异常按发送失败处理. */
    private boolean deliver(boolean byEmail, String contact, String code) {
        try {
            if (byEmail) {
                return mail.sendMail(
                        contact, "CaratCraft 员工密码找回", "验证码为 " + code + "，5 分钟内有效。请勿转告他人。");
            }
            var config = sms.configuration();
            return sms.configurationError(config) == null
                    && sms.send(config, contact, code) == null;
        } catch (RuntimeException error) {
            return false;
        }
    }

    /** 使用原子的固定时间窗口限制请求, 不在 Redis 键名中记录联系方式. */
    private boolean withinLimit(String scope, long limit, int seconds) {
        Long count =
                redis.execute(
                        LIMIT, List.of(PREFIX + "rate:" + digest(scope)), String.valueOf(seconds));
        return count != null && count <= limit;
    }

    /** 每个员工只保留一个找回验证码及其尝试次数. */
    private List<String> challengeKeys(SysUserEntity employee) {
        String key = PREFIX + "challenge:" + digest(employee.getUserId());
        return List.of(key, key + ":attempts");
    }

    /** 将验证码绑定于账号、渠道、联系方式和当前密码哈希. */
    private String proof(SysUserEntity employee, boolean byEmail, String contact, String code) {
        return digest(
                employee.getUserId()
                        + ":"
                        + byEmail
                        + ":"
                        + contact
                        + ":"
                        + employee.getPassword()
                        + ":"
                        + code);
    }

    /** 生成固定长度的摘要用于验证码和限流上下文. */
    private String digest(String value) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("无法初始化验证码摘要", error);
        }
    }
}
