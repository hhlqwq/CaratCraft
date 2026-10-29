package com.platform.modules.mall.service;

import com.platform.modules.mall.entity.MallGoodsEntity;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

/** 商品专属参数和媒体归属校验, 不依赖目录查询或页面逻辑. */
@Service
public class PearlProductValidator {
    public static final Set<String> SOURCES = Set.of("FRESHWATER", "SEAWATER");
    public static final Set<String> TYPES = Set.of("NECKLACE", "EARRINGS", "BRACELET", "RING");
    private final JdbcTemplate jdbc;

    /** 注入已验证媒体记录的读取接口. */
    public PearlProductValidator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 商品创建和编辑共同校验专属参数, 视频地址只从已完成上传记录取得. */
    public void validate(MallGoodsEntity item) {
        if (item == null) {
            throw new IllegalArgumentException("商品不能为空");
        }
        String source = clean(item.getPearlSource(), 16);
        String type = clean(item.getJewelryType(), 16);
        if ((!source.isEmpty() && !SOURCES.contains(source))
                || (!type.isEmpty() && !TYPES.contains(type))) {
            throw new IllegalArgumentException("商品珍珠来源或成品品类无效");
        }
        if (Integer.valueOf(1).equals(item.getIsOnSale()) && (source.isEmpty() || type.isEmpty())) {
            throw new IllegalArgumentException("上架前请选择珍珠来源和成品品类");
        }
        item.setPearlSource(source);
        item.setJewelryType(type);
        item.setPearlVariety(clean(item.getPearlVariety(), 60));
        item.setPearlOrigin(clean(item.getPearlOrigin(), 100));
        item.setPearlShape(clean(item.getPearlShape(), 30));
        item.setPearlLuster(clean(item.getPearlLuster(), 30));
        item.setPearlBlemish(clean(item.getPearlBlemish(), 30));
        validateDiameter(item.getPearlDiameterMin(), item.getPearlDiameterMax());
        bindVideo(item);
    }

    /** 直径区间允许完全未知, 填写时必须有有效且有序的两个端点. */
    private static void validateDiameter(BigDecimal min, BigDecimal max) {
        if ((min == null) != (max == null)) {
            throw new IllegalArgumentException("直径区间需同时填写上下限或同时留空");
        }
        if (min != null
                && (min.signum() <= 0
                        || max.compareTo(min) < 0
                        || max.compareTo(new BigDecimal("3000")) > 0
                        || min.scale() > 3
                        || max.scale() > 3)) {
            throw new IllegalArgumentException("珍珠直径区间无效, 单位mm, 最多三位小数");
        }
    }

    /** 只允许引用已完成上传的媒体标识, 零标识代表清除商品视频. */
    private void bindVideo(MallGoodsEntity item) {
        Long mediaId = item.getVideoMediaId();
        if (mediaId == null || mediaId == 0) {
            item.setVideoMediaId(0L);
            item.setVideoUrl("");
            return;
        }
        var media =
                jdbc.queryForList(
                        "SELECT url FROM CC_PRODUCT_VIDEO WHERE id=? AND url<>''", mediaId);
        if (media.isEmpty()) {
            throw new IllegalArgumentException("商品视频尚未完成安全上传");
        }
        item.setVideoUrl(String.valueOf(media.get(0).get("url")));
    }

    /** 限制每个公开参数长度, 未知信息留空而不补造商品事实. */
    private static String clean(String value, int length) {
        String result = value == null ? "" : value.trim();
        if (result.length() > length) {
            throw new IllegalArgumentException("珍珠参数超过长度限制");
        }
        return result;
    }
}
