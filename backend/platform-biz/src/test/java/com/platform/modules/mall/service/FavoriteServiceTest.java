package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.platform.common.exception.BusinessException;
import com.platform.modules.mall.dao.MallCollectDao;
import com.platform.modules.mall.entity.MallCollectEntity;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.impl.MallCollectServiceImpl;
import com.platform.modules.wx.entity.WxUserEntity;
import com.platform.modules.wx.service.WxUserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

/** 使用真实收藏服务验证归属、商品状态与持久化失败, 不连接业务数据库. */
class FavoriteServiceTest {
    private MallCollectServiceImpl service;
    private MallCollectDao dao;
    private WxUserService users;
    private MallGoodsService goods;
    private MallGoodsEntity item;

    /** 准备可售商品和认证用户, 所有持久化替身均可观察. */
    @BeforeEach
    void setUp() {
        service = new MallCollectServiceImpl();
        dao = mock(MallCollectDao.class);
        users = mock(WxUserService.class);
        goods = mock(MallGoodsService.class);
        ReflectionTestUtils.setField(service, "baseMapper", dao);
        ReflectionTestUtils.setField(service, "users", users);
        ReflectionTestUtils.setField(service, "goods", goods);
        when(users.getOne(any())).thenReturn(new WxUserEntity());
        when(dao.selectList(any())).thenReturn(List.of());
        when(dao.insert(any(MallCollectEntity.class))).thenReturn(1);
        item = new MallGoodsEntity();
        item.setId(10);
        item.setIsDelete(0);
        item.setIsOnSale(1);
        when(goods.getById(10)).thenReturn(item);
    }

    /** 新增仅写当前用户和明确商品类型, 时间为秒时间戳. */
    @Test
    void addsOnlyAuthenticatedOwnersGoods() {
        assertEquals("add", service.toggleOwned(7L, 0, 10));
        var favorite = ArgumentCaptor.forClass(MallCollectEntity.class);
        verify(dao).insert(favorite.capture());
        assertEquals(7L, favorite.getValue().getUserId());
        assertEquals(0, favorite.getValue().getTypeId());
        assertEquals(10, favorite.getValue().getValueId());
        assertTrue(favorite.getValue().getAddTime() < 100_000_000_000L);
    }

    /** 删除用本人条件清除重复行, 不因商品已下架而禁止取消收藏. */
    @Test
    void deletesOwnedRowsWithoutRequiringSaleState() {
        when(dao.selectList(any())).thenReturn(List.of(new MallCollectEntity()));
        when(dao.delete(any()))
                .thenAnswer(
                        call -> {
                            var wrapper = (QueryWrapper<?>) call.getArgument(0);
                            wrapper.getSqlSegment();
                            assertTrue(wrapper.getParamNameValuePairs().containsValue(7L));
                            assertTrue(wrapper.getParamNameValuePairs().containsValue(0));
                            assertTrue(wrapper.getParamNameValuePairs().containsValue(10));
                            return 2;
                        });
        assertEquals("delete", service.toggleOwned(7L, 0, 10));
        verifyNoInteractions(goods);
    }

    /** 无效用户、类型和商品标识在任何查询或写入前拒绝. */
    @Test
    void invalidInputsNeverReachPersistence() {
        assertThrows(IllegalArgumentException.class, () -> service.toggleOwned(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> service.toggleOwned(7L, 1, 10));
        assertThrows(IllegalArgumentException.class, () -> service.toggleOwned(7L, 0, null));
        assertThrows(IllegalArgumentException.class, () -> service.listOwned(7L, null));
        verifyNoInteractions(dao, users, goods);
    }

    /** 已失效的认证用户不得创建收藏行. */
    @Test
    void missingUserNeverReachesFavorites() {
        when(users.getOne(any())).thenReturn(null);
        assertThrows(BusinessException.class, () -> service.toggleOwned(7L, 0, 10));
        verifyNoInteractions(dao, goods);
    }

    /** 下架或回收商品不能创建新收藏, 不吞掉服务端状态变化. */
    @Test
    void unavailableGoodsCannotBeAdded() {
        item.setIsOnSale(0);
        assertThrows(BusinessException.class, () -> service.toggleOwned(7L, 0, 10));
        item.setIsOnSale(1);
        item.setIsDelete(1);
        assertThrows(BusinessException.class, () -> service.toggleOwned(7L, 0, 10));
        verify(dao, never()).insert(any(MallCollectEntity.class));
    }

    /** 数据库未成功变更时不得给客户端返回新增或取消成功. */
    @Test
    void failedWritesAreNotReportedAsSuccess() {
        when(dao.insert(any(MallCollectEntity.class))).thenReturn(0);
        assertThrows(BusinessException.class, () -> service.toggleOwned(7L, 0, 10));
        when(dao.selectList(any())).thenReturn(List.of(new MallCollectEntity()));
        when(dao.delete(any())).thenReturn(0);
        assertThrows(BusinessException.class, () -> service.toggleOwned(7L, 0, 10));
    }
}
