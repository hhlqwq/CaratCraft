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
-- MySQL 不允许在同一语句中重复读取同一个临时表, 因此按关联方向分别删除。
-- MySQL cannot reopen one temporary table twice in the same statement, so each relation direction is deleted separately.
DELETE related
FROM `mall_related_goods` related
INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = related.`goods_id`;
DELETE related
FROM `mall_related_goods` related
INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = related.`related_goods_id`;
DELETE stock FROM `CC_COMPONENT_STOCK` stock INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = stock.`goods_id`;
DELETE option_row FROM `CC_COMPONENT_OPTION` option_row INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = option_row.`goods_id`;
DELETE custom_product FROM `CC_CUSTOM_PRODUCT` custom_product INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = custom_product.`goods_id`;
DELETE goods FROM `mall_goods` goods INNER JOIN `CC_LEGACY_GOODS_IDS` legacy ON legacy.`id` = goods.`id`;

DELETE FROM `mall_category` WHERE `id` < 1999000 OR `id` >= 2000000;

-- 商城首页只保留克拉匠造自有入口, 清除原严选广告、频道、品牌、专题和常见问题。
-- Keeps only CaratCraft-owned storefront content and removes legacy Yanxuan content.
DELETE FROM `mall_ad` WHERE `id` <> 9000;
DELETE FROM `mall_channel` WHERE `id` NOT IN (1999000, 1999001);
DELETE FROM `mall_brand`;
DELETE FROM `mall_topic`;
DELETE FROM `mall_topic_category`;
DELETE FROM `mall_goods_issue`;
DELETE FROM `mall_help_issue`;
DELETE FROM `mall_help_type`;

-- 删除未绑定历史订单的底座演示优惠券, 保留已经使用或仍有关联的真实记录。
-- Removes unbound legacy demo coupons while preserving records referenced by historical orders.
DELETE FROM `mall_user_coupon`
WHERE (`order_id` IS NULL OR `order_id` = 0)
  AND `coupon_id` IN (1, 2, 4, 7, 8, 9);
DELETE coupon
FROM `mall_coupon` coupon
LEFT JOIN `mall_order` order_row ON order_row.`coupon_id` = coupon.`id`
LEFT JOIN `mall_user_coupon` user_coupon ON user_coupon.`coupon_id` = coupon.`id`
WHERE coupon.`id` IN (1, 2, 4, 7, 8, 9)
  AND order_row.`id` IS NULL
  AND user_coupon.`id` IS NULL;

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

-- 为当前珠宝商品重建本地图库, 修复旧外链和空图库导致的小程序白屏。
-- Rebuilds local galleries for current jewelry goods to avoid blank remote images.
DELETE FROM `mall_goods_gallery` WHERE `goods_id` BETWEEN 1999101 AND 1999105;
INSERT INTO `mall_goods_gallery` (`goods_id`, `img_url`, `img_desc`, `sort_order`)
SELECT `id`, `list_pic_url`, `name`, 1
FROM `mall_goods`
WHERE `id` BETWEEN 1999101 AND 1999105
  AND `list_pic_url` IS NOT NULL
  AND `list_pic_url` <> '';

DROP TEMPORARY TABLE `CC_LEGACY_GOODS_IDS`;
COMMIT;
