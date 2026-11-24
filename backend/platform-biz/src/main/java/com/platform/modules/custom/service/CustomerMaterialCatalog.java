package com.platform.modules.custom.service;

import com.platform.common.utils.JsonContract;
import com.platform.modules.custom.entity.CustomComponentOptionEntity;
import com.platform.modules.custom.entity.CustomProductEntity;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** 客户资料形成待商家核对的候选, 同一资源供模板和新库生成使用. */
public final class CustomerMaterialCatalog {
    /** 工具类不允许实例化. */
    private CustomerMaterialCatalog() {}

    /** 输出当前商品的停用导入模板, 不写库、不替换商家现有材料和库存. */
    public static Map<String, Object> template(Integer goodsId) {
        if (goodsId == null || goodsId < 1) {
            throw new IllegalArgumentException("请选择实际商城商品");
        }
        try (var input =
                new ClassPathResource("catalog/customer-material-catalog.json").getInputStream()) {
            var catalog =
                    JsonContract.object(new String(input.readAllBytes(), StandardCharsets.UTF_8));
            var options =
                    java.util.Arrays.asList(
                            JsonContract.read(
                                    catalog.path("options").toString(),
                                    CustomComponentOptionEntity[].class));
            for (var option : options) {
                MaterialTypes.require(option.getComponentType());
                option.setGoodsId(goodsId);
                option.setId(null);
                option.setEnabled(0);
            }
            var product = new CustomProductEntity();
            product.setGoodsId(goodsId);
            product.setTemplateCode("necklace-v1");
            product.setEnabled(0);
            product.setPreviewWidth(750);
            product.setPreviewHeight(750);
            product.setRequiredComponentTypes("pearl,clasp,chain");
            return Map.of(
                    "product",
                    product,
                    "options",
                    options,
                    "replaceOptions",
                    false,
                    "catalogNotice",
                    catalog.path("notice").asText(),
                    "accessorySourceText",
                    catalog.path("accessorySourceText").asText(),
                    "referenceImages", catalog.path("referenceImages"),
                    "sourceSha256",
                    catalog.path("sourceSha256").asText());
        } catch (IOException error) {
            throw new IllegalStateException("客户候选材料目录无法读取", error);
        }
    }
}
