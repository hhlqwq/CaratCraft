package com.platform.modules.custom.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dao.CustomComponentOptionDao;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.service.impl.CustomStockServiceImpl;

import org.junit.jupiter.api.Test;

/** 库存比较更新与流水失败边界回归. */
class StockAdjustmentTest {
    /** 销售改变库存后拒绝覆盖, 不写入错误流水. */
    @Test
    void staleStockCannotOverwriteConcurrentSale() {
        var options = mock(CustomComponentOptionDao.class);
        var stock = mock(ComponentStockDao.class);
        CustomComponentOptionEntity option = new CustomComponentOptionEntity();
        option.setGoodsId(10);
        option.setComponentType("pearl");
        option.setOptionCode("p1");
        when(options.selectById(1L)).thenReturn(option);
        when(stock.update(isNull(), notNull())).thenReturn(0);
        var service = new CustomStockServiceImpl(options, stock);
        assertThrows(
                IllegalArgumentException.class, () -> service.updateStock(1L, 100, 20, "商家", "盘点"));
        verify(stock, never()).logMovement(any(), any(), any(), anyInt(), any(), any());
        when(stock.update(isNull(), notNull())).thenReturn(1);
        assertThrows(
                IllegalStateException.class, () -> service.updateStock(1L, 100, 20, "商家", "盘点"));
        verify(stock).logMovement(10, "pearl", "p1", 80, "商家", "盘点");
    }
}
