package com.platform.modules.custom.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.modules.custom.entity.ComponentStockEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/** 组件库存数据访问 / Component stock data access. */
@Mapper
public interface ComponentStockDao extends BaseMapper<ComponentStockEntity> {

    /** 与余额修改处于同一事务, 流水写失败时余额必须回滚. */
    @org.apache.ibatis.annotations.Insert(
            "INSERT INTO"
                + " CC_STOCK_LOG(goods_id,component_type,option_code,quantity_delta,balance,operator_name,reason)"
                + " SELECT goods_id,component_type,option_code,#{delta},stock,#{operator},#{reason}"
                + " FROM CC_COMPONENT_STOCK WHERE goods_id=#{goodsId} AND component_type=#{type}"
                + " AND option_code=#{code}")
    int logMovement(
            @Param("goodsId") Integer goodsId,
            @Param("type") String type,
            @Param("code") String code,
            @Param("delta") int delta,
            @Param("operator") String operator,
            @Param("reason") String reason);

    /** 初始化库存为0, 重复导入不重置已有余额. */
    @org.apache.ibatis.annotations.Insert(
            "INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock,update_time)"
                    + " VALUES(#{goodsId},#{type},#{code},0,NOW()) ON DUPLICATE KEY UPDATE"
                    + " goods_id=goods_id")
    int initialize(
            @Param("goodsId") Integer goodsId,
            @Param("type") String type,
            @Param("code") String code);

    /** 依据订单冻结的稳定材料代码恢复实际用量, 不依赖当前配置是否启用. */
    @Update(
            "UPDATE CC_COMPONENT_STOCK SET stock=stock+#{quantity},update_time=NOW() WHERE"
                    + " goods_id=#{goodsId} AND component_type=#{type} AND option_code=#{code}")
    int restoreStock(
            @Param("goodsId") Integer goodsId,
            @Param("type") String type,
            @Param("code") String code,
            @Param("quantity") int quantity);

    /**
     * 扣减组件库存 / Deducts component stock.
     *
     * @param goodsId 商品ID / goods id
     * @param componentType 组件类型 / component type
     * @param optionCode 选项代码 / option code
     * @param quantity 扣减数量 / quantity to deduct
     * @return 影响行数 / affected rows
     */
    @Update(
            "UPDATE CC_COMPONENT_STOCK SET stock = stock - #{quantity} "
                    + "WHERE goods_id = #{goodsId} AND component_type = #{componentType} "
                    + "AND option_code = #{optionCode} AND stock >= #{quantity}")
    int deductStock(
            @Param("goodsId") Integer goodsId,
            @Param("componentType") String componentType,
            @Param("optionCode") String optionCode,
            @Param("quantity") int quantity);

    /**
     * 根据选项ID扣减库存 / Deducts stock by option id.
     *
     * @param goodsId 商品ID / goods id
     * @param optionId 选项ID / option id
     * @param quantity 扣减数量 / quantity to deduct
     * @return 影响行数 / affected rows
     */
    @Update(
            "UPDATE CC_COMPONENT_STOCK s INNER JOIN CC_COMPONENT_OPTION o ON s.goods_id ="
                + " o.goods_id AND s.component_type = o.component_type AND s.option_code ="
                + " o.option_code SET s.stock = s.stock - #{quantity} WHERE s.goods_id = #{goodsId}"
                + " AND o.id = #{optionId} AND s.stock >= #{quantity}")
    int deductByOptionId(
            @Param("goodsId") Integer goodsId,
            @Param("optionId") Long optionId,
            @Param("quantity") int quantity);

    /**
     * 根据选项ID恢复库存 / Restores stock by option id.
     *
     * @param goodsId 商品ID / goods id
     * @param optionId 选项ID / option id
     * @param quantity 恢复数量 / quantity to restore
     * @return 影响行数 / affected rows
     */
    @Update(
            "UPDATE CC_COMPONENT_STOCK s "
                    + "INNER JOIN CC_COMPONENT_OPTION o ON s.goods_id = o.goods_id "
                    + "AND s.component_type = o.component_type AND s.option_code = o.option_code "
                    + "SET s.stock = s.stock + #{quantity} "
                    + "WHERE s.goods_id = #{goodsId} AND o.id = #{optionId}")
    int restoreByOptionId(
            @Param("goodsId") Integer goodsId,
            @Param("optionId") Long optionId,
            @Param("quantity") int quantity);
}
