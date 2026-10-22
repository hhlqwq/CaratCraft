package com.platform.modules.mall.service;

import com.github.binarywang.wxpay.bean.request.BaseWxPayRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundQueryRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.mall.entity.MallOrderEntity;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** 持久化退款申请与对账, 外部受理不等于资金退款完成. */
@Service
public class OrderRefundService {
    private final MallOrderService orders;
    private final CustomDesignService designs;
    private final JdbcTemplate jdbc;
    private final ObjectProvider<WxPayService> gateway;
    private final TransactionTemplate transactions;

    /** 注入事务边界与既有支付客户端, 不在网络调用期间持有订单行锁. */
    public OrderRefundService(
            MallOrderService orders,
            CustomDesignService designs,
            JdbcTemplate jdbc,
            ObjectProvider<WxPayService> gateway,
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
                    int fen = BaseWxPayRequest.yuanToFen(order.getActualPrice().toPlainString());
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
                                            + " order_id=? AND status='PENDING_REVIEW'",
                                    approved ? "APPROVED" : "REJECTED",
                                    operator,
                                    reason,
                                    orderId);
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

    /** 分页展示审核及对账结果, 不扫描全部订单. */
    public Map<String, Object> page(int page, int limit) {
        if (page < 1 || limit < 1 || limit > 100) {
            throw new IllegalArgumentException("分页参数不正确");
        }
        return Map.of(
                "records",
                jdbc.queryForList(
                        "SELECT r.*,o.order_sn FROM CC_REFUND r JOIN mall_order o ON"
                                + " o.id=r.order_id ORDER BY r.update_time DESC LIMIT ? OFFSET ?",
                        limit,
                        (long) (page - 1) * limit),
                "total",
                jdbc.queryForObject("SELECT COUNT(*) FROM CC_REFUND", Long.class));
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

    /** 对有到期租约的退款每批最多处理50项, 异常保留重试信息供人工对账. */
    public int reconcile() {
        if (gateway.getIfAvailable() == null) {
            return 0;
        }
        List<Integer> ids =
                jdbc.queryForList(
                        "SELECT order_id FROM CC_REFUND WHERE status IN"
                                + " ('APPROVED','PROCESSING','RETRY','MANUAL_REVIEW') AND"
                                + " (next_check_time IS NULL OR next_check_time<=NOW()) ORDER BY"
                                + " update_time LIMIT 50",
                        Integer.class);
        for (Integer id : ids) {
            reconcileOne(id);
        }
        return ids.size();
    }

    /** 租约防止多实例重复并行, 同一退款号使网络超时重试仍保持幂等. */
    public void reconcileOne(Integer id) {
        WxPayService pay = gateway.getIfAvailable();
        if (pay == null) {
            throw new IllegalStateException("支付服务尚未配置");
        }
        if (jdbc.update(
                        "UPDATE CC_REFUND SET next_check_time=DATE_ADD(NOW(),INTERVAL 2"
                                + " MINUTE),attempts=attempts+1 WHERE order_id=? AND status IN"
                                + " ('APPROVED','PROCESSING','RETRY','MANUAL_REVIEW') AND"
                                + " (next_check_time IS NULL OR next_check_time<=NOW())",
                        id)
                != 1) {
            return;
        }
        Map<String, Object> row = jdbc.queryForMap("SELECT * FROM CC_REFUND WHERE order_id=?", id);
        String refundNo = row.get("refund_no").toString();
        try {
            WxPayRefundQueryRequest query = new WxPayRefundQueryRequest();
            query.setOutRefundNo(refundNo);
            com.github.binarywang.wxpay.bean.result.WxPayRefundQueryResult result;
            try {
                result = pay.refundQuery(query);
            } catch (WxPayException missing) {
                if (!"REFUNDNOTEXIST".equals(missing.getErrCode())) {
                    throw missing;
                }
                if ("MANUAL_REVIEW".equals(row.get("status"))) {
                    // 人工处理中的申请只查单, 不再次提交退款请求.
                    return;
                }
                MallOrderEntity order = orders.getById(id);
                WxPayRefundRequest request = new WxPayRefundRequest();
                request.setOutTradeNo(order.getOrderSn());
                request.setOutRefundNo(refundNo);
                request.setTotalFee(((Number) row.get("amount_fen")).intValue());
                request.setRefundFee(request.getTotalFee());
                request.setRefundDesc("商家审核通过的退款申请");
                var accepted = pay.refund(request);
                if (!"SUCCESS".equals(accepted.getResultCode())) {
                    throw new IllegalStateException("退款未受理");
                }
                jdbc.update(
                        "UPDATE CC_REFUND SET"
                            + " status='PROCESSING',provider_status='ACCEPTED',update_time=NOW()"
                            + " WHERE order_id=?",
                        id);
                return;
            }
            if (!"SUCCESS".equals(result.getResultCode()) || result.getRefundRecords() == null) {
                throw new IllegalStateException("退款查单未返回有效记录");
            }
            var record =
                    result.getRefundRecords().stream()
                            .filter(value -> refundNo.equals(value.getOutRefundNo()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("退款号不匹配"));
            if (!row.get("amount_fen").toString().equals(String.valueOf(record.getRefundFee()))) {
                throw new IllegalStateException("退款金额不匹配");
            }
            String state = record.getRefundStatus();
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
            if ("MANUAL_REVIEW".equals(row.get("status"))) {
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
