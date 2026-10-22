-- 统一现售SKU为具名组合, 旧格式仅在迁移时转换, 不改历史订单快照.
SET NAMES utf8mb4;
SET @cc_previous_group_concat_max_len = @@SESSION.group_concat_max_len;
SET SESSION group_concat_max_len = 65535;
DROP TEMPORARY TABLE IF EXISTS cc_sku_conversion;
CREATE TEMPORARY TABLE cc_sku_conversion AS
SELECT p.id, p.goods_id,
       COALESCE(GROUP_CONCAT(CONCAT(s.specification_id, ':', s.value)
         ORDER BY s.specification_id SEPARATOR '|'), '') AS canonical_key,
       COUNT(s.id) AS matched,
       COUNT(DISTINCT s.specification_id) AS matched_groups,
       CASE
         WHEN COALESCE(p.goods_specification_ids, '') = '' THEN 0
         WHEN p.goods_specification_ids REGEXP '^[1-9][0-9]*(_[1-9][0-9]*)*$'
           THEN 1 + CHAR_LENGTH(p.goods_specification_ids) - CHAR_LENGTH(REPLACE(p.goods_specification_ids, '_', ''))
         WHEN p.goods_specification_ids LIKE '%:%'
           THEN 1 + CHAR_LENGTH(p.goods_specification_ids) - CHAR_LENGTH(REPLACE(p.goods_specification_ids, '|', ''))
         ELSE -1
       END AS expected,
       (SELECT COUNT(DISTINCT known.specification_id) FROM mall_goods_specification known
        WHERE known.goods_id = p.goods_id) AS dimensions,
       COALESCE(SUM(s.value = '' OR s.value <> TRIM(s.value) OR s.value LIKE '%|%'
         OR s.specification_id < 1), 0) AS unsafe_values
FROM mall_product p
LEFT JOIN mall_goods_specification s ON s.goods_id = p.goods_id AND (
    (p.goods_specification_ids REGEXP '^[1-9][0-9]*(_[1-9][0-9]*)*$'
    AND LOCATE(CONCAT('_', s.id, '_'), CONCAT('_', p.goods_specification_ids, '_')) > 0)
    OR (p.goods_specification_ids LIKE '%:%' AND LOCATE(
    CAST(CONCAT('|', s.specification_id, ':', s.value, '|') AS BINARY),
    CAST(CONCAT('|', p.goods_specification_ids, '|') AS BINARY)) > 0))
GROUP BY p.id, p.goods_id, p.goods_specification_ids;

-- 任一映射不完整、名称含歧义或重复组合时, 在修改业务表前明确失败.
DROP TEMPORARY TABLE IF EXISTS cc_sku_contract_guard;
CREATE TEMPORARY TABLE cc_sku_contract_guard (
    valid_value INT NOT NULL, CONSTRAINT cc_sku_contract_valid CHECK (valid_value = 1)
);
INSERT INTO cc_sku_contract_guard
SELECT 0 FROM cc_sku_conversion
WHERE expected <> matched OR expected <> matched_groups OR expected <> dimensions
   OR unsafe_values <> 0 OR CHAR_LENGTH(canonical_key) > 2048
LIMIT 1;
INSERT INTO cc_sku_contract_guard
SELECT 0 FROM cc_sku_conversion GROUP BY goods_id, CAST(canonical_key AS BINARY)
HAVING COUNT(*) > 1 LIMIT 1;
INSERT INTO cc_sku_contract_guard
SELECT 0 FROM cc_sku_conversion GROUP BY goods_id HAVING COUNT(*) > 512 LIMIT 1;

ALTER TABLE mall_product MODIFY goods_specification_ids VARCHAR(2048) NOT NULL DEFAULT '' COMMENT '按组ID升序的具名SKU组合';
ALTER TABLE mall_cart MODIFY goods_specifition_ids VARCHAR(2048) NOT NULL DEFAULT '' COMMENT '当前SKU具名组合';
ALTER TABLE mall_order_goods MODIFY goods_specifition_ids VARCHAR(2048) DEFAULT '' COMMENT '成交时冻结的SKU组合, 历史值不改写';
UPDATE mall_product p JOIN cc_sku_conversion c ON c.id = p.id
SET p.goods_specification_ids = c.canonical_key;
UPDATE mall_cart c JOIN mall_product p ON c.product_id = p.id AND c.goods_id = p.goods_id
SET c.goods_specifition_ids = p.goods_specification_ids;
DROP TEMPORARY TABLE cc_sku_contract_guard;
DROP TEMPORARY TABLE cc_sku_conversion;
SET SESSION group_concat_max_len = @cc_previous_group_concat_max_len;
