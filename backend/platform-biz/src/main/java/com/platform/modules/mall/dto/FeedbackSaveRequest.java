package com.platform.modules.mall.dto;

import lombok.Data;

@Data
public class FeedbackSaveRequest {
    private String mobile;
    private Integer index;
    private String content;
}
