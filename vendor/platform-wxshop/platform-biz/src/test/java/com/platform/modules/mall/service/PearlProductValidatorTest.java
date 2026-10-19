package com.platform.modules.mall.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.platform.modules.mall.dao.MallGoodsDao;
import com.platform.modules.mall.entity.MallGoodsEntity;
import com.platform.modules.mall.service.impl.MallGoodsServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

/** 珍珠参数和视频关联回归, 不访问真实数据库或存储服务. */
class PearlProductValidatorTest {
  /** 旧客户端的局部更新不应把新字段规范化为空值或清除已有测量数据. */
  @Test
  void legacyPartialUpdatePreservesNewMetadata() {
    var service = new MallGoodsServiceImpl();
    MallGoodsDao mapper = mock(MallGoodsDao.class);
    PearlProductValidator validator = mock(PearlProductValidator.class);
    ReflectionTestUtils.setField(service, "baseMapper", mapper);
    ReflectionTestUtils.setField(service, "pearlProductValidator", validator);
    MallGoodsEntity item = new MallGoodsEntity();
    item.setId(12);
    item.setIsOnSale(0);
    when(mapper.updateById(item)).thenReturn(1);
    assertTrue(service.update(item));
    verify(mapper).updateById(item);
    verifyNoInteractions(validator);
    assertNull(item.getVideoMediaId());
  }

  /** 新建上架商品必须同时具有来源和品类, 不能靠名称自动推断. */
  @Test
  void publishedCreationRequiresBothCatalogDimensions() {
    MallGoodsEntity item = new MallGoodsEntity();
    item.setIsOnSale(1);
    var validator = new PearlProductValidator(mock(JdbcTemplate.class));
    assertThrows(IllegalArgumentException.class, () -> validator.validate(item, true));
    item.setPearlSource("FRESHWATER");
    item.setJewelryType("NECKLACE");
    assertDoesNotThrow(() -> validator.validate(item, true));
  }

  /** 未知尺寸可留空, 单端、倒序及超精度区间均被拒绝. */
  @Test
  void validatesOptionalDiameterRange() {
    MallGoodsEntity item = new MallGoodsEntity();
    var validator = new PearlProductValidator(mock(JdbcTemplate.class));
    assertDoesNotThrow(() -> validator.validate(item, true));
    item.setPearlDiameterMin(new BigDecimal("8.001"));
    assertThrows(IllegalArgumentException.class, () -> validator.validate(item, true));
    item.setPearlDiameterMax(new BigDecimal("8"));
    assertThrows(IllegalArgumentException.class, () -> validator.validate(item, true));
    item.setPearlDiameterMax(new BigDecimal("9.0001"));
    assertThrows(IllegalArgumentException.class, () -> validator.validate(item, true));
    item.setPearlDiameterMax(new BigDecimal("9.001"));
    assertDoesNotThrow(() -> validator.validate(item, true));
  }

  /** 商品视频地址只能来自完成上传的记录, 不能提交任意外部URL. */
  @Test
  void derivesVideoUrlFromCompletedRecord() {
    JdbcTemplate jdbc = mock(JdbcTemplate.class);
    var validator = new PearlProductValidator(jdbc);
    MallGoodsEntity item = new MallGoodsEntity();
    item.setVideoMediaId(42L);
    item.setVideoUrl("https://untrusted.invalid/video.mp4");
    when(jdbc.queryForList("SELECT url FROM CC_PRODUCT_VIDEO WHERE id=? AND url<>''", 42L))
        .thenReturn(List.of(Map.of("url", "/upload/video/verified.mp4")));
    validator.validate(item, false);
    assertEquals("/upload/video/verified.mp4", item.getVideoUrl());
    item.setVideoMediaId(0L);
    validator.validate(item, false);
    assertEquals("", item.getVideoUrl());
  }

  /** 不存在的媒体不能被绑定, 参数异常也不能继续查库. */
  @Test
  void rejectsUnknownMetadataAndUnfinishedMedia() {
    JdbcTemplate jdbc = mock(JdbcTemplate.class);
    var validator = new PearlProductValidator(jdbc);
    MallGoodsEntity item = new MallGoodsEntity();
    item.setPearlSource("UNKNOWN");
    assertThrows(IllegalArgumentException.class, () -> validator.validate(item, false));
    verifyNoInteractions(jdbc);
    item.setPearlSource("");
    item.setVideoMediaId(1L);
    assertThrows(IllegalArgumentException.class, () -> validator.validate(item, false));
  }
}
