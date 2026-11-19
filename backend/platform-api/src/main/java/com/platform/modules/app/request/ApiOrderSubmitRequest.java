package com.platform.modules.app.request;

import lombok.Data;

/**
 * @author hehailong
 */
@Data
public class ApiOrderSubmitRequest {
    private Integer addressId;
    private Integer couponId;
    private String type;
    private String postscript;
    private Long designSnapshotId;
    private String checkoutId;
    private String requestKey;
    private java.math.BigDecimal expectedActualPrice;
    // 保留输入数值原型, 由业务固定DTO拒绝小数或字符串身份, 不先隐式转为整数.
    private java.util.List<java.util.Map<String, Object>> expectedItems;
}
