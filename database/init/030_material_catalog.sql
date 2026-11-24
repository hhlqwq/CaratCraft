SET NAMES utf8mb4;

-- 由tools/database/build-customer-material-catalog.py生成, 只用于新建空库.
-- 候选尚未确认实际尺寸及库存, 默认停用且库存为0.
START TRANSACTION;

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (128,1999105,'pearl','client-southsea-white-8-9','澳白 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"澳白","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',0,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (128,1999105,'pearl','client-southsea-white-8-9',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (129,1999105,'pearl','client-southsea-white-9-10','澳白 9-10mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"澳白","candidateDiameterMinMm":9,"candidateDiameterMaxMm":10,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',1,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (129,1999105,'pearl','client-southsea-white-9-10',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (130,1999105,'pearl','client-southsea-white-10-11','澳白 10-11mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"澳白","candidateDiameterMinMm":10,"candidateDiameterMaxMm":11,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',2,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (130,1999105,'pearl','client-southsea-white-10-11',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (131,1999105,'pearl','client-southsea-white-11-12','澳白 11-12mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"澳白","candidateDiameterMinMm":11,"candidateDiameterMaxMm":12,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',3,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (131,1999105,'pearl','client-southsea-white-11-12',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (132,1999105,'pearl','client-southsea-white-12-13','澳白 12-13mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"澳白","candidateDiameterMinMm":12,"candidateDiameterMaxMm":13,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',4,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (132,1999105,'pearl','client-southsea-white-12-13',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (133,1999105,'pearl','client-southsea-white-13-14','澳白 13-14mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"澳白","candidateDiameterMinMm":13,"candidateDiameterMaxMm":14,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',5,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (133,1999105,'pearl','client-southsea-white-13-14',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (134,1999105,'pearl','client-southsea-gold-8-9','金珠 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"金珠","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',6,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (134,1999105,'pearl','client-southsea-gold-8-9',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (135,1999105,'pearl','client-southsea-gold-9-10','金珠 9-10mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"金珠","candidateDiameterMinMm":9,"candidateDiameterMaxMm":10,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',7,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (135,1999105,'pearl','client-southsea-gold-9-10',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (136,1999105,'pearl','client-southsea-gold-10-11','金珠 10-11mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"金珠","candidateDiameterMinMm":10,"candidateDiameterMaxMm":11,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',8,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (136,1999105,'pearl','client-southsea-gold-10-11',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (137,1999105,'pearl','client-southsea-gold-11-12','金珠 11-12mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"金珠","candidateDiameterMinMm":11,"candidateDiameterMaxMm":12,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',9,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (137,1999105,'pearl','client-southsea-gold-11-12',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (138,1999105,'pearl','client-southsea-gold-12-13','金珠 12-13mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"金珠","candidateDiameterMinMm":12,"candidateDiameterMaxMm":13,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',10,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (138,1999105,'pearl','client-southsea-gold-12-13',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (139,1999105,'pearl','client-southsea-gold-13-14','金珠 13-14mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"金珠","candidateDiameterMinMm":13,"candidateDiameterMaxMm":14,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',11,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (139,1999105,'pearl','client-southsea-gold-13-14',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (140,1999105,'pearl','client-tahiti-8-9','大溪地 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"大溪地","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',12,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (140,1999105,'pearl','client-tahiti-8-9',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (141,1999105,'pearl','client-tahiti-9-10','大溪地 9-10mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"大溪地","candidateDiameterMinMm":9,"candidateDiameterMaxMm":10,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',13,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (141,1999105,'pearl','client-tahiti-9-10',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (142,1999105,'pearl','client-tahiti-10-11','大溪地 10-11mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"大溪地","candidateDiameterMinMm":10,"candidateDiameterMaxMm":11,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',14,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (142,1999105,'pearl','client-tahiti-10-11',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (143,1999105,'pearl','client-tahiti-11-12','大溪地 11-12mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"大溪地","candidateDiameterMinMm":11,"candidateDiameterMaxMm":12,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',15,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (143,1999105,'pearl','client-tahiti-11-12',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (144,1999105,'pearl','client-tahiti-12-13','大溪地 12-13mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"大溪地","candidateDiameterMinMm":12,"candidateDiameterMaxMm":13,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',16,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (144,1999105,'pearl','client-tahiti-12-13',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (145,1999105,'pearl','client-tahiti-13-14','大溪地 13-14mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"大溪地","candidateDiameterMinMm":13,"candidateDiameterMaxMm":14,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',17,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (145,1999105,'pearl','client-tahiti-13-14',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (146,1999105,'pearl','client-akoya-white-4-5','Akoya白色 4-5mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya白色","candidateDiameterMinMm":4,"candidateDiameterMaxMm":5,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',18,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (146,1999105,'pearl','client-akoya-white-4-5',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (147,1999105,'pearl','client-akoya-white-5-6','Akoya白色 5-6mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya白色","candidateDiameterMinMm":5,"candidateDiameterMaxMm":6,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',19,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (147,1999105,'pearl','client-akoya-white-5-6',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (148,1999105,'pearl','client-akoya-white-6-7','Akoya白色 6-7mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya白色","candidateDiameterMinMm":6,"candidateDiameterMaxMm":7,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',20,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (148,1999105,'pearl','client-akoya-white-6-7',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (149,1999105,'pearl','client-akoya-white-7-8','Akoya白色 7-8mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya白色","candidateDiameterMinMm":7,"candidateDiameterMaxMm":8,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',21,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (149,1999105,'pearl','client-akoya-white-7-8',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (150,1999105,'pearl','client-akoya-white-8-9','Akoya白色 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya白色","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',22,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (150,1999105,'pearl','client-akoya-white-8-9',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (151,1999105,'pearl','client-akoya-madama-4-5','Akoya真多麻 4-5mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya真多麻","candidateDiameterMinMm":4,"candidateDiameterMaxMm":5,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',23,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (151,1999105,'pearl','client-akoya-madama-4-5',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (152,1999105,'pearl','client-akoya-madama-5-6','Akoya真多麻 5-6mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya真多麻","candidateDiameterMinMm":5,"candidateDiameterMaxMm":6,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',24,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (152,1999105,'pearl','client-akoya-madama-5-6',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (153,1999105,'pearl','client-akoya-madama-6-7','Akoya真多麻 6-7mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya真多麻","candidateDiameterMinMm":6,"candidateDiameterMaxMm":7,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',25,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (153,1999105,'pearl','client-akoya-madama-6-7',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (154,1999105,'pearl','client-akoya-madama-7-8','Akoya真多麻 7-8mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya真多麻","candidateDiameterMinMm":7,"candidateDiameterMaxMm":8,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',26,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (154,1999105,'pearl','client-akoya-madama-7-8',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (155,1999105,'pearl','client-akoya-madama-8-9','Akoya真多麻 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya真多麻","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',27,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (155,1999105,'pearl','client-akoya-madama-8-9',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (156,1999105,'pearl','client-akoya-gold-4-5','Akoya羽黄金 4-5mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya羽黄金","candidateDiameterMinMm":4,"candidateDiameterMaxMm":5,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',28,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (156,1999105,'pearl','client-akoya-gold-4-5',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (157,1999105,'pearl','client-akoya-gold-5-6','Akoya羽黄金 5-6mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya羽黄金","candidateDiameterMinMm":5,"candidateDiameterMaxMm":6,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',29,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (157,1999105,'pearl','client-akoya-gold-5-6',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (158,1999105,'pearl','client-akoya-gold-6-7','Akoya羽黄金 6-7mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya羽黄金","candidateDiameterMinMm":6,"candidateDiameterMaxMm":7,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',30,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (158,1999105,'pearl','client-akoya-gold-6-7',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (159,1999105,'pearl','client-akoya-gold-7-8','Akoya羽黄金 7-8mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya羽黄金","candidateDiameterMinMm":7,"candidateDiameterMaxMm":8,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',31,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (159,1999105,'pearl','client-akoya-gold-7-8',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (160,1999105,'pearl','client-akoya-gold-8-9','Akoya羽黄金 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"Akoya羽黄金","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"海水珍珠"}',32,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (160,1999105,'pearl','client-akoya-gold-8-9',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (161,1999105,'pearl','client-freshwater-ak-4-5','淡水AK 4-5mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水AK","candidateDiameterMinMm":4,"candidateDiameterMaxMm":5,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',33,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (161,1999105,'pearl','client-freshwater-ak-4-5',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (162,1999105,'pearl','client-freshwater-ak-5-6','淡水AK 5-6mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水AK","candidateDiameterMinMm":5,"candidateDiameterMaxMm":6,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',34,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (162,1999105,'pearl','client-freshwater-ak-5-6',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (163,1999105,'pearl','client-freshwater-ak-6-7','淡水AK 6-7mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水AK","candidateDiameterMinMm":6,"candidateDiameterMaxMm":7,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',35,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (163,1999105,'pearl','client-freshwater-ak-6-7',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (164,1999105,'pearl','client-freshwater-ak-7-8','淡水AK 7-8mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水AK","candidateDiameterMinMm":7,"candidateDiameterMaxMm":8,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',36,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (164,1999105,'pearl','client-freshwater-ak-7-8',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (165,1999105,'pearl','client-edison-9-10','爱迪生 9-10mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"爱迪生","candidateDiameterMinMm":9,"candidateDiameterMaxMm":10,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',37,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (165,1999105,'pearl','client-edison-9-10',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (166,1999105,'pearl','client-edison-10-11','爱迪生 10-11mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"爱迪生","candidateDiameterMinMm":10,"candidateDiameterMaxMm":11,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',38,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (166,1999105,'pearl','client-edison-10-11',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (167,1999105,'pearl','client-edison-11-12','爱迪生 11-12mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"爱迪生","candidateDiameterMinMm":11,"candidateDiameterMaxMm":12,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',39,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (167,1999105,'pearl','client-edison-11-12',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (168,1999105,'pearl','client-edison-12-13','爱迪生 12-13mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"爱迪生","candidateDiameterMinMm":12,"candidateDiameterMaxMm":13,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',40,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (168,1999105,'pearl','client-edison-12-13',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (169,1999105,'pearl','client-edison-13-14','爱迪生 13-14mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"爱迪生","candidateDiameterMinMm":13,"candidateDiameterMaxMm":14,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',41,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (169,1999105,'pearl','client-edison-13-14',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (170,1999105,'pearl','client-freshwater-white-3-4','淡水无核白 3-4mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":3,"candidateDiameterMaxMm":4,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',42,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (170,1999105,'pearl','client-freshwater-white-3-4',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (171,1999105,'pearl','client-freshwater-white-4-5','淡水无核白 4-5mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":4,"candidateDiameterMaxMm":5,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',43,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (171,1999105,'pearl','client-freshwater-white-4-5',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (172,1999105,'pearl','client-freshwater-white-5-6','淡水无核白 5-6mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":5,"candidateDiameterMaxMm":6,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',44,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (172,1999105,'pearl','client-freshwater-white-5-6',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (173,1999105,'pearl','client-freshwater-white-6-7','淡水无核白 6-7mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":6,"candidateDiameterMaxMm":7,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',45,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (173,1999105,'pearl','client-freshwater-white-6-7',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (174,1999105,'pearl','client-freshwater-white-7-8','淡水无核白 7-8mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":7,"candidateDiameterMaxMm":8,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',46,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (174,1999105,'pearl','client-freshwater-white-7-8',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (175,1999105,'pearl','client-freshwater-white-8-9','淡水无核白 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',47,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (175,1999105,'pearl','client-freshwater-white-8-9',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (176,1999105,'pearl','client-freshwater-white-9-10','淡水无核白 9-10mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":9,"candidateDiameterMaxMm":10,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',48,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (176,1999105,'pearl','client-freshwater-white-9-10',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (177,1999105,'pearl','client-freshwater-white-10-11','淡水无核白 10-11mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核白","candidateDiameterMinMm":10,"candidateDiameterMaxMm":11,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',49,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (177,1999105,'pearl','client-freshwater-white-10-11',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (178,1999105,'pearl','client-freshwater-purple-3-4','淡水无核紫 3-4mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":3,"candidateDiameterMaxMm":4,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',50,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (178,1999105,'pearl','client-freshwater-purple-3-4',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (179,1999105,'pearl','client-freshwater-purple-4-5','淡水无核紫 4-5mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":4,"candidateDiameterMaxMm":5,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',51,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (179,1999105,'pearl','client-freshwater-purple-4-5',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (180,1999105,'pearl','client-freshwater-purple-5-6','淡水无核紫 5-6mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":5,"candidateDiameterMaxMm":6,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',52,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (180,1999105,'pearl','client-freshwater-purple-5-6',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (181,1999105,'pearl','client-freshwater-purple-6-7','淡水无核紫 6-7mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":6,"candidateDiameterMaxMm":7,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',53,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (181,1999105,'pearl','client-freshwater-purple-6-7',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (182,1999105,'pearl','client-freshwater-purple-7-8','淡水无核紫 7-8mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":7,"candidateDiameterMaxMm":8,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',54,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (182,1999105,'pearl','client-freshwater-purple-7-8',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (183,1999105,'pearl','client-freshwater-purple-8-9','淡水无核紫 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',55,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (183,1999105,'pearl','client-freshwater-purple-8-9',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (184,1999105,'pearl','client-freshwater-purple-9-10','淡水无核紫 9-10mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":9,"candidateDiameterMaxMm":10,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',56,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (184,1999105,'pearl','client-freshwater-purple-9-10',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (185,1999105,'pearl','client-freshwater-purple-10-11','淡水无核紫 10-11mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核紫","candidateDiameterMinMm":10,"candidateDiameterMaxMm":11,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',57,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (185,1999105,'pearl','client-freshwater-purple-10-11',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (186,1999105,'pearl','client-freshwater-orange-3-4','淡水无核橙 3-4mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":3,"candidateDiameterMaxMm":4,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',58,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (186,1999105,'pearl','client-freshwater-orange-3-4',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (187,1999105,'pearl','client-freshwater-orange-4-5','淡水无核橙 4-5mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":4,"candidateDiameterMaxMm":5,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',59,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (187,1999105,'pearl','client-freshwater-orange-4-5',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (188,1999105,'pearl','client-freshwater-orange-5-6','淡水无核橙 5-6mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":5,"candidateDiameterMaxMm":6,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',60,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (188,1999105,'pearl','client-freshwater-orange-5-6',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (189,1999105,'pearl','client-freshwater-orange-6-7','淡水无核橙 6-7mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":6,"candidateDiameterMaxMm":7,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',61,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (189,1999105,'pearl','client-freshwater-orange-6-7',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (190,1999105,'pearl','client-freshwater-orange-7-8','淡水无核橙 7-8mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":7,"candidateDiameterMaxMm":8,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',62,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (190,1999105,'pearl','client-freshwater-orange-7-8',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (191,1999105,'pearl','client-freshwater-orange-8-9','淡水无核橙 8-9mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":8,"candidateDiameterMaxMm":9,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',63,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (191,1999105,'pearl','client-freshwater-orange-8-9',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (192,1999105,'pearl','client-freshwater-orange-9-10','淡水无核橙 9-10mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":9,"candidateDiameterMaxMm":10,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',64,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (192,1999105,'pearl','client-freshwater-orange-9-10',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (193,1999105,'pearl','client-freshwater-orange-10-11','淡水无核橙 10-11mm',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"淡水无核橙","candidateDiameterMinMm":10,"candidateDiameterMaxMm":11,"shape":"近正圆","luster":"极光","blemish":"微瑕","pearlSource":"淡水珍珠"}',65,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (193,1999105,'pearl','client-freshwater-orange-10-11',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (194,1999105,'pearl','client-baroque','巴洛克, 实物规格待录入',0,'{"stockQuantity":1,"stockUnit":"颗","candidateOnly":true,"variety":"巴洛克","shape":"待实测","luster":"极光","blemish":"微瑕"}',66,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (194,1999105,'pearl','client-baroque',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (195,1999105,'clasp','client-extension','延长链',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"claspRole":"EXTENDER"}',67,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (195,1999105,'clasp','client-extension',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (196,1999105,'clasp','client-ingot','元宝扣',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',68,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (196,1999105,'clasp','client-ingot',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (197,1999105,'clasp','client-magnetic','磁吸扣',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',69,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (197,1999105,'clasp','client-magnetic',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (198,1999105,'clasp','client-hug','抱抱扣',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',70,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (198,1999105,'clasp','client-hug',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (199,1999105,'accessory','client-accessory-01','万能扣',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',71,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (199,1999105,'accessory','client-accessory-01',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (200,1999105,'accessory','client-accessory-02','戴妃',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',72,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (200,1999105,'accessory','client-accessory-02',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (201,1999105,'accessory','client-accessory-03','小睫毛',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',73,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (201,1999105,'accessory','client-accessory-03',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (202,1999105,'accessory','client-accessory-04','大睫毛',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',74,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (202,1999105,'accessory','client-accessory-04',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (203,1999105,'accessory','client-accessory-05','三钻',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',75,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (203,1999105,'accessory','client-accessory-05',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (204,1999105,'accessory','client-accessory-06','奶嘴',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',76,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (204,1999105,'accessory','client-accessory-06',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (205,1999105,'accessory','client-accessory-07','四叶草',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',77,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (205,1999105,'accessory','client-accessory-07',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (206,1999105,'accessory','client-accessory-08','瓜子扣',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',78,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (206,1999105,'accessory','client-accessory-08',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (207,1999105,'accessory','client-accessory-09','满钻瓜子扣',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',79,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (207,1999105,'accessory','client-accessory-09',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (208,1999105,'accessory','client-accessory-10','路路通',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',80,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (208,1999105,'accessory','client-accessory-10',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (209,1999105,'accessory','client-accessory-11','大冰雪',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',81,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (209,1999105,'accessory','client-accessory-11',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (210,1999105,'accessory','client-accessory-12','小冰雪',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',82,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (210,1999105,'accessory','client-accessory-12',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (211,1999105,'accessory','client-accessory-13','方糖',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true}',83,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (211,1999105,'accessory','client-accessory-13',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (212,1999105,'accessory','client-accessory-14','皇冠',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"sameNameNeedsConfirmation":true}',84,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (212,1999105,'accessory','client-accessory-14',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (213,1999105,'chain','client-steel-wire','钢丝',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"钢丝"}',85,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (213,1999105,'chain','client-steel-wire',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (214,1999105,'chain','client-cotton','棉线',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"棉线"}',86,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (214,1999105,'chain','client-cotton',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (215,1999105,'chain','client-o-alloy','O字链 / 合金',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"O字链","material":"合金"}',87,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (215,1999105,'chain','client-o-alloy',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (216,1999105,'chain','client-o-s925-yellow','O字链 / S925纯银镀金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"O字链","material":"S925纯银镀金黄"}',88,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (216,1999105,'chain','client-o-s925-yellow',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (217,1999105,'chain','client-o-s925-white','O字链 / S925纯银镀金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"O字链","material":"S925纯银镀金白"}',89,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (217,1999105,'chain','client-o-s925-white',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (218,1999105,'chain','client-o-18k-yellow','O字链 / 18K金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"O字链","material":"18K金黄"}',90,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (218,1999105,'chain','client-o-18k-yellow',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (219,1999105,'chain','client-o-18k-white','O字链 / 18K金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"O字链","material":"18K金白"}',91,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (219,1999105,'chain','client-o-18k-white',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (220,1999105,'chain','client-chopin-alloy','肖邦链 / 合金',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"肖邦链","material":"合金"}',92,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (220,1999105,'chain','client-chopin-alloy',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (221,1999105,'chain','client-chopin-s925-yellow','肖邦链 / S925纯银镀金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"肖邦链","material":"S925纯银镀金黄"}',93,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (221,1999105,'chain','client-chopin-s925-yellow',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (222,1999105,'chain','client-chopin-s925-white','肖邦链 / S925纯银镀金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"肖邦链","material":"S925纯银镀金白"}',94,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (222,1999105,'chain','client-chopin-s925-white',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (223,1999105,'chain','client-chopin-18k-yellow','肖邦链 / 18K金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"肖邦链","material":"18K金黄"}',95,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (223,1999105,'chain','client-chopin-18k-yellow',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (224,1999105,'chain','client-chopin-18k-white','肖邦链 / 18K金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"肖邦链","material":"18K金白"}',96,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (224,1999105,'chain','client-chopin-18k-white',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (225,1999105,'chain','client-y-alloy','Y字链 / 合金',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"Y字链","material":"合金"}',97,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (225,1999105,'chain','client-y-alloy',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (226,1999105,'chain','client-y-s925-yellow','Y字链 / S925纯银镀金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"Y字链","material":"S925纯银镀金黄"}',98,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (226,1999105,'chain','client-y-s925-yellow',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (227,1999105,'chain','client-y-s925-white','Y字链 / S925纯银镀金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"Y字链","material":"S925纯银镀金白"}',99,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (227,1999105,'chain','client-y-s925-white',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (228,1999105,'chain','client-y-18k-yellow','Y字链 / 18K金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"Y字链","material":"18K金黄"}',100,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (228,1999105,'chain','client-y-18k-yellow',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (229,1999105,'chain','client-y-18k-white','Y字链 / 18K金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"Y字链","material":"18K金白"}',101,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (229,1999105,'chain','client-y-18k-white',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (230,1999105,'chain','client-box-alloy','盒仔链 / 合金',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"盒仔链","material":"合金"}',102,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (230,1999105,'chain','client-box-alloy',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (231,1999105,'chain','client-box-s925-yellow','盒仔链 / S925纯银镀金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"盒仔链","material":"S925纯银镀金黄"}',103,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (231,1999105,'chain','client-box-s925-yellow',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (232,1999105,'chain','client-box-s925-white','盒仔链 / S925纯银镀金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"盒仔链","material":"S925纯银镀金白"}',104,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (232,1999105,'chain','client-box-s925-white',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (233,1999105,'chain','client-box-18k-yellow','盒仔链 / 18K金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"盒仔链","material":"18K金黄"}',105,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (233,1999105,'chain','client-box-18k-yellow',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (234,1999105,'chain','client-box-18k-white','盒仔链 / 18K金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"盒仔链","material":"18K金白"}',106,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (234,1999105,'chain','client-box-18k-white',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (235,1999105,'chain','client-cartier-alloy','卡地亚链 / 合金',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"卡地亚链","material":"合金"}',107,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (235,1999105,'chain','client-cartier-alloy',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (236,1999105,'chain','client-cartier-s925-yellow','卡地亚链 / S925纯银镀金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"卡地亚链","material":"S925纯银镀金黄"}',108,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (236,1999105,'chain','client-cartier-s925-yellow',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (237,1999105,'chain','client-cartier-s925-white','卡地亚链 / S925纯银镀金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"卡地亚链","material":"S925纯银镀金白"}',109,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (237,1999105,'chain','client-cartier-s925-white',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (238,1999105,'chain','client-cartier-18k-yellow','卡地亚链 / 18K金黄',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"卡地亚链","material":"18K金黄"}',110,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (238,1999105,'chain','client-cartier-18k-yellow',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (239,1999105,'chain','client-cartier-18k-white','卡地亚链 / 18K金白',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"chainStyle":"卡地亚链","material":"18K金白"}',111,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (239,1999105,'chain','client-cartier-18k-white',0);

INSERT INTO CC_COMPONENT_OPTION(id,goods_id,component_type,option_code,name,price_delta,material_params,z_index,enabled)
VALUES (240,1999105,'accessory','client-accessory-15-crown','皇冠',0,'{"stockQuantity":1,"stockUnit":"件","candidateOnly":true,"sameNameNeedsConfirmation":true}',72,0);
INSERT INTO CC_COMPONENT_STOCK(id,goods_id,component_type,option_code,stock)
VALUES (240,1999105,'accessory','client-accessory-15-crown',0);

COMMIT;
