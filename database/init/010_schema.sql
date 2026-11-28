SET NAMES utf8mb4;

-- 完整初始化, 仅用于新建空库; 不用于保留真实数据的升级.
SET FOREIGN_KEY_CHECKS=0;
CREATE TABLE `CC_COMMUNITY_LIKE` (
    `post_id` bigint NOT NULL,
    `user_id` bigint NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`post_id`,`user_id`),
    KEY `ix_like_owner` (`user_id`,`post_id`),
    CONSTRAINT `fk_community_like_post` FOREIGN KEY (`post_id`) REFERENCES `CC_COMMUNITY_POST` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_COMMUNITY_LOG` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `post_id` bigint NOT NULL,
    `operator_name` varchar(100) NOT NULL,
    `from_status` varchar(16) NOT NULL,
    `to_status` varchar(16) NOT NULL,
    `reason` varchar(300) NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `ix_community_log` (`post_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_COMMUNITY_MEDIA` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL,
    `url` varchar(1000) NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `ix_media_owner` (`user_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_COMMUNITY_POST` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL,
    `request_key` varchar(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    `request_hash` char(64) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL,
    `kind` varchar(16) NOT NULL,
    `title` varchar(60) NOT NULL,
    `content` varchar(2000) NOT NULL,
    `images_json` json NOT NULL,
    `design_json` json DEFAULT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'PENDING',
    `version` bigint NOT NULL DEFAULT '0',
    `moderation_reason` varchar(300) NOT NULL DEFAULT '',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `scope_org_no` varchar(10) DEFAULT NULL,
    `staff_deleted` tinyint NOT NULL DEFAULT 0,
    KEY `ix_community_scope_org` (`scope_org_no`),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_request` (`user_id`,`request_key`),
    KEY `ix_post_feed` (`status`,`id`),
    KEY `ix_post_owner` (`user_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_COMPONENT_OPTION` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `goods_id` int NOT NULL,
    `component_type` varchar(32) NOT NULL,
    `option_code` varchar(64) NOT NULL,
    `name` varchar(128) NOT NULL,
    `price_delta` decimal(10,2) NOT NULL DEFAULT '0.00',
    `asset_url` varchar(512) DEFAULT NULL,
    `model_url` varchar(512) DEFAULT NULL,
    `material_params` json DEFAULT NULL,
    `z_index` int NOT NULL DEFAULT '0',
    `enabled` tinyint NOT NULL DEFAULT '1',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cc_option` (`goods_id`,`component_type`,`option_code`),
    KEY `idx_cc_option_goods` (`goods_id`,`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=240 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CaratCraft组件选项 / component option';
CREATE TABLE `CC_COMPONENT_STOCK` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `goods_id` int NOT NULL,
    `component_type` varchar(32) NOT NULL,
    `option_code` varchar(64) NOT NULL,
    `stock` int NOT NULL DEFAULT '0',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cc_stock` (`goods_id`,`component_type`,`option_code`)
) ENGINE=InnoDB AUTO_INCREMENT=240 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CaratCraft组件库存 / component stock';
CREATE TABLE `CC_CUSTOM_PRODUCT` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `goods_id` int NOT NULL,
    `template_code` varchar(64) NOT NULL,
    `preview_width` int NOT NULL DEFAULT '750',
    `preview_height` int NOT NULL DEFAULT '750',
    `model_url` varchar(512) DEFAULT NULL,
    `required_component_types` varchar(512) DEFAULT NULL,
    `enabled` tinyint NOT NULL DEFAULT '1',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `length_rules` text COMMENT '商家成品长度工艺规则JSON',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cc_product_goods` (`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CaratCraft定制商品 / custom product';
CREATE TABLE `CC_DESIGN_SNAPSHOT` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint DEFAULT NULL,
    `goods_id` int NOT NULL,
    `configuration_json` json NOT NULL,
    `price_breakdown_json` json NOT NULL,
    `preview_url` varchar(512) DEFAULT NULL,
    `status` varchar(32) NOT NULL DEFAULT 'DRAFT',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `version` bigint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `idx_cc_snapshot_user` (`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CaratCraft设计快照 / design snapshot';
CREATE TABLE `CC_LEGAL_ACCEPTANCE` (
    `user_id` bigint NOT NULL,
    `kind` varchar(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    `version` int NOT NULL,
    `accepted_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`,`kind`,`version`),
    KEY `fk_acceptance_document` (`kind`,`version`),
    CONSTRAINT `fk_acceptance_document` FOREIGN KEY (`kind`, `version`) REFERENCES `CC_LEGAL_DOCUMENT` (`kind`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_LEGAL_DOCUMENT` (
    `kind` varchar(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    `version` int NOT NULL,
    `title` varchar(120) NOT NULL,
    `content` mediumtext NOT NULL,
    `status` varchar(16) NOT NULL DEFAULT 'DRAFT',
    `operator_id` varchar(64) NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `published_at` datetime DEFAULT NULL,
    PRIMARY KEY (`kind`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_LEGAL_HEAD` (
    `kind` varchar(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    `revision` int NOT NULL DEFAULT '0',
    `published_version` int NOT NULL DEFAULT '0',
    PRIMARY KEY (`kind`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_LOGIN_IDENTITY_LOCK` (
    `identity_hash` char(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    `user_id` bigint DEFAULT NULL COMMENT '经服务端验证后绑定的账户, 不保存原始身份',
    `touched_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`identity_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_MERCHANT_CONTACT` (
    `id` int NOT NULL,
    `merchant_name` varchar(120) NOT NULL DEFAULT '',
    `phone` varchar(30) NOT NULL DEFAULT '',
    `wechat_id` varchar(64) NOT NULL DEFAULT '',
    `qr_url` varchar(1000) NOT NULL DEFAULT '',
    `wx_chat_enabled` tinyint NOT NULL DEFAULT '0',
    `version` int NOT NULL DEFAULT '0',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_MERCHANT_CONTACT_LOG` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `version` int NOT NULL,
    `operator_id` varchar(64) NOT NULL,
    `snapshot_json` json NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_contact_version` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_ORDER_CUSTOMIZATION` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `order_id` int NOT NULL,
    `order_goods_id` int NOT NULL,
    `snapshot_id` bigint NOT NULL,
    `status` varchar(32) NOT NULL DEFAULT 'LOCKED',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cc_order_customization` (`order_id`),
    UNIQUE KEY `uk_cc_order_snapshot` (`snapshot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CaratCraft定制订单关联 / custom order binding';
CREATE TABLE `CC_ORDER_LEGAL` (
    `order_id` int NOT NULL,
    `user_id` bigint NOT NULL,
    `kind` varchar(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    `version` int NOT NULL,
    `accepted_at` datetime NOT NULL,
    PRIMARY KEY (`order_id`,`kind`),
    KEY `fk_order_legal_document` (`kind`,`version`),
    CONSTRAINT `fk_order_legal_document` FOREIGN KEY (`kind`, `version`) REFERENCES `CC_LEGAL_DOCUMENT` (`kind`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_ORDER_PRICE_LOG` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `order_id` int NOT NULL,
    `old_price` decimal(12,2) NOT NULL,
    `new_price` decimal(12,2) NOT NULL,
    `adjustment` decimal(12,2) NOT NULL,
    `operator_id` varchar(64) NOT NULL,
    `reason` varchar(200) NOT NULL,
    `create_time` datetime NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_order_price_log_order` (`order_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_PRODUCTION_WORK_ORDER` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `work_order_no` varchar(64) NOT NULL,
    `order_id` int NOT NULL,
    `order_goods_id` int NOT NULL,
    `snapshot_id` bigint NOT NULL,
    `status` varchar(32) NOT NULL DEFAULT 'PENDING_REVIEW',
    `craft_json` json NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cc_work_order_no` (`work_order_no`),
    UNIQUE KEY `uk_cc_work_order_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CaratCraft生产工单 / production work order';
CREATE TABLE `CC_PRODUCT_VIDEO` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `operator_id` varchar(64) NOT NULL,
    `url` varchar(1000) NOT NULL DEFAULT '',
    `duration_ms` int NOT NULL DEFAULT '0',
    `width` int NOT NULL DEFAULT '0',
    `height` int NOT NULL DEFAULT '0',
    `byte_size` int NOT NULL DEFAULT '0',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `ix_video_operator` (`operator_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_REFUND` (
    `order_id` int NOT NULL,
    `refund_no` varchar(64) NOT NULL,
    `status` varchar(24) NOT NULL,
    `original_status` int NOT NULL,
    `amount_fen` int NOT NULL,
    `reason` varchar(200) NOT NULL,
    `operator_name` varchar(100) DEFAULT NULL,
    `decision_note` varchar(200) DEFAULT NULL,
    `provider_status` varchar(32) DEFAULT NULL,
    `attempts` int NOT NULL DEFAULT '0',
    `next_check_time` datetime DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `review_result` varchar(12) DEFAULT NULL,
    `review_note` varchar(200) DEFAULT NULL,
    `reviewed_by` varchar(32) DEFAULT NULL,
    `reviewed_at` datetime DEFAULT NULL,
    `support_note` varchar(200) DEFAULT NULL,
    `support_handled_at` datetime DEFAULT NULL,
    PRIMARY KEY (`order_id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    KEY `ix_refund_retry` (`status`,`next_check_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_STOCK_LOG` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `goods_id` int NOT NULL,
    `component_type` varchar(32) NOT NULL,
    `option_code` varchar(64) NOT NULL,
    `quantity_delta` int NOT NULL,
    `balance` int NOT NULL,
    `operator_name` varchar(100) NOT NULL,
    `reason` varchar(200) NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `ix_stock_history` (`goods_id`,`component_type`,`option_code`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `CC_WORK_ORDER_LOG` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `work_order_id` bigint NOT NULL,
    `operator_id` bigint DEFAULT NULL,
    `operator_name` varchar(64) DEFAULT NULL,
    `from_status` varchar(32) DEFAULT NULL,
    `to_status` varchar(32) NOT NULL,
    `remark` varchar(512) DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_cc_wol_work_order` (`work_order_id`),
    KEY `idx_cc_wol_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CaratCraft工单操作日志 / work order operation log';
CREATE TABLE `QRTZ_BLOB_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `BLOB_DATA` blob,
    PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    KEY `SCHED_NAME` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_BLOB_TRIGGERS_IBFK_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `QRTZ_CALENDARS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `CALENDAR_NAME` varchar(200) NOT NULL,
    `CALENDAR` blob NOT NULL,
    PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `QRTZ_CRON_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `CRON_EXPRESSION` varchar(120) NOT NULL,
    `TIME_ZONE_ID` varchar(80) DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_CRON_TRIGGERS_IBFK_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `QRTZ_FIRED_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `ENTRY_ID` varchar(95) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `INSTANCE_NAME` varchar(200) NOT NULL,
    `FIRED_TIME` bigint NOT NULL,
    `SCHED_TIME` bigint NOT NULL,
    `PRIORITY` int NOT NULL,
    `STATE` varchar(16) NOT NULL,
    `JOB_NAME` varchar(200) DEFAULT NULL,
    `JOB_GROUP` varchar(200) DEFAULT NULL,
    `IS_NONCONCURRENT` varchar(1) DEFAULT NULL,
    `REQUESTS_RECOVERY` varchar(1) DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`),
    KEY `IDX_QRTZ_FT_TRIG_INST_NAME` (`SCHED_NAME`,`INSTANCE_NAME`),
    KEY `IDX_QRTZ_FT_INST_JOB_REQ_RCVRY` (`SCHED_NAME`,`INSTANCE_NAME`,`REQUESTS_RECOVERY`),
    KEY `IDX_QRTZ_FT_J_G` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
    KEY `IDX_QRTZ_FT_JG` (`SCHED_NAME`,`JOB_GROUP`),
    KEY `IDX_QRTZ_FT_T_G` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    KEY `IDX_QRTZ_FT_TG` (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `QRTZ_JOB_DETAILS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `JOB_NAME` varchar(200) NOT NULL,
    `JOB_GROUP` varchar(200) NOT NULL,
    `DESCRIPTION` varchar(250) DEFAULT NULL,
    `JOB_CLASS_NAME` varchar(250) NOT NULL,
    `IS_DURABLE` varchar(1) NOT NULL,
    `IS_NONCONCURRENT` varchar(1) NOT NULL,
    `IS_UPDATE_DATA` varchar(1) NOT NULL,
    `REQUESTS_RECOVERY` varchar(1) NOT NULL,
    `JOB_DATA` blob,
    PRIMARY KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
    KEY `IDX_QRTZ_J_REQ_RECOVERY` (`SCHED_NAME`,`REQUESTS_RECOVERY`),
    KEY `IDX_QRTZ_J_GRP` (`SCHED_NAME`,`JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `QRTZ_LOCKS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `LOCK_NAME` varchar(40) NOT NULL,
    PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `QRTZ_SCHEDULER_STATE` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `INSTANCE_NAME` varchar(200) NOT NULL,
    `LAST_CHECKIN_TIME` bigint NOT NULL,
    `CHECKIN_INTERVAL` bigint NOT NULL,
    PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `QRTZ_SIMPLE_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `REPEAT_COUNT` bigint NOT NULL,
    `REPEAT_INTERVAL` bigint NOT NULL,
    `TIMES_TRIGGERED` bigint NOT NULL,
    PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_IBFK_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `QRTZ_SIMPROP_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `STR_PROP_1` varchar(512) DEFAULT NULL,
    `STR_PROP_2` varchar(512) DEFAULT NULL,
    `STR_PROP_3` varchar(512) DEFAULT NULL,
    `INT_PROP_1` int DEFAULT NULL,
    `INT_PROP_2` int DEFAULT NULL,
    `LONG_PROP_1` bigint DEFAULT NULL,
    `LONG_PROP_2` bigint DEFAULT NULL,
    `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
    `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
    `BOOL_PROP_1` varchar(1) DEFAULT NULL,
    `BOOL_PROP_2` varchar(1) DEFAULT NULL,
    PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_IBFK_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `QRTZ_TRIGGERS` (
    `SCHED_NAME` varchar(120) NOT NULL,
    `TRIGGER_NAME` varchar(200) NOT NULL,
    `TRIGGER_GROUP` varchar(200) NOT NULL,
    `JOB_NAME` varchar(200) NOT NULL,
    `JOB_GROUP` varchar(200) NOT NULL,
    `DESCRIPTION` varchar(250) DEFAULT NULL,
    `NEXT_FIRE_TIME` bigint DEFAULT NULL,
    `PREV_FIRE_TIME` bigint DEFAULT NULL,
    `PRIORITY` int DEFAULT NULL,
    `TRIGGER_STATE` varchar(16) NOT NULL,
    `TRIGGER_TYPE` varchar(8) NOT NULL,
    `START_TIME` bigint NOT NULL,
    `END_TIME` bigint DEFAULT NULL,
    `CALENDAR_NAME` varchar(200) DEFAULT NULL,
    `MISFIRE_INSTR` smallint DEFAULT NULL,
    `JOB_DATA` blob,
    PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
    KEY `IDX_QRTZ_T_J` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
    KEY `IDX_QRTZ_T_JG` (`SCHED_NAME`,`JOB_GROUP`),
    KEY `IDX_QRTZ_T_C` (`SCHED_NAME`,`CALENDAR_NAME`),
    KEY `IDX_QRTZ_T_G` (`SCHED_NAME`,`TRIGGER_GROUP`),
    KEY `IDX_QRTZ_T_STATE` (`SCHED_NAME`,`TRIGGER_STATE`),
    KEY `IDX_QRTZ_T_N_STATE` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
    KEY `IDX_QRTZ_T_N_G_STATE` (`SCHED_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
    KEY `IDX_QRTZ_T_NEXT_FIRE_TIME` (`SCHED_NAME`,`NEXT_FIRE_TIME`),
    KEY `IDX_QRTZ_T_NFT_ST` (`SCHED_NAME`,`TRIGGER_STATE`,`NEXT_FIRE_TIME`),
    KEY `IDX_QRTZ_T_NFT_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`),
    KEY `IDX_QRTZ_T_NFT_ST_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_STATE`),
    KEY `IDX_QRTZ_T_NFT_ST_MISFIRE_GRP` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
    CONSTRAINT `QRTZ_TRIGGERS_IBFK_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE `SCHEDULE_JOB` (
    `JOB_ID` varchar(32) NOT NULL COMMENT '任务id',
    `BEAN_NAME` varchar(200) DEFAULT NULL COMMENT 'spring bean名称',
    `METHOD_NAME` varchar(100) DEFAULT NULL COMMENT '方法名',
    `PARAMS` varchar(2000) DEFAULT NULL COMMENT '参数',
    `CRON_EXPRESSION` varchar(100) DEFAULT NULL COMMENT 'cron表达式',
    `STATUS` tinyint DEFAULT NULL COMMENT '任务状态  0：正常  1：暂停',
    `REMARK` varchar(256) DEFAULT NULL COMMENT '备注',
    `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`JOB_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='定时任务';
CREATE TABLE `SCHEDULE_JOB_LOG` (
    `LOG_ID` varchar(32) NOT NULL COMMENT '任务日志id',
    `JOB_ID` varchar(32) NOT NULL COMMENT '任务id',
    `BEAN_NAME` varchar(200) DEFAULT NULL COMMENT 'spring bean名称',
    `METHOD_NAME` varchar(100) DEFAULT NULL COMMENT '方法名',
    `PARAMS` varchar(2000) DEFAULT NULL COMMENT '参数',
    `STATUS` tinyint NOT NULL COMMENT '任务状态    0：成功    1：失败',
    `ERROR` varchar(2000) DEFAULT NULL COMMENT '失败信息',
    `TIMES` int NOT NULL COMMENT '耗时(单位：毫秒)',
    `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`LOG_ID`),
    KEY `JOB_ID` (`JOB_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='定时任务日志';
CREATE TABLE `SYS_CAPTCHA` (
    `UUID` char(36) NOT NULL COMMENT 'uuid',
    `CODE` varchar(6) NOT NULL COMMENT '验证码',
    `EXPIRE_TIME` datetime DEFAULT NULL COMMENT '过期时间',
    PRIMARY KEY (`UUID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统验证码';
CREATE TABLE `SYS_CONFIG` (
    `ID` varchar(32) NOT NULL,
    `PARAM_KEY` varchar(50) DEFAULT NULL COMMENT 'key',
    `PARAM_VALUE` text COMMENT 'value',
    `STATUS` tinyint DEFAULT '1' COMMENT '状态   0：隐藏   1：显示',
    `REMARK` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `PARAM_KEY` (`PARAM_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统配置信息表';
CREATE TABLE `SYS_DICT` (
    `ID` varchar(32) NOT NULL,
    `GROUP_ID` varchar(32) DEFAULT NULL COMMENT '所属分组ID',
    `NAME` varchar(100) DEFAULT NULL COMMENT '字典名称',
    `VALUE` varchar(64) DEFAULT NULL COMMENT '字典值',
    `SORT` int DEFAULT NULL COMMENT '排序号',
    `STATUS` int DEFAULT NULL COMMENT '状态码',
    `REMARK` text COMMENT '备注',
    PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据字典';
CREATE TABLE `SYS_DICT_GROUP` (
    `ID` varchar(32) NOT NULL,
    `CODE` varchar(64) NOT NULL COMMENT '分组编码',
    `NAME` varchar(100) DEFAULT NULL COMMENT '分组名称',
    `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
    `REMARK` text COMMENT '备注',
    PRIMARY KEY (`ID`,`CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='数据字典分组';
CREATE TABLE `SYS_LOG` (
    `ID` varchar(32) NOT NULL,
    `USER_NAME` varchar(50) DEFAULT NULL COMMENT '用户名',
    `OPERATION` varchar(50) DEFAULT NULL COMMENT '用户操作',
    `METHOD` varchar(200) DEFAULT NULL COMMENT '请求方法',
    `PARAMS` varchar(5000) DEFAULT NULL COMMENT '请求参数',
    `TIME` bigint NOT NULL COMMENT '执行时长(毫秒)',
    `IP` varchar(64) DEFAULT NULL COMMENT 'IP地址',
    `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
    `SCOPE_ORG_NO` varchar(10) DEFAULT NULL COMMENT '操作人当时所属机构',
    `actor_id` varchar(32) DEFAULT NULL,
    `actor_type` varchar(16) DEFAULT NULL,
    `module` varchar(50) DEFAULT NULL,
    `operation_type` varchar(32) DEFAULT NULL,
    `target_id` varchar(128) DEFAULT NULL,
    `result` varchar(16) DEFAULT NULL,
    `summary` varchar(1000) DEFAULT NULL,
    `error_code` varchar(40) DEFAULT NULL,
    PRIMARY KEY (`ID`),
    KEY `ix_log_scope_org` (`SCOPE_ORG_NO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统日志';
CREATE TABLE `SYS_MENU` (
    `MENU_ID` varchar(8) NOT NULL,
    `PARENT_ID` varchar(8) DEFAULT NULL COMMENT '父菜单ID，一级菜单为0',
    `NAME` varchar(50) DEFAULT NULL COMMENT '菜单名称',
    `URL` varchar(200) DEFAULT NULL COMMENT '菜单URL',
    `PERMS` varchar(500) DEFAULT NULL COMMENT '授权(多个用逗号分隔，如：user:list,user:create)',
    `TYPE` tinyint DEFAULT NULL COMMENT '类型   0：目录   1：菜单   2：按钮',
    `ICON` varchar(50) DEFAULT NULL COMMENT '菜单图标',
    `ORDER_NUM` int DEFAULT NULL COMMENT '排序',
    PRIMARY KEY (`MENU_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单管理';
CREATE TABLE `SYS_ORG` (
    `ORG_NO` varchar(10) NOT NULL COMMENT '机构编码',
    `ORG_NAME` varchar(50) DEFAULT NULL COMMENT '部门名称',
    `PARENT_NO` varchar(10) DEFAULT NULL COMMENT '上级部门ID，一级部门为0',
    `ORG_TYPE` int DEFAULT NULL COMMENT '级别',
    `STATUS` int DEFAULT '1' COMMENT '状态  0：无效   1：有效',
    `SORT` int DEFAULT NULL COMMENT '排序',
    `CREATE_USER_ID` varchar(32) DEFAULT NULL COMMENT '创建者ID',
    `CREATE_USER_ORG_NO` varchar(32) DEFAULT NULL COMMENT '创建人所属机构',
    `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`ORG_NO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='组织机构';
CREATE TABLE `SYS_OSS` (
    `ID` varchar(32) NOT NULL,
    `URL` varchar(1000) DEFAULT NULL COMMENT 'URL地址',
    `CREATE_USER_ID` varchar(32) DEFAULT NULL COMMENT '创建者ID',
    `CREATE_USER_ORG_NO` varchar(32) DEFAULT NULL COMMENT '创建人所属机构',
    `CREATE_DATE` datetime DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件上传';
CREATE TABLE `SYS_ROLE` (
    `ROLE_ID` varchar(32) NOT NULL,
    `ROLE_NAME` varchar(100) DEFAULT NULL COMMENT '角色名称',
    `REMARK` varchar(100) DEFAULT NULL COMMENT '备注',
    `CREATE_USER_ID` varchar(32) DEFAULT NULL COMMENT '创建者ID',
    `CREATE_USER_ORG_NO` varchar(32) DEFAULT NULL COMMENT '创建者所属机构',
    `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
    `DELEGABLE` tinyint NOT NULL DEFAULT 0,
    `DIRECT_USER_ID` varchar(32) DEFAULT NULL,
    PRIMARY KEY (`ROLE_ID`),
    UNIQUE KEY `uk_sys_role_direct_user` (`DIRECT_USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色';
CREATE TABLE `SYS_ROLE_MENU` (
    `ID` varchar(32) NOT NULL,
    `ROLE_ID` varchar(32) DEFAULT NULL COMMENT '角色ID',
    `MENU_ID` varchar(8) DEFAULT NULL COMMENT '菜单ID',
    PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色与菜单对应关系';
CREATE TABLE `SYS_ROLE_ORG` (
    `ID` varchar(32) NOT NULL,
    `ROLE_ID` varchar(32) DEFAULT NULL COMMENT '角色ID',
    `ORG_NO` varchar(32) DEFAULT NULL COMMENT '部门ID',
    PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色与机构对应关系';
CREATE TABLE `SYS_SMS_LOG` (
    `ID` varchar(32) NOT NULL COMMENT '主键',
    `TEMPLATE_ID` varchar(32) DEFAULT NULL COMMENT '模板ID',
    `CODE` varchar(32) DEFAULT NULL COMMENT '验证码',
    `MOBILE` text COMMENT '手机号码。多个以英文逗号隔开',
    `STIME` datetime DEFAULT NULL COMMENT '发送时间',
    `SIGN` varchar(32) DEFAULT NULL COMMENT '必填参数。用户签名',
    `SEND_STATUS` int DEFAULT NULL COMMENT '0成功 1失败',
    `SEND_ID` varchar(32) DEFAULT NULL COMMENT '发送编号',
    `SUCCESS_NUM` int DEFAULT NULL COMMENT '成功提交数',
    `RETURN_MSG` varchar(50) DEFAULT NULL COMMENT '返回消息',
    PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='短信发送日志';
CREATE TABLE `SYS_USER` (
    `USER_ID` varchar(32) NOT NULL,
    `USER_NAME` varchar(50) NOT NULL COMMENT '用户名',
    `REAL_NAME` varchar(64) NOT NULL,
    `SEX` tinyint NOT NULL,
    `ORG_NO` varchar(32) DEFAULT NULL COMMENT '机构编码',
    `SALT` varchar(20) DEFAULT NULL COMMENT '盐',
    `EMAIL_HOST` varchar(32) DEFAULT NULL COMMENT '邮件服务器地址',
    `EMAIL` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `EMAIL_PW` varchar(64) DEFAULT NULL COMMENT '用户邮箱密码',
    `MOBILE` varchar(100) DEFAULT NULL COMMENT '手机号',
    `STATUS` tinyint DEFAULT NULL COMMENT '状态  0：禁用   1：正常',
    `PASSWORD` varchar(100) DEFAULT NULL COMMENT '密码',
    `CREATE_USER_ID` varchar(32) DEFAULT NULL COMMENT '创建者ID',
    `CREATE_USER_ORG_NO` varchar(32) DEFAULT NULL COMMENT '创建人所属机构',
    `CREATE_TIME` datetime NOT NULL COMMENT '创建时间',
    `LAST_LOGIN_AT` datetime DEFAULT NULL,
    `PASSWORD_CHANGE_REQUIRED` tinyint(1) NOT NULL DEFAULT 0,
    `ACCOUNT_TYPE` varchar(16) NOT NULL DEFAULT 'EMPLOYEE',
    `TEMPORARY_PASSWORD_EXPIRES_AT` datetime DEFAULT NULL,
    CONSTRAINT `ck_sys_user_account_type` CHECK (
        (`USER_ID` = '1' AND `ACCOUNT_TYPE` = 'PLATFORM_ADMIN')
        OR (`USER_ID` <> '1' AND `ACCOUNT_TYPE` IN ('MERCHANT_ADMIN', 'EMPLOYEE'))
    ),
    PRIMARY KEY (`USER_ID`),
    UNIQUE KEY `USERNAME` (`USER_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统用户';
CREATE TABLE `SYS_USER_ROLE` (
    `ID` varchar(32) NOT NULL,
    `USER_ID` varchar(32) DEFAULT NULL COMMENT '用户ID',
    `ROLE_ID` varchar(32) DEFAULT NULL COMMENT '角色ID',
    PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户与角色对应关系';
CREATE TABLE `SYS_USER_TOKEN` (
    `USER_ID` varchar(32) NOT NULL,
    `TOKEN` varchar(100) NOT NULL COMMENT 'token',
    `EXPIRE_TIME` datetime DEFAULT NULL COMMENT '过期时间',
    `UPDATE_TIME` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`USER_ID`),
    UNIQUE KEY `TOKEN` (`TOKEN`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统用户Token';
CREATE TABLE `WX_MSG` (
    `ID` varchar(32) NOT NULL COMMENT '主键',
    `OPENID` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '微信用户ID',
    `IN_OUT` tinyint unsigned DEFAULT NULL COMMENT '消息方向',
    `MSG_TYPE` char(25) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '消息类型',
    `DETAIL` json DEFAULT NULL COMMENT '消息详情',
    `CREATE_TIME` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='微信消息';
CREATE TABLE `WX_MSG_REPLY_RULE` (
    `RULE_ID` varchar(32) NOT NULL,
    `RULE_NAME` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '规则名称',
    `MATCH_VALUE` varchar(200) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '匹配的关键词、事件等',
    `EXACT_MATCH` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否精确匹配',
    `REPLY_TYPE` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT '1' COMMENT '回复消息类型',
    `REPLY_CONTENT` varchar(1024) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '回复消息内容',
    `STATUS` tinyint(1) NOT NULL DEFAULT '1' COMMENT '规则是否有效',
    `DESC` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '备注说明',
    `EFFECT_TIME_START` time DEFAULT '00:00:00' COMMENT '生效起始时间',
    `EFFECT_TIME_END` time DEFAULT '23:59:59' COMMENT '生效结束时间',
    `PRIORITY` int unsigned DEFAULT '0' COMMENT '规则优先级',
    `UPDATE_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`RULE_ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='自动回复规则';
CREATE TABLE `WX_MSG_TEMPLATE` (
    `ID` varchar(32) NOT NULL COMMENT 'ID',
    `TEMPLATE_ID` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '公众号模板ID',
    `NAME` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '模版名称',
    `TITLE` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '标题',
    `CONTENT` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci COMMENT '模板内容',
    `DATA` json DEFAULT NULL COMMENT '消息内容',
    `URL` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '链接',
    `MINIPROGRAM` json DEFAULT NULL COMMENT '小程序信息',
    `STATUS` tinyint unsigned NOT NULL COMMENT '是否有效',
    `UPDATE_TIME` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`ID`) USING BTREE,
    UNIQUE KEY `IDX_NAME` (`NAME`) USING BTREE COMMENT '模板名称',
    KEY `IDX_STATUS` (`STATUS`) USING BTREE COMMENT '模板状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='消息模板';
CREATE TABLE `WX_QR_CODE` (
    `ID` varchar(32) NOT NULL COMMENT 'ID',
    `IS_TEMP` tinyint(1) DEFAULT NULL COMMENT '是否为临时二维码',
    `SCENE_STR` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '场景值ID',
    `TICKET` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '二维码ticket',
    `URL` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '二维码图片解析后的地址',
    `EXPIRE_TIME` datetime DEFAULT NULL COMMENT '该二维码失效时间',
    `CREATE_TIME` datetime DEFAULT NULL COMMENT '该二维码创建时间',
    PRIMARY KEY (`ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='公众号带参二维码';
CREATE TABLE `WX_TEMPLATE_MSG_LOG` (
    `LOG_ID` varchar(32) NOT NULL COMMENT 'ID',
    `TOUSER` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户openid',
    `TEMPLATE_ID` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT 'templateid',
    `DATA` json DEFAULT NULL COMMENT '消息数据',
    `URL` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '消息链接',
    `MINIPROGRAM` json DEFAULT NULL COMMENT '小程序信息',
    `SEND_TIME` datetime DEFAULT NULL COMMENT '发送时间',
    `SEND_RESULT` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '发送结果',
    PRIMARY KEY (`LOG_ID`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='微信模版消息发送记录';
CREATE TABLE `WX_USER` (
    `ID` mediumint unsigned NOT NULL AUTO_INCREMENT,
    `MA_OPENID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
    `OPENID` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
    `PHONE` char(32) DEFAULT NULL COMMENT '手机号',
    `NICKNAME` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '微信昵称',
    `SEX` tinyint DEFAULT NULL COMMENT '性别(0-未知、1-男、2-女)',
    `CITY` varchar(32) DEFAULT NULL COMMENT '城市',
    `PROVINCE` varchar(32) DEFAULT NULL COMMENT '省份',
    `HEAD_IMG_URL` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '用户头像',
    `SUBSCRIBE_TIME` datetime DEFAULT NULL COMMENT '订阅时间',
    `SUBSCRIBE` tinyint unsigned DEFAULT '1' COMMENT '是否关注',
    `UNION_ID` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
    `REMARK` varchar(256) DEFAULT NULL COMMENT '备注',
    `TAGID_LIST` json DEFAULT NULL COMMENT '标签ID列表',
    `SUBSCRIBE_SCENE` varchar(50) DEFAULT NULL COMMENT '关注场景',
    `QR_SCENE_STR` varchar(64) DEFAULT NULL COMMENT '扫码场景值',
    `ALL_INTEGRAL` int DEFAULT '0' COMMENT '总积分',
    `INTEGRAL` int DEFAULT '0' COMMENT '剩余积分',
    `USER_LEVEL_ID` varchar(32) DEFAULT NULL COMMENT '会员等级ID',
    `REGISTER_TIME` datetime DEFAULT NULL COMMENT '注册时间',
    `REGISTER_IP` varchar(64) DEFAULT NULL COMMENT '注册ip',
    `LAST_LOGIN_TIME` datetime DEFAULT NULL COMMENT '最后登录时间',
    `LAST_LOGIN_IP` varchar(32) DEFAULT NULL COMMENT '最后登录IP',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UNION_ID` (`UNION_ID`),
    UNIQUE KEY `uk_cc_ma_openid` (`MA_OPENID`),
    UNIQUE KEY `uk_cc_openid` (`OPENID`),
    KEY `IDX_UNIONID` (`UNION_ID`) USING BTREE COMMENT 'unionId'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='会员表';
CREATE TABLE `mall_ad` (
    `id` smallint unsigned NOT NULL AUTO_INCREMENT,
    `ad_position_id` smallint unsigned NOT NULL DEFAULT '0',
    `media_type` tinyint unsigned NOT NULL DEFAULT '0',
    `name` varchar(60) DEFAULT '',
    `link` varchar(255) DEFAULT '',
    `image_url` text,
    `content` varchar(255) DEFAULT '',
    `end_time` datetime DEFAULT NULL,
    `enabled` tinyint unsigned DEFAULT '1',
    PRIMARY KEY (`id`),
    KEY `position_id` (`ad_position_id`),
    KEY `enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=9001 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_ad_position` (
    `id` tinyint unsigned NOT NULL AUTO_INCREMENT,
    `name` varchar(60) NOT NULL DEFAULT '''',
    `width` smallint unsigned NOT NULL DEFAULT '0',
    `height` smallint unsigned NOT NULL DEFAULT '0',
    `desc` varchar(255) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_address` (
    `id` mediumint unsigned NOT NULL AUTO_INCREMENT,
    `user_id` mediumint unsigned DEFAULT '0' COMMENT '会员ID',
    `user_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT '' COMMENT '收货人姓名',
    `is_default` int DEFAULT '0',
    `tel_number` varchar(60) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT '' COMMENT '手机',
    `postal_Code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT '' COMMENT '邮编',
    `national_Code` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT '0' COMMENT '收货地址国家码',
    `province_Name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT '0' COMMENT '省',
    `city_Name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT '0' COMMENT '市',
    `county_Name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT '0' COMMENT '区',
    `detail_Info` varchar(120) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT '' COMMENT '详细收货地址信息',
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;
CREATE TABLE `mall_attribute` (
    `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `attribute_category_id` int unsigned NOT NULL DEFAULT '0' COMMENT '商品类型',
    `name` varchar(60) NOT NULL DEFAULT '' COMMENT '属性名称',
    `input_type` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '当添加商品时,该属性的添加类别; 0为手功输入;1为选择输入;2为多行文本输入',
    `value` text NOT NULL COMMENT '即选择输入,则attr_name对应的值的取值就是该这字段值 ',
    `sort_order` tinyint unsigned NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `cat_id` (`attribute_category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=utf8mb3 COMMENT='商品参数属性';
CREATE TABLE `mall_attribute_category` (
    `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(60) NOT NULL DEFAULT '',
    `enabled` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '1开启; 0关闭;',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1036002 DEFAULT CHARSET=utf8mb3 COMMENT='商品参数类型';
CREATE TABLE `mall_brand` (
    `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(255) NOT NULL DEFAULT '' COMMENT '品牌名称',
    `list_pic_url` varchar(255) NOT NULL DEFAULT '' COMMENT '图片',
    `simple_desc` varchar(255) NOT NULL DEFAULT '' COMMENT '描述',
    `pic_url` varchar(255) NOT NULL DEFAULT '' COMMENT '图片',
    `sort_order` tinyint unsigned DEFAULT '50' COMMENT '排序',
    `is_show` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '显示',
    `floor_price` decimal(10,2) DEFAULT '0.00',
    `app_list_pic_url` varchar(255) NOT NULL DEFAULT '' COMMENT 'app显示图片',
    `is_new` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '新品牌',
    `new_pic_url` varchar(255) NOT NULL DEFAULT '' COMMENT '图片',
    `new_sort_order` tinyint unsigned NOT NULL DEFAULT '10' COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `is_show` (`is_show`)
) ENGINE=InnoDB AUTO_INCREMENT=1046011 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_cart` (
    `id` mediumint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '会员Id',
    `session_id` char(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin NOT NULL DEFAULT '' COMMENT 'sessionId',
    `goods_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '商品Id',
    `goods_sn` varchar(60) NOT NULL DEFAULT '' COMMENT '商品序列号',
    `product_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '产品Id',
    `goods_name` varchar(120) NOT NULL DEFAULT '' COMMENT '产品名称',
    `market_price` decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '市场价',
    `retail_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '零售价格',
    `number` smallint unsigned NOT NULL DEFAULT '0' COMMENT '数量',
    `goods_specifition_name_value` text COMMENT '规格属性组成的字符串，用来显示用',
    `goods_specifition_ids` varchar(2048) NOT NULL DEFAULT '' COMMENT '当前SKU具名组合',
    `checked` tinyint unsigned NOT NULL DEFAULT '1',
    `list_pic_url` varchar(255) NOT NULL DEFAULT '' COMMENT '商品图片',
    PRIMARY KEY (`id`),
    KEY `session_id` (`session_id`)
) ENGINE=InnoDB AUTO_INCREMENT=154 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_category` (
    `id` int unsigned NOT NULL AUTO_INCREMENT,
    `name` varchar(90) NOT NULL DEFAULT '',
    `keywords` varchar(255) DEFAULT '',
    `front_desc` varchar(255) DEFAULT '',
    `parent_id` int unsigned DEFAULT '0',
    `sort_order` tinyint unsigned DEFAULT '50',
    `show_index` tinyint(1) DEFAULT '0',
    `is_show` tinyint unsigned DEFAULT '1',
    `banner_url` varchar(255) DEFAULT '',
    `icon_url` varchar(255) DEFAULT NULL,
    `img_url` varchar(255) DEFAULT NULL,
    `wap_banner_url` varchar(255) DEFAULT NULL,
    `level` varchar(255) DEFAULT NULL,
    `type` int DEFAULT '0',
    `front_name` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1999002 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_channel` (
    `id` int NOT NULL AUTO_INCREMENT,
    `name` varchar(45) NOT NULL DEFAULT '',
    `url` varchar(255) NOT NULL DEFAULT '',
    `icon_url` varchar(255) NOT NULL DEFAULT '',
    `sort_order` int unsigned NOT NULL DEFAULT '10',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1999001 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_collect` (
    `id` mediumint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '用户Id',
    `value_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '产品Id',
    `add_time` int unsigned NOT NULL DEFAULT '0' COMMENT '添加时间',
    `is_attention` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否提醒',
    `type_id` int unsigned NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`),
    KEY `goods_id` (`value_id`),
    KEY `is_attention` (`is_attention`)
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_comment` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `type_id` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '类型',
    `value_id` int DEFAULT '0',
    `content` varchar(6550) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '储存为base64编码',
    `add_time` bigint unsigned DEFAULT '0' COMMENT '记录时间',
    `status` tinyint unsigned DEFAULT '0' COMMENT '状态',
    `user_id` int DEFAULT '0' COMMENT '会员Id',
    `staff_reply` varchar(1000) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `id_value` (`value_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `mall_comment_picture` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `comment_id` int unsigned NOT NULL DEFAULT '0' COMMENT '评价Id',
    `pic_url` varchar(255) NOT NULL DEFAULT '' COMMENT '评价图片',
    `sort_order` tinyint unsigned NOT NULL DEFAULT '5' COMMENT '排序',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_coupon` (
    `id` smallint unsigned NOT NULL AUTO_INCREMENT,
    `name` varchar(60) DEFAULT '',
    `type_money` decimal(10,2) DEFAULT '0.00',
    `send_type` tinyint unsigned DEFAULT '0',
    `min_amount` decimal(10,2) unsigned DEFAULT '0.00',
    `max_amount` decimal(10,2) unsigned DEFAULT '0.00',
    `send_start_date` datetime DEFAULT NULL,
    `send_end_date` datetime DEFAULT NULL,
    `use_start_date` datetime DEFAULT NULL,
    `use_end_date` datetime DEFAULT NULL,
    `min_goods_amount` decimal(10,2) unsigned DEFAULT '0.00',
    `min_transmit_num` int DEFAULT NULL COMMENT '转发次数',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_coupon_goods` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `coupon_id` int NOT NULL COMMENT '优惠券Id',
    `goods_id` int NOT NULL COMMENT '商品id',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券关联商品';
CREATE TABLE `mall_feedback` (
    `msg_id` mediumint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '会员Id',
    `user_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '会员会员名称',
    `mobile` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '手机',
    `feed_Type` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '反馈类型',
    `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细内容',
    `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '状态',
    `add_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '反馈时间',
    `scope_org_no` varchar(10) DEFAULT NULL,
    `staff_reply` varchar(500) DEFAULT NULL,
    KEY `ix_feedback_scope_org` (`scope_org_no`),
    PRIMARY KEY (`msg_id`),
    KEY `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE `mall_footprint` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` int NOT NULL DEFAULT '0' COMMENT '会员Id',
    `goods_id` int NOT NULL DEFAULT '0' COMMENT '商品id',
    `add_time` int NOT NULL DEFAULT '0' COMMENT '记录时间',
    `referrer` int DEFAULT NULL COMMENT '转发人',
    PRIMARY KEY (`id`),
    KEY `ix_footprint_owner_goods` (`user_id`,`goods_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_goods` (
    `id` int unsigned NOT NULL AUTO_INCREMENT,
    `category_id` int unsigned DEFAULT '0',
    `goods_sn` varchar(60) DEFAULT '',
    `name` varchar(120) NOT NULL DEFAULT '',
    `brand_id` int unsigned DEFAULT '0',
    `goods_number` mediumint unsigned DEFAULT '0',
    `keywords` varchar(255) DEFAULT '',
    `goods_brief` varchar(255) DEFAULT '',
    `goods_desc` text,
    `is_on_sale` tinyint unsigned DEFAULT '1',
    `add_time` datetime DEFAULT NULL,
    `sort_order` smallint unsigned DEFAULT '100',
    `is_delete` tinyint unsigned DEFAULT '0',
    `delete_time` datetime DEFAULT NULL COMMENT '移入回收站时间',
    `attribute_category` int unsigned DEFAULT '0',
    `counter_price` decimal(10,2) unsigned DEFAULT '0.00' COMMENT '专柜价格',
    `extra_price` decimal(10,2) unsigned DEFAULT '0.00' COMMENT '附加价格',
    `is_new` tinyint unsigned DEFAULT '0',
    `goods_unit` varchar(45) DEFAULT NULL COMMENT '商品单位',
    `primary_pic_url` varchar(255) DEFAULT NULL COMMENT '商品主图',
    `list_pic_url` varchar(255) DEFAULT NULL COMMENT '商品列表图',
    `retail_price` decimal(10,2) unsigned DEFAULT '0.00' COMMENT '零售价格',
    `sell_volume` int unsigned DEFAULT '0' COMMENT '销售量',
    `primary_product_id` int unsigned DEFAULT '0' COMMENT '主sku　product_id',
    `unit_price` decimal(10,2) unsigned DEFAULT '0.00' COMMENT '单位价格，单价',
    `promotion_desc` varchar(255) DEFAULT NULL,
    `promotion_tag` varchar(45) DEFAULT NULL,
    `app_exclusive_price` decimal(10,2) unsigned DEFAULT NULL COMMENT 'APP专享价',
    `is_app_exclusive` tinyint unsigned DEFAULT NULL COMMENT '是否是APP专属',
    `is_limited` tinyint unsigned DEFAULT NULL,
    `is_hot` tinyint unsigned DEFAULT '0',
    `market_price` decimal(10,2) DEFAULT '0.00',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` bigint DEFAULT NULL COMMENT '修改人ID',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `create_user_dept_id` bigint DEFAULT NULL,
    `pearl_source` varchar(16) NOT NULL DEFAULT '',
    `jewelry_type` varchar(16) NOT NULL DEFAULT '',
    `pearl_variety` varchar(60) NOT NULL DEFAULT '',
    `pearl_origin` varchar(100) NOT NULL DEFAULT '',
    `pearl_shape` varchar(30) NOT NULL DEFAULT '',
    `pearl_luster` varchar(30) NOT NULL DEFAULT '',
    `pearl_blemish` varchar(30) NOT NULL DEFAULT '',
    `pearl_diameter_min` decimal(8,3) DEFAULT NULL,
    `pearl_diameter_max` decimal(8,3) DEFAULT NULL,
    `video_media_id` bigint NOT NULL DEFAULT '0',
    `video_url` varchar(1000) NOT NULL DEFAULT '',
    `scope_org_no` varchar(10) DEFAULT NULL,
    KEY `ix_goods_scope_org` (`scope_org_no`),
    PRIMARY KEY (`id`),
    KEY `goods_sn` (`goods_sn`),
    KEY `cat_id` (`category_id`),
    KEY `brand_id` (`brand_id`),
    KEY `goods_number` (`goods_number`),
    KEY `sort_order` (`sort_order`),
    KEY `idx_goods_recycle` (`is_delete`,`delete_time`),
    KEY `ix_pearl_catalog` (`is_delete`,`is_on_sale`,`pearl_source`,`jewelry_type`,`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1999107 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_goods_attribute` (
    `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `goods_id` int unsigned NOT NULL DEFAULT '0' COMMENT '商品Id',
    `attribute_id` int unsigned NOT NULL DEFAULT '0' COMMENT '属性Id',
    `value` text NOT NULL COMMENT '属性值',
    PRIMARY KEY (`id`),
    KEY `goods_id` (`goods_id`),
    KEY `attr_id` (`attribute_id`)
) ENGINE=InnoDB AUTO_INCREMENT=872 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_goods_gallery` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `goods_id` int DEFAULT '0' COMMENT '商品id',
    `img_url` longblob COMMENT '图片',
    `img_desc` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_unicode_ci DEFAULT '' COMMENT '描述',
    `sort_order` int DEFAULT '5' COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `goods_id` (`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=682 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci;
CREATE TABLE `mall_goods_issue` (
    `id` int NOT NULL AUTO_INCREMENT,
    `question` varchar(500) DEFAULT NULL,
    `answer` varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_goods_specification` (
    `id` int unsigned NOT NULL AUTO_INCREMENT,
    `goods_id` int unsigned NOT NULL DEFAULT '0',
    `specification_id` int unsigned NOT NULL DEFAULT '0',
    `value` varchar(50) DEFAULT '',
    `pic_url` varchar(255) DEFAULT '',
    PRIMARY KEY (`id`),
    KEY `goods_id` (`goods_id`),
    KEY `specification_id` (`specification_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb3 COMMENT='商品对应规格表值表';
CREATE TABLE `mall_help_issue` (
    `id` int NOT NULL AUTO_INCREMENT,
    `type_id` int DEFAULT NULL COMMENT '问题分类',
    `question` varchar(500) DEFAULT NULL,
    `answer` varchar(500) DEFAULT NULL,
    `sort` int DEFAULT NULL COMMENT '排序',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_help_type` (
    `id` int NOT NULL AUTO_INCREMENT,
    `type_name` varchar(50) DEFAULT NULL COMMENT '问题分类',
    `sort` int DEFAULT NULL COMMENT '排序',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_keywords` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `keyword` varchar(90) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '关键字',
    `is_hot` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '热搜',
    `is_default` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '默认',
    `is_show` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '显示',
    `sort_order` int NOT NULL DEFAULT '100' COMMENT '排序',
    `scheme_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '关键词的跳转链接',
    `type` int NOT NULL DEFAULT '0' COMMENT '类型',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键词热搜表';
CREATE TABLE `mall_order` (
    `id` mediumint unsigned NOT NULL AUTO_INCREMENT,
    `order_sn` varchar(32) DEFAULT '',
    `user_id` mediumint unsigned DEFAULT '0',
    `order_status` int unsigned DEFAULT '0',
    `shipping_status` tinyint unsigned DEFAULT '0',
    `pay_status` tinyint unsigned DEFAULT '0',
    `consignee` varchar(60) DEFAULT '',
    `country` varchar(50) DEFAULT NULL,
    `province` varchar(50) DEFAULT NULL,
    `city` varchar(50) DEFAULT NULL,
    `district` varchar(50) DEFAULT NULL,
    `address` varchar(255) DEFAULT '',
    `mobile` varchar(60) DEFAULT '',
    `postscript` varchar(255) DEFAULT '',
    `shipping_id` tinyint DEFAULT '0',
    `shipping_name` varchar(120) DEFAULT '',
    `pay_id` varchar(64) DEFAULT '0',
    `pay_name` varchar(120) DEFAULT '',
    `shipping_fee` decimal(10,2) DEFAULT '0.00',
    `actual_price` decimal(10,2) DEFAULT '0.00' COMMENT '实际需要支付的金额',
    `integral` int unsigned DEFAULT '0',
    `integral_money` decimal(10,2) DEFAULT '0.00',
    `order_price` decimal(10,2) DEFAULT '0.00' COMMENT '订单总价',
    `goods_price` decimal(10,2) DEFAULT '0.00' COMMENT '商品总价',
    `add_time` datetime DEFAULT NULL,
    `confirm_time` datetime DEFAULT NULL,
    `pay_time` datetime DEFAULT NULL,
    `freight_price` int unsigned DEFAULT '0' COMMENT '配送费用',
    `coupon_id` mediumint unsigned DEFAULT '0' COMMENT '使用的优惠券id',
    `parent_id` mediumint unsigned DEFAULT '0',
    `coupon_price` decimal(10,2) DEFAULT NULL,
    `callback_status` enum('true','false') DEFAULT 'true',
    `shipping_no` varchar(32) DEFAULT NULL,
    `full_cut_price` decimal(10,2) DEFAULT NULL,
    `order_type` varchar(1) DEFAULT NULL,
    `request_key` varchar(80) DEFAULT NULL,
    `request_digest` varchar(64) DEFAULT NULL,
    `scope_org_no` varchar(10) DEFAULT NULL,
    KEY `ix_order_scope_org` (`scope_org_no`),
    PRIMARY KEY (`id`),
    UNIQUE KEY `order_sn` (`order_sn`),
    UNIQUE KEY `uk_order_request` (`user_id`,`request_key`),
    KEY `user_id` (`user_id`),
    KEY `order_status` (`order_status`),
    KEY `shipping_status` (`shipping_status`),
    KEY `pay_status` (`pay_status`),
    KEY `shipping_id` (`shipping_id`),
    KEY `pay_id` (`pay_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_order_goods` (
    `id` mediumint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '订单Id',
    `goods_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '商品id',
    `goods_name` varchar(120) NOT NULL DEFAULT '' COMMENT '商品名称',
    `goods_sn` varchar(60) NOT NULL DEFAULT '' COMMENT '商品序列号',
    `product_id` mediumint unsigned NOT NULL DEFAULT '0' COMMENT '产品Id',
    `number` smallint unsigned NOT NULL DEFAULT '1' COMMENT '商品数量',
    `market_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '市场价',
    `retail_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '零售价格',
    `goods_specifition_name_value` text COMMENT '商品规格详情',
    `is_real` tinyint unsigned DEFAULT '0' COMMENT '虚拟商品',
    `goods_specifition_ids` varchar(2048) DEFAULT '' COMMENT '成交时冻结的SKU组合, 历史值不改写',
    `list_pic_url` varchar(255) DEFAULT '' COMMENT '图片链接',
    PRIMARY KEY (`id`),
    KEY `order_id` (`order_id`),
    KEY `goods_id` (`goods_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_product` (
    `id` mediumint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `goods_id` mediumint unsigned DEFAULT '0' COMMENT '商品Id',
    `goods_specification_ids` varchar(2048) NOT NULL DEFAULT '' COMMENT '按组ID升序的具名SKU组合',
    `goods_sn` varchar(60) DEFAULT '' COMMENT '商品序列号',
    `goods_number` mediumint unsigned DEFAULT '0' COMMENT '商品编码',
    `retail_price` decimal(10,2) unsigned DEFAULT '0.00' COMMENT '零售价格',
    `market_price` decimal(10,2) unsigned DEFAULT '0.00' COMMENT '价格',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1999207 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_related_goods` (
    `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `goods_id` int unsigned NOT NULL DEFAULT '0' COMMENT '商品Id',
    `related_goods_id` int unsigned NOT NULL DEFAULT '0' COMMENT '关联商品id',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_search_history` (
    `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `keyword` char(50) NOT NULL COMMENT '关键字',
    `from` varchar(45) DEFAULT '' COMMENT '搜索来源，如PC、小程序、APP等',
    `add_time` int NOT NULL DEFAULT '0' COMMENT '搜索时间',
    `user_id` varchar(45) DEFAULT NULL COMMENT '会员Id',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_shipping` (
    `id` int NOT NULL AUTO_INCREMENT,
    `code` varchar(50) DEFAULT NULL,
    `name` varchar(100) DEFAULT NULL,
    `status` int(1) unsigned zerofill NOT NULL DEFAULT '0' COMMENT '状态 1正常 0删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=103 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_sms_log` (
    `id` mediumint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` int NOT NULL,
    `phone` varchar(32) DEFAULT NULL,
    `log_date` bigint DEFAULT NULL,
    `sms_code` varchar(12) DEFAULT NULL,
    `send_status` bigint DEFAULT NULL,
    `sms_text` varchar(255) DEFAULT NULL COMMENT '1成功 0失败',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_specification` (
    `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` varchar(60) NOT NULL DEFAULT '' COMMENT '规范名称',
    `sort_order` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '排序',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3 COMMENT='规格表';
CREATE TABLE `mall_topic` (
    `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title` varchar(255) NOT NULL DEFAULT '''''' COMMENT '活动主题',
    `content` text COMMENT '活动内容',
    `avatar` varchar(255) NOT NULL DEFAULT '' COMMENT '化名',
    `item_pic_url` varchar(255) NOT NULL DEFAULT '' COMMENT '活动条例图片',
    `subtitle` varchar(255) NOT NULL DEFAULT '''' COMMENT '子标题',
    `topic_category_id` int unsigned NOT NULL DEFAULT '0' COMMENT '活动类别',
    `price_info` decimal(10,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '活动价格',
    `read_count` varchar(255) NOT NULL DEFAULT '0',
    `scene_pic_url` varchar(255) NOT NULL DEFAULT '' COMMENT '场景图片链接',
    `topic_template_id` int unsigned NOT NULL DEFAULT '0' COMMENT '活动模板Id',
    `topic_tag_id` int unsigned NOT NULL DEFAULT '0' COMMENT '活动标签Id',
    KEY `topic_id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=315 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_topic_category` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title` varchar(255) NOT NULL DEFAULT '' COMMENT '活动类别主题',
    `pic_url` varchar(255) NOT NULL DEFAULT '' COMMENT '活动类别图片链接',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_user` (
    `id` mediumint unsigned NOT NULL AUTO_INCREMENT,
    `username` varchar(60) NOT NULL DEFAULT '',
    `password` varchar(32) DEFAULT '',
    `gender` tinyint(1) DEFAULT NULL,
    `birthday` datetime DEFAULT NULL,
    `register_time` datetime DEFAULT NULL,
    `last_login_time` datetime DEFAULT NULL,
    `last_login_ip` varchar(15) DEFAULT '',
    `user_level_id` tinyint DEFAULT NULL,
    `nickname` varchar(60) DEFAULT '',
    `mobile` varchar(20) DEFAULT '',
    `register_ip` varchar(45) DEFAULT '',
    `avatar` varchar(255) DEFAULT '',
    `weixin_openid` varchar(50) DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_name` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_user_coupon` (
    `id` mediumint unsigned NOT NULL AUTO_INCREMENT,
    `coupon_id` tinyint unsigned DEFAULT '0',
    `coupon_number` varchar(20) DEFAULT '',
    `user_id` int unsigned DEFAULT '0',
    `used_time` datetime DEFAULT NULL,
    `add_time` datetime DEFAULT NULL,
    `order_id` mediumint unsigned DEFAULT '0',
    `coupon_status` tinyint unsigned DEFAULT '1' COMMENT '状态 1. 可用 2. 已用 3. 过期',
    `source_key` varchar(64) DEFAULT NULL COMMENT '来源key',
    `referrer` int DEFAULT NULL COMMENT '发券人',
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=68 DEFAULT CHARSET=utf8mb3;
CREATE TABLE `mall_user_level` (
    `id` tinyint unsigned NOT NULL AUTO_INCREMENT,
    `name` varchar(30) NOT NULL DEFAULT '',
    `description` varchar(255) NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3;

CREATE TABLE CC_SCHEMA_VERSION (
    id TINYINT PRIMARY KEY,
    version INT NOT NULL,
    CONSTRAINT chk_schema_singleton CHECK (id = 1)
) ENGINE=InnoDB;
SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE IF NOT EXISTS CC_GOODS_QUESTION (
    id BIGINT NOT NULL AUTO_INCREMENT,
    goods_id INT NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    question VARCHAR(500) NOT NULL,
    answer VARCHAR(1000) DEFAULT NULL,
    status VARCHAR(12) NOT NULL DEFAULT 'PENDING',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY ix_goods_question_public (goods_id, status, id),
    KEY ix_goods_question_user (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
