SET NAMES utf8mb4;

-- 只补齐商品1999105目录缺失的第二个皇冠. 商家确认前保留为停用参考项.
SELECT id, goods_id, option_code, name, enabled, price_delta
FROM CC_COMPONENT_OPTION
WHERE goods_id = 1999105 AND option_code IN ('client-accessory-14', 'client-accessory-15-crown');

START TRANSACTION;

INSERT INTO CC_COMPONENT_OPTION
    (goods_id, component_type, option_code, name, price_delta, material_params, z_index, enabled)
SELECT 1999105, 'accessory', 'client-accessory-15-crown', '皇冠', 0,
    JSON_OBJECT('stockQuantity', 1, 'stockUnit', '件', 'candidateOnly', CAST('true' AS JSON),
        'sameNameNeedsConfirmation', CAST('true' AS JSON)), 72, 0
FROM DUAL
WHERE EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION
    WHERE goods_id = 1999105 AND component_type = 'accessory' AND option_code = 'client-accessory-14')
  AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_OPTION
    WHERE goods_id = 1999105 AND option_code = 'client-accessory-15-crown');
SET @cc_inserted_crown = ROW_COUNT();

INSERT INTO CC_COMPONENT_STOCK (goods_id, component_type, option_code, stock)
SELECT 1999105, 'accessory', 'client-accessory-15-crown', 0
FROM DUAL
WHERE @cc_inserted_crown = 1
  AND NOT EXISTS (SELECT 1 FROM CC_COMPONENT_STOCK
    WHERE goods_id = 1999105 AND component_type = 'accessory' AND option_code = 'client-accessory-15-crown');
COMMIT;

SELECT opt.option_code, opt.name, opt.enabled, opt.price_delta, stock.stock
FROM CC_COMPONENT_OPTION opt
LEFT JOIN CC_COMPONENT_STOCK stock ON stock.goods_id = opt.goods_id
    AND stock.component_type = opt.component_type AND stock.option_code = opt.option_code
WHERE opt.goods_id = 1999105 AND opt.option_code IN ('client-accessory-14', 'client-accessory-15-crown')
ORDER BY opt.option_code;
