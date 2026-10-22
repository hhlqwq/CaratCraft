package com.platform.modules.custom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/** 定制订单关联 / Custom order binding. */
@Data
@TableName("CC_ORDER_CUSTOMIZATION")
public class CustomOrderCustomizationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer orderId;
    private Integer orderGoodsId;
    private Long snapshotId;
    private String status;
    private Date createTime;
    private Date updateTime;
}
