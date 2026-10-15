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

-- 修复未配置、本地磁盘或仍指向失效演示 OSS 且没有密钥的实例。
-- Repairs unconfigured, disk-backed, or credential-free legacy demo OSS instances.
UPDATE `SYS_CONFIG`
SET `PARAM_VALUE` = JSON_SET(
        CAST(`PARAM_VALUE` AS JSON),
        '$.type', 4,
        '$.diskPath', '/data/uploads',
        '$.proxyServer', '/upload'
    )
WHERE `PARAM_KEY` = 'CLOUD_STORAGE_CONFIG_KEY'
  AND JSON_VALID(`PARAM_VALUE`)
  AND (
    COALESCE(JSON_EXTRACT(`PARAM_VALUE`, '$.type'), 0) IN (0, 4)
    OR (
      JSON_EXTRACT(`PARAM_VALUE`, '$.type') = 2
      AND COALESCE(JSON_UNQUOTE(JSON_EXTRACT(`PARAM_VALUE`, '$.aliyunDomain')), '') =
          'https://platform-wxmall.oss-cn-beijing.aliyuncs.com'
      AND COALESCE(JSON_UNQUOTE(JSON_EXTRACT(`PARAM_VALUE`, '$.aliyunAccessKeyId')), '') = ''
      AND COALESCE(JSON_UNQUOTE(JSON_EXTRACT(`PARAM_VALUE`, '$.aliyunAccessKeySecret')), '') = ''
    )
  );

COMMIT;
