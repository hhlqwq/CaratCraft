-- 由tools/build-migration-baseline.py生成, 只用于新建空库初始化.
CREATE TABLE IF NOT EXISTS CC_SCHEMA_MIGRATION (
  version VARCHAR(191) PRIMARY KEY, sha256 CHAR(64) NOT NULL,
  applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 版本: V20261015_01_order_integrity.sql
-- 订单改价流水只追加不覆盖, 与订单实付金额变化在同一事务内保存.
CREATE TABLE IF NOT EXISTS CC_ORDER_PRICE_LOG (
  id BIGINT NOT NULL AUTO_INCREMENT,
  order_id INT NOT NULL,
  old_price DECIMAL(12,2) NOT NULL,
  new_price DECIMAL(12,2) NOT NULL,
  adjustment DECIMAL(12,2) NOT NULL,
  operator_id VARCHAR(64) NOT NULL,
  reason VARCHAR(200) NOT NULL,
  create_time DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY idx_order_price_log_order (order_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO CC_SCHEMA_MIGRATION(version,sha256)
VALUES ('V20261015_01_order_integrity.sql', '5aa9ec989fae16e317cc1e7ccca724a3d4e30d39236309c08e75168fd7f4b2ff');

-- 版本: V20261015_02_disable_obsolete_demo.sql
-- 仅停用仍带原始演示货号和模板代码的旧耳饰示例, 不依据ID范围处理商家商品.
UPDATE CC_CUSTOM_PRODUCT c
JOIN mall_goods g ON g.id=c.goods_id
SET c.enabled=0
WHERE g.goods_sn='DEMO-DIY-EAR-001' AND c.template_code='earring-diy-v1';

UPDATE mall_goods
SET is_on_sale=0
WHERE goods_sn='DEMO-DIY-EAR-001' AND id=1999106;
INSERT IGNORE INTO CC_SCHEMA_MIGRATION(version,sha256)
VALUES ('V20261015_02_disable_obsolete_demo.sql', '484a6ecae51d54f02e49e2db9ce066679f9174c7f19681970cb8b5d840fde7a7');

-- 版本: V20261016_01_checkout_refunds.sql
-- 订单提交键与退款对账流水, 不覆盖现有业务数据.
ALTER TABLE mall_order ADD COLUMN request_key VARCHAR(80) NULL,
  ADD COLUMN request_digest VARCHAR(64) NULL,
  ADD UNIQUE KEY uk_order_request (user_id, request_key);

CREATE TABLE CC_REFUND (
  order_id INT NOT NULL PRIMARY KEY,
  refund_no VARCHAR(64) NOT NULL,
  status VARCHAR(24) NOT NULL,
  original_status INT NOT NULL,
  amount_fen INT NOT NULL,
  reason VARCHAR(200) NOT NULL,
  operator_name VARCHAR(100) NULL,
  decision_note VARCHAR(200) NULL,
  provider_status VARCHAR(32) NULL,
  attempts INT NOT NULL DEFAULT 0,
  next_check_time DATETIME NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_refund_no (refund_no),
  KEY ix_refund_retry (status, next_check_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO CC_SCHEMA_MIGRATION(version,sha256)
VALUES ('V20261016_01_checkout_refunds.sql', '2caae186218d76a9172038230cb7c1af64793fd8a3a08c987effbdb35ca5a190');

-- 版本: V20261016_02_material_history.sql
-- 草稿版本用于乐观并发, 材料流水保留每次变更后的余额.
ALTER TABLE CC_DESIGN_SNAPSHOT ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
CREATE TABLE CC_STOCK_LOG (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  goods_id INT NOT NULL,
  component_type VARCHAR(32) NOT NULL,
  option_code VARCHAR(64) NOT NULL,
  quantity_delta INT NOT NULL,
  balance INT NOT NULL,
  operator_name VARCHAR(100) NOT NULL,
  reason VARCHAR(200) NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_stock_history (goods_id, component_type, option_code, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO CC_SCHEMA_MIGRATION(version,sha256)
VALUES ('V20261016_02_material_history.sql', 'd90a64a7e7b03bf20efa31cfd530d0ba6e09696c79a5c2b8d78b2e5fb8804245');

-- 版本: V20261019_01_community.sql
-- 社区只提供发布和点赞, 不建立评论、私信或好友关系.
CREATE TABLE CC_COMMUNITY_POST (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  request_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  request_hash CHAR(64) CHARACTER SET ascii NOT NULL,
  kind VARCHAR(16) NOT NULL,
  title VARCHAR(60) NOT NULL,
  content VARCHAR(2000) NOT NULL,
  images_json JSON NOT NULL,
  design_json JSON NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  version BIGINT NOT NULL DEFAULT 0,
  moderation_reason VARCHAR(300) NOT NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_request (user_id, request_key),
  KEY ix_post_feed (status, id),
  KEY ix_post_owner (user_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE CC_COMMUNITY_LIKE (
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (post_id, user_id),
  KEY ix_like_owner (user_id, post_id),
  CONSTRAINT fk_community_like_post FOREIGN KEY (post_id) REFERENCES CC_COMMUNITY_POST(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE CC_COMMUNITY_MEDIA (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  url VARCHAR(1000) NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_media_owner (user_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE CC_COMMUNITY_LOG (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  post_id BIGINT NOT NULL,
  operator_name VARCHAR(100) NOT NULL,
  from_status VARCHAR(16) NOT NULL,
  to_status VARCHAR(16) NOT NULL,
  reason VARCHAR(300) NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_community_log (post_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO SYS_MENU (menu_id, parent_id, name, url, perms, type, icon, order_num)
VALUES (9300, 0, '社区管理', 'community/posts', 'community:post:list', 1, 'menu', 45),
       (930001, 9300, '审核与下架', NULL, 'community:post:moderate', 2, NULL, 1);
INSERT IGNORE INTO CC_SCHEMA_MIGRATION(version,sha256)
VALUES ('V20261019_01_community.sql', 'fb7117094281422e1c8393cdaa2c2e5c173c5169caf5dd46dbaa9a509f95afe9');

-- 版本: V20261019_02_storefront.sql
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
INSERT IGNORE INTO CC_SCHEMA_MIGRATION(version,sha256)
VALUES ('V20261019_02_storefront.sql', 'dd631bfbdaa1118c20fbfe506f9245c09629a16bc1b34ca0a370ddfc6537caba');
