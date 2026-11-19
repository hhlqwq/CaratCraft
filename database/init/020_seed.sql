SET NAMES utf8mb4;

-- 完整初始化, 仅用于新建空库; 不用于保留真实数据的升级.
SET FOREIGN_KEY_CHECKS=0;
SET TIME_ZONE='+00:00';
START TRANSACTION;

INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (33,1999105,'pearl','pearl-fresh-white-6','演示 · 淡水白珠 · 日常级 · 6–7mm',180.00,'/static/images/designer/layers/pearl-akoya-white.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (34,1999105,'pearl','pearl-fresh-purple-8','演示 · 淡水紫珠 · 精选级 · 7–8mm',360.00,'/static/images/designer/layers/pearl-fresh-purple.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (35,1999105,'pearl','pearl-edison-10','演示 · 淡水爱迪生珠 · 珠宝级 · 10–11mm',980.00,'/static/images/designer/layers/pearl-akoya-white.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (36,1999105,'pearl','pearl-baroque-10','演示 · 淡水巴洛克珠 · 精选级 · 9–11mm',520.00,'/static/images/designer/layers/pearl-baroque-white.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (37,1999105,'pearl','pearl-akoya-7','演示 · Akoya 海水珠 · 精选级 · 7–7.5mm',880.00,'/static/images/designer/layers/pearl-akoya-white.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (38,1999105,'pearl','pearl-akoya-8-jewelry','演示 · Akoya 海水珠 · 珠宝级 · 8–8.5mm',1600.00,'/static/images/designer/layers/pearl-akoya-white.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (39,1999105,'pearl','pearl-tahiti-10','演示 · 大溪地黑珍珠 · 精选级 · 9–10mm',1900.00,'/static/images/designer/layers/pearl-tahiti-black.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (40,1999105,'pearl','pearl-southsea-gold-11','演示 · 南洋金珠 · 精选级 · 10–11mm',2000.00,'/static/images/designer/layers/pearl-southsea-gold.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (41,1999105,'material','material-s925','演示 · S925 银',100.00,'/static/images/designer/chain-platinum.svg',NULL,NULL,10,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (42,1999105,'material','material-18k-white','演示 · 18K 白金',900.00,'/static/images/designer/chain-platinum.svg',NULL,NULL,10,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (43,1999105,'material','material-18k-yellow','演示 · 18K 黄金',850.00,'/static/images/designer/chain-gold.svg',NULL,NULL,10,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (44,1999105,'material','material-18k-rose','演示 · 18K 玫瑰金',850.00,'/static/images/designer/chain-rose.svg',NULL,NULL,10,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (45,1999105,'material','material-pt950','演示 · PT950 铂金',1600.00,'/static/images/designer/chain-platinum.svg',NULL,NULL,10,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (46,1999105,'gem','gem-none','演示 · 不镶嵌宝石',0.00,NULL,NULL,NULL,35,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (47,1999105,'gem','gem-zircon','演示 · 锆石点缀',100.00,'/static/images/designer/gem-diamond.svg',NULL,NULL,35,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (48,1999105,'gem','gem-moissanite-010','演示 · 莫桑石 · 0.10ct',280.00,'/static/images/designer/gem-diamond.svg',NULL,NULL,35,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (49,1999105,'gem','gem-diamond-010','演示 · 天然钻石 · 0.10ct',1200.00,'/static/images/designer/gem-diamond.svg',NULL,NULL,35,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (50,1999105,'gem','gem-diamond-020','演示 · 天然钻石 · 0.20ct',2000.00,'/static/images/designer/gem-diamond.svg',NULL,NULL,35,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (51,1999105,'fitting','fitting-clasp-lobster','演示 · 龙虾扣',180.00,NULL,NULL,NULL,18,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (52,1999105,'fitting','fitting-clasp-spring','演示 · 弹簧扣',100.00,NULL,NULL,NULL,18,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (53,1999105,'fitting','fitting-stud-18k','演示 · 18K 白金耳钉托',600.00,NULL,NULL,NULL,18,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (54,1999105,'fitting','fitting-hook-18k','演示 · 18K 金耳钩',450.00,NULL,NULL,NULL,18,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (55,1999105,'fitting','fitting-clip-s925','演示 · S925 银耳夹',500.00,NULL,NULL,NULL,18,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (56,1999105,'fitting','fitting-hoop-18k','演示 · 18K 金耳圈',800.00,NULL,NULL,NULL,18,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (57,1999105,'style','style-solitaire','演示 · 经典单珠吊坠',280.00,'/static/images/designer/pendant-solitaire.svg',NULL,NULL,20,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (58,1999105,'style','style-double','演示 · 双珠叠戴',520.00,'/static/images/designer/pendant-heart.svg',NULL,NULL,20,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (59,1999105,'style','style-stud-pair','演示 · 成对珍珠耳钉',360.00,'/static/images/designer/pearl-white.svg',NULL,NULL,20,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (60,1999105,'style','style-drop-earring','演示 · 垂坠珍珠耳环',480.00,'/static/images/designer/pendant-solitaire.svg',NULL,NULL,20,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (61,1999105,'length','length-40','演示 · 40cm 锁骨链',100.00,NULL,NULL,NULL,15,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (62,1999105,'length','length-45','演示 · 45cm 公主链',150.00,NULL,NULL,NULL,15,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (63,1999105,'length','length-50','演示 · 50cm 中长链',260.00,NULL,NULL,NULL,15,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (64,1999105,'length','length-not-applicable','演示 · 耳饰款不计长度',0.00,NULL,NULL,NULL,15,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (96,1999106,'pearl','pearl-fresh-white-6','演示 · 淡水白珠 · 日常级 · 6–7mm',180.00,'/static/images/designer/layers/pearl-akoya-white.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (97,1999106,'pearl','pearl-fresh-purple-8','演示 · 淡水紫珠 · 精选级 · 7–8mm',360.00,'/static/images/designer/layers/pearl-fresh-purple.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (98,1999106,'pearl','pearl-edison-10','演示 · 淡水爱迪生珠 · 珠宝级 · 10–11mm',980.00,'/static/images/designer/layers/pearl-akoya-white.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (99,1999106,'pearl','pearl-baroque-10','演示 · 淡水巴洛克珠 · 精选级 · 9–11mm',520.00,'/static/images/designer/layers/pearl-baroque-white.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (100,1999106,'pearl','pearl-akoya-7','演示 · Akoya 海水珠 · 精选级 · 7–7.5mm',880.00,'/static/images/designer/layers/pearl-akoya-white.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (101,1999106,'pearl','pearl-akoya-8-jewelry','演示 · Akoya 海水珠 · 珠宝级 · 8–8.5mm',1600.00,'/static/images/designer/layers/pearl-akoya-white.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (102,1999106,'pearl','pearl-tahiti-10','演示 · 大溪地黑珍珠 · 精选级 · 9–10mm',1900.00,'/static/images/designer/layers/pearl-tahiti-black.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (103,1999106,'pearl','pearl-southsea-gold-11','演示 · 南洋金珠 · 精选级 · 10–11mm',2000.00,'/static/images/designer/layers/pearl-southsea-gold.webp',NULL,NULL,30,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (104,1999106,'material','material-s925','演示 · S925 银',100.00,'/static/images/designer/chain-platinum.svg',NULL,NULL,10,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (105,1999106,'material','material-18k-white','演示 · 18K 白金',900.00,'/static/images/designer/chain-platinum.svg',NULL,NULL,10,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (106,1999106,'material','material-18k-yellow','演示 · 18K 黄金',850.00,'/static/images/designer/chain-gold.svg',NULL,NULL,10,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (107,1999106,'material','material-18k-rose','演示 · 18K 玫瑰金',850.00,'/static/images/designer/chain-rose.svg',NULL,NULL,10,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (108,1999106,'material','material-pt950','演示 · PT950 铂金',1600.00,'/static/images/designer/chain-platinum.svg',NULL,NULL,10,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (109,1999106,'gem','gem-zircon','演示 · 锆石点缀',100.00,'/static/images/designer/gem-diamond.svg',NULL,NULL,35,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (110,1999106,'gem','gem-moissanite-010','演示 · 莫桑石 · 0.10ct',280.00,'/static/images/designer/gem-diamond.svg',NULL,NULL,35,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (111,1999106,'gem','gem-diamond-010','演示 · 天然钻石 · 0.10ct',1200.00,'/static/images/designer/gem-diamond.svg',NULL,NULL,35,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (112,1999106,'gem','gem-diamond-020','演示 · 天然钻石 · 0.20ct',2000.00,'/static/images/designer/gem-diamond.svg',NULL,NULL,35,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (113,1999106,'fitting','fitting-stud-18k','演示 · 18K 白金耳钉托',600.00,NULL,NULL,NULL,18,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (114,1999106,'fitting','fitting-hook-18k','演示 · 18K 金耳钩',450.00,NULL,NULL,NULL,18,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (115,1999106,'fitting','fitting-clip-s925','演示 · S925 银耳夹',500.00,NULL,NULL,NULL,18,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (116,1999106,'fitting','fitting-hoop-18k','演示 · 18K 金耳圈',800.00,NULL,NULL,NULL,18,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (117,1999106,'style','style-stud-pair','演示 · 成对珍珠耳钉',360.00,'/static/images/designer/pearl-white.svg',NULL,NULL,20,0);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (118,1999106,'style','style-drop-earring','演示 · 垂坠珍珠耳环',480.00,'/static/images/designer/pendant-solitaire.svg',NULL,NULL,20,0);

INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (33,1999105,'pearl','pearl-fresh-white-6',80,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (34,1999105,'pearl','pearl-fresh-purple-8',60,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (35,1999105,'pearl','pearl-edison-10',30,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (36,1999105,'pearl','pearl-baroque-10',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (37,1999105,'pearl','pearl-akoya-7',35,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (38,1999105,'pearl','pearl-akoya-8-jewelry',20,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (39,1999105,'pearl','pearl-tahiti-10',15,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (40,1999105,'pearl','pearl-southsea-gold-11',12,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (41,1999105,'material','material-s925',80,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (42,1999105,'material','material-18k-white',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (43,1999105,'material','material-18k-yellow',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (44,1999105,'material','material-18k-rose',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (45,1999105,'material','material-pt950',25,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (46,1999105,'gem','gem-none',999,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (47,1999105,'gem','gem-zircon',100,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (48,1999105,'gem','gem-moissanite-010',60,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (49,1999105,'gem','gem-diamond-010',30,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (50,1999105,'gem','gem-diamond-020',15,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (51,1999105,'fitting','fitting-clasp-lobster',80,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (52,1999105,'fitting','fitting-clasp-spring',80,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (53,1999105,'fitting','fitting-stud-18k',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (54,1999105,'fitting','fitting-hook-18k',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (55,1999105,'fitting','fitting-clip-s925',30,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (56,1999105,'fitting','fitting-hoop-18k',30,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (57,1999105,'style','style-solitaire',60,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (58,1999105,'style','style-double',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (59,1999105,'style','style-stud-pair',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (60,1999105,'style','style-drop-earring',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (61,1999105,'length','length-40',100,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (62,1999105,'length','length-45',100,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (63,1999105,'length','length-50',100,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (64,1999105,'length','length-not-applicable',999,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (96,1999106,'pearl','pearl-fresh-white-6',80,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (97,1999106,'pearl','pearl-fresh-purple-8',60,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (98,1999106,'pearl','pearl-edison-10',30,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (99,1999106,'pearl','pearl-baroque-10',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (100,1999106,'pearl','pearl-akoya-7',35,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (101,1999106,'pearl','pearl-akoya-8-jewelry',20,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (102,1999106,'pearl','pearl-tahiti-10',15,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (103,1999106,'pearl','pearl-southsea-gold-11',12,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (104,1999106,'material','material-s925',80,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (105,1999106,'material','material-18k-white',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (106,1999106,'material','material-18k-yellow',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (107,1999106,'material','material-18k-rose',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (108,1999106,'material','material-pt950',25,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (109,1999106,'gem','gem-zircon',100,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (110,1999106,'gem','gem-moissanite-010',60,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (111,1999106,'gem','gem-diamond-010',30,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (112,1999106,'gem','gem-diamond-020',15,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (113,1999106,'fitting','fitting-stud-18k',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (114,1999106,'fitting','fitting-hook-18k',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (115,1999106,'fitting','fitting-clip-s925',30,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (116,1999106,'fitting','fitting-hoop-18k',30,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (117,1999106,'style','style-stud-pair',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (118,1999106,'style','style-drop-earring',40,CURRENT_TIMESTAMP);

INSERT INTO `CC_CUSTOM_PRODUCT` (`id`, `goods_id`, `template_code`, `preview_width`, `preview_height`, `model_url`, `required_component_types`, `enabled`, `create_time`, `update_time`, `length_rules`) VALUES (2,1999105,'necklace-diy-v1',750,750,NULL,'pearl,clasp,chain',0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,NULL);
INSERT INTO `CC_CUSTOM_PRODUCT` (`id`, `goods_id`, `template_code`, `preview_width`, `preview_height`, `model_url`, `required_component_types`, `enabled`, `create_time`, `update_time`, `length_rules`) VALUES (3,1999106,'earring-diy-v1',750,750,NULL,'pearl,clasp,chain',0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,NULL);

INSERT INTO `CC_LEGAL_DOCUMENT` (`kind`, `version`, `title`, `content`, `status`, `operator_id`, `create_time`, `published_at`) VALUES ('PRIVACY',1,'克拉匠造隐私政策','版本：1
发布日期：2026年11月16日
生效日期：2026年11月16日

克拉匠造经营者重视个人信息和隐私保护。本政策适用于克拉匠造微信小程序及与其配套的订单、定制、社区和售后服务。使用相关服务前，请仔细阅读本政策；涉及登录、新交易或新增个人信息处理的功能，会在依法需要时另行取得你的主动确认。

一、个人信息处理者及联系方式
个人信息处理者为克拉匠造经营者。你可通过小程序内的客服、意见反馈或经营者另行公示的联系方式，行使个人信息权利或咨询本政策。经营者补充正式主体及联系信息后，应通过新版本公示，不以空白信息替代。

二、我们处理的信息及用途
1. 游客浏览：无需登录即可浏览商品、公开社区内容、商家公开信息及本政策。不会因为辅助统计而强制你登录。
2. 微信登录：你主动点击登录并确认协议后，我们使用微信提供的一次性登录凭证换取账号标识，保存会话令牌及你确认的协议类别、版本和确认时间，用于识别账号、保障会话安全和履行确认记录。我们不会获取你的微信密码。
3. 自愿填写的资料：你可选择设置昵称、头像；需要绑定手机号时，我们处理手机号及六位短信验证码，用于账号关联和安全校验。昵称、头像可稍后填写，不影响仅浏览公开内容。
4. 商品交易：为完成购物车、结算、订单、支付、配送、售后及依法留存交易记录，我们处理所选商品和规格、数量、金额、收货人、手机号、收货地址、订单状态、支付状态、物流及售后信息。支付结果以服务端查询和支付机构通知为准。
5. 定制设计：为保存草稿、排列材料、计算价格、核对库存和制作定制商品，我们处理材料类别与选项、数量、排列位置、目标长度、报价及设计快照。你主动填写实测颈围时，该数据仅用于本次尺寸与材料参考；我们不采集身高体重，不使用模型推断颈围或生成身体画像。
6. 社区互动：你主动发布设计、成品或建议时，我们处理发布内容、审核状态和点赞记录。公开设计使用公开副本，不公开私人刻字、实测颈围、地址或订单信息。首版社区不开放评论、私信或好友功能。
7. 客服、反馈与售后：你主动使用时，我们处理反馈类型、正文、你填写的手机号、上传的图片以及售后申请和工单内容，用于联系、核实和解决问题。
8. 安全与运维：为保障账号、接口、交易和数据安全，我们可能处理必要的请求时间、接口状态、错误及安全审计信息。日志不记录会话令牌原文、支付凭证原文或与运维无关的私人正文。

三、设备权限与第三方能力
仅在对应功能需要你主动操作时申请或使用必要权限。例如，你选择上传图片时才使用相册或相机能力。微信登录、微信支付、短信发送、图片存储或配送等能力实际启用时，可能由微信平台、已配置的短信服务商、存储服务商或配送服务方处理必要信息。我们仅按实现该功能所需范围提供信息，不出售个人信息，也不要求你开放与当前操作无关的权限。

四、信息的保存与安全
我们仅在实现处理目的、履行合同及适用法律要求的期限内保存个人信息；期限届满后删除或匿名化，法律另有规定的除外。我们采取访问控制、身份校验、传输及存储保护、最小权限和审计等措施，并对发布、订单、库存和协议版本保留必要的完整性约束。互联网服务无法承诺绝对安全；发生个人信息安全事件时，我们依法采取补救和通知措施。

五、公开、共享与委托处理
你的个人设计、订单、地址、手机号和反馈默认不公开。只有你主动发布且经审核的社区公开内容才会向其他用户展示。为实现登录、支付、短信、存储、配送或法律要求，我们可能委托或向相应服务方提供必要信息，并要求其按约定目的和安全要求处理。除实现服务、履行法定义务或取得你的另行同意外，不向无关第三方提供个人信息。

六、你的权利
你可通过小程序相应页面查看或管理本人资料、设计、订单、地址、足迹、协议确认及公开内容，也可通过客服或意见反馈请求查阅、复制、更正、补充或删除个人信息，撤回非必要授权或注销相关使用状态。撤回对协议的确认可能影响登录或新交易，但不影响此前基于确认已进行处理的效力，也不要求删除法律要求保留的交易记录。为保护账号安全，我们需要先核实请求人身份。

七、未成年人保护
未成年人应在监护人阅读并同意本政策后使用相关服务。涉及交易、公开发布或填写个人信息时，应取得监护人的指导和同意。发现未获适当授权的未成年人信息时，请通过客服或反馈入口联系我们处理。

八、政策更新与版本
每次修改均生成新的修订号；发布时使用新的版本号，已发布正文和历史确认记录不被覆盖。涉及个人信息处理目的、方式、种类或你的权利等重要变化时，我们展示新版本并依法重新取得确认。历史订单保留成交时确认的协议版本，不因后续更新被改写。

九、联系我们
对本政策或个人信息处理有疑问、意见或请求时，可通过小程序客服、意见反馈或经营者公示的联系方式提出。我们会在核实身份和请求内容后依法处理。','PUBLISHED','system-init',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO `CC_LEGAL_DOCUMENT` (`kind`, `version`, `title`, `content`, `status`, `operator_id`, `create_time`, `published_at`) VALUES ('TERMS',1,'克拉匠造用户协议','版本：1
发布日期：2026年11月16日
生效日期：2026年11月16日

欢迎使用克拉匠造。本协议由你与克拉匠造经营者共同遵守，适用于克拉匠造微信小程序提供的商品浏览、定制设计、社区、交易、订单、售后及个人服务。请在使用前仔细阅读；你主动勾选并点击登录，表示同意当前版本的用户协议和隐私政策。仅浏览公开内容无需登录。

一、账号与使用资格
1. 你使用微信身份登录，无需向我们提供微信密码。昵称和头像可自愿填写，手机号仅在绑定或交易、售后等确有需要时提供。
2. 你应妥善保管微信账号和登录设备，不得出租、出借账号或利用他人身份操作。发现异常时请及时通过客服或反馈入口联系我们。
3. 未成年人应在监护人指导和同意下使用服务及进行交易。你提交的信息和操作应真实、合法，不得冒用他人资料。

二、商品信息与定制服务
1. 商家维护商品名称、图片、视频、来源、品类、规格、价格、库存、材料和工艺等信息，实际可售状态以服务端在结算和下单时的核验为准。宣传示意图不等于对天然珍珠完全一致外观的承诺，商品差异及参数以详情页明示信息为准。
2. 定制功能支持按商家已配置范围选择珍珠、尾扣、配饰和链条，调整用量和排列，保存或撤回本人草稿。服务端重新计算价格并校验必选材料、尺寸、工艺和库存；页面编辑结果不代替最终成交校验。
3. 尺寸或材料参考仅用于辅助比较，不构成医疗、身体评估或绝对合身保证。你应核对商品页面、定制清单、目标长度和订单快照；信息不完整或库存不足时，系统可阻止保存、报价或下单。
4. 订单冻结成交时的商品、材料、数量、金额、工艺和协议版本。后续商品或规则更新不追溯改写已成交订单。

三、订单、支付与配送
1. 下单前请确认商品或定制内容、数量、价格、收货人、手机号和地址。重复提交、库存变化、价格变化或协议版本更新时，系统可能要求你重新核对，不会自动重放被阻断的写操作。
2. 支付能力实际开通后，应通过页面提供的微信支付流程完成。支付结果以服务端确认状态为准；不要仅凭客户端提示判断付款成功，也不要向个人账号或页面以外的不明收款方转账。
3. 配送范围、时效、运费和物流状态以订单及商家公示为准。你应及时核对包裹和商品；需要取消、退款、退货或售后时，通过订单和售后入口申请并保留必要凭证。
4. 定制品是否不适用七日无理由退货，应依据适用法律及下单前显著确认规则处理；排除无理由退货不影响你因质量问题、描述不符或商家违约依法享有的权利。

四、设计、社区与知识产权
1. 你可在社区主动发布设计、成品或建议并点赞。发布内容先经审核，只有公开内容向其他用户展示；首版不提供评论、私信或好友功能。
2. 你应确保发布内容不侵犯他人知识产权、肖像权、隐私权或其他合法权益，不包含违法、有害、欺诈、骚扰或无关营销信息。我们可依法及按运营规则审核、限制展示或删除违规内容。
3. 你保留对原创内容的合法权利。为实现你主动选择的保存、展示、分享和订单制作，你授予我们在相应服务范围内处理该内容的非独占许可；我们不会因此取得你原创设计的全部权利。
4. 小程序自身的程序、界面、品牌标识及商家合法素材受相应权利保护。未经许可不得复制、抓取、反向工程、出售或用于与本服务无关的商业用途，法律另有允许的除外。

五、用户行为规范
不得利用服务实施欺诈、盗号、恶意下单、绕过库存或支付校验、干扰接口、传播恶意代码、侵犯他人权益或其他违法行为。不得把客服、社区、反馈或定制入口用于非法目的。发现漏洞或异常时，可通过反馈入口告知，不得利用漏洞损害平台或其他用户。

六、客服、反馈与售后
你可通过页面提供的客服、意见反馈、订单和售后入口联系我们。商家配置的真实联系方式或服务入口以页面展示为准。我们根据订单、沟通和处理记录核实问题，并按适用法律及公示规则处理。

七、隐私与个人信息
我们按照当前发布的克拉匠造隐私政策处理个人信息。你可在协议页面查看正文及本人确认记录；涉及新版本或新增个人数据处理时，依法重新取得确认。具体信息种类、用途、保存和权利行使方式以隐私政策为准。

八、协议修改与版本管理
每次修改均生成新的修订号；发布时使用新的版本号。已发布正文不可原地覆盖，历史版本及确认记录保留。重要变化通过页面展示并依法重新取得确认。新交易须确认当前版本，已有订单仍保留成交时冻结的版本。

九、服务调整、中止与责任
为保障安全、维护系统或遵守法律，我们可对服务进行合理维护、限制或调整，并在需要时提示。因不可抗力、基础网络或第三方平台异常导致服务受影响的，我们依法采取合理补救措施。本条不排除或限制经营者依法应承担的商品质量、交易、个人信息保护及其他法定责任。

十、违约处理与争议解决
违反本协议或侵害他人权益的，我们可依据事实和法律采取提醒、限制功能、停止服务或追究责任等措施。因使用服务发生争议时，双方可先通过客服或反馈渠道协商；协商不成的，任何一方可依法向有管辖权的机构请求处理。

十一、联系我们
对本协议、订单、定制、社区或售后有疑问时，可通过小程序客服、意见反馈或经营者公示的联系方式联系我们。经营者补充正式主体及联系信息后，应通过新版本公示。','PUBLISHED','system-init',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO `CC_LEGAL_HEAD` (`kind`, `revision`, `published_version`) VALUES ('PRIVACY',1,1);
INSERT INTO `CC_LEGAL_HEAD` (`kind`, `revision`, `published_version`) VALUES ('TERMS',1,1);

INSERT INTO `CC_MERCHANT_CONTACT` (`id`, `merchant_name`, `phone`, `wechat_id`, `qr_url`, `wx_chat_enabled`, `version`, `update_time`) VALUES (1,'','','','',0,0,CURRENT_TIMESTAMP);

INSERT INTO `SCHEDULE_JOB` (`JOB_ID`, `BEAN_NAME`, `METHOD_NAME`, `PARAMS`, `CRON_EXPRESSION`, `STATUS`, `REMARK`, `CREATE_TIME`) VALUES ('7a737ccd0cf55d1447a73ad8d4417c83','testTask','test',NULL,'0 0/1 * * * ?',0,'demo','2019-07-19 16:54:54');
INSERT INTO `SCHEDULE_JOB` (`JOB_ID`, `BEAN_NAME`, `METHOD_NAME`, `PARAMS`, `CRON_EXPRESSION`, `STATUS`, `REMARK`, `CREATE_TIME`) VALUES ('goodsRecycleTask','goodsRecycleTask','purgeExpiredGoods',NULL,'0 20 3 * * ?',0,'每天凌晨3点20分清理超过三十天的回收站商品',CURRENT_TIMESTAMP);
INSERT INTO `SCHEDULE_JOB` (`JOB_ID`, `BEAN_NAME`, `METHOD_NAME`, `PARAMS`, `CRON_EXPRESSION`, `STATUS`, `REMARK`, `CREATE_TIME`) VALUES ('orderTimeoutTask','orderTimeoutTask','closeExpiredOrders',NULL,'0 */5 * * * ?',0,'订单超时自动关闭(5分钟)','2026-11-01 12:03:55');

INSERT INTO `SYS_CONFIG` (`ID`, `PARAM_KEY`, `PARAM_VALUE`, `STATUS`, `REMARK`) VALUES ('1','CLOUD_STORAGE_CONFIG_KEY','{\"type\": 4, \"diskPath\": \"/data/uploads\", \"proxyServer\": \"/upload\", \"qiniuDomain\": \"\", \"qiniuPrefix\": \"\", \"aliyunDomain\": \"\", \"aliyunPrefix\": \"\", \"qcloudDomain\": \"\", \"qcloudPrefix\": \"\", \"aliyunEndPoint\": \"\", \"qcloudSecretId\": \"\", \"qiniuAccessKey\": \"\", \"qiniuSecretKey\": \"\", \"qcloudSecretKey\": \"\", \"qiniuBucketName\": \"\", \"aliyunBucketName\": \"\", \"qcloudBucketName\": \"\", \"aliyunAccessKeyId\": \"\", \"aliyunAccessKeySecret\": \"\"}',0,'云存储配置信息');
INSERT INTO `SYS_CONFIG` (`ID`, `PARAM_KEY`, `PARAM_VALUE`, `STATUS`, `REMARK`) VALUES ('2','SMS_CONFIG_KEY','{\"domain\":\"\",\"name\":\"\",\"pwd\":\"\",\"sign\":\"\",\"type\":1}',0,'短信配置, 由部署者设置');

INSERT INTO `SYS_DICT` (`ID`, `GROUP_ID`, `NAME`, `VALUE`, `SORT`, `STATUS`, `REMARK`) VALUES ('37f73ea6b07c40ab8baec7f58b10e69e','0b5e3fc9c30a4839a881bef0f85fc8af','男','1',1,1,NULL);
INSERT INTO `SYS_DICT` (`ID`, `GROUP_ID`, `NAME`, `VALUE`, `SORT`, `STATUS`, `REMARK`) VALUES ('979439be76954bc1852fdf2aeccf3cbc','0b5e3fc9c30a4839a881bef0f85fc8af','未知','0',3,1,NULL);
INSERT INTO `SYS_DICT` (`ID`, `GROUP_ID`, `NAME`, `VALUE`, `SORT`, `STATUS`, `REMARK`) VALUES ('fc982423addd41e3852c70f8396a0c6c','0b5e3fc9c30a4839a881bef0f85fc8af','女','2',2,1,NULL);

INSERT INTO `SYS_DICT_GROUP` (`ID`, `CODE`, `NAME`, `CREATE_TIME`, `REMARK`) VALUES ('0b5e3fc9c30a4839a881bef0f85fc8af','SEX','性别','2021-01-23 20:16:36','性别，1：男 2：女 0：未知');

INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('10','0','系统设置',NULL,NULL,0,'system',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1001','10','菜单管理','sys/menu','sys:menu:list,sys:menu:info',1,'menu',1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100101','1001','新增',NULL,'sys:menu:save,sys:menu:select',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100102','1001','修改',NULL,'sys:menu:update,sys:menu:select',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100103','1001','删除',NULL,'sys:menu:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1002','10','组织机构','sys/org','sys:org:list,sys:org:info',1,'org',2);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100201','1002','新增',NULL,'sys:org:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100202','1002','修改',NULL,'sys:org:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100203','1002','删除',NULL,'sys:org:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1003','10','系统参数','sys/config','sys:config:list,sys:config:info',1,'xitongpeizhi',3);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100301','1003','新增',NULL,'sys:config:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100302','1003','修改',NULL,'sys:config:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100303','1003','删除',NULL,'sys:config:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1004','10','字典管理','sys/dictgroup','sys:dictgroup:list,sys:dictgroup:info,sys:dict:list,sys:dict:info',1,'dict',4);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100401','1004','数据字典新增',NULL,'sys:dict:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100402','1004','数据字典修改',NULL,'sys:dict:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100403','1004','数据字典删除',NULL,'sys:dict:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100404','1004','数据字典分组新增',NULL,'sys:dictgroup:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100405','1004','数据字典分组修改',NULL,'sys:dictgroup:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100406','1004','数据字典分组删除',NULL,'sys:dictgroup:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1005','10','文件上传','oss/oss','sys:oss:list',1,'oss',5);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100501','1005','云存储配置',NULL,'sys:oss:config',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100502','1005','删除',NULL,'sys:oss:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1006','10','短信配置','sys/smslog','sys:smslog:list',1,'duanxin',6);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100601','1006','修改配置',NULL,'sys:smslog:config',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100602','1006','删除',NULL,'sys:smslog:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100603','1006','发送短信',NULL,'sys:smslog:send',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1007','10','定时任务','job/schedule','sys:schedule:list,sys:schedule:info',1,'job',7);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100701','1007','删除',NULL,'sys:schedule:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100702','1007','暂停',NULL,'sys:schedule:pause',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100703','1007','恢复',NULL,'sys:schedule:resume',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100704','1007','立即执行',NULL,'sys:schedule:run',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100705','1007','日志列表',NULL,'sys:schedule:log',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100706','1007','新增',NULL,'sys:schedule:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('100707','1007','修改',NULL,'sys:schedule:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1008','10','系统日志','sys/log','sys:log:list',1,'log',8);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('11','0','权限管理',NULL,NULL,0,'quanxian',1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1101','11','管理员列表','sys/user','sys:user:list,sys:user:info',1,'admin',1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('110101','1101','重置密码',NULL,'sys:user:resetPw',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('110102','1101','新增',NULL,'sys:user:save,sys:role:select',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('110103','1101','修改',NULL,'sys:user:update,sys:role:select',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('110104','1101','删除',NULL,'sys:user:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('110105','1101','系统监控',NULL,'sys:monitor:server',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1102','11','角色管理','sys/role','sys:role:list,sys:role:info',1,'role',2);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('110201','1102','新增',NULL,'sys:role:save,sys:menu:list',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('110202','1102','修改',NULL,'sys:role:update,sys:menu:list',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('110203','1102','删除',NULL,'sys:role:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('12','0','开发工具',NULL,NULL,0,'dev',2);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1201','12','令牌管理','sys/usertoken','sys:usertoken:list',1,'zaixian',1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('120101','1201','删除',NULL,'sys:usertoken:offline',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1202','12','缓存信息','sys/redis','sys:cache:queryAll',1,'redis',2);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('120201','1202','删除',NULL,'sys:cache:deleteCache',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1203','12','SQL监控','http://localhost:8888/platform-framework/druid/sql.html',NULL,1,'sql',3);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1204','12','admin接口文档','http://localhost:8888/platform-framework/doc.html',NULL,1,'interface',4);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1205','12','api接口文档','http://localhost:8889/platform-framework-api/doc.html',NULL,1,'interface',5);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('120501','1205','生成代码',NULL,'sys:generator:code',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1206','12','代码生成器','gen/generator','sys:generator:list',1,'code',6);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('13','0','公众号管理','','',0,'wechat',3);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1301','13','公众号消息','wx/wx-msg','wx:wxmsg:list,wx:wxmsg:info,wx:wxuser:list',1,'duanxin',1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130101','1301','回复','','wx:wxmsg:save',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130102','1301','删除','','wx:wxmsg:delete',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1302','13','公众号菜单','wx/wx-menu','',1,'leibie',2);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130201','1302','更新公众号菜单','','wx:menu:save',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1303','13','消息模板','wx/msg-template','wx:msgtemplate:list,wx:msgtemplate:info',1,'tempmsg',3);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130301','1303','新增','','wx:msgtemplate:save',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130302','1303','修改','','wx:msgtemplate:update',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130303','1303','删除','','wx:msgtemplate:delete',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1304','13','消息模版日志','wx/template-msg-log','wx:templatemsglog:list,wx:templatemsglog:info',1,'orders',4);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130401','1304','删除','','wx:templatemsglog:delete',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1305','13','素材管理','wx/wx-assets','wx:wxassets:list',1,'files',5);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130501','1305','新增修改','','wx:wxassets:save',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130502','1305','删除','','wx:wxassets:delete',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1306','13','草稿箱','wx/wx-draft','wx:draft:listDraft,wx:draft:getDraft',1,'',6);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130601','1306','新增','','wx:draft:addDraft',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130602','1306','修改','','wx:draft:updateDraft',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130603','1306','删除','','wx:draft:delDraft',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130604','1306','发布','','wx:freepublish:submit',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1307','13','发布记录','wx/wx-free-publish','wx:freepublish:getPublicationRecords,wx:freepublish:getArticleFromId',1,'',7);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130701','1307','删除','','wx:freepublish:deletePush',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1308','13','自动回复规则','wx/msg-reply-rule','wx:msgreplyrule:list,wx:msgreplyrule:info',1,'guize',8);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130801','1308','新增','','wx:msgreplyrule:save',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130802','1308','修改','','wx:msgreplyrule:update',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130803','1308','删除','','wx:msgreplyrule:delete',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1309','13','带参二维码','wx/wx-qrcode','wx:wxqrcode:list,wx:wxqrcode:info',1,'qrcode',9);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130901','1309','新增','','wx:wxqrcode:save',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('130902','1309','删除','','wx:wxqrcode:delete',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('14','0','会员管理','','',0,'admin',4);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1401','14','用户管理','wx/wx-user','wx:wxuser:list,wx:wxuser:info',1,'vip',1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('140101','1401','标签操作','','wx:wxuser:save',2,'',0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1402','14','会员等级配置','mall/userlevel','mall:userlevel:list,mall:userlevel:info',1,'level',2);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('140201','1402','新增',NULL,'mall:userlevel:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('140202','1402','修改',NULL,'mall:userlevel:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('140203','1402','删除',NULL,'mall:userlevel:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1403','14','会员购物车','mall/cart','mall:cart:list,mall:cart:info',1,'cart',3);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('140301','1403','删除',NULL,'mall:cart:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1404','14','收货地址','mall/address','mall:address:list,mall:address:info',1,'dangdifill',4);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('140401','1404','删除',NULL,'mall:address:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1405','14','会员收藏','mall/collect','mall:collect:list,mall:collect:info',1,'collect',5);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('140501','1405','删除',NULL,'mall:collect:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1406','14','会员反馈','mall/feedback','mall:feedback:list,mall:feedback:info',1,'feedback',6);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('140601','1406','删除',NULL,'mall:feedback:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1407','14','历史足迹','mall/footprint','mall:footprint:list,mall:footprint:info',1,'footprint',7);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('140701','1407','删除',NULL,'mall:footprint:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1408','14','会员优惠券','mall/usercoupon','mall:usercoupon:list,mall:usercoupon:info',1,'coupon',8);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('140801','1408','删除',NULL,'mall:usercoupon:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1409','14','会员搜索历史','mall/searchhistory','mall:searchhistory:list,mall:searchhistory:info',1,'sousuo',9);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('140901','1409','删除',NULL,'mall:searchhistory:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('15','0','商城配置',NULL,NULL,0,'mall',5);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1501','15','品牌制造商','mall/brand','mall:brand:list,mall:brand:info',1,'brand',1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150101','1501','新增',NULL,'mall:brand:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150102','1501','修改',NULL,'mall:brand:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150103','1501','删除',NULL,'mall:brand:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1502','15','商品分类','mall/category','mall:category:list,mall:category:info',1,'leibie',2);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150201','1502','新增',NULL,'mall:category:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150202','1502','修改',NULL,'mall:category:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150203','1502','删除',NULL,'mall:category:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1503','15','广告位','mall/adposition','mall:adposition:list,mall:adposition:info',1,'dangdifill',3);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150301','1503','新增',NULL,'mall:adposition:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150302','1503','修改',NULL,'mall:adposition:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150303','1503','删除',NULL,'mall:adposition:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1504','15','广告管理','mall/ad','mall:ad:list,mall:ad:info',1,'tubiao',4);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150401','1504','新增',NULL,'mall:ad:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150402','1504','修改',NULL,'mall:ad:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150403','1504','删除',NULL,'mall:ad:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1505','15','专题分类','mall/topiccategory','mall:topiccategory:list,mall:topiccategory:info',1,'zhedie',5);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150501','1505','新增',NULL,'mall:topiccategory:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150502','1505','修改',NULL,'mall:topiccategory:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150503','1505','删除',NULL,'mall:topiccategory:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1506','15','专题管理','mall/topic','mall:topic:list,mall:topic:info',1,'guize',6);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150601','1506','新增',NULL,'mall:topic:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150602','1506','修改',NULL,'mall:topic:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150603','1506','删除',NULL,'mall:topic:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1507','15','商品参数类型','mall/attributecategory','mall:attributecategory:list,mall:attributecategory:info',1,'zhedie',7);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150701','1507','新增',NULL,'mall:attributecategory:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150702','1507','修改',NULL,'mall:attributecategory:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150703','1507','删除',NULL,'mall:attributecategory:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1508','15','商品参数属性','mall/attribute','mall:attribute:list,mall:attribute:info',1,'guize',8);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150801','1508','新增',NULL,'mall:attribute:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150802','1508','修改',NULL,'mall:attribute:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150803','1508','删除',NULL,'mall:attribute:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1509','15','关键词热搜','mall/keywords','mall:keywords:list,mall:keywords:info',1,'sousuo',9);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150901','1509','新增',NULL,'mall:keywords:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150902','1509','修改',NULL,'mall:keywords:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('150903','1509','删除',NULL,'mall:keywords:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1510','15','物流公司','mall/shipping','mall:shipping:list,mall:shipping:info',1,'shipping',10);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('151001','1510','新增',NULL,'mall:shipping:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('151002','1510','修改',NULL,'mall:shipping:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('151003','1510','删除',NULL,'mall:shipping:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1511','15','金刚区配置','mall/channel','mall:channel:list,mall:channel:info',1,'daohang',11);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('151101','1511','新增',NULL,'mall:channel:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('151102','1511','修改',NULL,'mall:channel:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('151103','1511','删除',NULL,'mall:channel:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1512','15','规格KEY配置','mall/specification','mall:specification:list,mall:specification:info',1,'zaixian',12);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('151201','1512','新增',NULL,'mall:specification:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('151202','1512','修改',NULL,'mall:specification:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('151203','1512','删除',NULL,'mall:specification:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('16','0','推广管理',NULL,NULL,0,'gift',6);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1601','16','优惠券管理','mall/coupon','mall:coupon:list,mall:coupon:info',1,'coupons',1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('160101','1601','新增',NULL,'mall:coupon:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('160102','1601','修改',NULL,'mall:coupon:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('160103','1601','删除',NULL,'mall:coupon:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('17','0','商品管理',NULL,NULL,0,'goods',7);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1701','17','商品管理','mall/goods','mall:goods:list,mall:goods:info',1,'coupons',1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('170101','1701','新增',NULL,'mall:goods:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('170102','1701','修改',NULL,'mall:goods:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('170103','1701','删除',NULL,'mall:goods:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1702','17','评论管理','mall/comment','mall:comment:list,mall:comment:info',1,'pinglun',2);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('170201','1702','新增',NULL,'mall:comment:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('170202','1702','修改',NULL,'mall:comment:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('170203','1702','删除',NULL,'mall:comment:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1703','17','商品回收站','mall/goods-recycle','mall:goods:recycle:list',1,'delete',3);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('170301','1703','恢复商品',NULL,'mall:goods:recycle:restore',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('170302','1703','永久删除',NULL,'mall:goods:recycle:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('18','0','订单管理',NULL,NULL,0,'orders',8);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1801','18','订单管理','mall/order','mall:order:list,mall:order:info,mall:ordergoods:list',1,'orders',1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('180101','1801','新增',NULL,'mall:order:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('180102','1801','修改',NULL,'mall:order:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('180103','1801','删除',NULL,'mall:order:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`,`PARENT_ID`,`NAME`,`URL`,`PERMS`,`TYPE`,`ICON`,`ORDER_NUM`) VALUES ('180104','1801','订单发货',NULL,'mall:order:ship',2,NULL,1);
INSERT INTO `SYS_MENU` (`MENU_ID`,`PARENT_ID`,`NAME`,`URL`,`PERMS`,`TYPE`,`ICON`,`ORDER_NUM`) VALUES ('180105','1802','执行退款',NULL,'mall:refund:execute',2,NULL,2);
INSERT INTO `SYS_MENU` (`MENU_ID`,`PARENT_ID`,`NAME`,`URL`,`PERMS`,`TYPE`,`ICON`,`ORDER_NUM`) VALUES ('180106','1802','重试退款',NULL,'mall:refund:retry',2,NULL,3);
INSERT INTO `SYS_MENU` (`MENU_ID`,`PARENT_ID`,`NAME`,`URL`,`PERMS`,`TYPE`,`ICON`,`ORDER_NUM`) VALUES ('180107','1802','退款对账写回',NULL,'mall:refund:reconcile',2,NULL,4);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('19','0','帮助中心',NULL,NULL,0,'pinglun',9);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1901','19','问题类型','mall/helptype','mall:helptype:list,mall:helptype:info',1,'zhedie',1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('190101','1901','新增',NULL,'mall:helptype:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('190102','1901','修改',NULL,'mall:helptype:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('190103','1901','删除',NULL,'mall:helptype:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1902','19','常见问题','mall/helpissue','mall:helpissue:list,mall:helpissue:info',1,'issue',1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('190201','1902','新增',NULL,'mall:helpissue:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('190202','1902','修改',NULL,'mall:helpissue:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('190203','1902','删除',NULL,'mall:helpissue:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('1903','19','商品页问答','mall/goodsissue','mall:goodsissue:list,mall:goodsissue:info',1,'issue',1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('190301','1903','新增',NULL,'mall:goodsissue:save',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('190302','1903','修改',NULL,'mall:goodsissue:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('190303','1903','删除',NULL,'mall:goodsissue:delete',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('20','0','定制管理',NULL,NULL,0,'system',20);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('2001','20','生产工单','custom/work-order','custom:work-order:list',1,'menu',1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('200101','2001','查看',NULL,'custom:work-order:list',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('200102','2001','流转状态',NULL,'custom:work-order:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('2002','20','组件库存','custom/component-stock','custom:stock:list',1,'databases',2);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('200201','2002','修改库存',NULL,'custom:stock:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('2003','20','定制配置','custom/configuration','custom:config:list',1,'setting',3);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('200301','2003','查看',NULL,'custom:config:list',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('200302','2003','保存配置',NULL,'custom:config:update',2,NULL,0);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('9300','0','社区管理','community/posts','community:post:list',1,'menu',45);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('930001','9300','审核与下架',NULL,'community:post:moderate',2,NULL,1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('9400','0','商家服务','merchant/contact','merchant:contact:read',1,'menu',46);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('940001','9400','维护客服资料',NULL,'merchant:contact:update',2,NULL,1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('9500','0','协议与隐私','merchant/legal','merchant:legal:read',1,'menu',47);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('950001','9500','编辑协议草稿',NULL,'merchant:legal:update',2,NULL,1);
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES ('950002','9500','发布协议版本',NULL,'merchant:legal:publish',2,NULL,2);

INSERT INTO `SYS_USER` (`USER_ID`, `USER_NAME`, `REAL_NAME`, `SEX`, `ORG_NO`, `SALT`, `EMAIL_HOST`, `EMAIL`, `EMAIL_PW`, `MOBILE`, `STATUS`, `PASSWORD`, `CREATE_USER_ID`, `CREATE_USER_ORG_NO`, `CREATE_TIME`) VALUES ('1','admin','何海龙',1,'01','YzcmCZNvbXocrsz9dm8e','smtp.qq.com','939961241@qq.com','','15209831990',1,'9ec9750e709431dad22365cabc5c625482e574c74adaebba7dd02f1129e4ce1d','',NULL,'2016-11-11 11:11:11');

INSERT INTO `mall_ad` (`id`, `ad_position_id`, `media_type`, `name`, `link`, `image_url`, `content`, `end_time`, `enabled`) VALUES (9000,1,1,'克拉匠造珠宝设计','/pages/designer/designer','/static/images/demo/product-pearl-necklace-diy.webp','项目演示, 商家正式发布前请替换.',NULL,1);

INSERT INTO `mall_category` (`id`, `name`, `keywords`, `front_desc`, `parent_id`, `sort_order`, `show_index`, `is_show`, `banner_url`, `icon_url`, `img_url`, `wap_banner_url`, `level`, `type`, `front_name`) VALUES (1999000,'珠宝成品演示','演示,珠宝,定制','商家维护的珠宝成品与 DIY 演示数据。',0,50,0,1,'',NULL,NULL,NULL,NULL,0,NULL);
INSERT INTO `mall_category` (`id`, `name`, `keywords`, `front_desc`, `parent_id`, `sort_order`, `show_index`, `is_show`, `banner_url`, `icon_url`, `img_url`, `wap_banner_url`, `level`, `type`, `front_name`) VALUES (1999001,'项链与耳饰','演示,项链,耳饰,DIY','项链、耳钉和耳环成品及定制商品。',1999000,1,1,1,'','','/static/images/demo/product-pearl-necklace-diy.webp',NULL,NULL,0,'项链与耳饰');

INSERT INTO `mall_channel` (`id`, `name`, `url`, `icon_url`, `sort_order`) VALUES (1999000,'珠宝定制','/pages/designer/designer','',1);

INSERT INTO `mall_goods` (`id`, `category_id`, `goods_sn`, `name`, `brand_id`, `goods_number`, `keywords`, `goods_brief`, `goods_desc`, `is_on_sale`, `add_time`, `sort_order`, `is_delete`, `delete_time`, `attribute_category`, `counter_price`, `extra_price`, `is_new`, `goods_unit`, `primary_pic_url`, `list_pic_url`, `retail_price`, `sell_volume`, `primary_product_id`, `unit_price`, `promotion_desc`, `promotion_tag`, `app_exclusive_price`, `is_app_exclusive`, `is_limited`, `is_hot`, `market_price`, `create_user_id`, `update_user_id`, `update_time`, `create_user_dept_id`, `pearl_source`, `jewelry_type`, `pearl_variety`, `pearl_origin`, `pearl_shape`, `pearl_luster`, `pearl_blemish`, `pearl_diameter_min`, `pearl_diameter_max`, `video_media_id`, `video_url`) VALUES (1999105,1999001,'DEMO-DIY-JWL-001','演示 · 珍珠项链 DIY',0,100,'演示,DIY,珍珠,项链,耳钉,耳环,18K金,铂金,钻石','选择珍珠与主体款式，可升级贵金属、宝石、长度和连接扣。','<p>珠宝 DIY 演示商品。所有选项均明确标注“演示”，真实内容由商家在管理网页维护。</p>',1,CURRENT_TIMESTAMP,0,0,NULL,0,5999.00,0.00,1,'件','/static/images/demo/product-pearl-necklace-diy.webp','/static/images/demo/product-pearl-necklace-diy.webp',399.00,0,1999205,0.00,NULL,'DEMO_DIY',NULL,NULL,NULL,1,5999.00,NULL,NULL,NULL,NULL,'','','','','','','',NULL,NULL,0,'');
INSERT INTO `mall_goods` (`id`, `category_id`, `goods_sn`, `name`, `brand_id`, `goods_number`, `keywords`, `goods_brief`, `goods_desc`, `is_on_sale`, `add_time`, `sort_order`, `is_delete`, `delete_time`, `attribute_category`, `counter_price`, `extra_price`, `is_new`, `goods_unit`, `primary_pic_url`, `list_pic_url`, `retail_price`, `sell_volume`, `primary_product_id`, `unit_price`, `promotion_desc`, `promotion_tag`, `app_exclusive_price`, `is_app_exclusive`, `is_limited`, `is_hot`, `market_price`, `create_user_id`, `update_user_id`, `update_time`, `create_user_dept_id`, `pearl_source`, `jewelry_type`, `pearl_variety`, `pearl_origin`, `pearl_shape`, `pearl_luster`, `pearl_blemish`, `pearl_diameter_min`, `pearl_diameter_max`, `video_media_id`, `video_url`) VALUES (1999106,1999001,'DEMO-DIY-EAR-001','演示 · 珍珠耳饰 DIY',0,100,'演示,DIY,珍珠,耳钉,耳钩,耳夹,18K金,铂金,钻石','独立选择珍珠、耳饰款式和佩戴结构，可升级贵金属与宝石。','<p>珍珠耳饰 DIY 演示商品。耳饰与项链使用独立模板，真实内容由商家在管理网页维护。</p>',0,CURRENT_TIMESTAMP,1,0,NULL,0,3999.00,0.00,1,'对','/static/images/demo/product-pearl-earrings-diy.webp','/static/images/demo/product-pearl-earrings-diy.webp',299.00,0,1999206,0.00,NULL,'DEMO_DIY',NULL,NULL,NULL,1,3999.00,NULL,NULL,NULL,NULL,'','','','','','','',NULL,NULL,0,'');

INSERT INTO `mall_goods_gallery` (`id`, `goods_id`, `img_url`, `img_desc`, `sort_order`) VALUES (681,1999105,'/static/images/demo/product-pearl-necklace-diy.webp','珍珠项链 DIY',1);

INSERT INTO `mall_product` (`id`, `goods_id`, `goods_specification_ids`, `goods_sn`, `goods_number`, `retail_price`, `market_price`) VALUES (1999205,1999105,'','DEMO-DIY-JWL-001-STD',100,399.00,5999.00);
INSERT INTO `mall_product` (`id`, `goods_id`, `goods_specification_ids`, `goods_sn`, `goods_number`, `retail_price`, `market_price`) VALUES (1999206,1999106,'','DEMO-DIY-EAR-001-STD',100,299.00,3999.00);

INSERT INTO `mall_specification` (`id`, `name`, `sort_order`) VALUES (1,'颜色',1);
INSERT INTO `mall_specification` (`id`, `name`, `sort_order`) VALUES (2,'规格',2);
INSERT INTO `mall_specification` (`id`, `name`, `sort_order`) VALUES (4,'重量',3);



-- 首版演示测试数据: 样例商品、项链DIY演示材料与社区已发布内容, 客户端真机验收前可核对效果.
-- 一、商城: 两个DIY商品补齐珍珠事实字段, 耳饰上架.
UPDATE `mall_goods` SET `pearl_source`='FRESHWATER', `jewelry_type`='NECKLACE', `pearl_variety`='淡水珍珠', `pearl_origin`='浙江诸暨(演示)', `pearl_shape`='近正圆', `pearl_luster`='极光', `pearl_blemish`='微瑕', `pearl_diameter_min`=6.000, `pearl_diameter_max`=11.000 WHERE `id`=1999105;
UPDATE `mall_goods` SET `is_on_sale`=1, `pearl_source`='FRESHWATER', `jewelry_type`='EARRINGS', `pearl_variety`='淡水珍珠', `pearl_origin`='浙江诸暨(演示)', `pearl_shape`='近正圆', `pearl_luster`='强光', `pearl_blemish`='微瑕', `pearl_diameter_min`=7.000, `pearl_diameter_max`=8.000 WHERE `id`=1999106;

-- 二、商城: 新增手链、戒指两个成品分类.
INSERT INTO `mall_category` (`id`, `name`, `keywords`, `front_desc`, `parent_id`, `sort_order`, `show_index`, `is_show`, `banner_url`, `icon_url`, `img_url`, `wap_banner_url`, `level`, `type`, `front_name`) VALUES (1999002,'手链','演示,手链,珍珠','珍珠手链成品演示。',1999000,2,1,1,'','','/static/images/demo/product-freshwater-bracelet.webp',NULL,NULL,0,'手链');
INSERT INTO `mall_category` (`id`, `name`, `keywords`, `front_desc`, `parent_id`, `sort_order`, `show_index`, `is_show`, `banner_url`, `icon_url`, `img_url`, `wap_banner_url`, `level`, `type`, `front_name`) VALUES (1999003,'戒指','演示,戒指,珍珠','珍珠戒指成品演示。',1999000,3,1,1,'','','/static/images/demo/product-akoya-ring.webp',NULL,NULL,0,'戒指');

-- 三、商城: 新增三个样例成品商品(淡水手链、海水戒指、海水项链), 支撑来源与品类组合筛选.
INSERT INTO `mall_goods` (`id`, `category_id`, `goods_sn`, `name`, `brand_id`, `goods_number`, `keywords`, `goods_brief`, `goods_desc`, `is_on_sale`, `add_time`, `sort_order`, `is_delete`, `delete_time`, `attribute_category`, `counter_price`, `extra_price`, `is_new`, `goods_unit`, `primary_pic_url`, `list_pic_url`, `retail_price`, `sell_volume`, `primary_product_id`, `unit_price`, `promotion_desc`, `promotion_tag`, `app_exclusive_price`, `is_app_exclusive`, `is_limited`, `is_hot`, `market_price`, `create_user_id`, `update_user_id`, `update_time`, `create_user_dept_id`, `pearl_source`, `jewelry_type`, `pearl_variety`, `pearl_origin`, `pearl_shape`, `pearl_luster`, `pearl_blemish`, `pearl_diameter_min`, `pearl_diameter_max`, `video_media_id`, `video_url`) VALUES (1999107,1999002,'DEMO-BRC-FRESH-001','演示 · 淡水珍珠手链',0,80,'演示,淡水珍珠,手链','淡水近圆珠成品手链, 演示数据。','<p>淡水珍珠手链演示商品。真实商品、价格和库存由商家在管理网页维护。</p>',1,CURRENT_TIMESTAMP,2,0,NULL,0,1299.00,0.00,1,'件','/static/images/demo/product-freshwater-bracelet.webp','/static/images/demo/product-freshwater-bracelet.webp',259.00,0,1999207,0.00,NULL,'DEMO',NULL,NULL,NULL,0,1299.00,NULL,NULL,NULL,NULL,'FRESHWATER','BRACELET','淡水珍珠','浙江诸暨(演示)','近正圆','强光','微瑕',6.500,7.500,0,'');
INSERT INTO `mall_goods` (`id`, `category_id`, `goods_sn`, `name`, `brand_id`, `goods_number`, `keywords`, `goods_brief`, `goods_desc`, `is_on_sale`, `add_time`, `sort_order`, `is_delete`, `delete_time`, `attribute_category`, `counter_price`, `extra_price`, `is_new`, `goods_unit`, `primary_pic_url`, `list_pic_url`, `retail_price`, `sell_volume`, `primary_product_id`, `unit_price`, `promotion_desc`, `promotion_tag`, `app_exclusive_price`, `is_app_exclusive`, `is_limited`, `is_hot`, `market_price`, `create_user_id`, `update_user_id`, `update_time`, `create_user_dept_id`, `pearl_source`, `jewelry_type`, `pearl_variety`, `pearl_origin`, `pearl_shape`, `pearl_luster`, `pearl_blemish`, `pearl_diameter_min`, `pearl_diameter_max`, `video_media_id`, `video_url`) VALUES (1999108,1999003,'DEMO-RING-AKOYA-001','演示 · Akoya 珍珠戒指',0,25,'演示,海水珍珠,Akoya,戒指','Akoya 海水珠戒指成品, 演示数据。','<p>Akoya 珍珠戒指演示商品。真实商品、价格和库存由商家在管理网页维护。</p>',1,CURRENT_TIMESTAMP,3,0,NULL,0,3499.00,0.00,1,'件','/static/images/demo/product-akoya-ring.webp','/static/images/demo/product-akoya-ring.webp',699.00,0,1999208,0.00,NULL,'DEMO',NULL,NULL,NULL,0,3499.00,NULL,NULL,NULL,NULL,'SEAWATER','RING','Akoya','日本(演示)','近正圆','极光','微瑕',7.000,7.500,0,'');
INSERT INTO `mall_goods` (`id`, `category_id`, `goods_sn`, `name`, `brand_id`, `goods_number`, `keywords`, `goods_brief`, `goods_desc`, `is_on_sale`, `add_time`, `sort_order`, `is_delete`, `delete_time`, `attribute_category`, `counter_price`, `extra_price`, `is_new`, `goods_unit`, `primary_pic_url`, `list_pic_url`, `retail_price`, `sell_volume`, `primary_product_id`, `unit_price`, `promotion_desc`, `promotion_tag`, `app_exclusive_price`, `is_app_exclusive`, `is_limited`, `is_hot`, `market_price`, `create_user_id`, `update_user_id`, `update_time`, `create_user_dept_id`, `pearl_source`, `jewelry_type`, `pearl_variety`, `pearl_origin`, `pearl_shape`, `pearl_luster`, `pearl_blemish`, `pearl_diameter_min`, `pearl_diameter_max`, `video_media_id`, `video_url`) VALUES (1999109,1999001,'DEMO-NCK-AKOYA-001','演示 · Akoya 海水珍珠项链',0,18,'演示,海水珍珠,Akoya,项链','Akoya 海水珠成品项链, 演示数据。','<p>Akoya 海水珍珠项链演示商品。真实商品、价格和库存由商家在管理网页维护。</p>',1,CURRENT_TIMESTAMP,4,0,NULL,0,5999.00,0.00,1,'件','/static/images/demo/product-akoya-necklace.webp','/static/images/demo/product-akoya-necklace.webp',1299.00,0,1999209,0.00,NULL,'DEMO',NULL,NULL,NULL,1,5999.00,NULL,NULL,NULL,NULL,'SEAWATER','NECKLACE','Akoya','日本(演示)','近正圆','极光','微瑕',7.000,7.500,0,'');

-- 四、商城: 新商品默认SKU与图库.
INSERT INTO `mall_product` (`id`, `goods_id`, `goods_specification_ids`, `goods_sn`, `goods_number`, `retail_price`, `market_price`) VALUES (1999207,1999107,'','DEMO-BRC-FRESH-001-STD',80,259.00,1299.00);
INSERT INTO `mall_product` (`id`, `goods_id`, `goods_specification_ids`, `goods_sn`, `goods_number`, `retail_price`, `market_price`) VALUES (1999208,1999108,'','DEMO-RING-AKOYA-001-STD',25,699.00,3499.00);
INSERT INTO `mall_product` (`id`, `goods_id`, `goods_specification_ids`, `goods_sn`, `goods_number`, `retail_price`, `market_price`) VALUES (1999209,1999109,'','DEMO-NCK-AKOYA-001-STD',18,1299.00,5999.00);
INSERT INTO `mall_goods_gallery` (`id`, `goods_id`, `img_url`, `img_desc`, `sort_order`) VALUES (682,1999106,'/static/images/demo/product-pearl-earrings-diy.webp','珍珠耳饰 DIY',1);
INSERT INTO `mall_goods_gallery` (`id`, `goods_id`, `img_url`, `img_desc`, `sort_order`) VALUES (683,1999107,'/static/images/demo/product-freshwater-bracelet.webp','淡水珍珠手链',1);
INSERT INTO `mall_goods_gallery` (`id`, `goods_id`, `img_url`, `img_desc`, `sort_order`) VALUES (684,1999108,'/static/images/demo/product-akoya-ring.webp','Akoya 珍珠戒指',1);
INSERT INTO `mall_goods_gallery` (`id`, `goods_id`, `img_url`, `img_desc`, `sort_order`) VALUES (685,1999109,'/static/images/demo/product-akoya-necklace.webp','Akoya 海水珍珠项链',1);

-- 五、DIY: 补齐演示珍珠的实测直径与品质参数, 未知尺寸不参与报价.
UPDATE `CC_COMPONENT_OPTION` SET `material_params`='{"stockQuantity":1,"stockUnit":"颗","diameterMm":6.5,"shape":"近正圆","luster":"强光","blemish":"微瑕"}' WHERE `id`=33;
UPDATE `CC_COMPONENT_OPTION` SET `material_params`='{"stockQuantity":1,"stockUnit":"颗","diameterMm":7.5,"shape":"近正圆","luster":"极光","blemish":"微瑕"}' WHERE `id`=34;
UPDATE `CC_COMPONENT_OPTION` SET `material_params`='{"stockQuantity":1,"stockUnit":"颗","diameterMm":10.5,"shape":"近正圆","luster":"极光","blemish":"微瑕"}' WHERE `id`=35;
UPDATE `CC_COMPONENT_OPTION` SET `material_params`='{"stockQuantity":1,"stockUnit":"颗","diameterMm":10,"shape":"巴洛克","luster":"强光","blemish":"微瑕"}' WHERE `id`=36;
UPDATE `CC_COMPONENT_OPTION` SET `material_params`='{"stockQuantity":1,"stockUnit":"颗","diameterMm":7.2,"shape":"近正圆","luster":"极光","blemish":"微瑕"}' WHERE `id`=37;
UPDATE `CC_COMPONENT_OPTION` SET `material_params`='{"stockQuantity":1,"stockUnit":"颗","diameterMm":8.2,"shape":"正圆","luster":"极光","blemish":"极微瑕"}' WHERE `id`=38;
UPDATE `CC_COMPONENT_OPTION` SET `material_params`='{"stockQuantity":1,"stockUnit":"颗","diameterMm":9.5,"shape":"近正圆","luster":"极光","blemish":"微瑕"}' WHERE `id`=39;
UPDATE `CC_COMPONENT_OPTION` SET `material_params`='{"stockQuantity":1,"stockUnit":"颗","diameterMm":10.5,"shape":"近正圆","luster":"强光","blemish":"微瑕"}' WHERE `id`=40;

-- 六、DIY: 新增演示尾扣、链条和配饰, 配饰明确串接或悬挂计长.
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (65,1999105,'clasp','demo-clasp-s925-lobster','演示 · S925 龙虾扣',80.00,'/static/images/designer/layers/chain-white-gold.webp',NULL,'{"stockQuantity":1,"stockUnit":"件","spanMm":10}',67,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (66,1999105,'clasp','demo-clasp-18k-spring','演示 · 18K 金弹簧扣',260.00,'/static/images/designer/layers/chain-yellow-gold.webp',NULL,'{"stockQuantity":1,"stockUnit":"件","spanMm":8}',67,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (70,1999105,'accessory','demo-acc-pearl-spacer','演示 · 珍珠隔珠 · 3mm',45.00,'/static/images/designer/layers/pearl-akoya-white.webp',NULL,'{"stockQuantity":1,"stockUnit":"颗","spanMm":3,"lengthRole":"INLINE"}',71,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (71,1999105,'accessory','demo-acc-star-pendant','演示 · 星形吊坠',90.00,'/static/images/designer/layers/gem-diamond.webp',NULL,'{"stockQuantity":1,"stockUnit":"件","spanMm":5,"lengthRole":"HANGING"}',71,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (67,1999105,'chain','demo-chain-s925-45','演示 · S925 银链 · 45cm',120.00,'/static/images/designer/layers/chain-white-gold.webp',NULL,'{"stockQuantity":1,"stockUnit":"条","lengthMm":450,"chainStyle":"O字链"}',85,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (68,1999105,'chain','demo-chain-18ky-45','演示 · 18K 黄金链 · 45cm',680.00,'/static/images/designer/layers/chain-yellow-gold.webp',NULL,'{"stockQuantity":1,"stockUnit":"条","lengthMm":450,"chainStyle":"O字链"}',85,1);
INSERT INTO `CC_COMPONENT_OPTION` (`id`, `goods_id`, `component_type`, `option_code`, `name`, `price_delta`, `asset_url`, `model_url`, `material_params`, `z_index`, `enabled`) VALUES (69,1999105,'chain','demo-chain-18kw-40','演示 · 18K 白金链 · 40cm',720.00,'/static/images/designer/layers/chain-white-gold.webp',NULL,'{"stockQuantity":1,"stockUnit":"条","lengthMm":400,"chainStyle":"O字链"}',85,1);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (65,1999105,'clasp','demo-clasp-s925-lobster',50,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (66,1999105,'clasp','demo-clasp-18k-spring',30,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (70,1999105,'accessory','demo-acc-pearl-spacer',100,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (71,1999105,'accessory','demo-acc-star-pendant',25,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (67,1999105,'chain','demo-chain-s925-45',40,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (68,1999105,'chain','demo-chain-18ky-45',20,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMPONENT_STOCK` (`id`, `goods_id`, `component_type`, `option_code`, `stock`, `update_time`) VALUES (69,1999105,'chain','demo-chain-18kw-40',15,CURRENT_TIMESTAMP);

-- 七、DIY: 开放项链模板, 配置商家工艺长度规则(OVERLAY_MAX模式).
UPDATE `CC_CUSTOM_PRODUCT` SET `enabled`=1, `length_rules`='{"mode":"OVERLAY_MAX","claspMm":10,"connectionMm":2,"lossMm":1,"toleranceMm":5,"minLengthMm":350,"maxLengthMm":600}' WHERE `id`=2;

-- 八、社区: 演示用户与已审核发布的测试内容.
INSERT INTO `WX_USER` (`ID`, `MA_OPENID`, `NICKNAME`, `SEX`, `HEAD_IMG_URL`, `SUBSCRIBE`, `REGISTER_TIME`) VALUES (900001,'demo-seed-user-openid-000001','克拉匠造演示号',0,'/static/images/demo/product-pearl-necklace-diy.webp',0,CURRENT_TIMESTAMP);
INSERT INTO `CC_COMMUNITY_POST` (`id`, `user_id`, `request_key`, `request_hash`, `kind`, `title`, `content`, `images_json`, `design_json`, `status`, `create_time`) VALUES (900001,900001,'demo-seed-post-00001',SHA2('demo-seed-post-00001',256),'DESIGN','淡水白珠锁骨链 · 设计分享','用 S925 银链配 6–7mm 淡水白珠, 中间加一颗隔珠, 日常佩戴很清爽。演示内容, 供大家参考排列思路。','["/static/images/demo/product-pearl-necklace-diy.webp"]','{"arrangement":[{"position":0,"componentType":"chain","name":"演示 · S925 银链 · 45cm","sizeMm":450},{"position":1,"componentType":"pearl","name":"演示 · 淡水白珠 · 日常级 · 6–7mm","sizeMm":6.5,"shape":"近正圆","luster":"强光","blemish":"微瑕"},{"position":2,"componentType":"accessory","name":"演示 · 珍珠隔珠 · 3mm","sizeMm":3,"lengthRole":"INLINE"},{"position":3,"componentType":"pearl","name":"演示 · 淡水白珠 · 日常级 · 6–7mm","sizeMm":6.5,"shape":"近正圆","luster":"强光","blemish":"微瑕"},{"position":4,"componentType":"clasp","name":"演示 · S925 龙虾扣","sizeMm":10}]}','PUBLISHED','2026-11-17 09:30:00');
INSERT INTO `CC_COMMUNITY_POST` (`id`, `user_id`, `request_key`, `request_hash`, `kind`, `title`, `content`, `images_json`, `design_json`, `status`, `create_time`) VALUES (900002,900001,'demo-seed-post-00002',SHA2('demo-seed-post-00002',256),'DESIGN','Akoya 单珠金链 · 设计分享','18K 黄金链配一颗 8–8.5mm 珠宝级 Akoya, 单珠就很提气质。演示内容, 欢迎点赞。','["/static/images/demo/product-akoya-necklace.webp"]','{"arrangement":[{"position":0,"componentType":"chain","name":"演示 · 18K 黄金链 · 45cm","sizeMm":450},{"position":1,"componentType":"pearl","name":"演示 · Akoya 海水珠 · 珠宝级 · 8–8.5mm","sizeMm":8.2,"shape":"正圆","luster":"极光","blemish":"极微瑕"},{"position":2,"componentType":"clasp","name":"演示 · 18K 金弹簧扣","sizeMm":8}]}','PUBLISHED','2026-11-17 10:05:00');
INSERT INTO `CC_COMMUNITY_POST` (`id`, `user_id`, `request_key`, `request_hash`, `kind`, `title`, `content`, `images_json`, `design_json`, `status`, `create_time`) VALUES (900003,900001,'demo-seed-post-00003',SHA2('demo-seed-post-00003',256),'FINISHED','玫瑰金心形款成品展示','定制的心形主题项链做好了, 玫瑰金很衬肤色。演示内容, 成品以商家实际制作为准。','["/static/images/demo/product-freshwater-bracelet.webp"]',NULL,'PUBLISHED','2026-11-17 11:20:00');
INSERT INTO `CC_COMMUNITY_POST` (`id`, `user_id`, `request_key`, `request_hash`, `kind`, `title`, `content`, `images_json`, `design_json`, `status`, `create_time`) VALUES (900004,900001,'demo-seed-post-00004',SHA2('demo-seed-post-00004',256),'FINISHED','星座主题项链成品展示','星座主题成品, 配深色吊坠很出效果。演示内容, 成品以商家实际制作为准。','["/static/images/demo/product-akoya-ring.webp"]',NULL,'PUBLISHED','2026-11-17 13:15:00');
INSERT INTO `CC_COMMUNITY_POST` (`id`, `user_id`, `request_key`, `request_hash`, `kind`, `title`, `content`, `images_json`, `design_json`, `status`, `create_time`) VALUES (900005,900001,'demo-seed-post-00005',SHA2('demo-seed-post-00005',256),'SUGGESTION','希望手链也能 DIY','项链 DIY 体验不错, 希望后续能出手链的定制模板, 可以按手腕围选长度。演示建议内容。','[]',NULL,'PUBLISHED','2026-11-17 14:00:00');
INSERT INTO `CC_COMMUNITY_LIKE` (`post_id`, `user_id`, `create_time`) VALUES (900001,900001,'2026-11-17 14:05:00');
INSERT INTO `CC_COMMUNITY_LIKE` (`post_id`, `user_id`, `create_time`) VALUES (900002,900001,'2026-11-17 14:06:00');
INSERT INTO `CC_COMMUNITY_LIKE` (`post_id`, `user_id`, `create_time`) VALUES (900003,900001,'2026-11-17 14:07:00');

COMMIT;
SET FOREIGN_KEY_CHECKS=1;

-- 岗位模板复用现有角色和菜单关联. 模板默认无机构范围, 无员工绑定.
INSERT INTO `SYS_MENU` (`MENU_ID`,`PARENT_ID`,`NAME`,`URL`,`PERMS`,`TYPE`,`ICON`,`ORDER_NUM`) VALUES ('1802','18','退款管理','mall/refund','mall:refund:list',1,'orders',2);
INSERT INTO `SYS_MENU` (`MENU_ID`,`PARENT_ID`,`NAME`,`URL`,`PERMS`,`TYPE`,`ICON`,`ORDER_NUM`) VALUES ('180201','1802','审核退款',NULL,'mall:refund:review',2,NULL,1);
INSERT INTO `SYS_MENU` (`MENU_ID`,`PARENT_ID`,`NAME`,`URL`,`PERMS`,`TYPE`,`ICON`,`ORDER_NUM`) VALUES ('1803','18','售后服务','mall/aftersale','mall:aftersale:list',1,'orders',3);
INSERT INTO `SYS_MENU` (`MENU_ID`,`PARENT_ID`,`NAME`,`URL`,`PERMS`,`TYPE`,`ICON`,`ORDER_NUM`) VALUES ('180301','1803','登记售后',NULL,'mall:aftersale:register',2,NULL,1);
INSERT INTO `SYS_MENU` (`MENU_ID`,`PARENT_ID`,`NAME`,`URL`,`PERMS`,`TYPE`,`ICON`,`ORDER_NUM`) VALUES ('180302','1803','处理售后',NULL,'mall:aftersale:handle',2,NULL,2);
INSERT INTO `SYS_MENU` (`MENU_ID`,`PARENT_ID`,`NAME`,`URL`,`PERMS`,`TYPE`,`ICON`,`ORDER_NUM`) VALUES ('180108','1801','支付前改价',NULL,'mall:order:price:update',2,NULL,5);
INSERT INTO `SYS_MENU` (`MENU_ID`,`PARENT_ID`,`NAME`,`URL`,`PERMS`,`TYPE`,`ICON`,`ORDER_NUM`) VALUES ('140602','1406','回复并处理反馈',NULL,'mall:feedback:update',2,NULL,2);
INSERT INTO `SYS_MENU` (`MENU_ID`,`PARENT_ID`,`NAME`,`URL`,`PERMS`,`TYPE`,`ICON`,`ORDER_NUM`) VALUES ('110106','1101','分配员工角色',NULL,'sys:user:assign',2,NULL,6);
INSERT INTO SYS_ROLE (ROLE_ID,ROLE_NAME,REMARK,DELEGABLE,CREATE_USER_ID,CREATE_TIME) VALUES ('cc-job-goods','商品运营','岗位模板，分配前确认数据机构',0,'1',CURRENT_TIMESTAMP);
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-goods','17');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-goods','1701');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-goods','170101');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-goods','170102');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-goods','20');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-goods','2002');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-goods','200201');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-goods','2003');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-goods','200302');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-goods','15');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-goods','1501');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-goods','1502');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-goods','1508');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-goods','1512');
INSERT INTO SYS_ROLE (ROLE_ID,ROLE_NAME,REMARK,DELEGABLE,CREATE_USER_ID,CREATE_TIME) VALUES ('cc-job-fulfillment','履约／制作','岗位模板，分配前确认数据机构',0,'1',CURRENT_TIMESTAMP);
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-fulfillment','18');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-fulfillment','1801');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-fulfillment','180104');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-fulfillment','20');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-fulfillment','2001');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-fulfillment','200102');
INSERT INTO SYS_ROLE (ROLE_ID,ROLE_NAME,REMARK,DELEGABLE,CREATE_USER_ID,CREATE_TIME) VALUES ('cc-job-support','客服／售后','岗位模板，分配前确认数据机构',0,'1',CURRENT_TIMESTAMP);
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-support','18');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-support','1803');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-support','180301');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-support','180302');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-support','14');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-support','1406');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-support','140602');
INSERT INTO SYS_ROLE (ROLE_ID,ROLE_NAME,REMARK,DELEGABLE,CREATE_USER_ID,CREATE_TIME) VALUES ('cc-job-refunds','退款负责人','岗位模板，分配前确认数据机构',0,'1',CURRENT_TIMESTAMP);
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-refunds','18');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-refunds','1802');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-refunds','180201');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-refunds','180105');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-refunds','180106');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-refunds','180107');
INSERT INTO SYS_ROLE (ROLE_ID,ROLE_NAME,REMARK,DELEGABLE,CREATE_USER_ID,CREATE_TIME) VALUES ('cc-job-community','社区运营','岗位模板，分配前确认数据机构',0,'1',CURRENT_TIMESTAMP);
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-community','9300');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-community','930001');
INSERT INTO SYS_ROLE (ROLE_ID,ROLE_NAME,REMARK,DELEGABLE,CREATE_USER_ID,CREATE_TIME) VALUES ('cc-job-system','系统管理员','岗位模板，分配前确认数据机构',0,'1',CURRENT_TIMESTAMP);
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','10');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','1001');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','100102');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','1003');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','1008');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','11');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','1101');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','110101');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','110102');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','110103');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','110106');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','1102');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','110201');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','110202');
INSERT INTO SYS_ROLE_MENU (ID,ROLE_ID,MENU_ID) VALUES (REPLACE(UUID(),'-',''),'cc-job-system','110203');
