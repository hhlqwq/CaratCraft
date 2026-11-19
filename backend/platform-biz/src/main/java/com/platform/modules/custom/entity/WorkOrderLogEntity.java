package com.platform.modules.custom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/** 工单操作日志 / Work order operation log. */
@Data
@TableName("CC_WORK_ORDER_LOG")
public class WorkOrderLogEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long workOrderId;
    private Long operatorId;
    private String operatorName;
    private String fromStatus;
    private String toStatus;
    private String remark;
    private Date createTime;
}
