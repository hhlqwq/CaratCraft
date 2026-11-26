SET NAMES utf8mb4;

-- 已有库备份并批准后执行. 使用现有优惠券结构, 不为存量用户刷券.
INSERT INTO mall_coupon (name, type_money, send_type, min_goods_amount)
SELECT '新人优惠券', 10.00, 8, 199.00 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM mall_coupon WHERE send_type = 8)
  AND (SELECT COALESCE(MAX(id), 0) FROM mall_coupon) < 255;

SELECT id, name, send_type, type_money, min_goods_amount, send_start_date, send_end_date
FROM mall_coupon WHERE send_type = 8;
