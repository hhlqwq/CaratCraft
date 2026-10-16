package com.platform.modules.custom.dto;

import java.util.List;
import lombok.Data;

/** DIY报价请求 / DIY quote request. */
@Data
public class CustomQuoteRequest {
  private Integer goodsId;
  private List<Long> optionIds;
  private String engraving;
  private String previewUrl;
  private Long draftId;
  private Long version;
}
