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
