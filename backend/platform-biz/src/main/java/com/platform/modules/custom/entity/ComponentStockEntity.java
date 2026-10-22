package com.platform.modules.custom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/** 组件库存 / Component stock. */
@Data
@TableName("CC_COMPONENT_STOCK")
public class ComponentStockEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer goodsId;
    private String componentType;
    private String optionCode;
    private Integer stock;
    private Date updateTime;
}
