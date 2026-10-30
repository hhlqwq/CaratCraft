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
}
