package com.platform.modules.custom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/** 定制组件选项 / Custom component option. */
@Data
@TableName("CC_COMPONENT_OPTION")
public class CustomComponentOptionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer goodsId;
    private String componentType;
    private String optionCode;
    private String name;
    private BigDecimal priceDelta;
    private String assetUrl;
    private String modelUrl;
    private String materialParams;
    private Integer zIndex;
    private Integer enabled;
}
