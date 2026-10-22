-- 旧版按ID段和全表删除的目录清理已撤销, 不再推断商家数据来源或删除业务记录.
-- 该文件仅保留新建空库所需的项目演示入口, 已有库升级只执行V版本迁移.
SET NAMES utf8mb4;

INSERT INTO mall_ad
(id, ad_position_id, media_type, name, link, image_url, content, end_time, enabled)
SELECT 9000, 1, 1, '克拉匠造珠宝设计', '/pages/designer/designer',
 '/static/images/demo/demo-platinum-pearl-necklace.webp', '项目演示, 商家正式发布前请替换.', NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM mall_ad WHERE id=9000);

INSERT INTO mall_channel (id, name, url, icon_url, sort_order)
SELECT 1999000, '珠宝定制', '/pages/designer/designer', '', 1
WHERE NOT EXISTS (SELECT 1 FROM mall_channel WHERE id=1999000);

-- 只有保留原始演示货号及模板代码的旧示例才停用, 不按类别范围处理真实商品.
UPDATE CC_CUSTOM_PRODUCT c
JOIN mall_goods g ON g.id=c.goods_id
SET c.enabled=0
WHERE g.goods_sn='DEMO-DIY-EAR-001' AND c.template_code='earring-diy-v1';
UPDATE mall_goods
SET is_on_sale=0
WHERE goods_sn='DEMO-DIY-EAR-001' AND id=1999106;
