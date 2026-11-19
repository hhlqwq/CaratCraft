package com.platform.modules.custom.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.modules.custom.dao.ComponentStockDao;
import com.platform.modules.custom.dto.NecklaceReferenceRequest;
import com.platform.modules.custom.entity.ComponentStockEntity;
import com.platform.modules.custom.entity.CustomProductEntity;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 基于公开长度档位、本人实测输入及商家配置提供确定性参考, 不调用外部服务. */
@Service
public class NecklaceReferenceService {
    private static final Map<String, List<BigDecimal>> COMPARISONS =
            Map.of(
                    "SHORT", List.of(new BigDecimal("405"), new BigDecimal("457.5")),
                    "MEDIUM", List.of(new BigDecimal("457.5"), new BigDecimal("507.5")),
                    "LONG", List.of(new BigDecimal("507.5"), new BigDecimal("610")));
    private static final List<Source> SOURCES =
            List.of(
                    new Source(
                            "Tiffany官方项链尺寸指南",
                            "https://www.tiffany.com/client-services/size-guide/necklaces-pendants-size.html",
                            "2026-10-28"));
    private final CustomProductService products;
    private final ComponentStockDao stocks;

    /** 注入可信商品配置及单项材料库存, 不依赖云模型或推荐票据. */
    public NecklaceReferenceService(CustomProductService products, ComponentStockDao stocks) {
        this.products = products;
        this.stocks = stocks;
    }

    /** 来源是通用尺寸资料, 不将其表述为个体合身或商家生产承诺. */
    public record Source(String title, String url, String reviewedAt) {}

    /** 单项库存可用的材料, 不代表已经验证完整组合或实际成交价格. */
    public record Material(Long optionId, String componentType, String name) {}

    /** 返回唯一mm长度契约、确定性理由及可追溯资料, 不保存身体参数. */
    public record Reference(
            Integer goodsId,
            String ruleVersion,
            List<BigDecimal> lengthsMm,
            boolean measured,
            boolean merchantRulesConfigured,
            String reason,
            String notice,
            List<Material> materials,
            List<Source> sources) {}

    /** 只筛选当前可售定制商品, 参考值不自动转换为生产尺寸或材料组合. */
    public Reference reference(Long userId, NecklaceReferenceRequest request) {
        validate(userId, request);
        products.getConfig(request.goodsId());
        CustomProductEntity product = products.getByGoodsId(request.goodsId());
        if (product == null) {
            throw new IllegalArgumentException("该商品暂未开放定制");
        }
        var rules = FinishedLengthRules.parse(product.getLengthRules());
        BigDecimal neck = request.neckCircumferenceMm();
        List<BigDecimal> lengths =
                COMPARISONS.get(request.preference()).stream()
                        .filter(length -> neck == null || length.compareTo(neck) > 0)
                        .filter(
                                length ->
                                        rules == null
                                                || (length.compareTo(
                                                                        rules.path("minLengthMm")
                                                                                .decimalValue())
                                                                >= 0
                                                        && length.compareTo(
                                                                        rules.path("maxLengthMm")
                                                                                .decimalValue())
                                                                <= 0))
                        .toList();
        String reason =
                lengths.isEmpty()
                        ? "当前偏好的常见长度与实测颈围或商家可制作范围没有交集, 请调整偏好或联系商家."
                        : "从公开常见长度中按偏好选取相邻档位供试戴比较"
                                + (neck == null ? ", 尚未按实测颈围筛选." : ", 已排除不大于实测颈围的长度.");
        String notice =
                "仅为长度比较参考, 颈围、吊坠和穿搭会影响佩戴位置, 请用绳线试戴确认. "
                        + "材料仅核对单项库存, 不代表组合兼容或可直接下单; 实际尺寸、工艺、价格及库存仍需重新校验."
                        + (neck == null ? " 未填写实测颈围, 不推算颈围或保证合身." : "")
                        + (rules == null ? " 商家尚未配置长度工艺规则, 暂不能确认可制作范围." : "");
        return new Reference(
                request.goodsId(),
                "necklace-reference-v1",
                lengths,
                neck != null,
                rules != null,
                reason,
                notice,
                materials(request.goodsId()),
                SOURCES);
    }

    /** 校验唯一输入格式, 不接受未知偏好或将缺失测量值解释为0. */
    private void validate(Long userId, NecklaceReferenceRequest request) {
        if (userId == null
                || userId <= 0
                || request == null
                || request.goodsId() == null
                || request.goodsId() <= 0
                || request.preference() == null
                || !COMPARISONS.containsKey(request.preference())) {
            throw new IllegalArgumentException("请登录并选择有效商品和佩戴偏好");
        }
        BigDecimal neck = request.neckCircumferenceMm();
        if (neck != null
                && (neck.scale() > 1
                        || neck.compareTo(new BigDecimal("200")) < 0
                        || neck.compareTo(new BigDecimal("800")) > 0)) {
            throw new IllegalArgumentException("实测颈围须为200到800mm, 最多1位小数; 未知请留空");
        }
    }

    /** 按稳定身份匹配库存和实际单项用量, 候选顺序不依赖数据库返回顺序. */
    private List<Material> materials(Integer goodsId) {
        Map<List<String>, Integer> balances = new HashMap<>();
        for (var stock :
                stocks.selectList(
                        new LambdaQueryWrapper<ComponentStockEntity>()
                                .eq(ComponentStockEntity::getGoodsId, goodsId))) {
            balances.put(
                    List.of(stock.getComponentType(), stock.getOptionCode()),
                    stock.getStock() == null ? 0 : stock.getStock());
        }
        return products.getEnabledOptions(goodsId).stream()
                .filter(
                        option -> {
                            int quantity =
                                    ComponentRules.parse(option.getMaterialParams()).quantity();
                            return quantity == 0
                                    || balances.getOrDefault(
                                                    List.of(
                                                            option.getComponentType(),
                                                            option.getOptionCode()),
                                                    0)
                                            >= quantity;
                        })
                .sorted(Comparator.comparing(option -> option.getId()))
                .map(
                        option ->
                                new Material(
                                        option.getId(),
                                        option.getComponentType(),
                                        option.getName()))
                .toList();
    }
}
