-- 不猜测旧商品工艺参数, 空规则保留草稿能力, 完整校验前禁止新定制下单.
ALTER TABLE CC_CUSTOM_PRODUCT ADD COLUMN length_rules TEXT NULL COMMENT '商家成品长度工艺规则JSON';
