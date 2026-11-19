package com.platform.modules.mall.controller;

import com.platform.common.annotation.SysLog;
import com.platform.common.utils.RestResponse;
import com.platform.datascope.StaffAuthority;
import com.platform.datascope.StaffAuthority.Resource;
import com.platform.modules.mall.service.OrderRefundService;
import com.platform.modules.sys.controller.AbstractController;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 商家退款查询、审核和人工补偿入口. */
@RestController
@RequestMapping("/mall/refund")
public class MallRefundController extends AbstractController {
    private final OrderRefundService refunds;
    private final StaffAuthority authority;

    /** 注入统一退款服务. */
    public MallRefundController(OrderRefundService refunds, StaffAuthority authority) {
        this.refunds = refunds;
        this.authority = authority;
    }

    /** 分页读取审核、处理中和需人工干预的申请. */
    @GetMapping("list")
    @RequiresPermissions("mall:refund:list")
    public RestResponse<Object> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String todo) {
        var listScope = authority.scope("mall:refund:list");
        String action =
                switch (todo == null ? "" : todo) {
                    case "refundReview" -> "mall:refund:review";
                    case "refundExecute" -> "mall:refund:execute";
                    case "refundRetry" -> "mall:refund:retry";
                    case "refundAttention" -> "mall:refund:reconcile";
                    default -> "";
                };
        var results =
                refunds.page(
                        page,
                        limit,
                        todo,
                        action.isEmpty()
                                ? listScope
                                : listScope.intersect(authority.scope(action)));
        var review = authority.scope("mall:refund:review");
        var execute = authority.scope("mall:refund:execute");
        var retry = authority.scope("mall:refund:retry");
        for (var row : results.records()) {
            String organization = (String) row.remove("scope_org_no");
            row.put("can_review", review.permits(organization));
            row.put("can_execute", execute.permits(organization));
            row.put("can_retry", retry.permits(organization));
        }
        return RestResponse.ok(results);
    }

    /** 非资金审核独立保存建议, 审核本身不交给自动退款任务. */
    @SysLog("退款审核")
    @PostMapping("review")
    @RequiresPermissions("mall:refund:review")
    public RestResponse<String> review(@RequestBody Decision decision) {
        if (decision.approved() == null) {
            throw new IllegalArgumentException("请选择审核结果");
        }
        authority.require("mall:refund:review", Resource.ORDER, decision.orderId());
        refunds.review(decision.orderId(), decision.approved(), getUserId(), decision.note());
        return RestResponse.ok();
    }

    /** 审核通过不宣称退款成功, 拒绝时解除冻结. */
    @SysLog("执行退款审核结果")
    @PostMapping("decide")
    @RequiresPermissions("mall:refund:execute")
    public RestResponse<Object> decide(@RequestBody Decision decision) {
        if (decision.approved() == null) {
            throw new IllegalArgumentException("请选择审核结果");
        }
        authority.require("mall:refund:execute", Resource.ORDER, decision.orderId());
        refunds.decide(
                decision.orderId(), decision.approved(), "商家:" + getUserId(), decision.note());
        return RestResponse.ok("审核结果已保存, 通过的申请由后台对账处理");
    }

    /** 仅对指定待重试申请按同一退款号处理, 已有渠道记录时按查单结果写回. */
    @SysLog("重试退款")
    @PostMapping("retry")
    @RequiresPermissions("mall:refund:retry")
    public RestResponse<String> retry(@RequestParam Integer orderId) {
        authority.require("mall:refund:retry", Resource.ORDER, orderId);
        refunds.retry(orderId);
        return RestResponse.ok("已处理本次退款重试, 请核对最新状态");
    }

    /** 手动触发有租约且限量的同号查单与状态写回. */
    @SysLog("退款对账写回")
    @PostMapping("reconcile")
    @RequiresPermissions("mall:refund:reconcile")
    public RestResponse<Object> reconcile() {
        return RestResponse.ok(
                Map.of(
                        "processed",
                        refunds.reconcileWriteback(authority.scope("mall:refund:reconcile"))));
    }

    /** 审核参数固定, 不允许客户端指定金额或退款号. */
    public record Decision(Integer orderId, Boolean approved, String note) {}
}
