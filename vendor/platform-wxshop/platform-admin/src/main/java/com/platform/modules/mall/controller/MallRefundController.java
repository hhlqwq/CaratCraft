package com.platform.modules.mall.controller;

import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.service.OrderRefundService;
import com.platform.modules.sys.controller.AbstractController;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 商家审核与退款对账入口, 复用订单管理权限. */
@RestController
@RequestMapping("/mall/refund")
public class MallRefundController extends AbstractController {
  private final OrderRefundService refunds;

  /** 注入统一退款服务. */
  public MallRefundController(OrderRefundService refunds) {
    this.refunds = refunds;
  }

  /** 分页读取审核、处理中和需人工干预的申请. */
  @GetMapping("list")
  @RequiresPermissions("mall:order:list")
  public RestResponse<Object> list(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int limit) {
    return RestResponse.ok(refunds.page(page, limit));
  }

  /** 审核通过不宣称退款成功, 拒绝时解除冻结. */
  @PostMapping("decide")
  @RequiresPermissions("mall:order:update")
  public RestResponse<Object> decide(@RequestBody Decision decision) {
    if (decision.approved() == null) throw new IllegalArgumentException("请选择审核结果");
    refunds.decide(decision.orderId(), decision.approved(), "商家:" + getUserId(), decision.note());
    return RestResponse.ok("审核结果已保存, 通过的申请由后台对账处理");
  }

  /** 手动触发有租约且限量的同号查单补偿. */
  @PostMapping("reconcile")
  @RequiresPermissions("mall:order:update")
  public RestResponse<Object> reconcile() {
    return RestResponse.ok(Map.of("processed", refunds.reconcile()));
  }

  /** 审核参数固定, 不允许客户端指定金额或退款号. */
  public record Decision(Integer orderId, Boolean approved, String note) {}
}
