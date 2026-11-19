package com.platform.modules.mall.service;

import com.platform.common.exception.BusinessException;
import com.platform.modules.mall.dao.MallFootprintDao;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/** 本人足迹按商品取最新浏览, 由业务服务限制分页并输出展示字段. */
@Service
@RequiredArgsConstructor
public class FootprintApplicationService {
    private final MallFootprintDao footprints;
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Shanghai");

    /** 显式日期采用中国时区, 不将秒数经实体序列化后重新猜测单位. */
    public record FootprintView(
            Integer id,
            Integer goodsId,
            String date,
            String name,
            String listPicUrl,
            String goodsBrief,
            BigDecimal retailPrice) {}

    /** 总数为去重商品条数, 页码和大小与实际SQL一致. */
    public record FootprintPage(List<FootprintView> records, long total, int current, int size) {}

    /** 同一读取事务内查询匹配总数及当前页, 不加载全量后再伪分页. */
    @Transactional(readOnly = true)
    public FootprintPage list(Long userId, int page, int size) {
        requireOwner(userId);
        if (page < 1 || page > 100000 || size < 1 || size > 100) {
            throw new IllegalArgumentException("分页参数无效, 每页须为1到100条");
        }
        long total = footprints.countOwned(userId);
        var records =
                footprints.listOwned(userId, (long) (page - 1) * size, size).stream()
                        .map(
                                row ->
                                        new FootprintView(
                                                row.getId(),
                                                row.getGoodsId(),
                                                Instant.ofEpochSecond(row.getAddTime())
                                                        .atZone(DISPLAY_ZONE)
                                                        .toLocalDate()
                                                        .toString(),
                                                row.getName(),
                                                row.getListPicUrl(),
                                                row.getGoodsBrief(),
                                                row.getRetailPrice()))
                        .toList();
        return new FootprintPage(records, total, page, size);
    }

    /** 原子限定所选记录和全部同商品记录的本人归属, 防止删除后旧记录再次出现. */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Integer footprintId) {
        requireOwner(userId);
        if (footprintId == null || footprintId < 1) {
            throw new IllegalArgumentException("请选择有效足迹");
        }
        if (footprints.deleteOwnedGoods(userId, footprintId) == 0) {
            throw new BusinessException("足迹不存在或不属于当前用户", 404);
        }
    }

    /** 所有私人查询和写入使用认证身份, 不接收客户端用户ID. */
    private void requireOwner(Long userId) {
        if (userId == null || userId < 1) {
            throw new BusinessException("请先登录", 401);
        }
    }
}
