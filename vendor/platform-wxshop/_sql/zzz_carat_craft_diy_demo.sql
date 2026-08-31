-- CaratCraft 独立珠宝 DIY 演示商品迁移 / Dedicated jewelry DIY demo product migration.
-- 可重复执行, 将旧家纺商品 1006002 的演示配置迁移到珠宝商品 1999105。
SET NAMES utf8mb4;

INSERT INTO `mall_category`
(`id`, `name`, `keywords`, `front_desc`, `parent_id`, `sort_order`, `show_index`, `is_show`,
 `icon_url`, `img_url`, `type`, `front_name`)
VALUES
(1999000, '珠宝成品演示', '演示,珠宝,定制', '商家维护的珠宝成品与 DIY 演示数据。', 0, 1, 1, 1,
 '', '/static/images/demo/demo-gold-solitaire-necklace.png', 0, '珠宝成品与定制'),
(1999001, '项链与耳饰', '演示,项链,耳饰,DIY', '项链、耳钉和耳环成品及定制商品。', 1999000, 1, 1, 1,
 '', '/static/images/demo/demo-platinum-pearl-necklace.png', 0, '项链与耳饰')
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`keywords` = VALUES(`keywords`),
`front_desc` = VALUES(`front_desc`),
`parent_id` = VALUES(`parent_id`),
`is_show` = 1;

INSERT INTO `mall_goods`
(`id`, `category_id`, `goods_sn`, `name`, `goods_number`, `keywords`, `goods_brief`,
 `goods_desc`, `is_on_sale`, `add_time`, `sort_order`, `is_delete`, `counter_price`, `is_new`,
 `goods_unit`, `primary_pic_url`, `list_pic_url`, `retail_price`, `is_hot`, `market_price`,
 `promotion_tag`)
VALUES
(1999105, 1999001, 'DEMO-DIY-JWL-001', '演示 · 珍珠项链与耳饰 DIY', 100,
 '演示,DIY,珍珠,项链,耳钉,耳环,18K金,铂金,钻石',
 '选择珍珠、贵金属、宝石、配件、款式和长度，价格与库存仅供功能演示。',
 '<p>珠宝 DIY 演示商品。所有选项均明确标注“演示”，真实内容由商家在管理网页维护。</p>',
 1, NOW(), 0, 0, 5999.00, 1, '件',
 '/static/images/demo/demo-platinum-pearl-necklace.png',
 '/static/images/demo/demo-platinum-pearl-necklace.png',
 399.00, 1, 5999.00, 'DEMO_DIY')
ON DUPLICATE KEY UPDATE
`category_id` = VALUES(`category_id`),
`goods_sn` = VALUES(`goods_sn`),
`name` = VALUES(`name`),
`goods_number` = VALUES(`goods_number`),
`keywords` = VALUES(`keywords`),
`goods_brief` = VALUES(`goods_brief`),
`goods_desc` = VALUES(`goods_desc`),
`is_on_sale` = 1,
`sort_order` = VALUES(`sort_order`),
`is_delete` = 0,
`counter_price` = VALUES(`counter_price`),
`retail_price` = VALUES(`retail_price`),
`market_price` = VALUES(`market_price`),
`promotion_tag` = VALUES(`promotion_tag`);

INSERT INTO `mall_product`
(`id`, `goods_id`, `goods_specification_ids`, `goods_sn`, `goods_number`, `retail_price`, `market_price`)
VALUES
(1999205, 1999105, '', 'DEMO-DIY-JWL-001-STD', 100, 399.00, 5999.00)
ON DUPLICATE KEY UPDATE
`goods_id` = VALUES(`goods_id`),
`goods_specification_ids` = VALUES(`goods_specification_ids`),
`goods_sn` = VALUES(`goods_sn`),
`goods_number` = VALUES(`goods_number`),
`retail_price` = VALUES(`retail_price`),
`market_price` = VALUES(`market_price`);

UPDATE `mall_goods`
SET `primary_product_id` = 1999205
WHERE `id` = 1999105;

-- 先复制后清理旧绑定, 保留已经迁移过的新配置以确保脚本可重复执行。
INSERT INTO `CC_CUSTOM_PRODUCT`
(`goods_id`, `template_code`, `preview_width`, `preview_height`, `model_url`,
 `required_component_types`, `enabled`)
SELECT 1999105, `template_code`, `preview_width`, `preview_height`, `model_url`,
       `required_component_types`, `enabled`
FROM `CC_CUSTOM_PRODUCT`
WHERE `goods_id` = 1006002
ON DUPLICATE KEY UPDATE
`template_code` = VALUES(`template_code`),
`preview_width` = VALUES(`preview_width`),
`preview_height` = VALUES(`preview_height`),
`model_url` = VALUES(`model_url`),
`required_component_types` = VALUES(`required_component_types`),
`enabled` = VALUES(`enabled`);

INSERT INTO `CC_COMPONENT_OPTION`
(`goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`,
 `model_url`, `material_params`, `z_index`, `enabled`)
SELECT 1999105, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`,
       `model_url`, `material_params`, `z_index`, `enabled`
FROM `CC_COMPONENT_OPTION`
WHERE `goods_id` = 1006002
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`),
`price_delta` = VALUES(`price_delta`),
`asset_url` = VALUES(`asset_url`),
`model_url` = VALUES(`model_url`),
`material_params` = VALUES(`material_params`),
`z_index` = VALUES(`z_index`),
`enabled` = VALUES(`enabled`);

INSERT INTO `CC_COMPONENT_STOCK`
(`goods_id`, `component_type`, `option_code`, `stock`)
SELECT 1999105, `component_type`, `option_code`, `stock`
FROM `CC_COMPONENT_STOCK`
WHERE `goods_id` = 1006002
ON DUPLICATE KEY UPDATE
`stock` = VALUES(`stock`);

DELETE FROM `CC_COMPONENT_STOCK` WHERE `goods_id` = 1006002;
DELETE FROM `CC_COMPONENT_OPTION` WHERE `goods_id` = 1006002;
DELETE FROM `CC_CUSTOM_PRODUCT` WHERE `goods_id` = 1006002;
