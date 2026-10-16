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
