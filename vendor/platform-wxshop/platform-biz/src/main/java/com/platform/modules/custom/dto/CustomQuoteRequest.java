package com.platform.modules.custom.dto;

import java.util.List;
import lombok.Data;

/** DIY报价请求 / DIY quote request. */
@Data
public class CustomQuoteRequest {
  private Integer goodsId;
  private List<Long> optionIds;
  // 有序材料实例, 同一选项可以重复; null表示历史单选模型.
  private List<Long> arrangementOptionIds;
  private String engraving;
  private String previewUrl;
  private Long draftId;
  private Long version;
}
