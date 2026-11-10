package com.platform.modules.mall.dto;

import lombok.Data;

/** 意见反馈输入契约, 用户身份和状态由服务端确定. */
@Data
public class FeedbackSaveRequest {
    private String mobile;
    private Integer index;
    private String content;
}
