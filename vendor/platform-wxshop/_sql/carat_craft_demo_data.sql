-- CaratCraft 定制模块演示数据 / Demo data for the customization module.
-- 绑定底座 mall_goods 中在售商品 1006002 作为 DIY 款 /
-- Binds on-sale mall goods 1006002 as the DIY demo product.
-- 幂等: 先清理同款旧数据再插入 / Idempotent: clears previous demo rows first.
SET NAMES utf8mb4;

DELETE FROM `CC_COMPONENT_STOCK` WHERE `goods_id` = 1006002;
DELETE FROM `CC_COMPONENT_OPTION` WHERE `goods_id` = 1006002;
DELETE FROM `CC_CUSTOM_PRODUCT` WHERE `goods_id` = 1006002;

INSERT INTO `CC_CUSTOM_PRODUCT` (`goods_id`, `template_code`, `required_component_types`) VALUES
(1006002, 'jewelry-diy-v3', 'pearl,material,gem,fitting,style,length');

INSERT INTO `CC_COMPONENT_OPTION` (`goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `z_index`, `enabled`) VALUES
(1006002, 'pearl', 'pearl-fresh-white-6', '演示 · 淡水白珠 · 日常级 · 6–7mm', 180.00, '/static/images/designer/pearl-white.svg', 30, 1),
(1006002, 'pearl', 'pearl-fresh-purple-8', '演示 · 淡水紫珠 · 精选级 · 7–8mm', 360.00, '/static/images/designer/pearl-white.svg', 30, 1),
(1006002, 'pearl', 'pearl-edison-10', '演示 · 淡水爱迪生珠 · 珠宝级 · 10–11mm', 980.00, '/static/images/designer/pearl-white.svg', 30, 1),
(1006002, 'pearl', 'pearl-baroque-10', '演示 · 淡水巴洛克珠 · 精选级 · 9–11mm', 520.00, '/static/images/designer/pearl-white.svg', 30, 1),
(1006002, 'pearl', 'pearl-akoya-7', '演示 · Akoya 海水珠 · 精选级 · 7–7.5mm', 880.00, '/static/images/designer/pearl-akoya.svg', 30, 1),
(1006002, 'pearl', 'pearl-akoya-8-jewelry', '演示 · Akoya 海水珠 · 珠宝级 · 8–8.5mm', 1600.00, '/static/images/designer/pearl-akoya.svg', 30, 1),
(1006002, 'pearl', 'pearl-tahiti-10', '演示 · 大溪地黑珍珠 · 精选级 · 9–10mm', 1900.00, '/static/images/designer/pearl-tahiti.svg', 30, 1),
(1006002, 'pearl', 'pearl-southsea-gold-11', '演示 · 南洋金珠 · 精选级 · 10–11mm', 2000.00, '/static/images/designer/pearl-akoya.svg', 30, 1),
(1006002, 'material', 'material-s925', '演示 · S925 银', 100.00, '/static/images/designer/chain-platinum.svg', 10, 1),
(1006002, 'material', 'material-18k-white', '演示 · 18K 白金', 900.00, '/static/images/designer/chain-platinum.svg', 10, 1),
(1006002, 'material', 'material-18k-yellow', '演示 · 18K 黄金', 850.00, '/static/images/designer/chain-gold.svg', 10, 1),
(1006002, 'material', 'material-18k-rose', '演示 · 18K 玫瑰金', 850.00, '/static/images/designer/chain-rose.svg', 10, 1),
(1006002, 'material', 'material-pt950', '演示 · PT950 铂金', 1600.00, '/static/images/designer/chain-platinum.svg', 10, 1),
(1006002, 'gem', 'gem-none', '演示 · 不镶嵌宝石', 0.00, NULL, 35, 1),
(1006002, 'gem', 'gem-zircon', '演示 · 锆石点缀', 100.00, '/static/images/designer/gem-diamond.svg', 35, 1),
(1006002, 'gem', 'gem-moissanite-010', '演示 · 莫桑石 · 0.10ct', 280.00, '/static/images/designer/gem-diamond.svg', 35, 1),
(1006002, 'gem', 'gem-diamond-010', '演示 · 天然钻石 · 0.10ct', 1200.00, '/static/images/designer/gem-diamond.svg', 35, 1),
(1006002, 'gem', 'gem-diamond-020', '演示 · 天然钻石 · 0.20ct', 2000.00, '/static/images/designer/gem-diamond.svg', 35, 1),
(1006002, 'fitting', 'fitting-clasp-lobster', '演示 · 龙虾扣', 180.00, NULL, 18, 1),
(1006002, 'fitting', 'fitting-clasp-spring', '演示 · 弹簧扣', 100.00, NULL, 18, 1),
(1006002, 'fitting', 'fitting-stud-18k', '演示 · 18K 白金耳钉托', 600.00, NULL, 18, 1),
(1006002, 'fitting', 'fitting-hook-18k', '演示 · 18K 金耳钩', 450.00, NULL, 18, 1),
(1006002, 'fitting', 'fitting-clip-s925', '演示 · S925 银耳夹', 500.00, NULL, 18, 1),
(1006002, 'fitting', 'fitting-hoop-18k', '演示 · 18K 金耳圈', 800.00, NULL, 18, 1),
(1006002, 'style', 'style-solitaire', '演示 · 经典单珠吊坠', 280.00, '/static/images/designer/pendant-solitaire.svg', 20, 1),
(1006002, 'style', 'style-double', '演示 · 双珠叠戴', 520.00, '/static/images/designer/pendant-heart.svg', 20, 1),
(1006002, 'style', 'style-stud-pair', '演示 · 成对珍珠耳钉', 360.00, '/static/images/designer/pearl-white.svg', 20, 1),
(1006002, 'style', 'style-drop-earring', '演示 · 垂坠珍珠耳环', 480.00, '/static/images/designer/pendant-solitaire.svg', 20, 1),
(1006002, 'length', 'length-40', '演示 · 40cm 锁骨链', 100.00, NULL, 15, 1),
(1006002, 'length', 'length-45', '演示 · 45cm 公主链', 150.00, NULL, 15, 1),
(1006002, 'length', 'length-50', '演示 · 50cm 中长链', 260.00, NULL, 15, 1),
(1006002, 'length', 'length-not-applicable', '演示 · 耳饰款不计长度', 0.00, NULL, 15, 1);

-- 组件初始库存, 与上方 CC_COMPONENT_OPTION 一一对应, 缺失会导致报价校验"库存不足" /
-- Initial component stock, must match CC_COMPONENT_OPTION rows above; missing rows fail the quote stock check.
INSERT INTO `CC_COMPONENT_STOCK` (`goods_id`, `component_type`, `option_code`, `stock`) VALUES
(1006002, 'pearl', 'pearl-fresh-white-6', 80),
(1006002, 'pearl', 'pearl-fresh-purple-8', 60),
(1006002, 'pearl', 'pearl-edison-10', 30),
(1006002, 'pearl', 'pearl-baroque-10', 40),
(1006002, 'pearl', 'pearl-akoya-7', 35),
(1006002, 'pearl', 'pearl-akoya-8-jewelry', 20),
(1006002, 'pearl', 'pearl-tahiti-10', 15),
(1006002, 'pearl', 'pearl-southsea-gold-11', 12),
(1006002, 'material', 'material-s925', 80),
(1006002, 'material', 'material-18k-white', 40),
(1006002, 'material', 'material-18k-yellow', 40),
(1006002, 'material', 'material-18k-rose', 40),
(1006002, 'material', 'material-pt950', 25),
(1006002, 'gem', 'gem-none', 999),
(1006002, 'gem', 'gem-zircon', 100),
(1006002, 'gem', 'gem-moissanite-010', 60),
(1006002, 'gem', 'gem-diamond-010', 30),
(1006002, 'gem', 'gem-diamond-020', 15),
(1006002, 'fitting', 'fitting-clasp-lobster', 80),
(1006002, 'fitting', 'fitting-clasp-spring', 80),
(1006002, 'fitting', 'fitting-stud-18k', 40),
(1006002, 'fitting', 'fitting-hook-18k', 40),
(1006002, 'fitting', 'fitting-clip-s925', 30),
(1006002, 'fitting', 'fitting-hoop-18k', 30),
(1006002, 'style', 'style-solitaire', 60),
(1006002, 'style', 'style-double', 40),
(1006002, 'style', 'style-stud-pair', 40),
(1006002, 'style', 'style-drop-earring', 40),
(1006002, 'length', 'length-40', 100),
(1006002, 'length', 'length-45', 100),
(1006002, 'length', 'length-50', 100),
(1006002, 'length', 'length-not-applicable', 999);
