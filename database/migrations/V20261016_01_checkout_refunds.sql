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
