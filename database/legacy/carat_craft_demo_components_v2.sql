-- 定制演示组件升级 / Customization demo component upgrade.
-- 幂等执行, 仅重建演示商品 1006002 的配件和组件库存 / Idempotent for demo goods 1006002 only.
SET NAMES utf8mb4;

DELETE FROM `CC_COMPONENT_STOCK` WHERE `goods_id` = 1006002;
DELETE FROM `CC_COMPONENT_OPTION` WHERE `goods_id` = 1006002;

UPDATE `CC_CUSTOM_PRODUCT`
SET `template_code` = 'necklace-v2',
    `required_component_types` = 'material,length,style,pearl',
    `enabled` = 1
WHERE `goods_id` = 1006002;

INSERT INTO `CC_COMPONENT_OPTION`
(`goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `z_index`, `enabled`) VALUES
(1006002, 'material', 'material-18k',   '演示 · 18K 黄金',       300.00, '/static/images/designer/chain-gold.svg', 10, 1),
(1006002, 'material', 'material-rose',  '演示 · 18K 玫瑰金',     320.00, '/static/images/designer/chain-rose.svg', 10, 1),
(1006002, 'material', 'material-pt950', '演示 · PT950 铂金',     800.00, '/static/images/designer/chain-platinum.svg', 10, 1),
(1006002, 'length',   'length-40',      '演示 · 40cm',             0.00, NULL, 15, 1),
(1006002, 'length',   'length-45',      '演示 · 45cm',            50.00, NULL, 15, 1),
(1006002, 'length',   'length-50',      '演示 · 50cm',           100.00, NULL, 15, 1),
(1006002, 'style',    'style-classic',  '演示 · 经典单珠',        280.00, '/static/images/designer/pendant-solitaire.svg', 20, 1),
(1006002, 'style',    'style-double',   '演示 · 双珠叠戴',        520.00, '/static/images/designer/pendant-heart.svg', 20, 1),
(1006002, 'style',    'style-star',     '演示 · 星月吊坠',        460.00, '/static/images/designer/pendant-solitaire.svg', 20, 1),
(1006002, 'pearl',    'pearl-fresh',    '演示 · 淡水白珠',        380.00, '/static/images/designer/pearl-white.svg', 30, 1),
(1006002, 'pearl',    'pearl-akoya',    '演示 · Akoya 海水珍珠', 1200.00, '/static/images/designer/pearl-akoya.svg', 30, 1),
(1006002, 'pearl',    'pearl-tahiti',   '演示 · 大溪地黑珍珠',   1800.00, '/static/images/designer/pearl-tahiti.svg', 30, 1);

INSERT INTO `CC_COMPONENT_STOCK` (`goods_id`, `component_type`, `option_code`, `stock`) VALUES
(1006002, 'material', 'material-18k',    50),
(1006002, 'material', 'material-rose',   50),
(1006002, 'material', 'material-pt950',  30),
(1006002, 'length',   'length-40',      100),
(1006002, 'length',   'length-45',      100),
(1006002, 'length',   'length-50',      100),
(1006002, 'style',    'style-classic',   40),
(1006002, 'style',    'style-double',    40),
(1006002, 'style',    'style-star',      40),
(1006002, 'pearl',    'pearl-fresh',     80),
(1006002, 'pearl',    'pearl-akoya',     30),
(1006002, 'pearl',    'pearl-tahiti',    20);
