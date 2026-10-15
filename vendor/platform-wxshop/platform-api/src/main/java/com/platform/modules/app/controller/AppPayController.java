package com.platform.modules.app.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.request.BaseWxPayRequest;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.common.utils.IpWhitelistUtil;
import com.platform.common.utils.RestResponse;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.mall.entity.MallOrderEntity;
import com.platform.modules.mall.entity.MallOrderGoodsEntity;
import com.platform.modules.mall.service.MallOrderGoodsService;
import com.platform.modules.mall.service.MallOrderService;
import com.platform.modules.wx.entity.WxUserEntity;
import com.platform.modules.mall.service.OrderPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者: @author Harmon <br>
 * 时间: 2017-08-11 08:32<br>
 *
 * @gitee https://gitee.com/hehailong/platform
 * 描述: AppController <br>
 */
@Tag(name = "商户支付-AppPayController")
@RestController
@RequestMapping("/app/pay")
@Slf4j
public class AppPayController extends AppBaseController {
    @Autowired
    private MallOrderService orderService;
    @Autowired
    private MallOrderGoodsService orderGoodsService;
    @Autowired(required = false)
    private WxPayService weixinPayService;
    @Autowired
    private OrderPaymentService orderPaymentService;
    @Autowired
    private com.platform.modules.mall.service.OrderPrepaymentService prepaymentService;
    @Autowired
    private CustomDesignService customDesignService;
    @Value("${wx.pay.baseNotifyUrl}")
    private String baseNotifyUrl;

    /** 是否启用支付回调 IP 白名单 / Whether the notify IP whitelist is enabled. */
    @Value("${wx.pay.notifyIpWhitelistEnabled:true}")
    private boolean notifyIpWhitelistEnabled;

    /** 支付回调 IP 白名单, 支持精确 IP 与 CIDR / Notify IP whitelist, exact ip or cidr. */
    @Value("${wx.pay.notifyIpWhitelist:}")
    private String notifyIpWhitelist;

    /**
     * 获取支付的请求参数
     */
    @Operation(summary = "获取支付的请求参数")
    @PostMapping("prepay")
    public RestResponse<Object> payPrepay(@LoginUser WxUserEntity loginUser, Integer orderId) {
        try {
            return RestResponse.ok(prepaymentService.prepare(orderId, loginUser, getClientIp()));
        } catch (IllegalArgumentException | IllegalStateException error) {
            return RestResponse.fail(error.getMessage());
        }
    }

    /**
     * 微信查询订单状态
     */
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
            WxPayOrderQueryResult result = weixinPayService.queryOrder("", orderDetail.getOrderSn());

            String tradeState = result.getTradeState();
            if ("SUCCESS".equals(tradeState)) {
                orderPaymentService.markPaid(orderId, result.getTotalFee());
                return RestResponse.ok("支付成功");
            } else if ("USERPAYING".equals(tradeState)) {
                return RestResponse.fail("订单支付中，请稍后重试");
            } else {
                // 失败
                return RestResponse.fail("查询失败,error=" + tradeState);
            }
        } catch (WxPayException e) {
            e.printStackTrace();
            return RestResponse.fail("下单失败,error=" + e.getMessage());
        }
    }

    /**
     * 微信订单回调接口
     *
     * @return
     */
    @Operation(hidden = true)
    @IgnoreAuth
    @RequestMapping("/notify")
    public void notify(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*");
            // 回调来源 IP 白名单校验, 拦截伪造请求(签名校验之外的纵深防御) /
            // Whitelist the notify source IP as defense in depth on top of signature checks.
            String clientIp = resolveNotifyIp();
            if (weixinPayService == null) {
                log.warn("支付服务未配置, 忽略支付回调, ip={}", clientIp);
                response.getWriter().write(setXml("FAIL", "PAY_NOT_CONFIGURED"));
                return;
            }
            if (!isTrustedNotifyIp(clientIp)) {
                log.warn("拒绝来自非白名单 IP 的支付回调, ip={}", clientIp);
                response.getWriter().write(setXml("FAIL", "FORBIDDEN"));
                return;
            }
            InputStream in = request.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.close();
            in.close();
            //xml数据
            String reponseXml = new String(out.toByteArray(), "utf-8");
            WxPayOrderNotifyResult result = weixinPayService.parseOrderNotifyResult(reponseXml);
            String resultCode = result.getResultCode();
            String outTradeNo = result.getOutTradeNo();
            if ("FAIL".equalsIgnoreCase(resultCode)) {
                log.error("订单{}支付失败", outTradeNo);
                response.getWriter().write(setXml("FAIL", "PAY_FAILED"));
            } else if ("SUCCESS".equalsIgnoreCase(resultCode)) {
                log.info("订单{}支付成功", outTradeNo);
                // 业务处理
                MallOrderEntity orderInfo = orderService.getOne(new QueryWrapper<MallOrderEntity>().eq("order_sn", outTradeNo));
                if (orderInfo == null) {
                    response.getWriter().write(setXml("FAIL", "ORDER_NOT_FOUND"));
                    return;
                }
                orderPaymentService.markPaid(orderInfo.getId(), result.getTotalFee());
                response.getWriter().write(setXml("SUCCESS", "OK"));
            }
        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            try {
                response.getWriter().write(setXml("FAIL", "PROCESS_ERROR"));
            } catch (Exception writeException) {
                log.error("写入微信支付回调响应失败", writeException);
            }
        }
    }

    /**
     * 订单退款请求
     */
    @Operation(summary = "订单退款请求")
    @PostMapping("refund")
    public RestResponse<String> refund(@LoginUser WxUserEntity loginUser ,Integer orderId) {
        //
        MallOrderEntity orderInfo = orderService.getById(orderId);
        if (null == orderInfo) {
            return RestResponse.fail("订单已取消");
        }
        Long userId = loginUser.getId();
        if (userId == null || !userId.equals(orderInfo.getUserId())) {
            return RestResponse.fail("越权操作！");
        }

        if (orderInfo.getOrderStatus() == 401 || orderInfo.getOrderStatus() == 402) {
            return RestResponse.fail("订单已退款");
        }
        if (weixinPayService == null) {
            return RestResponse.fail("支付服务未配置，请联系管理员填写微信支付参数");
        }
        try {
            customDesignService.assertOrderCancellable(orderId);
        } catch (IllegalStateException e) {
            return RestResponse.fail(e.getMessage());
        }
        int refundedStatus;
        if (orderInfo.getOrderStatus() == 201) {
            refundedStatus = 401;
        } else if (orderInfo.getOrderStatus() == 300 || orderInfo.getOrderStatus() == 301) {
            refundedStatus = 402;
        } else {
            return RestResponse.fail("当前订单状态不支持退款");
        }
        WxPayRefundRequest request = new WxPayRefundRequest();
        request.setOutTradeNo(orderInfo.getOrderSn());
        request.setOutRefundNo(orderInfo.getOrderSn());
        request.setTotalFee(BaseWxPayRequest.yuanToFen(orderInfo.getActualPrice().toString()));
        request.setRefundFee(BaseWxPayRequest.yuanToFen(orderInfo.getActualPrice().toString()));
        request.setRefundDesc("用户取消订单");
        WxPayRefundResult result;
        try {
            result = weixinPayService.refund(request);
        } catch (WxPayException e) {
            return RestResponse.fail("退款失败：" + e.getErrCode());
        }

        if ("SUCCESS".equals(result.getResultCode())) {
            orderService.completeRefund(orderId, refundedStatus);
            return RestResponse.ok("成功退款");
        } else {
            return RestResponse.fail("退款失败");
        }
    }


    /**
     * 解析回调请求的真实来源 IP / Resolves the trusted source IP of the notify request.
     * <p>
     * 优先 X-Real-IP(Nginx 从 TCP 连接写入, 不可伪造), 其次 X-Forwarded-For 首段, 最后 remoteAddr。
     * 不直接信任完整 XFF 头, 防止客户端伪造白名单 IP 绕过校验。
     * Prefers X-Real-IP (written by nginx from the TCP peer, cannot be spoofed), then the
     * first entry of X-Forwarded-For, then the remote address.
     * </p>
     */
    private String resolveNotifyIp() {
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
     * <p>
     * 白名单未启用或未配置时不拦截, 依赖 XML 签名校验兜底 /
     * Skips the check when disabled or unconfigured, signature verification still applies.
     * </p>
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

    //返回微信服务
    public static String setXml(String returnCode, String returnMsg) {
        return "<xml><return_code><![CDATA[" + returnCode + "]]></return_code><return_msg><![CDATA[" + returnMsg + "]]></return_msg></xml>";
    }

    //模拟微信回调接口
    public static String callbakcXml(String orderNum) {
        return "<xml><appid><![CDATA[wx2421b1c4370ec43b]]></appid><attach><![CDATA[支付测试]]></attach><bank_type><![CDATA[CFT]]></bank_type><fee_type><![CDATA[CNY]]></fee_type> <is_subscribe><![CDATA[Y]]></is_subscribe><mch_id><![CDATA[10000100]]></mch_id><nonce_str><![CDATA[5d2b6c2a8db53831f7eda20af46e531c]]></nonce_str><openid><![CDATA[oUpF8uMEb4qRXf22hE3X68TekukE]]></openid> <out_trade_no><![CDATA[" + orderNum + "]]></out_trade_no>  <result_code><![CDATA[SUCCESS]]></result_code> <return_code><![CDATA[SUCCESS]]></return_code><sign><![CDATA[B552ED6B279343CB493C5DD0D78AB241]]></sign><sub_mch_id><![CDATA[10000100]]></sub_mch_id> <time_end><![CDATA[20140903131540]]></time_end><total_fee>1</total_fee><trade_type><![CDATA[JSAPI]]></trade_type><transaction_id><![CDATA[1004400740201409030005092168]]></transaction_id></xml>";
    }
}
