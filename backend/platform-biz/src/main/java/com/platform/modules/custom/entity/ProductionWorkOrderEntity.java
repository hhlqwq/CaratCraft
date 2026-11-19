package com.platform.modules.custom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/** 生产工单 / Production work order. */
@Data
@TableName("CC_PRODUCTION_WORK_ORDER")
public class ProductionWorkOrderEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String workOrderNo;
    private Integer orderId;
    private Integer orderGoodsId;
    private Long snapshotId;
    private String status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private java.util.List<String> allowedActions = java.util.List.of();

    private String craftJson;
    private Date createTime;
    private Date updateTime;
}
