package com.platform.modules.custom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/** 定制设计快照 / Custom design snapshot. */
@Data
@TableName("CC_DESIGN_SNAPSHOT")
public class CustomDesignSnapshotEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long version;
    private Long userId;
    private Integer goodsId;
    private String configurationJson;
    private String priceBreakdownJson;
    private String previewUrl;
    private String status;
    private Date createTime;
    private Date updateTime;
}
