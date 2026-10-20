package com.platform.modules.custom.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dao.CustomDesignSnapshotDao;
import com.platform.modules.custom.dao.CustomOrderCustomizationDao;
import com.platform.modules.custom.dao.ProductionWorkOrderDao;
import com.platform.modules.custom.dto.CustomQuoteRequest;
import com.platform.modules.custom.entity.CustomDesignSnapshotEntity;
import com.platform.modules.custom.entity.CustomOrderCustomizationEntity;
import com.platform.modules.custom.entity.ProductionWorkOrderEntity;
import com.platform.modules.custom.service.CustomDesignService;
import com.platform.modules.custom.service.CustomQuoteService;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 定制设计服务实现 / Custom design service implementation. */
@Service
public class CustomDesignServiceImpl implements CustomDesignService {
  private final CustomDesignSnapshotDao snapshotDao;
  private final CustomOrderCustomizationDao orderCustomizationDao;
  private final ProductionWorkOrderDao workOrderDao;
  private final CustomQuoteService quoteService;
  private final ComponentStockDao componentStockDao;

  @org.springframework.beans.factory.annotation.Autowired
  private com.platform.modules.custom.dao.WorkOrderLogDao workOrderLogDao;

  public CustomDesignServiceImpl(
      CustomDesignSnapshotDao snapshotDao,
      CustomOrderCustomizationDao orderCustomizationDao,
      ProductionWorkOrderDao workOrderDao,
      CustomQuoteService quoteService,
      ComponentStockDao componentStockDao) {
    this.snapshotDao = snapshotDao;
    this.orderCustomizationDao = orderCustomizationDao;
    this.workOrderDao = workOrderDao;
    this.quoteService = quoteService;
    this.componentStockDao = componentStockDao;
  }

  /** 保存设计草稿，并固化当时的服务端报价 / Saves a draft and its server quote. */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> saveDraft(Long userId, CustomQuoteRequest request) {
    if (userId == null) {
      throw new IllegalArgumentException("请先登录");
    }
    Map<String, Object> quote = quoteService.quote(request);
    CustomDesignSnapshotEntity snapshot =
        request.getDraftId() == null
            ? new CustomDesignSnapshotEntity()
            : getOwned(request.getDraftId(), userId);
    boolean creating = snapshot.getId() == null;
    if (!creating
        && (!"DRAFT".equals(snapshot.getStatus())
            || request.getVersion() == null
            || !request.getVersion().equals(snapshot.getVersion())
            || !request.getGoodsId().equals(snapshot.getGoodsId()))) {
      throw new IllegalArgumentException("草稿已被修改或锁定, 请刷新后再编辑");
    }
    snapshot.setUserId(userId);
    snapshot.setGoodsId(request.getGoodsId());
    snapshot.setConfigurationJson(JSON.toJSONString(request));
    snapshot.setPriceBreakdownJson(JSON.toJSONString(quote));
    snapshot.setPreviewUrl(request.getPreviewUrl());
    snapshot.setStatus("DRAFT");
    snapshot.setPreviewUrl(null);
    snapshot.setUpdateTime(new Date());
    if (creating) {
      snapshot.setVersion(0L);
      snapshot.setCreateTime(new Date());
      snapshotDao.insert(snapshot);
    } else {
      if (snapshotDao.update(
              null,
              new UpdateWrapper<CustomDesignSnapshotEntity>()
                  .eq("id", snapshot.getId())
                  .eq("user_id", userId)
                  .eq("status", "DRAFT")
                  .eq("version", request.getVersion())
                  .set("configuration_json", snapshot.getConfigurationJson())
                  .set("price_breakdown_json", snapshot.getPriceBreakdownJson())
                  .set("preview_url", null)
                  .set("update_time", new Date())
                  .setSql("version=version+1"))
          != 1) {
        throw new IllegalStateException("草稿发生并发编辑, 请刷新重试");
      }
      snapshot.setVersion(snapshot.getVersion() + 1);
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("snapshotId", snapshot.getId());
    result.put("version", snapshot.getVersion());
    result.put("status", snapshot.getStatus());
    result.put("quote", quote);
    return result;
  }

  /** 仅归档本人可编辑草稿, 不物理删除订单历史. */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void archive(Long id, Long userId, Long version) {
    if (version == null
        || snapshotDao.update(
                null,
                new UpdateWrapper<CustomDesignSnapshotEntity>()
                    .eq("id", id)
                    .eq("user_id", userId)
                    .eq("status", "DRAFT")
                    .eq("version", version)
                    .set("status", "ARCHIVED")
                    .set("update_time", new Date())
                    .setSql("version=version+1"))
            != 1) {
      throw new IllegalArgumentException("草稿已变化、下单或不属于当前用户");
    }
  }

  /** 获取当前用户拥有的设计 / Gets a design owned by the current user. */
  @Override
  public CustomDesignSnapshotEntity getOwned(Long snapshotId, Long userId) {
    CustomDesignSnapshotEntity snapshot = snapshotDao.selectById(snapshotId);
    if (snapshot == null || !userId.equals(snapshot.getUserId())) {
      throw new IllegalArgumentException("设计不存在或无权访问");
    }
    return snapshot;
  }

  /** 按创建时间倒序查询用户设计 / Lists user designs by creation time. */
  @Override
  public List<CustomDesignSnapshotEntity> listOwned(Long userId) {
    return snapshotDao.selectList(
        new LambdaQueryWrapper<CustomDesignSnapshotEntity>()
            .eq(CustomDesignSnapshotEntity::getUserId, userId)
            .ne(CustomDesignSnapshotEntity::getStatus, "ARCHIVED")
            .orderByDesc(CustomDesignSnapshotEntity::getCreateTime)
            .last("LIMIT 100"));
  }

  /** 批量装配设计与订单关联, 避免列表逐条查询 / Batch assembles design-order relations. */
  @Override
  public Map<String, Object> listOwnedWithOrders(Long userId, int page, int limit) {
    if (page < 1 || limit < 1 || limit > 100) throw new IllegalArgumentException("分页参数不正确");
    var selectedPage =
        snapshotDao.selectPage(
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<
                CustomDesignSnapshotEntity>(page, limit),
            new LambdaQueryWrapper<CustomDesignSnapshotEntity>()
                .eq(CustomDesignSnapshotEntity::getUserId, userId)
                .ne(CustomDesignSnapshotEntity::getStatus, "ARCHIVED")
                .orderByDesc(CustomDesignSnapshotEntity::getId));
    List<CustomDesignSnapshotEntity> snapshots = selectedPage.getRecords();
    Map<Long, Integer> orderIdsBySnapshot = new HashMap<>();
    if (!snapshots.isEmpty()) {
      List<Long> snapshotIds = snapshots.stream().map(CustomDesignSnapshotEntity::getId).toList();
      List<CustomOrderCustomizationEntity> bindings =
          orderCustomizationDao.selectList(
              new LambdaQueryWrapper<CustomOrderCustomizationEntity>()
                  .in(CustomOrderCustomizationEntity::getSnapshotId, snapshotIds));
      for (CustomOrderCustomizationEntity binding : bindings) {
        orderIdsBySnapshot.put(binding.getSnapshotId(), binding.getOrderId());
      }
    }
    List<Map<String, Object>> records =
        snapshots.stream()
            .map(
                snapshot -> {
                  Map<String, Object> item = new LinkedHashMap<>();
                  item.put("id", snapshot.getId());
                  item.put("goodsId", snapshot.getGoodsId());
                  item.put("version", snapshot.getVersion());
                  item.put("configurationJson", snapshot.getConfigurationJson());
                  item.put("priceBreakdownJson", snapshot.getPriceBreakdownJson());
                  item.put("previewUrl", snapshot.getPreviewUrl());
                  item.put("status", snapshot.getStatus());
                  item.put("createTime", snapshot.getCreateTime());
                  item.put("updateTime", snapshot.getUpdateTime());
                  item.put("orderId", orderIdsBySnapshot.get(snapshot.getId()));
                  return item;
                })
            .toList();
    return Map.of("records", records, "total", selectedPage.getTotal());
  }

  /** 解析快照并返回最新报价 / Parses a snapshot and returns its latest quote. */
  @Override
  public Map<String, Object> quoteOwned(Long snapshotId, Long userId) {
    CustomDesignSnapshotEntity snapshot = getOwned(snapshotId, userId);
    CustomQuoteRequest request =
        JSON.parseObject(snapshot.getConfigurationJson(), CustomQuoteRequest.class);
    Map<String, Object> quote = quoteService.quote(request);
    requireFinishedLength(quote);
    return quote;
  }

  /** 按最新商品和选项价格重新报价并锁定 / Requotes with current prices and locks the design. */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public BigDecimal lockAndGetPrice(Long snapshotId, Long userId) {
    CustomDesignSnapshotEntity snapshot = getOwned(snapshotId, userId);
    if (!"DRAFT".equals(snapshot.getStatus())) {
      throw new IllegalArgumentException("该设计正在结算或已提交订单");
    }
    CustomQuoteRequest request =
        JSON.parseObject(snapshot.getConfigurationJson(), CustomQuoteRequest.class);
    Map<String, Object> quote = quoteService.quote(request);
    BigDecimal totalPrice = new BigDecimal(String.valueOf(quote.get("totalPrice")));
    requireFinishedLength(quote);
    int updated =
        snapshotDao.update(
            null,
            new UpdateWrapper<CustomDesignSnapshotEntity>()
                .eq("id", snapshotId)
                .eq("user_id", userId)
                .eq("status", "DRAFT")
                .eq("version", snapshot.getVersion())
                .set("price_breakdown_json", JSON.toJSONString(quote))
                .setSql("version=version+1")
                .set("status", "LOCKED")
                .set("update_time", new Date()));
    if (updated != 1) {
      throw new IllegalArgumentException("设计已被其他结算请求占用");
    }
    return totalPrice;
  }

  /** 将已锁定设计绑定到订单商品 / Binds a locked design to an order item. */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void bindOrder(Long snapshotId, Long userId, Integer orderId, Integer orderGoodsId) {
    CustomDesignSnapshotEntity snapshot = getOwned(snapshotId, userId);
    if (!"LOCKED".equals(snapshot.getStatus())) {
      throw new IllegalArgumentException("设计尚未锁定");
    }
    // 扣减组件库存 / Deduct component stock.
    deductComponentStock(snapshot);
    CustomOrderCustomizationEntity binding = new CustomOrderCustomizationEntity();
    binding.setOrderId(orderId);
    binding.setOrderGoodsId(orderGoodsId);
    binding.setSnapshotId(snapshotId);
    binding.setStatus("LOCKED");
    binding.setCreateTime(new Date());
    binding.setUpdateTime(new Date());
    orderCustomizationDao.insert(binding);
    snapshot.setStatus("ORDERED");
    snapshot.setUpdateTime(new Date());
    snapshotDao.updateById(snapshot);
  }

  /** 未具备完整商家长度规则和实测输入时只允许保存草稿, 不进入新订单. */
  private static void requireFinishedLength(Map<String, Object> quote) {
    if (!Boolean.TRUE.equals(quote.get("checkoutReady"))) {
      throw new IllegalArgumentException("成品长度尚未校验通过, 请补齐实测颈围、目标长度及商家工艺规则");
    }
  }

  /** 按报价冻结的稳定代码和实际用量扣减, 不依赖可变的选项配置. */
  private void deductComponentStock(CustomDesignSnapshotEntity snapshot) {
    moveStock(snapshot, false);
  }

  /** 按下单时的用量恢复, 后续配置改动不能改变回补数量. */
  private void restoreComponentStock(CustomDesignSnapshotEntity snapshot) {
    moveStock(snapshot, true);
  }

  /** 新快照使用BOM, 老订单保留旧ID回退且恢复失败明确中止, 不猜测缺失材料. */
  private void moveStock(CustomDesignSnapshotEntity snapshot, boolean restore) {
    com.alibaba.fastjson.JSONObject quote = JSON.parseObject(snapshot.getPriceBreakdownJson());
    com.alibaba.fastjson.JSONArray components =
        quote == null ? null : quote.getJSONArray("components");
    if (components != null) {
      for (int i = 0; i < components.size(); i++) {
        com.alibaba.fastjson.JSONObject line = components.getJSONObject(i);
        int quantity = line.getIntValue("quantity");
        if (quantity == 0) continue;
        if (quantity < 0 || quantity > 10000) throw new IllegalStateException("快照用量无效");
        String type = line.getString("componentType");
        String code = line.getString("optionCode");
        int updated =
            restore
                ? componentStockDao.restoreStock(snapshot.getGoodsId(), type, code, quantity)
                : componentStockDao.deductStock(snapshot.getGoodsId(), type, code, quantity);
        if (updated != 1
            || componentStockDao.logMovement(
                    snapshot.getGoodsId(),
                    type,
                    code,
                    restore ? quantity : -quantity,
                    "订单库存服务",
                    "设计快照: " + snapshot.getId())
                != 1) {
          throw new IllegalStateException("材料库存或流水更新失败, 本次交易回滚");
        }
      }
      return;
    }
    CustomQuoteRequest request =
        JSON.parseObject(snapshot.getConfigurationJson(), CustomQuoteRequest.class);
    if (request == null || request.getOptionIds() == null) return;
    for (Long id : request.getOptionIds()) {
      int updated =
          restore
              ? componentStockDao.restoreByOptionId(snapshot.getGoodsId(), id, 1)
              : componentStockDao.deductByOptionId(snapshot.getGoodsId(), id, 1);
      if (updated != 1) throw new IllegalStateException("历史材料信息缺失, 需人工核对库存, 本次交易回滚");
    }
  }

  /** 幂等更新定制状态并创建待生产工单 / Idempotently marks paid and creates a work order. */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void markOrderPaid(Integer orderId) {
    CustomOrderCustomizationEntity binding =
        orderCustomizationDao.selectOne(
            new LambdaQueryWrapper<CustomOrderCustomizationEntity>()
                .eq(CustomOrderCustomizationEntity::getOrderId, orderId));
    if (binding == null) {
      return;
    }
    // 仅允许未支付绑定流转为已支付, 防止迟到回调复活已取消订单 /
    // Only LOCKED bindings may become PAID so late callbacks cannot revive cancelled orders.
    int updated =
        orderCustomizationDao.update(
            null,
            new UpdateWrapper<CustomOrderCustomizationEntity>()
                .eq("id", binding.getId())
                .eq("status", "LOCKED")
                .set("status", "PAID")
                .set("update_time", new Date()));
    if (updated != 1) {
      return;
    }
    Long count =
        workOrderDao.selectCount(
            new LambdaQueryWrapper<ProductionWorkOrderEntity>()
                .eq(ProductionWorkOrderEntity::getOrderId, orderId));
    if (count > 0) {
      return;
    }
    CustomDesignSnapshotEntity snapshot = snapshotDao.selectById(binding.getSnapshotId());
    if (snapshot == null) {
      throw new IllegalStateException("定制设计快照不存在");
    }
    ProductionWorkOrderEntity workOrder = new ProductionWorkOrderEntity();
    workOrder.setWorkOrderNo("CC-" + orderId);
    workOrder.setOrderId(orderId);
    workOrder.setOrderGoodsId(binding.getOrderGoodsId());
    workOrder.setSnapshotId(binding.getSnapshotId());
    workOrder.setStatus("PENDING_REVIEW");
    workOrder.setCraftJson(snapshot.getPriceBreakdownJson());
    workOrder.setCreateTime(new Date());
    workOrder.setUpdateTime(new Date());
    workOrderDao.insert(workOrder);
  }

  /**
   * 取消尚未完成的定制生产流程, 释放设计并恢复组件库存 / Cancels the workflow, releases the design and restores component
   * stock.
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void cancelOrder(Integer orderId) {
    CustomOrderCustomizationEntity binding =
        orderCustomizationDao.selectOne(
            new LambdaQueryWrapper<CustomOrderCustomizationEntity>()
                .eq(CustomOrderCustomizationEntity::getOrderId, orderId));
    if (binding == null) {
      return;
    }
    if ("CANCELLED".equals(binding.getStatus())) {
      return;
    }
    if (!Set.of("LOCKED", "PAID").contains(binding.getStatus())) {
      throw new IllegalStateException("定制绑定状态不允许取消");
    }
    assertOrderCancellable(orderId);
    boolean wasUnpaid = "LOCKED".equals(binding.getStatus());
    // 条件更新保证取消只执行一次, 防止与支付回调并发或重复调用导致重复恢复库存 /
    // Conditional update ensures cancellation runs once, avoiding double stock restoration.
    int updated =
        orderCustomizationDao.update(
            null,
            new UpdateWrapper<CustomOrderCustomizationEntity>()
                .eq("id", binding.getId())
                .in("status", "LOCKED", "PAID")
                .set("status", "CANCELLED")
                .set("update_time", new Date()));
    if (updated != 1) {
      return;
    }
    if (binding.getSnapshotId() != null) {
      CustomDesignSnapshotEntity snapshot = snapshotDao.selectById(binding.getSnapshotId());
      if (snapshot != null) {
        // 恢复组件库存 / Restore component stock.
        restoreComponentStock(snapshot);
        // 保留已取消订单的原快照, 复制一个新草稿用于再次购买.
        if (wasUnpaid && "ORDERED".equals(snapshot.getStatus())) {
          CustomDesignSnapshotEntity draft = new CustomDesignSnapshotEntity();
          org.springframework.beans.BeanUtils.copyProperties(snapshot, draft);
          draft.setId(null);
          draft.setStatus("DRAFT");
          draft.setCreateTime(new Date());
          draft.setUpdateTime(new Date());
          if (snapshotDao.insert(draft) != 1) {
            throw new IllegalStateException("取消后的设计草稿复制失败");
          }
          snapshot.setStatus("CANCELLED");
          snapshot.setUpdateTime(new Date());
          snapshotDao.updateById(snapshot);
        }
      }
    }
    ProductionWorkOrderEntity workOrder =
        workOrderDao.selectOne(
            new LambdaQueryWrapper<ProductionWorkOrderEntity>()
                .eq(ProductionWorkOrderEntity::getOrderId, orderId));
    if (workOrder != null
        && !"COMPLETED".equals(workOrder.getStatus())
        && !"CANCELLED".equals(workOrder.getStatus())) {
      String previousStatus = workOrder.getStatus();
      if (workOrderDao.update(
              null,
              new UpdateWrapper<ProductionWorkOrderEntity>()
                  .eq("id", workOrder.getId())
                  .in("status", "PENDING_REVIEW", "REVIEWED")
                  .set("status", "CANCELLED")
                  .set("update_time", new Date()))
          != 1) {
        throw new IllegalStateException("工单已进入生产或发生并发变更, 取消回滚");
      }
      com.platform.modules.custom.entity.WorkOrderLogEntity log =
          new com.platform.modules.custom.entity.WorkOrderLogEntity();
      log.setWorkOrderId(workOrder.getId());
      log.setOperatorName("订单取消服务");
      log.setFromStatus(previousStatus);
      log.setToStatus("CANCELLED");
      log.setRemark("订单取消或退款完成触发, 关联订单: " + orderId);
      log.setCreateTime(new Date());
      if (workOrderLogDao.insert(log) != 1) {
        throw new IllegalStateException("工单取消日志保存失败, 本次取消回滚");
      }
    }
  }

  /** 校验已开工的定制订单不可直接退款 / Prevents direct refunds after production starts. */
  @Override
  public void assertOrderCancellable(Integer orderId) {
    ProductionWorkOrderEntity workOrder =
        workOrderDao.selectOne(
            new LambdaQueryWrapper<ProductionWorkOrderEntity>()
                .eq(ProductionWorkOrderEntity::getOrderId, orderId));
    if (workOrder == null) {
      return;
    }
    Set<String> nonCancellable = Set.of("PRODUCING", "QUALITY_CHECK", "COMPLETED");
    if (nonCancellable.contains(workOrder.getStatus())) {
      throw new IllegalStateException("定制商品已进入生产，请联系客服处理退款");
    }
  }
}
