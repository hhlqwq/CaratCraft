package com.platform.modules.mall.service;

import com.platform.common.utils.RestResponse;
import com.platform.modules.mall.dto.AddressSaveRequest;
import com.platform.modules.mall.entity.MallAddressEntity;
import com.platform.modules.wx.entity.WxUserEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 收货地址应用编排, 认证身份来自入口, 校验与归属规则不分散于Controller. */
@Service
public class AddressApplicationService {
    @Autowired private MallAddressService addressService;

    /** 获取用户的收货地址 */
    public RestResponse<List<MallAddressEntity>> list(WxUserEntity loginUser) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", loginUser.getId());
        List<MallAddressEntity> addressEntities = addressService.queryList(param);
        return RestResponse.ok(addressEntities);
    }

    /** 获取收货地址的详情 */
    public RestResponse<MallAddressEntity> detail(Integer id, WxUserEntity loginUser) {
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
    public RestResponse<MallAddressEntity> save(
            WxUserEntity loginUser, AddressSaveRequest request) {
        if (request == null
                || request.getUserName() == null
                || request.getUserName().isBlank()
                || request.getUserName().length() > 50
                || request.getDetailInfo() == null
                || request.getDetailInfo().isBlank()
                || request.getDetailInfo().length() > 120
                || request.getProvinceName() == null
                || request.getProvinceName().isBlank()
                || request.getProvinceName().length() > 50
                || request.getCityName() == null
                || request.getCityName().isBlank()
                || request.getCityName().length() > 50
                || request.getCountyName() == null
                || request.getCountyName().isBlank()
                || request.getCountyName().length() > 50
                || (request.getPostalCode() != null && request.getPostalCode().length() > 50)
                || (request.getNationalCode() != null && request.getNationalCode().length() > 50)
                || request.getTelNumber() == null
                || !request.getTelNumber().matches("1[3-9][0-9]{9}")) {
            return RestResponse.fail("请填写完整地址、收货人和有效手机号");
        }
        if (request.getId() != null && request.getId() < 0) {
            return RestResponse.fail("地址标识无效");
        }
        MallAddressEntity entity = new MallAddressEntity();
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
        if (null == entity.getId() || entity.getId() == 0) {
            entity.setId(null);
        }
        addressService.saveOwned(entity);
        return RestResponse.ok(entity);
    }

    /** 删除指定的收货地址 */
    public RestResponse<String> delete(WxUserEntity loginUser, Integer id) {
        if (id == null || id <= 0) {
            return RestResponse.fail("地址参数不完整");
        }
        boolean deleted =
                addressService.remove(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<
                                        MallAddressEntity>()
                                .eq("id", id)
                                .eq("user_id", loginUser.getId()));
        if (!deleted) {
            return RestResponse.fail(404, "地址不存在或无权删除");
        }
        return RestResponse.ok("");
    }
}

/**
 * 作者: @author hehailong <br>
 *
 * <p>时间: 2017-08-11 08:32<br>
 * 描述: AppController <br>
 */
