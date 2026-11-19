package com.platform.modules.mall.service;

import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.sys.service.OrgActionScope;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 持久化退款申请与对账, 外部受理不等于资金退款完成. */
@Service
public class OrderRefundService {
    private enum Reconciliation {
        AUTOMATIC("'APPROVED','PROCESSING','RETRY','MANUAL_REVIEW'", true),
        WRITEBACK("'PROCESSING','RETRY','MANUAL_REVIEW'", false),
        RETRY("'RETRY'", true);

        private final String statuses;
        private final boolean canSubmit;

        /** 声明本入口的固定状态集合及渠道提交能力, 不接受客户端指定SQL. */
        Reconciliation(String statuses, boolean canSubmit) {
            this.statuses = statuses;
            this.canSubmit = canSubmit;
        }
    }

    private final MallOrderService orders;
    private final CustomDesignService designs;
    private final JdbcTemplate jdbc;
    private final ObjectProvider<WechatPaymentGateway> gateway;
    private final TransactionTemplate transactions;

    /** 注入事务边界与既有支付客户端, 不在网络调用期间持有订单行锁. */
    public OrderRefundService(
            MallOrderService orders,
            CustomDesignService designs,
            JdbcTemplate jdbc,
            ObjectProvider<WechatPaymentGateway> gateway,
            PlatformTransactionManager manager) {
        this.orders = orders;
        this.designs = designs;
        this.jdbc = jdbc;
        this.gateway = gateway;
        transactions = new TransactionTemplate(manager);
    }

    /** 本人申请仅进入审核, 发货后退款不自动恢复库存. */
    public Map<String, Object> request(Integer orderId, Long userId, String reason) {
        String note = requireNote(reason);
        return transactions.execute(
                status -> {
                    MallOrderEntity order = requireOrder(orderId);
                    if (!order.getUserId().equals(userId)) {
                        throw new IllegalArgumentException("订单不存在或无权访问");
                    }
                    List<Map<String, Object>> existing =
                            jdbc.queryForList("SELECT * FROM CC_REFUND WHERE order_id=?", orderId);
                    if (!existing.isEmpty()) {
                        return publicStatus(existing.get(0));
                    }
                    if (!Integer.valueOf(2).equals(order.getPayStatus())
                            || !Set.of(201, 300, 301).contains(order.getOrderStatus())) {
                        throw new IllegalArgumentException("当前订单状态不允许申请退款");
                    }
                    if (Integer.valueOf(201).equals(order.getOrderStatus())) {
                        designs.assertOrderCancellable(orderId);
                    }
                    int fen = order.getActualPrice().movePointRight(2).intValueExact();
                    jdbc.update(
                            "INSERT INTO"
                                + " CC_REFUND(order_id,refund_no,status,original_status,amount_fen,reason)"
                                + " VALUES(?,?,'PENDING_REVIEW',?,?,?)",
                            orderId,
                            "CCRF-" + order.getOrderSn(),
                            order.getOrderStatus(),
                            fen,
                            note);
                    if (jdbc.update(
                                    "UPDATE mall_order SET pay_status=3 WHERE id=? AND"
                                            + " pay_status=2",
                                    orderId)
                            != 1) {
                        throw new IllegalStateException("订单退款冻结失败, 本次申请回滚");
                    }
                    return Map.of("status", "PENDING_REVIEW", "message", "退款申请已提交, 等待商家审核");
                });
    }

    /** 商家确认退货或无需退货后审核, 决策和订单冻结状态一并提交. */
    public void decide(Integer orderId, boolean approved, String operator, String note) {
        String reason = requireNote(note);
        transactions.executeWithoutResult(
                status -> {
                    requireOrder(orderId);
                    int changed =
                            jdbc.update(
                                    "UPDATE CC_REFUND SET status=?,operator_name=?,decision_note=?,"
                                            + " update_time=NOW(),next_check_time=NOW() WHERE"
                                            + " order_id=? AND status='PENDING_REVIEW' AND"
                                            + " review_result=?",
                                    approved ? "APPROVED" : "REJECTED",
                                    operator,
                                    reason,
                                    orderId,
                                    approved ? "APPROVE" : "REJECT");
                    if (changed != 1) {
                        throw new IllegalArgumentException("退款申请状态已变化, 请刷新");
                    }
                    if (!approved) {
                        jdbc.update(
                                "UPDATE mall_order SET pay_status=2 WHERE id=? AND pay_status=3",
                                orderId);
                    }
                });
    }

    /** 分页展示负责人所需字段, 不返回支付渠道标识、租约或客户联系方式. */
    public RefundPage page(int page, int limit, String todo, OrgActionScope scope) {
        if (page < 1 || page > 100000 || limit < 1 || limit > 100) {
            throw new IllegalArgumentException("分页参数不正确");
        }
        List<Object> parameters = new ArrayList<>();
        String filter =
                " FROM CC_REFUND r JOIN mall_order o ON o.id=r.order_id WHERE 1=1"
                        + scope.andWhere("o.scope_org_no", parameters);
        filter +=
                switch (todo == null ? "" : todo) {
                    case "refundReview" ->
                            " AND r.status='PENDING_REVIEW' AND r.review_result IS NULL";
                    case "refundExecute" ->
                            " AND r.status='PENDING_REVIEW' AND r.review_result IS NOT NULL";
                    case "refundRetry" ->
                            " AND r.status='RETRY' AND (r.next_check_time IS NULL OR"
                                    + " r.next_check_time<=NOW())";
                    case "refundAttention" ->
                            " AND r.status IN ('PROCESSING','RETRY','MANUAL_REVIEW') AND"
                                    + " (r.next_check_time IS NULL OR r.next_check_time<=NOW())";
                    default -> "";
                };
        Long total =
                jdbc.queryForObject("SELECT COUNT(*)" + filter, Long.class, parameters.toArray());
        parameters.add(limit);
        parameters.add((long) (page - 1) * limit);
        return new RefundPage(
                jdbc.queryForList(
                        "SELECT"
                            + " r.order_id,o.order_sn,o.scope_org_no,r.status,r.amount_fen,r.reason,r.decision_note,"
                            + "r.review_result,r.review_note,r.reviewed_at,r.update_time"
                                + filter
                                + " ORDER BY r.update_time DESC,r.order_id LIMIT ? OFFSET ?",
                        parameters.toArray()),
                total == null ? 0L : total);
    }

    /** 管理端分页结果, 返回前由调用方以动作标记替换内部归属字段. */
    public record RefundPage(List<Map<String, Object>> records, long total) {}

    /** 非资金审核仅保存建议, 审核不会把申请置为可出款状态. */
    @Transactional(rollbackFor = Exception.class)
    public void review(Integer orderId, boolean approve, String operator, String note) {
        requireOrder(orderId);
        if (jdbc.update(
                        "UPDATE CC_REFUND SET"
                            + " review_result=?,review_note=?,reviewed_by=?,reviewed_at=NOW(),update_time=NOW()"
                            + " WHERE order_id=? AND status='PENDING_REVIEW'",
                        approve ? "APPROVE" : "REJECT",
                        requireNote(note),
                        operator,
                        orderId)
                != 1) {
            throw new IllegalStateException("退款申请状态已变化");
        }
    }

    /** 按客服权限读取处理队列, 省略支付流水、退款金额及审核内部字段. */
    public Map<String, Object> supportPage(
            int page, int limit, OrgActionScope scope, boolean pending) {
        if (page < 1 || page > 100000 || limit < 1 || limit > 100) {
            throw new IllegalArgumentException("分页参数不正确");
        }
        List<Object> parameters = new ArrayList<>();
        String filter =
                " FROM CC_REFUND r JOIN mall_order o ON o.id=r.order_id WHERE 1=1"
                        + scope.andWhere("o.scope_org_no", parameters);
        if (pending) {
            filter += " AND r.support_handled_at IS NULL AND r.status='PENDING_REVIEW'";
        }
        Long total =
                jdbc.queryForObject("SELECT COUNT(*)" + filter, Long.class, parameters.toArray());
        parameters.add(limit);
        parameters.add((long) (page - 1) * limit);
        return Map.of(
                "records",
                jdbc.queryForList(
                        "SELECT r.order_id,o.order_sn,o.user_id AS"
                            + " customer_id,r.status,r.reason,r.support_note,r.support_handled_at,r.update_time,CASE"
                            + " WHEN CHAR_LENGTH(o.mobile)>=7 THEN RIGHT(o.mobile,4) END AS"
                            + " mobile_hint"
                                + filter
                                + " ORDER BY r.update_time DESC,r.order_id LIMIT ? OFFSET ?",
                        parameters.toArray()),
                "total",
                total == null ? 0L : total);
    }

    /** 客服只能写入处理记录, 退款状态以及渠道动作保持独立. */
    public void handleSupport(Integer orderId, String note) {
        if (jdbc.update(
                        "UPDATE CC_REFUND SET support_note=?,support_handled_at=NOW() WHERE"
                                + " order_id=?",
                        requireNote(note),
                        orderId)
                != 1) {
            throw new IllegalStateException("售后记录不存在");
        }
    }

    /** 本人查询退款详情, 不泄露商户支付凭证及他人订单. */
    public Map<String, Object> owned(Integer orderId, Long userId) {
        MallOrderEntity order = orders.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在或无权访问");
        }
        List<Map<String, Object>> rows =
                jdbc.queryForList("SELECT * FROM CC_REFUND WHERE order_id=?", orderId);
        return rows.isEmpty() ? Map.of("status", "NONE") : publicStatus(rows.get(0));
    }

    /** 用户中心分页只返回本人的售后公开字段, 不泄露支付侧标识、内部重试或租约. */
    @Transactional(readOnly = true)
    public Map<String, Object> mine(Long userId, int page, int limit) {
        if (userId == null || page < 1 || page > 100000 || limit < 1 || limit > 50) {
            throw new IllegalArgumentException("售后分页参数无效");
        }
        List<Map<String, Object>> records =
                jdbc.queryForList(
                        "SELECT r.order_id AS orderId,o.order_sn AS orderSn,r.status,r.amount_fen"
                            + " AS amountFen,r.reason,r.create_time AS createTime,r.update_time AS"
                            + " updateTime FROM CC_REFUND r JOIN mall_order o ON o.id=r.order_id"
                            + " WHERE o.user_id=? ORDER BY r.create_time DESC,r.order_id DESC LIMIT"
                            + " ? OFFSET ?",
                        userId,
                        limit,
                        (page - 1) * limit);
        Long total =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM CC_REFUND r JOIN mall_order o ON o.id=r.order_id"
                                + " WHERE o.user_id=?",
                        Long.class,
                        userId);
        return Map.of("records", records, "total", total == null ? 0L : total);
    }

    /** 系统任务处理已审核退款, 保留自动提交及补偿. */
    public int reconcile() {
        return reconcileBatch(Reconciliation.AUTOMATIC, new OrgActionScope(true, List.of()));
    }

    /** 人工批量查单并写回, 不领取尚待首次提交的申请, 也不发起退款. */
    public int reconcileWriteback(OrgActionScope scope) {
        return reconcileBatch(Reconciliation.WRITEBACK, scope);
    }

    /** 重试指定退款, 仅允许状态为RETRY且租约已到期的申请. */
    public void retry(Integer orderId) {
        WechatPaymentGateway pay = gateway.getIfAvailable();
        if (pay == null) {
            throw new IllegalStateException("支付服务尚未配置");
        }
        if (!reconcileOne(orderId, pay, Reconciliation.RETRY)) {
            throw new IllegalStateException("退款不在可重试状态或正在处理, 请刷新");
        }
    }

    /** 领取到期租约的退款每批最多处理50项, 返回实际领取数. */
    private int reconcileBatch(Reconciliation mode, OrgActionScope scope) {
        WechatPaymentGateway pay = gateway.getIfAvailable();
        if (pay == null) {
            return 0;
        }
        List<Object> parameters = new ArrayList<>();
        String organization = scope.andWhere("o.scope_org_no", parameters);
        List<Integer> ids =
                jdbc.queryForList(
                        "SELECT r.order_id FROM CC_REFUND r JOIN mall_order o ON o.id=r.order_id"
                                + " WHERE r.status IN ("
                                + mode.statuses
                                + ") AND (r.next_check_time IS NULL OR r.next_check_time<=NOW())"
                                + organization
                                + " ORDER BY r.update_time LIMIT 50",
                        Integer.class,
                        parameters.toArray());
        int processed = 0;
        for (Integer id : ids) {
            if (reconcileOne(id, pay, mode)) {
                processed++;
            }
        }
        return processed;
    }

    /** 租约防止多实例重复并行, 同一退款号使网络超时重试仍保持幂等. */
    private boolean reconcileOne(Integer id, WechatPaymentGateway pay, Reconciliation mode) {
        if (jdbc.update(
                        "UPDATE CC_REFUND SET next_check_time=DATE_ADD(NOW(),INTERVAL 2"
                                + " MINUTE),attempts=attempts+1 WHERE order_id=? AND status IN ("
                                + mode.statuses
                                + ") AND"
                                + " (next_check_time IS NULL OR next_check_time<=NOW())",
                        id)
                != 1) {
            return false;
        }
        Map<String, Object> row = jdbc.queryForMap("SELECT * FROM CC_REFUND WHERE order_id=?", id);
        String refundNo = row.get("refund_no").toString();
        try {
            var record = pay.findRefund(refundNo).orElse(null);
            if (record == null && (!mode.canSubmit || "MANUAL_REVIEW".equals(row.get("status")))) {
                return true;
            }
            MallOrderEntity order = orders.getById(id);
            int amount = ((Number) row.get("amount_fen")).intValue();
            if (order == null || order.getOrderSn() == null) {
                throw new IllegalStateException("退款订单不存在");
            }
            if (record == null) {
                pay.submitRefund(refundNo, order.getOrderSn(), amount);
                jdbc.update(
                        "UPDATE CC_REFUND SET"
                            + " status='PROCESSING',provider_status='ACCEPTED',update_time=NOW()"
                            + " WHERE order_id=?",
                        id);
                return true;
            }
            if (!refundNo.equals(record.refundNo())
                    || !order.getOrderSn().equals(record.orderNo())
                    || amount != record.cents()) {
                throw new IllegalStateException("退款渠道订单、退款号或金额不匹配");
            }
            String state = record.state();
            if ("SUCCESS".equals(state)) {
                transactions.executeWithoutResult(
                        status -> {
                            orders.lockById(id);
                            orders.completeRefund(
                                    id,
                                    ((Number) row.get("original_status")).intValue() == 201
                                            ? 401
                                            : 402);
                            if (((Number) row.get("original_status")).intValue() != 201) {
                                jdbc.update(
                                        "UPDATE CC_ORDER_CUSTOMIZATION SET"
                                                + " status='REFUNDED',update_time=NOW() WHERE"
                                                + " order_id=?",
                                        id);
                            }
                            jdbc.update(
                                    "UPDATE CC_REFUND SET"
                                        + " status='SUCCEEDED',provider_status='SUCCESS',update_time=NOW()"
                                        + " WHERE order_id=?",
                                    id);
                        });
            } else {
                String local =
                        state != null
                                        && Set.of(
                                                        "REFUNDCLOSE",
                                                        "CHANGE",
                                                        "CLOSED",
                                                        "ABNORMAL",
                                                        "FAIL")
                                                .contains(state)
                                ? "MANUAL_REVIEW"
                                : "PROCESSING";
                jdbc.update(
                        "UPDATE CC_REFUND SET status=?,provider_status=?,update_time=NOW() WHERE"
                                + " order_id=?",
                        local,
                        state,
                        id);
            }
        } catch (Exception failure) {
            // 不记录支付响应正文或秘密, 租约到期后继续同号查单, 不提前恢复库存.
            if (!mode.canSubmit || "MANUAL_REVIEW".equals(row.get("status"))) {
                jdbc.update(
                        "UPDATE CC_REFUND SET"
                            + " provider_status='QUERY_OR_PROCESS_ERROR',update_time=NOW() WHERE"
                            + " order_id=?",
                        id);
            } else {
                jdbc.update(
                        "UPDATE CC_REFUND SET"
                            + " status='RETRY',provider_status='QUERY_OR_PROCESS_ERROR',update_time=NOW()"
                            + " WHERE order_id=?",
                        id);
            }
        }
        return true;
    }

    /** 订单锁用于退款、发货、生产及支付状态之间的互斥. */
    private MallOrderEntity requireOrder(Integer id) {
        MallOrderEntity order = orders.lockById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        return order;
    }

    /** 必填理由可追溯审核决定, 拒绝无限长度内容. */
    private String requireNote(String note) {
        if (note == null || note.isBlank() || note.length() > 200) {
            throw new IllegalArgumentException("请填写1到200字理由");
        }
        return note.trim();
    }

    /** 公开有限的退款状态, 不返回内部租约及商户查单信息. */
    private Map<String, Object> publicStatus(Map<String, Object> row) {
        return Map.of(
                "status",
                row.get("status"),
                "reason",
                row.get("reason"),
                "message",
                "退款是否完成以查单确认结果为准, 可联系商家查询审核进度");
    }
}
