-- 只冻结新订单已确认的条款版本, 不补造历史订单的确认凭据.
CREATE TABLE CC_ORDER_LEGAL (
  order_id INT NOT NULL,
  user_id BIGINT NOT NULL,
  kind VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  version INT NOT NULL,
  accepted_at DATETIME NOT NULL,
  PRIMARY KEY(order_id,kind),
  CONSTRAINT fk_order_legal_document FOREIGN KEY(kind,version)
    REFERENCES CC_LEGAL_DOCUMENT(kind,version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
