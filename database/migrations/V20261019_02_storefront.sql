-- 商品分类、珍珠参数和独立视频, 不猜测历史商品品种或批量改动售价库存.
ALTER TABLE mall_goods
  ADD COLUMN pearl_source VARCHAR(16) NOT NULL DEFAULT '',
  ADD COLUMN jewelry_type VARCHAR(16) NOT NULL DEFAULT '',
  ADD COLUMN pearl_variety VARCHAR(60) NOT NULL DEFAULT '',
  ADD COLUMN pearl_origin VARCHAR(100) NOT NULL DEFAULT '',
  ADD COLUMN pearl_shape VARCHAR(30) NOT NULL DEFAULT '',
  ADD COLUMN pearl_luster VARCHAR(30) NOT NULL DEFAULT '',
  ADD COLUMN pearl_blemish VARCHAR(30) NOT NULL DEFAULT '',
  ADD COLUMN pearl_diameter_min DECIMAL(8,3) NULL,
  ADD COLUMN pearl_diameter_max DECIMAL(8,3) NULL,
  ADD COLUMN video_media_id BIGINT NOT NULL DEFAULT 0,
  ADD COLUMN video_url VARCHAR(1000) NOT NULL DEFAULT '',
  ADD INDEX ix_pearl_catalog (is_delete,is_on_sale,pearl_source,jewelry_type,id);
CREATE TABLE CC_PRODUCT_VIDEO (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  operator_id VARCHAR(64) NOT NULL,
  url VARCHAR(1000) NOT NULL DEFAULT '',
  duration_ms INT NOT NULL DEFAULT 0,
  width INT NOT NULL DEFAULT 0,
  height INT NOT NULL DEFAULT 0,
  byte_size INT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_video_operator (operator_id,create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
