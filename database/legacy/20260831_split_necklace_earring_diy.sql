-- CaratCraft 项链与耳饰 DIY 拆分迁移。
-- 可重复执行, 保留历史设计并通过 enabled 隐藏不适用组件。
SET NAMES utf8mb4;

INSERT INTO `mall_goods`
(`id`, `category_id`, `goods_sn`, `name`, `goods_number`, `keywords`, `goods_brief`,
 `goods_desc`, `is_on_sale`, `add_time`, `sort_order`, `is_delete`, `counter_price`, `is_new`,
 `goods_unit`, `primary_pic_url`, `list_pic_url`, `retail_price`, `is_hot`, `market_price`,
 `promotion_tag`)
VALUES
(1999106, 1999001, 'DEMO-DIY-EAR-001', '演示 · 珍珠耳饰 DIY', 100,
 '演示,DIY,珍珠,耳钉,耳钩,耳夹,18K金,铂金,钻石',
 '独立选择珍珠、耳饰款式和佩戴结构，可升级贵金属与宝石。',
 '<p>珍珠耳饰 DIY 演示商品。耳饰与项链使用独立模板，真实内容由商家在管理网页维护。</p>',
 1, NOW(), 1, 0, 3999.00, 1, '对',
 '/static/images/demo/demo-platinum-pearl-necklace.webp',
 '/static/images/demo/demo-platinum-pearl-necklace.webp',
 299.00, 1, 3999.00, 'DEMO_DIY')
ON DUPLICATE KEY UPDATE
`category_id` = VALUES(`category_id`),
`goods_sn` = VALUES(`goods_sn`),
`name` = VALUES(`name`),
`goods_number` = VALUES(`goods_number`),
`keywords` = VALUES(`keywords`),
`goods_brief` = VALUES(`goods_brief`),
`goods_desc` = VALUES(`goods_desc`),
`is_on_sale` = 1,
`is_delete` = 0,
`counter_price` = VALUES(`counter_price`),
`retail_price` = VALUES(`retail_price`),
`market_price` = VALUES(`market_price`),
`promotion_tag` = VALUES(`promotion_tag`);

INSERT INTO `mall_product`
(`id`, `goods_id`, `goods_specification_ids`, `goods_sn`, `goods_number`, `retail_price`, `market_price`)
VALUES
(1999206, 1999106, '', 'DEMO-DIY-EAR-001-STD', 100, 299.00, 3999.00)
ON DUPLICATE KEY UPDATE
`goods_id` = VALUES(`goods_id`),
`goods_sn` = VALUES(`goods_sn`),
`goods_number` = VALUES(`goods_number`),
`retail_price` = VALUES(`retail_price`),
`market_price` = VALUES(`market_price`);

UPDATE `mall_goods` SET
`name` = '演示 · 珍珠项链 DIY',
`goods_brief` = '选择珍珠与主体款式，可升级贵金属、宝石、长度和连接扣。',
`primary_product_id` = 1999205
WHERE `id` = 1999105;

UPDATE `mall_goods`
SET `primary_product_id` = 1999206
WHERE `id` = 1999106;

UPDATE `CC_CUSTOM_PRODUCT`
SET `template_code` = 'necklace-diy-v1',
    `required_component_types` = 'style,pearl',
    `enabled` = 1
WHERE `goods_id` = 1999105;

INSERT INTO `CC_CUSTOM_PRODUCT`
(`goods_id`, `template_code`, `preview_width`, `preview_height`, `required_component_types`, `enabled`)
VALUES
(1999106, 'earring-diy-v1', 750, 750, 'style,pearl,fitting', 1)
ON DUPLICATE KEY UPDATE
`template_code` = VALUES(`template_code`),
`preview_width` = VALUES(`preview_width`),
`preview_height` = VALUES(`preview_height`),
`required_component_types` = VALUES(`required_component_types`),
`enabled` = 1;

-- 在隐藏项链中的耳饰组件前复制到独立耳饰商品。
INSERT INTO `CC_COMPONENT_OPTION`
(`goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`,
 `model_url`, `material_params`, `z_index`, `enabled`)
SELECT 1999106, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`,
       `model_url`, `material_params`, `z_index`, 1
FROM `CC_COMPONENT_OPTION`
WHERE `goods_id` = 1999105
  AND (
    `component_type` IN ('pearl', 'material')
    OR (`component_type` = 'gem' AND `option_code` <> 'gem-none')
    OR (`component_type` = 'fitting' AND `option_code` IN
        ('fitting-stud-18k', 'fitting-hook-18k', 'fitting-clip-s925', 'fitting-hoop-18k'))
    OR (`component_type` = 'style' AND `option_code` IN
        ('style-stud-pair', 'style-drop-earring'))
  )
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`price_delta` = VALUES(`price_delta`),
`asset_url` = VALUES(`asset_url`),
`model_url` = VALUES(`model_url`),
`material_params` = VALUES(`material_params`),
`z_index` = VALUES(`z_index`),
`enabled` = 1;

INSERT INTO `CC_COMPONENT_STOCK`
(`goods_id`, `component_type`, `option_code`, `stock`)
SELECT 1999106, `component_type`, `option_code`, `stock`
FROM `CC_COMPONENT_STOCK`
WHERE `goods_id` = 1999105
  AND (
    `component_type` IN ('pearl', 'material')
    OR (`component_type` = 'gem' AND `option_code` <> 'gem-none')
    OR (`component_type` = 'fitting' AND `option_code` IN
        ('fitting-stud-18k', 'fitting-hook-18k', 'fitting-clip-s925', 'fitting-hoop-18k'))
    OR (`component_type` = 'style' AND `option_code` IN
        ('style-stud-pair', 'style-drop-earring'))
  )
ON DUPLICATE KEY UPDATE
`stock` = VALUES(`stock`);

-- 项链仅开放项链适用组件, 历史选项行保留供旧设计追溯。
UPDATE `CC_COMPONENT_OPTION`
SET `enabled` = CASE
  WHEN `component_type` IN ('pearl', 'material', 'length') THEN 1
  WHEN `component_type` = 'gem' AND `option_code` <> 'gem-none' THEN 1
  WHEN `component_type` = 'fitting'
       AND `option_code` IN ('fitting-clasp-lobster', 'fitting-clasp-spring') THEN 1
  WHEN `component_type` = 'style'
       AND `option_code` IN ('style-solitaire', 'style-double') THEN 1
  ELSE 0
END
WHERE `goods_id` = 1999105;

-- 耳饰不开放项链长度、项链扣或项链款式。
UPDATE `CC_COMPONENT_OPTION`
SET `enabled` = CASE
  WHEN `component_type` IN ('pearl', 'material') THEN 1
  WHEN `component_type` = 'gem' AND `option_code` <> 'gem-none' THEN 1
  WHEN `component_type` = 'fitting'
       AND `option_code` IN
           ('fitting-stud-18k', 'fitting-hook-18k', 'fitting-clip-s925', 'fitting-hoop-18k') THEN 1
  WHEN `component_type` = 'style'
       AND `option_code` IN ('style-stud-pair', 'style-drop-earring') THEN 1
  ELSE 0
END
WHERE `goods_id` = 1999106;
