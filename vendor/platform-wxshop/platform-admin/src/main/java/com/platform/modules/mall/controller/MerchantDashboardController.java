package com.platform.modules.mall.controller;

import com.platform.common.utils.RestResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** 商家工作台只显示有权查看的真实业务统计, 与服务器监控分离. */
@RestController
@RequestMapping("/mall/dashboard")
public class MerchantDashboardController {
    private final JdbcTemplate jdbc;

    /** 注入只读统计所需连接. */
    public MerchantDashboardController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 权限逐项检查, 不向只有库存权限的用户泄露销售金额. */
    @GetMapping("summary")
    @RequiresPermissions(
            value = {"mall:order:list", "custom:work-order:list", "custom:stock:list"},
            logical = Logical.OR)
    public RestResponse<Object> summary() {
        Map<String, Object> data = new LinkedHashMap<>();
        var subject = SecurityUtils.getSubject();
        if (subject.isPermitted("mall:order:list")) {
            data.put(
                    "unpaid",
                    count(
                            "SELECT COUNT(*) FROM mall_order WHERE order_status=0 AND pay_status IN"
                                    + " (0,1)"));
            data.put(
                    "toShip",
                    count(
                            "SELECT COUNT(*) FROM mall_order WHERE order_status=201 AND"
                                    + " pay_status=2"));
            data.put(
                    "refundReview",
                    count("SELECT COUNT(*) FROM CC_REFUND WHERE status='PENDING_REVIEW'"));
            data.put(
                    "refundAttention",
                    count(
                            "SELECT COUNT(*) FROM CC_REFUND WHERE status IN"
                                    + " ('RETRY','MANUAL_REVIEW')"));
        }
        if (subject.isPermitted("custom:work-order:list")) {
            data.put(
                    "workReview",
                    count(
                            "SELECT COUNT(*) FROM CC_PRODUCTION_WORK_ORDER WHERE"
                                    + " status='PENDING_REVIEW'"));
            data.put(
                    "workMaking",
                    count(
                            "SELECT COUNT(*) FROM CC_PRODUCTION_WORK_ORDER WHERE status IN"
                                    + " ('REVIEWED','PRODUCING','QUALITY_CHECK')"));
        }
        if (subject.isPermitted("custom:stock:list")) {
            data.put(
                    "stockOut",
                    count(
                            "SELECT COUNT(*) FROM CC_COMPONENT_OPTION o LEFT JOIN"
                                + " CC_COMPONENT_STOCK s ON o.goods_id=s.goods_id AND"
                                + " o.component_type=s.component_type AND"
                                + " o.option_code=s.option_code WHERE o.enabled=1 AND"
                                + " COALESCE(s.stock,0) < CASE WHEN JSON_VALID(o.material_params)"
                                + " THEN"
                                + " COALESCE(CAST(JSON_UNQUOTE(JSON_EXTRACT(o.material_params,'$.stockQuantity'))"
                                + " AS SIGNED),1) ELSE 1 END"));
        }
        return RestResponse.ok(data);
    }

    /** 统一统计返回类型, 不拼接客户端输入. */
    private long count(String sql) {
        Long count = jdbc.queryForObject(sql, Long.class);
        return count == null ? 0 : count;
    }
}
