package com.platform.modules.mall.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/** 保存支付前商家改价记录, 与订单金额修改在同一事务内提交. */
@Mapper
public interface OrderPriceLogDao {
    /** 记录原实付、现实付、差额、操作人和操作理由. */
    @Insert(
            "INSERT INTO CC_ORDER_PRICE_LOG"
                + " (order_id,old_price,new_price,adjustment,operator_id,reason,create_time) VALUES"
                + " (#{id},#{oldPrice},#{newPrice},#{adjustment},#{operator},#{reason},NOW())")
    int insert(
            @Param("id") Integer id,
            @Param("oldPrice") BigDecimal oldPrice,
            @Param("newPrice") BigDecimal newPrice,
            @Param("adjustment") BigDecimal adjustment,
            @Param("operator") String operator,
            @Param("reason") String reason);
}
