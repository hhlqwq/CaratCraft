-- CaratCraft 简化项链定制并停用三维配置。
-- 可重复执行, 保留旧耳饰商品和历史设计数据供追溯。
SET NAMES utf8mb4;
START TRANSACTION;

UPDATE `CC_CUSTOM_PRODUCT`
SET `required_component_types` = 'pearl,material,style',
    `model_url` = NULL,
    `enabled` = 1
WHERE `goods_id` = 1999105;

-- 耳饰不再作为独立定制主体, 商城和定制入口均停用。
UPDATE `CC_CUSTOM_PRODUCT`
SET `model_url` = NULL,
    `enabled` = 0
WHERE `goods_id` = 1999106;

UPDATE `mall_goods`
SET `is_on_sale` = 0
WHERE `id` = 1999106;

-- 项链已有明确长度可选项, 隐藏迁移过程中遗留的“不适用”占位项。
UPDATE `CC_COMPONENT_OPTION`
SET `enabled` = 0
WHERE `goods_id` = 1999105
  AND `component_type` = 'length'
  AND `option_code` = 'length-not-applicable';

-- 将常用耳饰结构作为项链定制中的可选搭配配件保留。
INSERT INTO `CC_COMPONENT_OPTION`
(`goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`,
 `model_url`, `material_params`, `z_index`, `enabled`)
SELECT 1999105, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`,
       NULL, NULL, `z_index`, 1
FROM `CC_COMPONENT_OPTION`
WHERE `goods_id` = 1999106
  AND `component_type` = 'fitting'
  AND `option_code` IN
      ('fitting-stud-18k', 'fitting-hook-18k', 'fitting-clip-s925', 'fitting-hoop-18k')
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`price_delta` = VALUES(`price_delta`),
`asset_url` = VALUES(`asset_url`),
`model_url` = NULL,
`material_params` = NULL,
`z_index` = VALUES(`z_index`),
`enabled` = 1;

INSERT INTO `CC_COMPONENT_STOCK`
(`goods_id`, `component_type`, `option_code`, `stock`)
SELECT 1999105, `component_type`, `option_code`, `stock`
FROM `CC_COMPONENT_STOCK`
WHERE `goods_id` = 1999106
  AND `component_type` = 'fitting'
  AND `option_code` IN
      ('fitting-stud-18k', 'fitting-hook-18k', 'fitting-clip-s925', 'fitting-hoop-18k')
ON DUPLICATE KEY UPDATE
`stock` = VALUES(`stock`);

-- 所有仍在使用的二维选项清空旧三维字段, 防止后台误导。
UPDATE `CC_COMPONENT_OPTION`
SET `model_url` = NULL,
    `material_params` = NULL
WHERE `goods_id` IN (1999105, 1999106);

COMMIT;
