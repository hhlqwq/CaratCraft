-- 清理原底座商城目录和外链 / Removes legacy storefront catalog data and remote assets.
-- 可重复执行, 保留历史订单、设计快照、定制订单与生产工单。
SET NAMES utf8mb4;

START TRANSACTION;

CREATE TEMPORARY TABLE IF NOT EXISTS `CC_LEGACY_GOODS_IDS` (
    `id` INT NOT NULL PRIMARY KEY
);
TRUNCATE TABLE `CC_LEGACY_GOODS_IDS`;
INSERT INTO `CC_LEGACY_GOODS_IDS` (`id`)
SELECT `id`
FROM `mall_goods`
WHERE `category_id` < 1999000 OR `category_id` >= 2000000;

DELETE picture
FROM `mall_comment_picture` picture
INNER JOIN `mall_comment` comment_row ON comment_row.`id` = picture.`comment_id`
INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = comment_row.`value_id`
WHERE comment_row.`type_id` = 0;
DELETE comment_row
FROM `mall_comment` comment_row
INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = comment_row.`value_id`
WHERE comment_row.`type_id` = 0;
DELETE cart FROM `mall_cart` cart INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = cart.`goods_id`;
DELETE collect FROM `mall_collect` collect INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = collect.`value_id` WHERE collect.`type_id` = 0;
DELETE coupon_goods FROM `mall_coupon_goods` coupon_goods INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = coupon_goods.`goods_id`;
DELETE footprint FROM `mall_footprint` footprint INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = footprint.`goods_id`;
DELETE attribute FROM `mall_goods_attribute` attribute INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = attribute.`goods_id`;
DELETE gallery FROM `mall_goods_gallery` gallery INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = gallery.`goods_id`;
DELETE specification FROM `mall_goods_specification` specification INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = specification.`goods_id`;
DELETE product FROM `mall_product` product INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = product.`goods_id`;
DELETE related FROM `mall_related_goods` related
WHERE related.`goods_id` IN (SELECT `id` FROM `CC_LEGACY_GOODS_IDS`)
   OR related.`related_goods_id` IN (SELECT `id` FROM `CC_LEGACY_GOODS_IDS`);
DELETE stock FROM `CC_COMPONENT_STOCK` stock INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = stock.`goods_id`;
DELETE option_row FROM `CC_COMPONENT_OPTION` option_row INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = option_row.`goods_id`;
DELETE custom_product FROM `CC_CUSTOM_PRODUCT` custom_product INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = custom_product.`goods_id`;
DELETE goods FROM `mall_goods` goods INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = goods.`id`;

DELETE FROM `mall_category` WHERE `id` < 1999000 OR `id` >= 2000000;

-- 清除仍会从首页接口返回的严选广告、频道、品牌和专题 / Removes legacy home-page content.
DELETE FROM `mall_ad`
WHERE `image_url` LIKE '%yanxuan.nosdn.127.net%'
   OR `image_url` LIKE '%platform-wxmall.oss-cn-beijing.aliyuncs.com%';
DELETE FROM `mall_channel`
WHERE `icon_url` LIKE '%yanxuan.nosdn.127.net%'
   OR `icon_url` LIKE '%platform-wxmall.oss-cn-beijing.aliyuncs.com%';
DELETE FROM `mall_brand`
WHERE `list_pic_url` LIKE '%yanxuan.nosdn.127.net%'
   OR `list_pic_url` LIKE '%platform-wxmall.oss-cn-beijing.aliyuncs.com%'
   OR `pic_url` LIKE '%yanxuan.nosdn.127.net%'
   OR `pic_url` LIKE '%platform-wxmall.oss-cn-beijing.aliyuncs.com%'
   OR `simple_desc` LIKE '%严选%';
DELETE FROM `mall_topic`
WHERE `content` LIKE '%yanxuan.nosdn.127.net%'
   OR `item_pic_url` LIKE '%yanxuan.nosdn.127.net%'
   OR `scene_pic_url` LIKE '%yanxuan.nosdn.127.net%';
DELETE FROM `mall_topic_category`
WHERE `pic_url` LIKE 'http://%' OR `pic_url` LIKE 'https://%';

INSERT INTO `mall_ad`
(`id`, `ad_position_id`, `media_type`, `name`, `link`, `image_url`, `content`, `end_time`, `enabled`)
VALUES
(9000, 1, 1, '克拉匠造珠宝设计', '/pages/designer/designer',
 '/static/images/demo/demo-platinum-pearl-necklace.png', '定制你的专属珠宝', NULL, 1)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`), `link` = VALUES(`link`), `image_url` = VALUES(`image_url`),
`content` = VALUES(`content`), `end_time` = VALUES(`end_time`), `enabled` = VALUES(`enabled`);

INSERT INTO `mall_channel` (`id`, `name`, `url`, `icon_url`, `sort_order`)
VALUES
(1999000, '珠宝作品', '/pages/category/category?id=1999000',
 '/static/images/demo/demo-gold-solitaire-necklace.png', 1),
(1999001, '专属定制', '/pages/designer/designer',
 '/static/images/demo/demo-platinum-pearl-necklace.png', 2)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`), `url` = VALUES(`url`), `icon_url` = VALUES(`icon_url`),
`sort_order` = VALUES(`sort_order`);

UPDATE `mall_goods`
SET `primary_pic_url` = '/static/images/logo.png'
WHERE `primary_pic_url` LIKE '%yanxuan.nosdn.127.net%'
   OR `primary_pic_url` LIKE '%platform-wxmall.oss-cn-beijing.aliyuncs.com%';
UPDATE `mall_goods`
SET `list_pic_url` = '/static/images/logo.png'
WHERE `list_pic_url` LIKE '%yanxuan.nosdn.127.net%'
   OR `list_pic_url` LIKE '%platform-wxmall.oss-cn-beijing.aliyuncs.com%';
UPDATE `mall_category`
SET `icon_url` = '', `img_url` = '/static/images/logo.png'
WHERE `icon_url` LIKE '%yanxuan.nosdn.127.net%'
   OR `icon_url` LIKE '%platform-wxmall.oss-cn-beijing.aliyuncs.com%'
   OR `img_url` LIKE '%yanxuan.nosdn.127.net%'
   OR `img_url` LIKE '%platform-wxmall.oss-cn-beijing.aliyuncs.com%';
UPDATE `WX_USER`
SET `HEAD_IMG_URL` = '/static/images/defaultAvatar.png'
WHERE `HEAD_IMG_URL` LIKE '%yanxuan.nosdn.127.net%'
   OR `HEAD_IMG_URL` LIKE '%platform-wxmall.oss-cn-beijing.aliyuncs.com%';

DROP TEMPORARY TABLE `CC_LEGACY_GOODS_IDS`;
COMMIT;
