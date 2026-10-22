package com.platform.modules.app.controller;

import com.platform.annotation.LoginUser;
import com.platform.common.utils.RestResponse;
import com.platform.modules.app.request.ApiAddressDeleteRequest;
import com.platform.modules.app.request.ApiAddressSaveRequest;
import com.platform.modules.mall.entity.MallAddressEntity;
import com.platform.modules.mall.service.MallAddressService;
import com.platform.modules.wx.entity.WxUserEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者: @author Harmon <br>
 *
 * <p>时间: 2017-08-11 08:32<br>
 * 描述: AppController <br>
 */
@Tag(name = "收货地址-AppAddressController")
@RestController
@RequestMapping("/app/address")
public class AppAddressController extends AppBaseController {
    @Autowired private MallAddressService addressService;

    /** 获取用户的收货地址 */
    @Operation(summary = "获取用户的收货地址接口")
    @PostMapping("list")
    public RestResponse<List<MallAddressEntity>> list(@LoginUser WxUserEntity loginUser) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", loginUser.getId());
        List<MallAddressEntity> addressEntities = addressService.queryList(param);
        return RestResponse.ok(addressEntities);
    }

    /** 获取收货地址的详情 */
    @Operation(summary = "获取收货地址的详情")
    @PostMapping("detail")
    public RestResponse<MallAddressEntity> detail(Integer id, @LoginUser WxUserEntity loginUser) {
        MallAddressEntity entity = addressService.getById(id);
        if (null == entity) {
            return RestResponse.fail("地址不存在");
        }
        // 判断越权行为
        if (!entity.getUserId().equals(loginUser.getId())) {
            return RestResponse.fail(403, "您无权查看");
        }
        return RestResponse.ok(entity);
    }

    /** 添加或更新收货地址 */
    @Operation(summary = "添加或更新收货地址")
    @PostMapping("save")
    public RestResponse<MallAddressEntity> save(
            @LoginUser WxUserEntity loginUser, @RequestBody ApiAddressSaveRequest request) {
        if (request == null
                || request.getUserName() == null
                || request.getUserName().isBlank()
                || request.getUserName().length() > 60
                || request.getDetailInfo() == null
                || request.getDetailInfo().isBlank()
                || request.getDetailInfo().length() > 300
                || request.getProvinceName() == null
                || request.getProvinceName().isBlank()
                || request.getCityName() == null
                || request.getCityName().isBlank()
                || request.getCountyName() == null
                || request.getCountyName().isBlank()
                || request.getTelNumber() == null
                || !request.getTelNumber().matches("1[3-9][0-9]{9}")) {
            return RestResponse.fail("请填写完整地址、收货人和有效手机号");
        }
        MallAddressEntity entity = new MallAddressEntity();
        if (null != request) {
            entity.setId(request.getId());
            entity.setUserId(loginUser.getId());
            entity.setUserName(request.getUserName());
            entity.setPostalCode(request.getPostalCode());
            entity.setProvinceName(request.getProvinceName());
            entity.setCityName(request.getCityName());
            entity.setCountyName(request.getCountyName());
            entity.setDetailInfo(request.getDetailInfo());
            entity.setNationalCode(request.getNationalCode());
            entity.setTelNumber(request.getTelNumber());
            entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()) ? 1 : 0);
        }
        if (null == entity.getId() || entity.getId() == 0) {
            entity.setId(null);
        }
        addressService.saveOwned(entity);
        return RestResponse.ok(entity);
    }

    /** 删除指定的收货地址 */
    @Operation(summary = "删除指定的收货地址")
    @PostMapping("delete")
    public RestResponse<String> delete(
            @LoginUser WxUserEntity loginUser, @RequestBody ApiAddressDeleteRequest request) {
        if (request == null || request.getId() == null) {
            return RestResponse.fail("地址参数不完整");
        }
        boolean deleted =
                addressService.remove(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<
                                        MallAddressEntity>()
                                .eq("id", request.getId())
                                .eq("user_id", loginUser.getId()));
        if (!deleted) {
            return RestResponse.fail(404, "地址不存在或无权删除");
        }
        return RestResponse.ok("");
    }
}
