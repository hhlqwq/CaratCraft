-- CaratCraft 定制模块演示数据 / Demo data for the customization module.
-- 绑定底座 mall_goods 中在售商品 1006002 作为 DIY 款 /
-- Binds on-sale mall goods 1006002 as the DIY demo product.
-- 幂等: 先清理同款旧数据再插入 / Idempotent: clears previous demo rows first.

DELETE FROM `CC_COMPONENT_OPTION` WHERE `goods_id` = 1006002;
DELETE FROM `CC_CUSTOM_PRODUCT` WHERE `goods_id` = 1006002;

INSERT INTO `CC_CUSTOM_PRODUCT` (`goods_id`, `template_code`, `required_component_types`) VALUES
(1006002, 'necklace-v1', 'chain,pendant');

INSERT INTO `CC_COMPONENT_OPTION` (`goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `z_index`, `enabled`) VALUES
(1006002, 'chain',   'chain-18k',      '18K 金链',    300.00, NULL, 10, 1),
(1006002, 'chain',   'chain-rose',     '玫瑰金链',    320.00, NULL, 10, 1),
(1006002, 'chain',   'chain-pt950',    '铂金 PT950',  800.00, NULL, 10, 1),
(1006002, 'length',  'len-40',         '40cm',         0.00, NULL, 15, 1),
(1006002, 'length',  'len-45',         '45cm',        50.00, NULL, 15, 1),
(1006002, 'pendant', 'pendant-solitaire', '六爪单钻坠', 1200.00, NULL, 20, 1),
(1006002, 'pendant', 'pendant-heart',     '心形宝石坠', 900.00, NULL, 20, 1),
(1006002, 'gem',     'gem-none',       '不加宝石',      0.00, NULL, 30, 1),
(1006002, 'gem',     'gem-diamond',    '配钻 10 分', 2000.00, NULL, 30, 1),
(1006002, 'gem',     'gem-ruby',       '红宝石 30 分',1500.00, NULL, 30, 1);
