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
