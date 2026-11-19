package com.platform.modules.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.common.utils.IpWhitelistUtil;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.mall.service.MallOrderService;
import com.platform.modules.mall.service.OrderPaymentService;
import com.platform.modules.mall.service.WechatPaymentGateway;
import com.platform.modules.mall.service.WechatPaymentGateway.ChannelFailure;
import com.platform.modules.mall.service.WechatPaymentGateway.PaymentNotice;
import com.platform.modules.wx.entity.WxUserEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 作者: @author hehailong <br>
 * 时间: 2017-08-11 08:32<br>
 *
 * @gitee https://gitee.com/hehailong/platform 描述: AppController <br>
 */
@Tag(name = "商户支付-AppPayController")
@RestController
@RequestMapping("/app/pay")
@Slf4j
public class AppPayController extends AppBaseController {
    @Autowired private com.platform.modules.mall.service.OrderRefundService refundService;
    @Autowired private MallOrderService orderService;

    @Autowired(required = false)
    private WechatPaymentGateway weixinPayService;

    @Autowired private OrderPaymentService orderPaymentService;
    @Autowired private com.platform.modules.mall.service.OrderPrepaymentService prepaymentService;

    /** 是否启用支付回调 IP 白名单 / Whether the notify IP whitelist is enabled. */
    @Value("${wx.pay.notifyIpWhitelistEnabled:true}")
    private boolean notifyIpWhitelistEnabled;

    /** 支付回调 IP 白名单, 支持精确 IP 与 CIDR / Notify IP whitelist, exact ip or cidr. */
    @Value("${wx.pay.notifyIpWhitelist:}")
    private String notifyIpWhitelist;

    /** 获取支付的请求参数 */
    @Operation(summary = "获取支付的请求参数")
    @PostMapping("prepay")
    public RestResponse<Object> payPrepay(@LoginUser WxUserEntity loginUser, Integer orderId) {
        try {
            return RestResponse.ok(prepaymentService.prepare(orderId, loginUser));
        } catch (IllegalArgumentException | IllegalStateException error) {
            return RestResponse.fail(error.getMessage());
        }
    }

    /** 微信查询订单状态 */
    @Operation(summary = "查询订单状态")
    @PostMapping("query")
    public RestResponse<String> orderQuery(@LoginUser WxUserEntity loginUser, Integer orderId) {
        if (orderId == null) {
            return RestResponse.fail("订单不存在");
        }

        MallOrderEntity orderDetail = orderService.getById(orderId);
        if (orderDetail == null || !loginUser.getId().equals(orderDetail.getUserId())) {
            return RestResponse.fail("订单不存在或无权访问");
        }
        if (weixinPayService == null) {
            return RestResponse.fail("支付服务未配置，请联系管理员填写微信支付参数");
        }
        try {
            var result = weixinPayService.findOrder(orderDetail.getOrderSn()).orElse(null);
            if (result != null && "SUCCESS".equals(result.state())) {
                orderPaymentService.markPaid(orderId, result.totalCents());
                return RestResponse.ok("支付成功");
            }
            if (result != null && "USERPAYING".equals(result.state())) {
                return RestResponse.fail("订单支付中，请稍后重试");
            }
            return RestResponse.fail("当前订单未支付成功");
        } catch (ChannelFailure error) {
            log.warn("查询订单支付状态失败, orderId={}", orderId);
            return RestResponse.fail("支付结果待确认, 请稍后重试");
        }
    }

    /** 验证 APIv3 原始通知签名, 只有交易已写入本地才返回 HTTP 204. */
    @Operation(hidden = true)
    @IgnoreAuth
    @PostMapping(value = "/notify", consumes = "application/json")
    public ResponseEntity<Map<String, String>> notify(HttpServletRequest request) {
        if (weixinPayService == null) {
            return notifyFailure(503, "支付服务未配置");
        }
        if (!isTrustedNotifyIp(resolveNotifyIp(request))) {
            return notifyFailure(403, "回调来源未获授权");
        }
        try {
            byte[] body = request.getInputStream().readNBytes(65537);
            if (body.length > 65536) {
                return notifyFailure(413, "通知正文过长");
            }
            var notice =
                    new PaymentNotice(
                            request.getHeader("Wechatpay-Serial"),
                            request.getHeader("Wechatpay-Nonce"),
                            request.getHeader("Wechatpay-Signature"),
                            request.getHeader("Wechatpay-Timestamp"),
                            new String(body, StandardCharsets.UTF_8));
            if (notice.serial() == null
                    || notice.nonce() == null
                    || notice.signature() == null
                    || notice.timestamp() == null) {
                return notifyFailure(400, "缺少通知签名");
            }
            var result = weixinPayService.parsePaymentNotice(notice);
            if (!"SUCCESS".equals(result.state())) {
                return notifyFailure(400, "通知交易状态不正确");
            }
            var order =
                    orderService.getOne(
                            new QueryWrapper<MallOrderEntity>().eq("order_sn", result.orderNo()));
            if (order == null) {
                return notifyFailure(404, "订单不存在");
            }
            orderPaymentService.markPaid(order.getId(), result.totalCents());
            return ResponseEntity.noContent().build();
        } catch (ChannelFailure error) {
            return notifyFailure(401, "通知验签失败");
        } catch (IOException | RuntimeException error) {
            log.warn("微信支付通知未能完成本地处理");
            return notifyFailure(500, "支付结果待写入");
        }
    }

    /** 按微信支付通知协议返回失败, 由渠道保留重试机会. */
    private ResponseEntity<Map<String, String>> notifyFailure(int status, String message) {
        return ResponseEntity.status(status).body(Map.of("code", "FAIL", "message", message));
    }

    /** 订单退款请求 */
    @Operation(summary = "订单退款请求")
    @PostMapping("refund")
    public RestResponse<String> refund(@LoginUser WxUserEntity loginUser, Integer orderId) {
        refundService.request(orderId, loginUser.getId(), "用户申请售后退款");
        return RestResponse.ok("退款申请已提交, 等待审核和支付侧确认");
    }

    /**
     * 解析回调请求的真实来源 IP / Resolves the trusted source IP of the notify request.
     *
     * <p>优先 X-Real-IP(Nginx 从 TCP 连接写入, 不可伪造), 其次 X-Forwarded-For 首段, 最后 remoteAddr。 不直接信任完整 XFF 头,
     * 防止客户端伪造白名单 IP 绕过校验。 Prefers X-Real-IP (written by nginx from the TCP peer, cannot be
     * spoofed), then the first entry of X-Forwarded-For, then the remote address.
     */
    private String resolveNotifyIp(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return firstIp(realIp);
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return firstIp(forwarded);
        }
        return request.getRemoteAddr();
    }

    /** 取链式 IP 头的第一段 / Returns the first entry of a chained IP header. */
    private String firstIp(String header) {
        int idx = header.indexOf(',');
        return idx > 0 ? header.substring(0, idx).trim() : header.trim();
    }

    /**
     * 校验回调来源 IP 是否在白名单内 / Checks whether the notify source IP is whitelisted.
     *
     * <p>白名单未启用或未配置时不拦截, 仍要求 APIv3 签名校验 / Skips the check when disabled or unconfigured, signature
     * verification still applies.
     *
     * @param clientIp 回调来源 IP / notify source ip
     * @return 是否放行 / true when trusted
     */
    private boolean isTrustedNotifyIp(String clientIp) {
        if (!notifyIpWhitelistEnabled) {
            return true;
        }
        if (notifyIpWhitelist == null || notifyIpWhitelist.isBlank()) {
            return true;
        }
        return IpWhitelistUtil.matchesAny(clientIp, notifyIpWhitelist);
    }
}
