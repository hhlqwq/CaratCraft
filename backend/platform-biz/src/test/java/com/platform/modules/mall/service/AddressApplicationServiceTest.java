package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.platform.modules.mall.dto.AddressSaveRequest;
import com.platform.modules.mall.entity.MallAddressEntity;
import com.platform.modules.wx.entity.WxUserEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

/** 地址应用服务输入、归属和默认标志回归, 不连接业务数据库. */
class AddressApplicationServiceTest {
    private AddressApplicationService service;
    private MallAddressService addresses;
    private WxUserEntity user;

    /** 每个用例独立建立持久化边界和认证身份. */
    @BeforeEach
    void setUp() {
        addresses = mock(MallAddressService.class);
        service = new AddressApplicationService();
        ReflectionTestUtils.setField(service, "addressService", addresses);
        user = new WxUserEntity();
        user.setId(7L);
    }

    /** 合法输入仅采用认证用户, 默认标志缺省为0. */
    @Test
    void mapsAuthenticatedIdentityAndOptionalDefault() {
        AddressSaveRequest request = valid();
        assertEquals(0, service.save(user, request).getCode());
        var saved = ArgumentCaptor.forClass(MallAddressEntity.class);
        verify(addresses).saveOwned(saved.capture());
        assertEquals(7L, saved.getValue().getUserId());
        assertEquals(0, saved.getValue().getIsDefault());
        assertEquals(null, saved.getValue().getId());
    }

    /** 缺省字段、非法手机号和负标识均在持久化前拒绝. */
    @Test
    void rejectsInvalidInputBeforePersistence() {
        assertEquals(400, service.save(user, null).getCode());
        AddressSaveRequest request = valid();
        request.setTelNumber("invalid");
        assertEquals(400, service.save(user, request).getCode());
        request = valid();
        request.setId(-1);
        assertEquals(400, service.save(user, request).getCode());
        request = valid();
        request.setUserName(" ");
        assertEquals(400, service.save(user, request).getCode());
        request = valid();
        request.setUserName("测".repeat(51));
        assertEquals(400, service.save(user, request).getCode());
        request = valid();
        request.setDetailInfo("测".repeat(121));
        assertEquals(400, service.save(user, request).getCode());
        request = valid();
        request.setProvinceName("测".repeat(51));
        assertEquals(400, service.save(user, request).getCode());
        verifyNoInteractions(addresses);
    }

    /** 查询其他用户地址不得返回其资料. */
    @Test
    void refusesForeignAddressDetails() {
        var foreign = new MallAddressEntity();
        foreign.setUserId(8L);
        when(addresses.getById(12)).thenReturn(foreign);
        assertEquals(403, service.detail(12, user).getCode());
    }

    /** 删除必须使用本人条件, 查无匹配时明确返回404. */
    @Test
    void deletesOnlyOwnedAddress() {
        assertEquals(400, service.delete(user, -1).getCode());
        verifyNoInteractions(addresses);
        when(addresses.remove(any()))
                .thenAnswer(
                        call -> {
                            var wrapper =
                                    (com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<?>)
                                            call.getArgument(0);
                            String sql = wrapper.getSqlSegment();
                            org.junit.jupiter.api.Assertions.assertTrue(
                                    sql.contains("user_id") && sql.contains("id"));
                            org.junit.jupiter.api.Assertions.assertTrue(
                                    wrapper.getParamNameValuePairs().containsValue(7L));
                            org.junit.jupiter.api.Assertions.assertTrue(
                                    wrapper.getParamNameValuePairs().containsValue(12));
                            return false;
                        });
        assertEquals(404, service.delete(user, 12).getCode());
        verify(addresses).remove(any());
    }

    /** 合成完整地址输入, 不包含真实个人资料. */
    private AddressSaveRequest valid() {
        var request = new AddressSaveRequest();
        request.setUserName("隔离用户");
        request.setProvinceName("隔离省");
        request.setCityName("隔离市");
        request.setCountyName("隔离区");
        request.setDetailInfo("隔离地址");
        request.setTelNumber("13800000000");
        return request;
    }
}
