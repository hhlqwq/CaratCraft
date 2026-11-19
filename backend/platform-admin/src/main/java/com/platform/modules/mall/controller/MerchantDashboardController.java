package com.platform.modules.mall.controller;

import com.platform.common.utils.RestResponse;
import com.platform.datascope.StaffAuthority;

import org.apache.shiro.SecurityUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** 商家工作台只显示有权查看的真实业务统计, 与服务器监控分离. */
@RestController
@RequestMapping("/mall/dashboard")
public class MerchantDashboardController {
    private final JdbcTemplate jdbc;
    private final StaffAuthority authority;

    /** 注入授权范围及真实统计所需连接. */
    public MerchantDashboardController(JdbcTemplate jdbc, StaffAuthority authority) {
        this.jdbc = jdbc;
        this.authority = authority;
    }

    /** 默认空工作台, 各卡片独立检查入口和动作权限再查询数量. */
    @GetMapping("summary")
    public RestResponse<Object> summary() {
        Map<String, Object> data = new LinkedHashMap<>();
        shortcut(data, "goods", "mall:goods:list", true);
        shortcut(data, "diy", "custom:config:list", true);
        shortcut(data, "employees", "sys:user:list", true);
        shortcut(data, "roles", "sys:role:list", true);
        shortcut(data, "permissions", "sys:menu:list", false);
        shortcut(data, "audit", "sys:log:list", true);
        count(
                data,
                "toShip",
                "mall:order:list",
                "mall:order:ship",
                "o.scope_org_no",
                "mall_order o WHERE o.order_status=201 AND o.pay_status=2 AND o.shipping_status=0");
        String work =
                "CC_PRODUCTION_WORK_ORDER w JOIN mall_order o ON w.order_id=o.id WHERE w.status=";
        count(
                data,
                "workReview",
                "custom:work-order:list",
                "custom:work-order:update",
                "o.scope_org_no",
                work + "'PENDING_REVIEW'");
        count(
                data,
                "workMaking",
                "custom:work-order:list",
                "custom:work-order:update",
                "o.scope_org_no",
                "CC_PRODUCTION_WORK_ORDER w JOIN mall_order o ON w.order_id=o.id"
                        + " WHERE w.status IN ('REVIEWED','PRODUCING')");
        count(
                data,
                "workQuality",
                "custom:work-order:list",
                "custom:work-order:update",
                "o.scope_org_no",
                work + "'QUALITY_CHECK'");
        String refunds = "CC_REFUND r JOIN mall_order o ON r.order_id=o.id";
        count(
                data,
                "supportPending",
                "mall:aftersale:list",
                "mall:aftersale:handle",
                "o.scope_org_no",
                refunds + " WHERE r.support_handled_at IS NULL AND r.status='PENDING_REVIEW'");
        count(
                data,
                "refundReview",
                "mall:refund:list",
                "mall:refund:review",
                "o.scope_org_no",
                refunds + " WHERE r.status='PENDING_REVIEW' AND r.review_result IS NULL");
        count(
                data,
                "refundExecute",
                "mall:refund:list",
                "mall:refund:execute",
                "o.scope_org_no",
                refunds + " WHERE r.status='PENDING_REVIEW' AND r.review_result IS NOT NULL");
        count(
                data,
                "refundRetry",
                "mall:refund:list",
                "mall:refund:retry",
                "o.scope_org_no",
                refunds
                        + " WHERE r.status='RETRY' AND (r.next_check_time IS NULL OR"
                        + " r.next_check_time<=NOW())");
        count(
                data,
                "refundAttention",
                "mall:refund:list",
                "mall:refund:reconcile",
                "o.scope_org_no",
                refunds
                        + " WHERE r.status IN ('PROCESSING','RETRY','MANUAL_REVIEW')"
                        + " AND (r.next_check_time IS NULL OR r.next_check_time<=NOW())");
        shortcut(data, "feedback", "mall:feedback:list", true);
        count(
                data,
                "feedbackPending",
                "mall:feedback:list",
                "mall:feedback:update",
                "f.scope_org_no",
                "mall_feedback f WHERE f.status=0");
        count(
                data,
                "communityReview",
                "community:post:list",
                "community:post:moderate",
                "p.scope_org_no",
                "CC_COMMUNITY_POST p WHERE p.status='PENDING'");
        count(
                data,
                "stockOut",
                "custom:stock:list",
                "custom:stock:update",
                "g.scope_org_no",
                "CC_COMPONENT_OPTION o JOIN mall_goods g ON o.goods_id=g.id LEFT JOIN"
                        + " CC_COMPONENT_STOCK s ON o.goods_id=s.goods_id AND"
                        + " o.component_type=s.component_type AND o.option_code=s.option_code WHERE"
                        + " g.is_delete=0 AND o.enabled=1 AND "
                        + com.platform.modules.custom.service.CustomStockService
                                .BELOW_REQUIRED_STOCK);
        return RestResponse.ok(data);
    }

    /** 只有可进入的页面才提供工作台快捷入口. */
    private void shortcut(
            Map<String, Object> result, String key, String permission, boolean scoped) {
        if (SecurityUtils.getSubject().isPermitted(permission)
                && (!scoped
                        || authority.emergency()
                        || !authority.scope(permission).organizations().isEmpty())) {
            result.put(key, true);
        }
    }

    /** 只统计同时可浏览及可办理的业务记录, 对未知范围不执行SQL. */
    private void count(
            Map<String, Object> result,
            String key,
            String permission,
            String action,
            String organization,
            String query) {
        var subject = SecurityUtils.getSubject();
        if (!subject.isPermittedAll(permission, action)) {
            return;
        }
        var scope = authority.scope(permission).intersect(authority.scope(action));
        if (!scope.emergency() && scope.organizations().isEmpty()) {
            return;
        }
        var arguments = new ArrayList<Object>();
        String filter = scope.andWhere(organization, arguments);
        Long count =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM " + query + filter, Long.class, arguments.toArray());
        result.put(key, count == null ? 0L : count);
    }
}
