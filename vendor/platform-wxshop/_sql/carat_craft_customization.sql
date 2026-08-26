-- CaratCraft 定制模块迁移 / Customization module migration.
CREATE TABLE IF NOT EXISTS `CC_CUSTOM_PRODUCT` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_id` int NOT NULL,
  `template_code` varchar(64) NOT NULL,
  `preview_width` int NOT NULL DEFAULT 750,
  `preview_height` int NOT NULL DEFAULT 750,
  `model_url` varchar(512) DEFAULT NULL,
  `enabled` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_cc_product_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CaratCraft定制商品 / custom product';

CREATE TABLE IF NOT EXISTS `CC_COMPONENT_OPTION` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `goods_id` int NOT NULL,
  `component_type` varchar(32) NOT NULL,
  `option_code` varchar(64) NOT NULL,
  `name` varchar(128) NOT NULL,
  `price_delta` decimal(10,2) NOT NULL DEFAULT 0.00,
  `asset_url` varchar(512) DEFAULT NULL,
  `model_url` varchar(512) DEFAULT NULL,
  `material_params` json DEFAULT NULL,
  `z_index` int NOT NULL DEFAULT 0,
  `enabled` tinyint NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_cc_option` (`goods_id`, `component_type`, `option_code`),
  KEY `idx_cc_option_goods` (`goods_id`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CaratCraft组件选项 / component option';

CREATE TABLE IF NOT EXISTS `CC_DESIGN_SNAPSHOT` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `goods_id` int NOT NULL,
  `configuration_json` json NOT NULL,
  `price_breakdown_json` json NOT NULL,
  `preview_url` varchar(512) DEFAULT NULL,
  `status` varchar(32) NOT NULL DEFAULT 'DRAFT',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), KEY `idx_cc_snapshot_user` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CaratCraft设计快照 / design snapshot';
