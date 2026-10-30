package com.platform.modules.app.controller;

import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.app.request.ApiAddressDeleteRequest;
import com.platform.modules.mall.dto.AddressSaveRequest;
import com.platform.modules.mall.entity.MallAddressEntity;
import com.platform.modules.mall.service.AddressApplicationService;
import com.platform.modules.wx.entity.WxUserEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 地址协议入口, 仅提供认证上下文与输入输出映射. */
@RestController
@RequestMapping("/app/address")
public class AppAddressController {
    private final AddressApplicationService addresses;

    /** 注入唯一地址应用服务. */
    public AppAddressController(AddressApplicationService addresses) {
        this.addresses = addresses;
    }

    /** 查询当前登录用户的地址. */
    @PostMapping("list")
    public RestResponse<List<MallAddressEntity>> list(@LoginUser WxUserEntity user) {
        return addresses.list(user);
    }

    /** 查询指定本人地址, 不接收客户端用户身份. */
    @PostMapping("detail")
    public RestResponse<MallAddressEntity> detail(Integer id, @LoginUser WxUserEntity user) {
        return addresses.detail(id, user);
    }

    /** 按统一契约保存本人地址. */
    @PostMapping("save")
    public RestResponse<MallAddressEntity> save(
            @LoginUser WxUserEntity user, @RequestBody AddressSaveRequest request) {
        return addresses.save(user, request);
    }

    /** 删除本人地址, 无效输入交由应用服务拒绝. */
    @PostMapping("delete")
    public RestResponse<String> delete(
            @LoginUser WxUserEntity user, @RequestBody ApiAddressDeleteRequest request) {
        return addresses.delete(user, request == null ? null : request.getId());
    }
}

/**
 * 作者: @author hehailong <br>
 *
 * <p>时间: 2017-08-11 08:32<br>
 * 描述: AppController <br>
 */
