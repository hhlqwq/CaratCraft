package com.platform.modules.mall.controller;

import com.platform.common.annotation.SysLog;
import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.service.MerchantContactService;
import com.platform.modules.sys.controller.AbstractController;

import lombok.RequiredArgsConstructor;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理员维护客服资料及查看版本记录, 权限与通用系统配置分开. */
@RestController
@RequiredArgsConstructor
@RequestMapping("merchant/contact")
public class MerchantContactController extends AbstractController {
    private final MerchantContactService contacts;

    /** 返回当前客服表单, 不返回其他系统配置. */
    @GetMapping
    @RequiresPermissions("merchant:contact:read")
    public RestResponse<Object> current() {
        return RestResponse.ok(contacts.current());
    }

    /** 保存经营者明确填写的联系方式, 操作者只来自当前管理会话. */
    @PostMapping
    @RequiresPermissions("merchant:contact:update")
    @SysLog("维护商家客服资料")
    public RestResponse<Object> save(@RequestBody MerchantContactService.Contact contact) {
        return RestResponse.ok(contacts.save(contact, getUserId()));
    }

    /** 最多返回最近50次修改, 不开放匿名版本记录接口. */
    @GetMapping("history")
    @RequiresPermissions("merchant:contact:read")
    public RestResponse<Object> history() {
        return RestResponse.ok(contacts.history());
    }
}
