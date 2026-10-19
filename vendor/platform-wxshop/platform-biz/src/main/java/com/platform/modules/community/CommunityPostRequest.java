package com.platform.modules.community;

import java.util.List;
import lombok.Data;

/** 用户显式提交的公开内容, 不接收用户ID、审核状态或价格. */
@Data
public class CommunityPostRequest {
  private String requestKey;
  private String kind;
  private String title;
  private String content;
  private List<Long> mediaIds;
  private Long snapshotId;
  private Boolean designConsent;
}
