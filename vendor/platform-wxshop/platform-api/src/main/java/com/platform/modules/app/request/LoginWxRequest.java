package com.platform.modules.app.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.platform.modules.mall.service.LegalDocumentService;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * @author pengjun
 */
@Data
@Schema(description = "微信小程序身份登录")
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginWxRequest {
    @Schema(description = "code", example = "oxaA11ulr9134oBL9Xscon5at_Gc")
    private String code;

    @Schema(description = "用户主动同意的当前协议版本")
    private LegalDocumentService.Acceptance legalAcceptance;
}
