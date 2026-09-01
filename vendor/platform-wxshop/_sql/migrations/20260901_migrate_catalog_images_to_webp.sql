-- 迁移 CaratCraft 内置商品图片路径并修复本地磁盘存储配置。
-- Migrates bundled catalog image paths and repairs local disk storage settings.
SET NAMES utf8mb4;

START TRANSACTION;

UPDATE `mall_goods`
SET `primary_pic_url` = REPLACE(`primary_pic_url`, '.png', '.webp')
WHERE `primary_pic_url` REGEXP '^/static/images/(demo|designer/layers)/.+\\.png$';

UPDATE `mall_goods`
SET `list_pic_url` = REPLACE(`list_pic_url`, '.png', '.webp')
WHERE `list_pic_url` REGEXP '^/static/images/(demo|designer/layers)/.+\\.png$';

UPDATE `mall_goods_gallery`
SET `img_url` = REPLACE(`img_url`, '.png', '.webp')
WHERE CONVERT(`img_url` USING utf8mb4) REGEXP '^/static/images/(demo|designer/layers)/.+\\.png$';

UPDATE `mall_category`
SET `banner_url` = REPLACE(`banner_url`, '.png', '.webp')
WHERE `banner_url` REGEXP '^/static/images/(demo|designer/layers)/.+\\.png$';

UPDATE `mall_category`
SET `icon_url` = REPLACE(`icon_url`, '.png', '.webp')
WHERE `icon_url` REGEXP '^/static/images/(demo|designer/layers)/.+\\.png$';

UPDATE `mall_category`
SET `img_url` = REPLACE(`img_url`, '.png', '.webp')
WHERE `img_url` REGEXP '^/static/images/(demo|designer/layers)/.+\\.png$';

UPDATE `mall_ad`
SET `image_url` = REPLACE(`image_url`, '.png', '.webp')
WHERE `image_url` REGEXP '^/static/images/(demo|designer/layers)/.+\\.png$';

UPDATE `mall_channel`
SET `icon_url` = REPLACE(`icon_url`, '.png', '.webp')
WHERE `icon_url` REGEXP '^/static/images/(demo|designer/layers)/.+\\.png$';

UPDATE `mall_goods_specification`
SET `pic_url` = REPLACE(`pic_url`, '.png', '.webp')
WHERE `pic_url` REGEXP '^/static/images/(demo|designer/layers)/.+\\.png$';

UPDATE `CC_COMPONENT_OPTION`
SET `asset_url` = REPLACE(`asset_url`, '.png', '.webp')
WHERE `asset_url` REGEXP '^/static/images/(demo|designer/layers)/.+\\.png$';

-- 仅修复未配置或已经使用磁盘存储的实例，不覆盖现有云存储配置。
-- Only repairs unconfigured or disk-backed instances and preserves cloud storage settings.
UPDATE `SYS_CONFIG`
SET `PARAM_VALUE` = JSON_SET(
        CAST(`PARAM_VALUE` AS JSON),
        '$.type', 4,
        '$.diskPath', '/data/uploads',
        '$.proxyServer', 'http://localhost:8888/upload'
    )
WHERE `PARAM_KEY` = 'CLOUD_STORAGE_CONFIG_KEY'
  AND JSON_VALID(`PARAM_VALUE`)
  AND COALESCE(JSON_EXTRACT(`PARAM_VALUE`, '$.type'), 0) IN (0, 4);

COMMIT;
