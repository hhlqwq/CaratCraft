package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.platform.common.exception.BusinessException;
import com.platform.modules.mall.dto.GoodsAggregateDTO;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.entity.MallGoodsSpecificationEntity;
import com.platform.modules.mall.entity.MallProductEntity;
import com.platform.modules.mall.service.impl.MallGoodsAggregateServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.math.BigDecimal;
import java.util.List;

/** 商品更新须保留未变SKU和规格身份, 不将表单中的任意ID作为持久化身份. */
class GoodsAggregateIdentityTest {
    private MallGoodsAggregateServiceImpl service;
    private MallGoodsService goods;
    private MallProductService products;
    private MallGoodsSpecificationService specifications;
    private MallProductEntity product;
    private MallGoodsSpecificationEntity specification;
    private GoodsAggregateDTO aggregate;

    /** 准备同一商品的已存组合与完整编辑请求. */
    @BeforeEach
    void setUp() {
        goods = mock(MallGoodsService.class);
        products = mock(MallProductService.class);
        specifications = mock(MallGoodsSpecificationService.class);
        service =
                new MallGoodsAggregateServiceImpl(
                        goods,
                        mock(MallGoodsAttributeService.class),
                        mock(MallGoodsGalleryService.class),
                        specifications,
                        products,
                        mock(MallAttributeService.class),
                        mock(MallSpecificationService.class));
        product = new MallProductEntity();
        product.setId(31);
        product.setGoodsId(10);
        product.setGoodsSpecificationIds("7:白色");
        product.setGoodsNumber(8);
        specification = new MallGoodsSpecificationEntity();
        specification.setId(4);
        specification.setGoodsId(10);
        specification.setSpecificationId(7);
        specification.setValue("白色");
        when(products.list(ArgumentMatchers.<Wrapper<MallProductEntity>>any()))
                .thenReturn(List.of(product));
        when(products.getOne(any())).thenReturn(product);
        when(specifications.list(ArgumentMatchers.<Wrapper<MallGoodsSpecificationEntity>>any()))
                .thenReturn(List.of(specification));
        aggregate = new GoodsAggregateDTO();
        MallGoodsEntity item = new MallGoodsEntity();
        item.setId(10);
        item.setName("隔离商品");
        item.setCategoryId(1);
        item.setRetailPrice(new BigDecimal("80.00"));
        item.setGoodsNumber(50);
        aggregate.setGoods(item);
        when(goods.getOne(any())).thenReturn(item);
        GoodsAggregateDTO.SpecGroup group = new GoodsAggregateDTO.SpecGroup();
        group.setSpecificationId(7);
        GoodsAggregateDTO.SpecValue value = new GoodsAggregateDTO.SpecValue();
        value.setValue("白色");
        group.setValues(List.of(value));
        aggregate.setSpecs(List.of(group));
        GoodsAggregateDTO.SkuItem sku = new GoodsAggregateDTO.SkuItem();
        sku.setId(999);
        sku.setGoodsSpecificationIds("7:白色");
        aggregate.setSkuList(List.of(sku));
    }

    /** 改价保留真实SKU与规格ID, 未提交库存时不把商品总库存写到SKU. */
    @Test
    void unchangedCombinationKeepsIdentityAndExistingStock() {
        assertEquals(10, service.updateAggregate(aggregate));
        assertEquals(31, product.getId());
        assertEquals(8, product.getGoodsNumber());
        assertEquals(4, specification.getId());
        assertEquals(new BigDecimal("80.00"), product.getRetailPrice());
        verify(products).updateBatchById(List.of(product));
        verify(products, never()).remove(any());
        verify(products, never()).saveBatch(any());
        verify(specifications, never()).remove(any());
        verify(specifications).updateBatchById(List.of(specification));
    }

    /** 商家明确修改库存时使用该值, 不被保留旧值规则覆盖. */
    @Test
    void explicitStockUpdateIsApplied() {
        aggregate.getSkuList().get(0).setGoodsNumber(3);
        service.updateAggregate(aggregate);
        assertEquals(3, product.getGoodsNumber());
    }

    /** 不完整组合在任何商品或子项写入前失败. */
    @Test
    void invalidCombinationNeverStartsPersistence() {
        aggregate.getSkuList().get(0).setGoodsSpecificationIds("7:金色");
        assertThrows(BusinessException.class, () -> service.updateAggregate(aggregate));
        verifyNoInteractions(goods, products, specifications);
    }

    /** 不存在或已回收的商品不得写入子项, 防止孤儿SKU和迟到编辑恢复商品. */
    @Test
    void missingOrRecycledGoodsNeverWritesChildren() {
        when(goods.getOne(any())).thenReturn(null);
        assertThrows(BusinessException.class, () -> service.updateAggregate(aggregate));
        aggregate.getGoods().setIsDelete(1);
        when(goods.getOne(any())).thenReturn(aggregate.getGoods());
        assertThrows(BusinessException.class, () -> service.updateAggregate(aggregate));
        verify(goods, never()).update(any(MallGoodsEntity.class));
        verifyNoInteractions(products, specifications);
    }
}
