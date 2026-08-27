package com.platform.modules.custom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.dto.ComponentStockView;
import com.platform.modules.custom.entity.ComponentStockEntity;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.service.CustomStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 组件库存维护服务实现 / Component stock administration service implementation.
 */
@Service
@RequiredArgsConstructor
public class CustomStockServiceImpl implements CustomStockService {
    private final CustomComponentOptionDao componentOptionDao;
    private final ComponentStockDao componentStockDao;

    /** 查询组件选项及库存, 以选项为主左联库存 / Lists options left-joined with stock rows. */
    @Override
    public List<ComponentStockView> listStock(Integer goodsId) {
        List<CustomComponentOptionEntity> options = componentOptionDao.selectList(
                new LambdaQueryWrapper<CustomComponentOptionEntity>()
                        .eq(goodsId != null, CustomComponentOptionEntity::getGoodsId, goodsId)
                        .orderByAsc(CustomComponentOptionEntity::getGoodsId)
                        .orderByAsc(CustomComponentOptionEntity::getZIndex)
                        .orderByAsc(CustomComponentOptionEntity::getId));
        // 以 (goodsId, componentType, optionCode) 建立库存索引 / Index stock rows by unique key.
        Map<String, ComponentStockEntity> stockMap = new LinkedHashMap<>();
        for (ComponentStockEntity stock : componentStockDao.selectList(null)) {
            stockMap.put(stockKey(stock.getGoodsId(), stock.getComponentType(), stock.getOptionCode()), stock);
        }
        return options.stream().map(option -> {
            ComponentStockView view = new ComponentStockView();
            view.setOptionId(option.getId());
            view.setGoodsId(option.getGoodsId());
            view.setComponentType(option.getComponentType());
            view.setOptionCode(option.getOptionCode());
            view.setName(option.getName());
            view.setPriceDelta(option.getPriceDelta());
            ComponentStockEntity stock = stockMap.get(
                    stockKey(option.getGoodsId(), option.getComponentType(), option.getOptionCode()));
            view.setStock(stock == null || stock.getStock() == null ? 0 : stock.getStock());
            return view;
        }).collect(java.util.stream.Collectors.toList());
    }

    /** 按选项 ID 更新库存, 无库存记录时自动创建 / Updates stock by option id, inserting when absent. */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStock(Long optionId, Integer stock) {
        if (optionId == null || stock == null || stock < 0) {
            throw new IllegalArgumentException("参数错误, 库存不可为负");
        }
        CustomComponentOptionEntity option = componentOptionDao.selectById(optionId);
        if (option == null) {
            throw new IllegalArgumentException("组件选项不存在");
        }
        int updated = componentStockDao.update(null, new UpdateWrapper<ComponentStockEntity>()
                .eq("goods_id", option.getGoodsId())
                .eq("component_type", option.getComponentType())
                .eq("option_code", option.getOptionCode())
                .set("stock", stock)
                .set("update_time", new Date()));
        if (updated != 1) {
            ComponentStockEntity entity = new ComponentStockEntity();
            entity.setGoodsId(option.getGoodsId());
            entity.setComponentType(option.getComponentType());
            entity.setOptionCode(option.getOptionCode());
            entity.setStock(stock);
            entity.setUpdateTime(new Date());
            componentStockDao.insert(entity);
        }
    }

    /** 构造库存唯一键 / Builds the stock unique key. */
    private String stockKey(Integer goodsId, String componentType, String optionCode) {
        return goodsId + "|" + componentType + "|" + optionCode;
    }
}
