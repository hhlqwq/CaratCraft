package com.platform.modules.custom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.dto.ComponentStockView;
import com.platform.modules.custom.entity.ComponentStockEntity;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.service.CustomStockService;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 组件库存维护服务实现 / Component stock administration service implementation. */
@Service
@RequiredArgsConstructor
public class CustomStockServiceImpl implements CustomStockService {
  private final CustomComponentOptionDao componentOptionDao;
  private final ComponentStockDao componentStockDao;

  /** 查询组件选项及库存, 以选项为主左联库存 / Lists options left-joined with stock rows. */
  @Override
  public com.baomidou.mybatisplus.extension.plugins.pagination.Page<ComponentStockView> listStock(
      Integer goodsId, int page, int limit) {
    if (page < 1 || limit < 1 || limit > 100) throw new IllegalArgumentException("分页参数不正确");
    var selectedPage =
        componentOptionDao.selectPage(
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<
                CustomComponentOptionEntity>(page, limit),
            new LambdaQueryWrapper<CustomComponentOptionEntity>()
                .eq(goodsId != null, CustomComponentOptionEntity::getGoodsId, goodsId)
                .orderByAsc(CustomComponentOptionEntity::getGoodsId)
                .orderByAsc(CustomComponentOptionEntity::getId));
    List<CustomComponentOptionEntity> options = selectedPage.getRecords();
    // 以 (goodsId, componentType, optionCode) 建立库存索引 / Index stock rows by unique key.
    Map<String, ComponentStockEntity> stockMap = new LinkedHashMap<>();
    var stockQuery =
        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ComponentStockEntity>();
    for (CustomComponentOptionEntity option : options) {
      stockQuery.or(
          part ->
              part.eq("goods_id", option.getGoodsId())
                  .eq("component_type", option.getComponentType())
                  .eq("option_code", option.getOptionCode()));
    }
    for (ComponentStockEntity stock :
        options.isEmpty()
            ? java.util.List.<ComponentStockEntity>of()
            : componentStockDao.selectList(stockQuery)) {
      stockMap.put(
          stockKey(stock.getGoodsId(), stock.getComponentType(), stock.getOptionCode()), stock);
    }
    List<ComponentStockView> records =
        options.stream()
            .map(
                option -> {
                  ComponentStockView view = new ComponentStockView();
                  view.setOptionId(option.getId());
                  view.setGoodsId(option.getGoodsId());
                  view.setComponentType(option.getComponentType());
                  view.setOptionCode(option.getOptionCode());
                  view.setName(option.getName());
                  view.setPriceDelta(option.getPriceDelta());
                  ComponentStockEntity stock =
                      stockMap.get(
                          stockKey(
                              option.getGoodsId(),
                              option.getComponentType(),
                              option.getOptionCode()));
                  view.setStock(stock == null || stock.getStock() == null ? 0 : stock.getStock());
                  return view;
                })
            .collect(java.util.stream.Collectors.toList());
    return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<ComponentStockView>(
            page, limit, selectedPage.getTotal())
        .setRecords(records);
  }

  /** 按选项 ID 更新库存, 无库存记录时自动创建 / Updates stock by option id, inserting when absent. */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void updateStock(
      Long optionId, Integer stock, Integer expectedStock, String operator, String reason) {
    if (optionId == null
        || stock == null
        || stock < 0
        || stock > 1000000000
        || expectedStock == null
        || expectedStock < 0
        || reason == null
        || reason.isBlank()
        || reason.length() > 200) {
      throw new IllegalArgumentException("参数错误, 库存不可为负");
    }
    CustomComponentOptionEntity option = componentOptionDao.selectById(optionId);
    if (option == null) {
      throw new IllegalArgumentException("组件选项不存在");
    }
    componentStockDao.initialize(
        option.getGoodsId(), option.getComponentType(), option.getOptionCode());
    int updated =
        componentStockDao.update(
            null,
            new UpdateWrapper<ComponentStockEntity>()
                .eq("goods_id", option.getGoodsId())
                .eq("component_type", option.getComponentType())
                .eq("option_code", option.getOptionCode())
                .eq("stock", expectedStock)
                .set("stock", stock)
                .set("update_time", new Date()));
    if (updated != 1) throw new IllegalArgumentException("库存已被销售或其他操作修改, 请刷新后再调整");
    if (componentStockDao.logMovement(
            option.getGoodsId(),
            option.getComponentType(),
            option.getOptionCode(),
            stock - expectedStock,
            operator,
            reason.trim())
        != 1) {
      throw new IllegalStateException("库存流水保存失败, 本次调整回滚");
    }
  }

  /** 构造库存唯一键 / Builds the stock unique key. */
  private String stockKey(Integer goodsId, String componentType, String optionCode) {
    return goodsId + "|" + componentType + "|" + optionCode;
  }
}
