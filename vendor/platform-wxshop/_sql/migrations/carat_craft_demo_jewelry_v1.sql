-- CaratCraft 商家珠宝成品演示数据 / Merchant jewelry showcase demo data.
-- 可重复执行，仅清理本脚本创建的固定 ID，不影响原商城样例商品 /
-- Idempotent; only removes rows created by this script and keeps legacy demo products.

DELETE FROM `mall_product` WHERE `goods_id` IN (1999101, 1999102, 1999103, 1999104);
DELETE FROM `mall_goods` WHERE `id` IN (1999101, 1999102, 1999103, 1999104);
DELETE FROM `mall_category` WHERE `id` IN (1999001, 1999000);

INSERT INTO `mall_category` (`id`, `name`, `keywords`, `front_desc`, `parent_id`, `sort_order`, `show_index`, `is_show`, `icon_url`, `img_url`, `type`, `front_name`) VALUES
(1999000, '珠宝成品演示', '演示,珠宝,项链', '商家预先设计完成的珠宝成品，仅用于演示。', 0, 1, 1, 1, '', '/static/images/demo/demo-gold-solitaire-necklace.png', 0, '商家珠宝成品'),
(1999001, '项链成品', '演示,项链', '商家设计项链成品。', 1999000, 1, 1, 1, '', '/static/images/demo/demo-rose-heart-necklace.png', 0, '项链成品');

INSERT INTO `mall_goods` (`id`, `category_id`, `goods_sn`, `name`, `goods_number`, `keywords`, `goods_brief`, `goods_desc`, `is_on_sale`, `add_time`, `sort_order`, `is_delete`, `counter_price`, `is_new`, `goods_unit`, `primary_pic_url`, `list_pic_url`, `retail_price`, `is_hot`, `market_price`, `promotion_tag`) VALUES
(1999101, 1999001, 'DEMO-JWL-001', '演示 · 18K 金单钻项链', 30, '演示,18K金,单钻,项链', '商家设计成品，仅作商城展示与流程演示。', '<p>18K 金单钻项链演示款。</p>', 1, NOW(), 1, 0, 2999.00, 1, '件', '/static/images/demo/demo-gold-solitaire-necklace.png', '/static/images/demo/demo-gold-solitaire-necklace.png', 2399.00, 1, 2999.00, 'DEMO_SHOWCASE'),
(1999102, 1999001, 'DEMO-JWL-002', '演示 · 玫瑰金心形宝石项链', 30, '演示,玫瑰金,心形,项链', '商家设计成品，仅作商城展示与流程演示。', '<p>玫瑰金心形宝石项链演示款。</p>', 1, NOW(), 2, 0, 2599.00, 1, '件', '/static/images/demo/demo-rose-heart-necklace.png', '/static/images/demo/demo-rose-heart-necklace.png', 2099.00, 1, 2599.00, 'DEMO_SHOWCASE'),
(1999103, 1999001, 'DEMO-JWL-003', '演示 · 铂金珍珠项链', 20, '演示,铂金,珍珠,项链', '商家设计成品，仅作商城展示与流程演示。', '<p>铂金珍珠项链演示款。</p>', 1, NOW(), 3, 0, 3299.00, 1, '件', '/static/images/demo/demo-platinum-pearl-necklace.png', '/static/images/demo/demo-platinum-pearl-necklace.png', 2699.00, 1, 3299.00, 'DEMO_SHOWCASE'),
(1999104, 1999001, 'DEMO-JWL-004', '演示 · 蓝宝星轨项链', 20, '演示,白金,蓝宝石,项链', '商家设计成品，仅作商城展示与流程演示。', '<p>白金蓝宝星轨项链演示款。</p>', 1, NOW(), 4, 0, 3899.00, 1, '件', '/static/images/demo/demo-sapphire-constellation-necklace.png', '/static/images/demo/demo-sapphire-constellation-necklace.png', 3199.00, 1, 3899.00, 'DEMO_SHOWCASE');

INSERT INTO `mall_product` (`id`, `goods_id`, `goods_specification_ids`, `goods_sn`, `goods_number`, `retail_price`, `market_price`) VALUES
(1999201, 1999101, '', 'DEMO-JWL-001-STD', 30, 2399.00, 2999.00),
(1999202, 1999102, '', 'DEMO-JWL-002-STD', 30, 2099.00, 2599.00),
(1999203, 1999103, '', 'DEMO-JWL-003-STD', 20, 2699.00, 3299.00),
(1999204, 1999104, '', 'DEMO-JWL-004-STD', 20, 3199.00, 3899.00);

UPDATE `mall_goods` SET `primary_product_id` = 1999201 WHERE `id` = 1999101;
UPDATE `mall_goods` SET `primary_product_id` = 1999202 WHERE `id` = 1999102;
UPDATE `mall_goods` SET `primary_product_id` = 1999203 WHERE `id` = 1999103;
UPDATE `mall_goods` SET `primary_product_id` = 1999204 WHERE `id` = 1999104;

-- 为现有定制器演示组件补齐本地 SVG 图层 / Adds local SVG layers to customization demo options.
UPDATE `CC_COMPONENT_OPTION` SET `asset_url` = '/static/images/designer/chain-gold.svg' WHERE `goods_id` = 1006002 AND `option_code` = 'chain-18k';
UPDATE `CC_COMPONENT_OPTION` SET `asset_url` = '/static/images/designer/chain-rose.svg' WHERE `goods_id` = 1006002 AND `option_code` = 'chain-rose';
UPDATE `CC_COMPONENT_OPTION` SET `asset_url` = '/static/images/designer/chain-platinum.svg' WHERE `goods_id` = 1006002 AND `option_code` = 'chain-pt950';
UPDATE `CC_COMPONENT_OPTION` SET `asset_url` = '/static/images/designer/pendant-solitaire.svg' WHERE `goods_id` = 1006002 AND `option_code` = 'pendant-solitaire';
UPDATE `CC_COMPONENT_OPTION` SET `asset_url` = '/static/images/designer/pendant-heart.svg' WHERE `goods_id` = 1006002 AND `option_code` = 'pendant-heart';
UPDATE `CC_COMPONENT_OPTION` SET `asset_url` = '/static/images/designer/gem-diamond.svg' WHERE `goods_id` = 1006002 AND `option_code` = 'gem-diamond';
UPDATE `CC_COMPONENT_OPTION` SET `asset_url` = '/static/images/designer/gem-ruby.svg' WHERE `goods_id` = 1006002 AND `option_code` = 'gem-ruby';
