SET NAMES utf8mb4;

-- 由tools/database/build-customer-material-catalog.py生成, 候选原文见同源JSON.
-- 不猜测旧类型归属, 保留原选项、库存和订单快照; 受影响模板须商家重新配置.
START TRANSACTION;

UPDATE CC_CUSTOM_PRODUCT p SET p.enabled=0, p.required_component_types='pearl,clasp,chain'
WHERE (p.required_component_types IS NOT NULL AND p.required_component_types<>''
    AND p.required_component_types NOT REGEXP '^(pearl|clasp|accessory|chain)(,(pearl|clasp|accessory|chain))*$')
OR EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=p.goods_id
    AND o.enabled=1 AND o.component_type NOT IN ('pearl','clasp','accessory','chain'));

UPDATE CC_COMPONENT_OPTION SET enabled=0
WHERE component_type NOT IN ('pearl','clasp','accessory','chain');

-- 不覆盖商家同代码配置; 仅匹配项目明确的项链演示货号和模板.
INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-southsea-white-8-9','澳白 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"澳白","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',0,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-southsea-white-8-9');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-southsea-white-8-9'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-southsea-white-9-10','澳白 9-10mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"澳白","candidateDiameterMinMm":9,"candidateDiameterMaxMm":10,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',1,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-southsea-white-9-10');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-southsea-white-9-10'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-southsea-white-10-11','澳白 10-11mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"澳白","candidateDiameterMinMm":10,"candidateDiameterMaxMm":11,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',2,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-southsea-white-10-11');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-southsea-white-10-11'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-southsea-white-11-12','澳白 11-12mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"澳白","candidateDiameterMinMm":11,"candidateDiameterMaxMm":12,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',3,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-southsea-white-11-12');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-southsea-white-11-12'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-southsea-white-12-13','澳白 12-13mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"澳白","candidateDiameterMinMm":12,"candidateDiameterMaxMm":13,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',4,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-southsea-white-12-13');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-southsea-white-12-13'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-southsea-white-13-14','澳白 13-14mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"澳白","candidateDiameterMinMm":13,"candidateDiameterMaxMm":14,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',5,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-southsea-white-13-14');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-southsea-white-13-14'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-southsea-gold-8-9','金珠 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"金珠","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',6,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-southsea-gold-8-9');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-southsea-gold-8-9'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-southsea-gold-9-10','金珠 9-10mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"金珠","candidateDiameterMinMm":9,"candidateDiameterMaxMm":10,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',7,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-southsea-gold-9-10');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-southsea-gold-9-10'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-southsea-gold-10-11','金珠 10-11mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"金珠","candidateDiameterMinMm":10,"candidateDiameterMaxMm":11,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',8,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-southsea-gold-10-11');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-southsea-gold-10-11'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-southsea-gold-11-12','金珠 11-12mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"金珠","candidateDiameterMinMm":11,"candidateDiameterMaxMm":12,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',9,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-southsea-gold-11-12');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-southsea-gold-11-12'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-southsea-gold-12-13','金珠 12-13mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"金珠","candidateDiameterMinMm":12,"candidateDiameterMaxMm":13,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',10,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-southsea-gold-12-13');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-southsea-gold-12-13'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-southsea-gold-13-14','金珠 13-14mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"金珠","candidateDiameterMinMm":13,"candidateDiameterMaxMm":14,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',11,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-southsea-gold-13-14');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-southsea-gold-13-14'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-tahiti-8-9','大溪地 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"大溪地","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',12,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-tahiti-8-9');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-tahiti-8-9'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-tahiti-9-10','大溪地 9-10mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"大溪地","candidateDiameterMinMm":9,"candidateDiameterMaxMm":10,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',13,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-tahiti-9-10');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-tahiti-9-10'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-tahiti-10-11','大溪地 10-11mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"大溪地","candidateDiameterMinMm":10,"candidateDiameterMaxMm":11,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',14,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-tahiti-10-11');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-tahiti-10-11'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-tahiti-11-12','大溪地 11-12mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"大溪地","candidateDiameterMinMm":11,"candidateDiameterMaxMm":12,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',15,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-tahiti-11-12');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-tahiti-11-12'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-tahiti-12-13','大溪地 12-13mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"大溪地","candidateDiameterMinMm":12,"candidateDiameterMaxMm":13,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',16,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-tahiti-12-13');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-tahiti-12-13'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-tahiti-13-14','大溪地 13-14mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"大溪地","candidateDiameterMinMm":13,"candidateDiameterMaxMm":14,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',17,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-tahiti-13-14');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-tahiti-13-14'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-white-4-5','Akoya白色 4-5mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya白色","candidateDiameterMinMm":4,"candidateDiameterMaxMm":5,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',18,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-white-4-5');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-white-4-5'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-white-5-6','Akoya白色 5-6mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya白色","candidateDiameterMinMm":5,"candidateDiameterMaxMm":6,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',19,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-white-5-6');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-white-5-6'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-white-6-7','Akoya白色 6-7mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya白色","candidateDiameterMinMm":6,"candidateDiameterMaxMm":7,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',20,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-white-6-7');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-white-6-7'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-white-7-8','Akoya白色 7-8mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya白色","candidateDiameterMinMm":7,"candidateDiameterMaxMm":8,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',21,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-white-7-8');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-white-7-8'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-white-8-9','Akoya白色 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya白色","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',22,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-white-8-9');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-white-8-9'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-madama-4-5','Akoya真多麻 4-5mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya真多麻","candidateDiameterMinMm":4,"candidateDiameterMaxMm":5,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',23,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-madama-4-5');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-madama-4-5'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-madama-5-6','Akoya真多麻 5-6mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya真多麻","candidateDiameterMinMm":5,"candidateDiameterMaxMm":6,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',24,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-madama-5-6');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-madama-5-6'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-madama-6-7','Akoya真多麻 6-7mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya真多麻","candidateDiameterMinMm":6,"candidateDiameterMaxMm":7,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',25,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-madama-6-7');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-madama-6-7'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-madama-7-8','Akoya真多麻 7-8mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya真多麻","candidateDiameterMinMm":7,"candidateDiameterMaxMm":8,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',26,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-madama-7-8');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-madama-7-8'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-madama-8-9','Akoya真多麻 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya真多麻","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',27,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-madama-8-9');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-madama-8-9'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-gold-4-5','Akoya羽黄金 4-5mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya羽黄金","candidateDiameterMinMm":4,"candidateDiameterMaxMm":5,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',28,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-gold-4-5');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-gold-4-5'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-gold-5-6','Akoya羽黄金 5-6mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya羽黄金","candidateDiameterMinMm":5,"candidateDiameterMaxMm":6,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',29,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-gold-5-6');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-gold-5-6'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-gold-6-7','Akoya羽黄金 6-7mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya羽黄金","candidateDiameterMinMm":6,"candidateDiameterMaxMm":7,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',30,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-gold-6-7');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-gold-6-7'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-gold-7-8','Akoya羽黄金 7-8mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya羽黄金","candidateDiameterMinMm":7,"candidateDiameterMaxMm":8,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',31,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-gold-7-8');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-gold-7-8'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-akoya-gold-8-9','Akoya羽黄金 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya羽黄金","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',32,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-akoya-gold-8-9');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-akoya-gold-8-9'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-ak-4-5','淡水AK 4-5mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水AK","candidateDiameterMinMm":4,"candidateDiameterMaxMm":5,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',33,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-ak-4-5');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-ak-4-5'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-ak-5-6','淡水AK 5-6mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水AK","candidateDiameterMinMm":5,"candidateDiameterMaxMm":6,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',34,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-ak-5-6');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-ak-5-6'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-ak-6-7','淡水AK 6-7mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水AK","candidateDiameterMinMm":6,"candidateDiameterMaxMm":7,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',35,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-ak-6-7');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-ak-6-7'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-ak-7-8','淡水AK 7-8mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水AK","candidateDiameterMinMm":7,"candidateDiameterMaxMm":8,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',36,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-ak-7-8');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-ak-7-8'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-edison-9-10','爱迪生 9-10mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"爱迪生","candidateDiameterMinMm":9,"candidateDiameterMaxMm":10,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',37,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-edison-9-10');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-edison-9-10'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-edison-10-11','爱迪生 10-11mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"爱迪生","candidateDiameterMinMm":10,"candidateDiameterMaxMm":11,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',38,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-edison-10-11');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-edison-10-11'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-edison-11-12','爱迪生 11-12mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"爱迪生","candidateDiameterMinMm":11,"candidateDiameterMaxMm":12,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',39,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-edison-11-12');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-edison-11-12'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-edison-12-13','爱迪生 12-13mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"爱迪生","candidateDiameterMinMm":12,"candidateDiameterMaxMm":13,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',40,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-edison-12-13');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-edison-12-13'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-edison-13-14','爱迪生 13-14mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"爱迪生","candidateDiameterMinMm":13,"candidateDiameterMaxMm":14,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',41,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-edison-13-14');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-edison-13-14'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-white-3-4','淡水无核白 3-4mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":3,"candidateDiameterMaxMm":4,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',42,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-white-3-4');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-white-3-4'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-white-4-5','淡水无核白 4-5mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":4,"candidateDiameterMaxMm":5,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',43,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-white-4-5');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-white-4-5'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-white-5-6','淡水无核白 5-6mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":5,"candidateDiameterMaxMm":6,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',44,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-white-5-6');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-white-5-6'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-white-6-7','淡水无核白 6-7mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":6,"candidateDiameterMaxMm":7,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',45,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-white-6-7');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-white-6-7'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-white-7-8','淡水无核白 7-8mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":7,"candidateDiameterMaxMm":8,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',46,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-white-7-8');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-white-7-8'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-white-8-9','淡水无核白 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',47,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-white-8-9');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-white-8-9'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-white-9-10','淡水无核白 9-10mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":9,"candidateDiameterMaxMm":10,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',48,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-white-9-10');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-white-9-10'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-white-10-11','淡水无核白 10-11mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":10,"candidateDiameterMaxMm":11,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',49,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-white-10-11');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-white-10-11'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-purple-3-4','淡水无核紫 3-4mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":3,"candidateDiameterMaxMm":4,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',50,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-3-4');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-3-4'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-purple-4-5','淡水无核紫 4-5mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":4,"candidateDiameterMaxMm":5,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',51,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-4-5');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-4-5'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-purple-5-6','淡水无核紫 5-6mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":5,"candidateDiameterMaxMm":6,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',52,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-5-6');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-5-6'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-purple-6-7','淡水无核紫 6-7mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":6,"candidateDiameterMaxMm":7,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',53,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-6-7');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-6-7'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-purple-7-8','淡水无核紫 7-8mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":7,"candidateDiameterMaxMm":8,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',54,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-7-8');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-7-8'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-purple-8-9','淡水无核紫 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',55,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-8-9');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-8-9'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-purple-9-10','淡水无核紫 9-10mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":9,"candidateDiameterMaxMm":10,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',56,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-9-10');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-9-10'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-purple-10-11','淡水无核紫 10-11mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":10,"candidateDiameterMaxMm":11,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',57,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-10-11');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-purple-10-11'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-orange-3-4','淡水无核橙 3-4mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":3,"candidateDiameterMaxMm":4,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',58,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-3-4');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-3-4'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-orange-4-5','淡水无核橙 4-5mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":4,"candidateDiameterMaxMm":5,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',59,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-4-5');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-4-5'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-orange-5-6','淡水无核橙 5-6mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":5,"candidateDiameterMaxMm":6,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',60,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-5-6');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-5-6'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-orange-6-7','淡水无核橙 6-7mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":6,"candidateDiameterMaxMm":7,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',61,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-6-7');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-6-7'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-orange-7-8','淡水无核橙 7-8mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":7,"candidateDiameterMaxMm":8,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',62,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-7-8');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-7-8'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-orange-8-9','淡水无核橙 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',63,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-8-9');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-8-9'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-orange-9-10','淡水无核橙 9-10mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":9,"candidateDiameterMaxMm":10,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',64,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-9-10');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-9-10'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-freshwater-orange-10-11','淡水无核橙 10-11mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":10,"candidateDiameterMaxMm":11,"shape":"近正圆","luster":"极光","blemish":"微瑕"}',65,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-10-11');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-freshwater-orange-10-11'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'pearl','client-baroque','巴洛克, 实物规格待录入',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"巴洛克","shape":"待实测","luster":"极光","blemish":"微瑕"}',66,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='pearl' AND o.option_code='client-baroque');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='pearl' AND o.option_code='client-baroque'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'clasp','client-extension','延长链',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',67,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='clasp' AND o.option_code='client-extension');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='clasp' AND o.option_code='client-extension'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'clasp','client-ingot','元宝扣',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',68,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='clasp' AND o.option_code='client-ingot');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='clasp' AND o.option_code='client-ingot'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'clasp','client-magnetic','磁吸扣',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',69,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='clasp' AND o.option_code='client-magnetic');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='clasp' AND o.option_code='client-magnetic'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'clasp','client-hug','抱抱扣',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',70,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='clasp' AND o.option_code='client-hug');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='clasp' AND o.option_code='client-hug'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'accessory','client-accessory-01','万能扣',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"provisionalName":true}',71,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='accessory' AND o.option_code='client-accessory-01');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='accessory' AND o.option_code='client-accessory-01'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'accessory','client-accessory-02','戴妃皇冠',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"provisionalName":true}',72,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='accessory' AND o.option_code='client-accessory-02');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='accessory' AND o.option_code='client-accessory-02'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'accessory','client-accessory-03','小睫毛',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"provisionalName":true}',73,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='accessory' AND o.option_code='client-accessory-03');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='accessory' AND o.option_code='client-accessory-03'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'accessory','client-accessory-04','大睫毛',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"provisionalName":true}',74,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='accessory' AND o.option_code='client-accessory-04');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='accessory' AND o.option_code='client-accessory-04'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'accessory','client-accessory-05','三钻',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"provisionalName":true}',75,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='accessory' AND o.option_code='client-accessory-05');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='accessory' AND o.option_code='client-accessory-05'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'accessory','client-accessory-06','奶嘴',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"provisionalName":true}',76,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='accessory' AND o.option_code='client-accessory-06');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='accessory' AND o.option_code='client-accessory-06'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'accessory','client-accessory-07','四叶草',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"provisionalName":true}',77,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='accessory' AND o.option_code='client-accessory-07');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='accessory' AND o.option_code='client-accessory-07'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'accessory','client-accessory-08','瓜子扣',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"provisionalName":true}',78,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='accessory' AND o.option_code='client-accessory-08');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='accessory' AND o.option_code='client-accessory-08'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'accessory','client-accessory-09','满钻瓜子扣',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"provisionalName":true}',79,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='accessory' AND o.option_code='client-accessory-09');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='accessory' AND o.option_code='client-accessory-09'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'accessory','client-accessory-10','路路通',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"provisionalName":true}',80,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='accessory' AND o.option_code='client-accessory-10');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='accessory' AND o.option_code='client-accessory-10'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'accessory','client-accessory-11','大冰雪',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"provisionalName":true}',81,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='accessory' AND o.option_code='client-accessory-11');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='accessory' AND o.option_code='client-accessory-11'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'accessory','client-accessory-12','小冰雪',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"provisionalName":true}',82,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='accessory' AND o.option_code='client-accessory-12');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='accessory' AND o.option_code='client-accessory-12'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'accessory','client-accessory-13','方糖',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"provisionalName":true}',83,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='accessory' AND o.option_code='client-accessory-13');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='accessory' AND o.option_code='client-accessory-13'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'accessory','client-accessory-14','皇冠',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"provisionalName":true}',84,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='accessory' AND o.option_code='client-accessory-14');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='accessory' AND o.option_code='client-accessory-14'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-steel-wire','钢丝',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"钢丝"}',85,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-steel-wire');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-steel-wire'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-cotton','棉线',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"棉线"}',86,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-cotton');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-cotton'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-o-alloy','o字链 / 合金',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"o字链","material":"合金"}',87,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-o-alloy');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-o-alloy'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-o-s925-yellow','o字链 / S925纯银镀金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"o字链","material":"S925纯银镀金黄"}',88,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-o-s925-yellow');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-o-s925-yellow'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-o-s925-white','o字链 / S925纯银镀金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"o字链","material":"S925纯银镀金白"}',89,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-o-s925-white');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-o-s925-white'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-o-18k-yellow','o字链 / 18K金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"o字链","material":"18K金黄"}',90,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-o-18k-yellow');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-o-18k-yellow'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-o-18k-white','o字链 / 18K金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"o字链","material":"18K金白"}',91,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-o-18k-white');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-o-18k-white'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-chopin-alloy','肖邦链 / 合金',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"肖邦链","material":"合金"}',92,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-chopin-alloy');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-chopin-alloy'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-chopin-s925-yellow','肖邦链 / S925纯银镀金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"肖邦链","material":"S925纯银镀金黄"}',93,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-chopin-s925-yellow');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-chopin-s925-yellow'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-chopin-s925-white','肖邦链 / S925纯银镀金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"肖邦链","material":"S925纯银镀金白"}',94,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-chopin-s925-white');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-chopin-s925-white'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-chopin-18k-yellow','肖邦链 / 18K金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"肖邦链","material":"18K金黄"}',95,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-chopin-18k-yellow');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-chopin-18k-yellow'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-chopin-18k-white','肖邦链 / 18K金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"肖邦链","material":"18K金白"}',96,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-chopin-18k-white');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-chopin-18k-white'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-y-alloy','Y字链 / 合金',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"Y字链","material":"合金"}',97,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-y-alloy');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-y-alloy'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-y-s925-yellow','Y字链 / S925纯银镀金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"Y字链","material":"S925纯银镀金黄"}',98,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-y-s925-yellow');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-y-s925-yellow'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-y-s925-white','Y字链 / S925纯银镀金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"Y字链","material":"S925纯银镀金白"}',99,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-y-s925-white');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-y-s925-white'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-y-18k-yellow','Y字链 / 18K金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"Y字链","material":"18K金黄"}',100,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-y-18k-yellow');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-y-18k-yellow'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-y-18k-white','Y字链 / 18K金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"Y字链","material":"18K金白"}',101,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-y-18k-white');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-y-18k-white'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-box-alloy','盒仔链 / 合金',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"盒仔链","material":"合金"}',102,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-box-alloy');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-box-alloy'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-box-s925-yellow','盒仔链 / S925纯银镀金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"盒仔链","material":"S925纯银镀金黄"}',103,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-box-s925-yellow');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-box-s925-yellow'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-box-s925-white','盒仔链 / S925纯银镀金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"盒仔链","material":"S925纯银镀金白"}',104,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-box-s925-white');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-box-s925-white'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-box-18k-yellow','盒仔链 / 18K金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"盒仔链","material":"18K金黄"}',105,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-box-18k-yellow');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-box-18k-yellow'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-box-18k-white','盒仔链 / 18K金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"盒仔链","material":"18K金白"}',106,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-box-18k-white');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-box-18k-white'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-cartier-alloy','卡地亚链 / 合金',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"卡地亚链","material":"合金"}',107,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-cartier-alloy');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-cartier-alloy'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-cartier-s925-yellow','卡地亚链 / S925纯银镀金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"卡地亚链","material":"S925纯银镀金黄"}',108,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-cartier-s925-yellow');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-cartier-s925-yellow'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-cartier-s925-white','卡地亚链 / S925纯银镀金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"卡地亚链","material":"S925纯银镀金白"}',109,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-cartier-s925-white');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-cartier-s925-white'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-cartier-18k-yellow','卡地亚链 / 18K金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"卡地亚链","material":"18K金黄"}',110,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-cartier-18k-yellow');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-cartier-18k-yellow'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
SELECT g.id,'chain','client-cartier-18k-white','卡地亚链 / 18K金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"卡地亚链","material":"18K金白"}',111,0
FROM mall_goods g JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION o WHERE o.goods_id=g.id AND o.component_type='chain' AND o.option_code='client-cartier-18k-white');
INSERT INTO CC_COMPONENT_STOCK(goods_id,component_type,option_code,stock)
SELECT o.goods_id,o.component_type,o.option_code,0 FROM CC_COMPONENT_OPTION o
JOIN mall_goods g ON g.id=o.goods_id JOIN CC_CUSTOM_PRODUCT p ON p.goods_id=g.id
WHERE g.goods_sn='DEMO-DIY-JWL-001' AND p.template_code='necklace-diy-v1'
AND o.component_type='chain' AND o.option_code='client-cartier-18k-white'
AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK s WHERE s.goods_id=o.goods_id AND s.component_type=o.component_type AND s.option_code=o.option_code);

COMMIT;
