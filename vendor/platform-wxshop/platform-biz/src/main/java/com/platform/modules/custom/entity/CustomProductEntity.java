package com.platform.modules.custom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 定制商品配置 / Custom product configuration.
 */
@Data
@TableName("CC_CUSTOM_PRODUCT")
public class CustomProductEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer goodsId;
    private String templateCode;
    private Integer previewWidth;
    private Integer previewHeight;
    private String modelUrl;
    private Integer enabled;
}
