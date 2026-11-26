package com.platform.modules.mall.service;

import com.alibaba.fastjson.JSON;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Map;

/** 公开客服配置采用独立字段白名单, 不向小程序返回系统存储凭证或支付配置. */
@Service
public class MerchantContactService {
    private final JdbcTemplate jdbc;

    /** 注入客服配置及修改记录的持久化接口. */
    public MerchantContactService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 版本随编辑提交, 防止两个管理员同时保存时覆盖他人的变更. */
    public record Contact(
            String merchantName,
            String phone,
            String wechatId,
            String qrUrl,
            int version) {}

    /** 仅返回对消费者公开的联系资料, 初始未配置时保留明确的空状态. */
    public Contact current() {
        Map<String, Object> row = jdbc.queryForMap("SELECT * FROM CC_MERCHANT_CONTACT WHERE id=1");
        return new Contact(
                String.valueOf(row.get("merchant_name")),
                String.valueOf(row.get("phone")),
                String.valueOf(row.get("wechat_id")),
                String.valueOf(row.get("qr_url")),
                ((Number) row.get("version")).intValue());
    }

    /** 资料与版本记录一并保存, 新二维码须本人上传, 已发布二维码可由其他授权管理员保留. */
    @Transactional(rollbackFor = Exception.class)
    public Contact save(Contact request, String operator) {
        if (request == null || request.version() < 0) {
            throw new IllegalArgumentException("客服配置或版本无效");
        }
        String name = clean(request.merchantName(), 120);
        String phone = clean(request.phone(), 30);
        String wechat = clean(request.wechatId(), 64);
        String qr = clean(request.qrUrl(), 1000);
        if (!phone.isEmpty()
                && (!phone.matches("\\+?[0-9(][0-9() -]{3,28}[0-9)]")
                        || phone.replaceAll("[^0-9]", "").length() < 5)) {
            throw new IllegalArgumentException("客服号码格式无效");
        }
        if (!wechat.isEmpty() && !wechat.matches("[a-zA-Z0-9_-]{1,64}")) {
            throw new IllegalArgumentException("客服微信号只能包含字母、数字、下划线和连字符");
        }
        Contact existing = current();
        if (existing.version() != request.version()) {
            throw new IllegalArgumentException("客服资料已被其他管理员修改, 请刷新后重新编辑");
        }
        if (!qr.isEmpty() && !qr.equals(existing.qrUrl())) {
            validateQr(qr, operator);
        }
        int changed =
                jdbc.update(
                        "UPDATE CC_MERCHANT_CONTACT SET"
                            + " merchant_name=?,phone=?,wechat_id=?,qr_url=?,version=version+1,update_time=NOW()"
                            + " WHERE id=1 AND version=?",
                        name,
                        phone,
                        wechat,
                        qr,
                        request.version());
        if (changed != 1) {
            throw new IllegalArgumentException("客服资料已被其他管理员修改, 请刷新后重新编辑");
        }
        Contact saved =
                new Contact(name, phone, wechat, qr, request.version() + 1);
        jdbc.update(
                "INSERT INTO CC_MERCHANT_CONTACT_LOG(version,operator_id,snapshot_json)"
                        + " VALUES(?,?,?)",
                saved.version(),
                operator,
                JSON.toJSONString(
                        Map.of(
                                "merchantName",
                                name,
                                "phone",
                                phone,
                                "wechatId",
                                wechat,
                                "qrUrl",
                                qr,
                                "version",
                                saved.version())));
        return saved;
    }

    /** 历史记录只允许管理端读取, 返回原始公开资料快照及修改人和时间. */
    public List<Map<String, Object>> history() {
        return jdbc.queryForList(
                "SELECT version,operator_id,CAST(snapshot_json AS CHAR CHARACTER SET utf8mb4) AS"
                        + " snapshot_json,create_time FROM CC_MERCHANT_CONTACT_LOG ORDER BY version"
                        + " DESC LIMIT 50");
    }

    /** 拒绝活动协议和未经后台登记的地址, 不把参考图或外部二维码自动设成客服. */
    private void validateQr(String value, String operator) {
        URI uri = URI.create(value);
        boolean local =
                uri.getScheme() == null
                        && uri.getHost() == null
                        && uri.normalize().getPath().startsWith("/upload/");
        boolean remote = "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null;
        if ((!local && !remote)
                || uri.getPath() == null
                || !uri.getPath().endsWith(".png")
                || uri.getFragment() != null
                || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("请通过后台安全图片入口上传客服二维码");
        }
        Integer count =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM SYS_OSS WHERE BINARY URL=BINARY ? AND"
                                + " CREATE_USER_ID=?",
                        Integer.class,
                        value,
                        operator);
        if (count == null || count == 0) {
            throw new IllegalArgumentException("请由当前管理员在后台上传自己的客服二维码");
        }
    }

    /** 去除首尾空白并限制文本长度, 不替经营者填造品牌或联系资料. */
    private static String clean(String value, int maximum) {
        String text = value == null ? "" : value.trim();
        if (text.length() > maximum) {
            throw new IllegalArgumentException("客服资料超过长度限制");
        }
        return text;
    }
}
