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
