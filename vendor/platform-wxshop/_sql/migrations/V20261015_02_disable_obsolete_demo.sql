-- 仅停用仍带原始演示货号和模板代码的旧耳饰示例, 不依据ID范围处理商家商品.
UPDATE CC_CUSTOM_PRODUCT c
JOIN mall_goods g ON g.id=c.goods_id
SET c.enabled=0
WHERE g.goods_sn='DEMO-DIY-EAR-001' AND c.template_code='earring-diy-v1';

UPDATE mall_goods
SET is_on_sale=0
WHERE goods_sn='DEMO-DIY-EAR-001' AND id=1999106;
