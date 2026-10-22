package com.platform.modules.custom.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.dao.CustomProductDao;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.MallGoodsService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 定制配置变更集中在服务事务中, 保留稳定代码、选项ID及历史库存. */
@Service
public class CustomConfigurationService {
    private final CustomProductDao products;
    private final CustomComponentOptionDao options;
    private final ComponentStockDao stocks;
    private final MallGoodsService goods;

    /** 注入配置和库存访问接口. */
    public CustomConfigurationService(
            CustomProductDao products,
            CustomComponentOptionDao options,
            ComponentStockDao stocks,
            MallGoodsService goods) {
        this.products = products;
        this.options = options;
        this.stocks = stocks;
        this.goods = goods;
    }

    /** 保存商品模板, 商品行锁串行化并发导入和编辑. */
    @Transactional(rollbackFor = Exception.class)
    public void saveProduct(CustomProductEntity product) {
        lockGoods(product == null ? null : product.getGoodsId());
        if (product.getTemplateCode() == null || product.getTemplateCode().isBlank()) {
            throw new IllegalArgumentException("模板代码不能为空");
        }
        product.setPreviewWidth(dimension(product.getPreviewWidth()));
        FinishedLengthRules.parse(product.getLengthRules());
        product.setPreviewHeight(dimension(product.getPreviewHeight()));
        product.setEnabled(Integer.valueOf(1).equals(product.getEnabled()) ? 1 : 0);
        if (product.getEnabled() == 1) {
            validatePublished(product);
        }
        CustomProductEntity old =
                products.selectOne(
                        new LambdaQueryWrapper<CustomProductEntity>()
                                .eq(CustomProductEntity::getGoodsId, product.getGoodsId()));
        product.setId(old == null ? null : old.getId());
        if (old == null) {
            products.insert(product);
        } else {
            products.updateById(product);
        }
    }

    /** 更新同一稳定代码的材料, 创建缺失库存而不覆盖已有库存. */
    @Transactional(rollbackFor = Exception.class)
    public void saveOption(CustomComponentOptionEntity option) {
        if (option == null) {
            throw new IllegalArgumentException("缺少材料配置");
        }
        lockGoods(option.getGoodsId());
        normalize(option);
        if (option.getId() != null) {
            CustomComponentOptionEntity old = options.selectById(option.getId());
            if (old == null
                    || !old.getGoodsId().equals(option.getGoodsId())
                    || !old.getComponentType().equals(option.getComponentType())
                    || !old.getOptionCode().equals(option.getOptionCode())) {
                throw new IllegalArgumentException("已有材料的商品、类型和代码不能修改, 请新建并停用旧项");
            }
        }
        CustomComponentOptionEntity same =
                options.selectOne(
                        new LambdaQueryWrapper<CustomComponentOptionEntity>()
                                .eq(CustomComponentOptionEntity::getGoodsId, option.getGoodsId())
                                .eq(
                                        CustomComponentOptionEntity::getComponentType,
                                        option.getComponentType())
                                .eq(
                                        CustomComponentOptionEntity::getOptionCode,
                                        option.getOptionCode()));
        if (option.getId() == null && same != null) {
            option.setId(same.getId());
        }
        if (option.getId() == null) {
            options.insert(option);
        } else {
            options.updateById(option);
        }
        stocks.initialize(option.getGoodsId(), option.getComponentType(), option.getOptionCode());
    }

    /** 删除操作仅停用, 旧草稿与订单仍能识别其材料. */
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        CustomComponentOptionEntity old = options.selectById(id);
        if (old == null) {
            throw new IllegalArgumentException("材料不存在");
        }
        lockGoods(old.getGoodsId());
        options.update(
                null,
                new UpdateWrapper<CustomComponentOptionEntity>().eq("id", id).set("enabled", 0));
    }

    /** 全量导入按稳定代码更新, 未出现的旧项停用, 不删除库存或重新生成旧选项ID. */
    @Transactional(rollbackFor = Exception.class)
    public void importConfiguration(
            CustomProductEntity product, List<CustomComponentOptionEntity> rows, boolean replace) {
        if (rows == null || rows.isEmpty() || rows.size() > 1000) {
            throw new IllegalArgumentException("请提供1到1000项材料");
        }
        lockGoods(product == null ? null : product.getGoodsId());
        Set<String> keys = new HashSet<>();
        for (CustomComponentOptionEntity row : rows) {
            if (row == null) {
                throw new IllegalArgumentException("材料配置不能为空");
            }
            row.setId(null);
            row.setGoodsId(product.getGoodsId());
            normalize(row);
            if (!keys.add(ComponentRules.key(row).toLowerCase(java.util.Locale.ROOT))) {
                throw new IllegalArgumentException("存在重复材料代码");
            }
        }
        if (replace) {
            options.update(
                    null,
                    new UpdateWrapper<CustomComponentOptionEntity>()
                            .eq("goods_id", product.getGoodsId())
                            .set("enabled", 0));
        }
        for (CustomComponentOptionEntity row : rows) {
            saveOption(row);
        }
        saveProduct(product);
    }

    /** 锁定真实商品, 不接受不存在或回收站商品的配置. */
    private void lockGoods(Integer id) {
        if (id == null
                || id < 1
                || goods.getOne(
                                new LambdaQueryWrapper<MallGoodsEntity>()
                                        .eq(MallGoodsEntity::getId, id)
                                        .eq(MallGoodsEntity::getIsDelete, 0)
                                        .last("FOR UPDATE"))
                        == null) {
            throw new IllegalArgumentException("商城商品不存在或已进入回收站");
        }
    }

    /** 规范稳定标识和规则, 名称及价格有明确边界. */
    private void normalize(CustomComponentOptionEntity row) {
        if (row.getComponentType() == null
                || !row.getComponentType().matches("[A-Za-z0-9_-]{1,32}")
                || row.getOptionCode() == null
                || !row.getOptionCode().matches("[A-Za-z0-9_.-]{1,64}")
                || row.getName() == null
                || row.getName().isBlank()
                || row.getName().length() > 100) {
            throw new IllegalArgumentException("材料类型/代码须为有效英文标识, 名称须为1到100字");
        }
        ComponentRules.parse(row.getMaterialParams());
        if (row.getAssetUrl() != null
                && !row.getAssetUrl().isBlank()
                && (!row.getAssetUrl().matches("^(https://[^\\s]+|/(?!/)[^\\s]*)$")
                        || row.getAssetUrl().length() > 1000)) {
            throw new IllegalArgumentException("图层须为HTTPS或站内图片地址");
        }
        row.setName(row.getName().trim());
        row.setPriceDelta(row.getPriceDelta() == null ? BigDecimal.ZERO : row.getPriceDelta());
        if (row.getPriceDelta().scale() > 2
                || row.getPriceDelta().abs().compareTo(new BigDecimal("9999999.99")) > 0) {
            throw new IllegalArgumentException("材料价格超出范围或精度");
        }
        row.setEnabled(Integer.valueOf(0).equals(row.getEnabled()) ? 0 : 1);
        row.setZIndex(row.getZIndex() == null ? 0 : row.getZIndex());
    }

    /** 限定预览尺寸, 避免无效比例或极大画布. */
    private int dimension(Integer value) {
        if (value == null) {
            return 750;
        }
        if (value < 100 || value > 4000) {
            throw new IllegalArgumentException("预览尺寸须为100到4000");
        }
        return value;
    }

    /** 开放定制前检查必选组至少有一种图层、规则与用量库存齐全的材料. */
    private void validatePublished(CustomProductEntity product) {
        if (product.getRequiredComponentTypes() == null
                || product.getRequiredComponentTypes().isBlank()) {
            throw new IllegalArgumentException("开放定制前请设置必选组件组");
        }
        List<CustomComponentOptionEntity> enabled =
                options.selectList(
                        new LambdaQueryWrapper<CustomComponentOptionEntity>()
                                .eq(CustomComponentOptionEntity::getGoodsId, product.getGoodsId())
                                .eq(CustomComponentOptionEntity::getEnabled, 1));
        for (String type : product.getRequiredComponentTypes().split(",")) {
            boolean ready = false;
            for (CustomComponentOptionEntity option : enabled) {
                if (!option.getComponentType().equals(type.trim())) {
                    continue;
                }
                ComponentRules rules = ComponentRules.parse(option.getMaterialParams());
                if (rules.quantity() == 0) {
                    ready = true;
                    break;
                }
                var stock =
                        stocks.selectOne(
                                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<
                                                com.platform.modules.custom.entity
                                                        .ComponentStockEntity>()
                                        .eq("goods_id", product.getGoodsId())
                                        .eq("component_type", option.getComponentType())
                                        .eq("option_code", option.getOptionCode()));
                if (stock != null
                        && stock.getStock() != null
                        && stock.getStock() >= rules.quantity()
                        && option.getAssetUrl() != null
                        && !option.getAssetUrl().isBlank()) {
                    ready = true;
                    break;
                }
            }
            if (!ready) {
                throw new IllegalArgumentException(
                        "必选组 " + type.trim() + " 缺少可用材料、预览图或库存, 请先停用并补齐配置");
            }
        }
    }
}
