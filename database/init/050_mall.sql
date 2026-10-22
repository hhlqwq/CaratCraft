-- CaratCraft商城结构与项目演示种子, 不包含第三方商品文案或图片.
-- 只用于新建空库; 不要在有业务数据的数据库执行.
SET NAMES utf8mb4;

CREATE TABLE `mall_ad`
(
    `id`             smallint(5) unsigned NOT NULL AUTO_INCREMENT,
    `ad_position_id` smallint(5) unsigned NOT NULL DEFAULT '0',
    `media_type`     tinyint(3) unsigned  NOT NULL DEFAULT '0',
    `name`           varchar(60)                   DEFAULT '',
    `link`           varchar(255)                  DEFAULT '',
    `image_url`      text,
    `content`        varchar(255)                  DEFAULT '',
    `end_time`       datetime                      DEFAULT NULL,
    `enabled`        tinyint(3) unsigned           DEFAULT '1',
    PRIMARY KEY (`id`),
    KEY `position_id` (`ad_position_id`),
    KEY `enabled` (`enabled`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_address`
(
    `id`            mediumint(8) unsigned                          NOT NULL AUTO_INCREMENT,
    `user_id`       mediumint(8) unsigned                DEFAULT '0' COMMENT '会员ID',
    `user_name`     varchar(50) COLLATE utf8_unicode_ci  DEFAULT '' COMMENT '收货人姓名',
    `is_default`    INT(1)                               DEFAULT 0 NULL,
    `tel_number`    varchar(60) COLLATE utf8_unicode_ci  DEFAULT '' COMMENT '手机',
    `postal_Code`   varchar(50) COLLATE utf8_unicode_ci  DEFAULT '' COMMENT '邮编',
    `national_Code` varchar(50) COLLATE utf8_unicode_ci  DEFAULT '0' COMMENT '收货地址国家码',
    `province_Name` varchar(50) COLLATE utf8_unicode_ci  DEFAULT '0' COMMENT '省',
    `city_Name`     varchar(50) COLLATE utf8_unicode_ci  DEFAULT '0' COMMENT '市',
    `county_Name`   varchar(50) COLLATE utf8_unicode_ci  DEFAULT '0' COMMENT '区',
    `detail_Info`   varchar(120) COLLATE utf8_unicode_ci DEFAULT '' COMMENT '详细收货地址信息',
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;

CREATE TABLE `mall_ad_position`
(
    `id`     tinyint(3) unsigned  NOT NULL AUTO_INCREMENT,
    `name`   varchar(60)          NOT NULL DEFAULT '''',
    `width`  smallint(5) unsigned NOT NULL DEFAULT '0',
    `height` smallint(5) unsigned NOT NULL DEFAULT '0',
    `desc`   varchar(255)         NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_attribute`
(
    `id`                    int(11) unsigned    NOT NULL AUTO_INCREMENT COMMENT '主键',
    `attribute_category_id` int(11) unsigned    NOT NULL DEFAULT '0' COMMENT '商品类型',
    `name`                  varchar(60)         NOT NULL DEFAULT '' COMMENT '属性名称',
    `input_type`            tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '当添加商品时,该属性的添加类别; 0为手功输入;1为选择输入;2为多行文本输入',
    `value`                 text                NOT NULL COMMENT '即选择输入,则attr_name对应的值的取值就是该这字段值 ',
    `sort_order`            tinyint(3) unsigned NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `cat_id` (`attribute_category_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 103
  DEFAULT CHARSET = utf8 COMMENT '商品参数属性';

CREATE TABLE `mall_attribute_category`
(
    `id`      int(11) unsigned    NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`    varchar(60)         NOT NULL DEFAULT '',
    `enabled` tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '1开启; 0关闭;',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1036002
  DEFAULT CHARSET = utf8 COMMENT '商品参数类型';

CREATE TABLE `mall_brand`
(
    `id`               int(11) unsigned    NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`             varchar(255)        NOT NULL DEFAULT '' COMMENT '品牌名称',
    `list_pic_url`     varchar(255)        NOT NULL DEFAULT '' COMMENT '图片',
    `simple_desc`      varchar(255)        NOT NULL DEFAULT '' COMMENT '描述',
    `pic_url`          varchar(255)        NOT NULL DEFAULT '' COMMENT '图片',
    `sort_order`       tinyint(3) unsigned          DEFAULT '50' COMMENT '排序',
    `is_show`          tinyint(1) unsigned NOT NULL DEFAULT '1' COMMENT '显示',
    `floor_price`      decimal(10, 2)               DEFAULT '0.00',
    `app_list_pic_url` varchar(255)        NOT NULL DEFAULT '' COMMENT 'app显示图片',
    `is_new`           tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '新品牌',
    `new_pic_url`      varchar(255)        NOT NULL DEFAULT '' COMMENT '图片',
    `new_sort_order`   tinyint(2) unsigned NOT NULL DEFAULT '10' COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `is_show` (`is_show`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1046011
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_cart`
(
    `id`                           mediumint(8) unsigned                        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`                      mediumint(8) unsigned                        NOT NULL DEFAULT '0' COMMENT '会员Id',
    `session_id`                   char(32) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'sessionId',
    `goods_id`                     mediumint(8) unsigned                        NOT NULL DEFAULT '0' COMMENT '商品Id',
    `goods_sn`                     varchar(60)                                  NOT NULL DEFAULT '' COMMENT '商品序列号',
    `product_id`                   mediumint(8) unsigned                        NOT NULL DEFAULT '0' COMMENT '产品Id',
    `goods_name`                   varchar(120)                                 NOT NULL DEFAULT '' COMMENT '产品名称',
    `market_price`                 decimal(10, 2) unsigned                      NOT NULL DEFAULT '0.00' COMMENT '市场价',
    `retail_price`                 decimal(10, 2)                               NOT NULL DEFAULT '0.00' COMMENT '零售价格',
    `number`                       smallint(5) unsigned                         NOT NULL DEFAULT '0' COMMENT '数量',
    `goods_specifition_name_value` text COMMENT '规格属性组成的字符串，用来显示用',
    `goods_specifition_ids`        varchar(60)                                           DEFAULT '' COMMENT 'product表对应的goods_specifition_ids',
    `checked`                      tinyint(1) unsigned                          NOT NULL DEFAULT '1',
    `list_pic_url`                 varchar(255)                                 NOT NULL DEFAULT '' COMMENT '商品图片',
    PRIMARY KEY (`id`),
    KEY `session_id` (`session_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 154
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_category`
(
    `id`             int(11) unsigned NOT NULL AUTO_INCREMENT,
    `name`           varchar(90)      NOT NULL DEFAULT '',
    `keywords`       varchar(255)              DEFAULT '',
    `front_desc`     varchar(255)              DEFAULT '',
    `parent_id`      int(11) unsigned          DEFAULT '0',
    `sort_order`     tinyint(1) unsigned       DEFAULT '50',
    `show_index`     tinyint(1)                DEFAULT '0',
    `is_show`        tinyint(1) unsigned       DEFAULT '1',
    `banner_url`     varchar(255)              DEFAULT '',
    `icon_url`       varchar(255)              DEFAULT NULL,
    `img_url`        varchar(255)              DEFAULT NULL,
    `wap_banner_url` varchar(255)              DEFAULT NULL,
    `level`          varchar(255)              DEFAULT NULL,
    `type`           int(11)                   DEFAULT '0',
    `front_name`     varchar(255)              DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `parent_id` (`parent_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1036007
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_channel`
(
    `id`         int(11)         NOT NULL AUTO_INCREMENT,
    `name`       varchar(45)     NOT NULL DEFAULT '',
    `url`        varchar(255)    NOT NULL DEFAULT '',
    `icon_url`   varchar(255)    NOT NULL DEFAULT '',
    `sort_order` int(4) unsigned NOT NULL DEFAULT '10',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_collect`
(
    `id`           mediumint(8) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT '用户Id',
    `value_id`     mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT '产品Id',
    `add_time`     int(11) unsigned      NOT NULL DEFAULT '0' COMMENT '添加时间',
    `is_attention` tinyint(1)            NOT NULL DEFAULT '0' COMMENT '是否提醒',
    `type_id`      int(2) unsigned       NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`),
    KEY `goods_id` (`value_id`),
    KEY `is_attention` (`is_attention`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 75
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_comment`
(
    `id`       int(11)             NOT NULL AUTO_INCREMENT COMMENT '主键',
    `type_id`  tinyint(3) unsigned NOT NULL             DEFAULT '0' COMMENT '类型',
    `value_id` int(11)                                  DEFAULT '0',
    `content`  varchar(6550) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '储存为base64编码',
    `add_time` bigint(12) unsigned                      DEFAULT '0' COMMENT '记录时间',
    `status`   tinyint(3) unsigned                      DEFAULT '0' COMMENT '状态',
    `user_id`  int(11)                                  DEFAULT '0' COMMENT '会员Id',
    PRIMARY KEY (`id`),
    KEY `id_value` (`value_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `mall_comment_picture`
(
    `id`         int(11)             NOT NULL AUTO_INCREMENT COMMENT '主键',
    `comment_id` int(11) unsigned    NOT NULL DEFAULT '0' COMMENT '评价Id',
    `pic_url`    varchar(255)        NOT NULL DEFAULT '' COMMENT '评价图片',
    `sort_order` tinyint(1) unsigned NOT NULL DEFAULT '5' COMMENT '排序',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_coupon`
(
    `id`               smallint(5) unsigned NOT NULL AUTO_INCREMENT,
    `name`             varchar(60)             DEFAULT '',
    `type_money`       decimal(10, 2)          DEFAULT '0.00',
    `send_type`        tinyint(3) unsigned     DEFAULT '0',
    `min_amount`       decimal(10, 2) unsigned DEFAULT '0.00',
    `max_amount`       decimal(10, 2) unsigned DEFAULT '0.00',
    `send_start_date`  datetime                DEFAULT NULL,
    `send_end_date`    datetime                DEFAULT NULL,
    `use_start_date`   datetime                DEFAULT NULL,
    `use_end_date`     datetime                DEFAULT NULL,
    `min_goods_amount` decimal(10, 2) unsigned DEFAULT '0.00',
    `min_transmit_num` int(4)                  DEFAULT NULL COMMENT '转发次数',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 10
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_coupon_goods`
(
    `id`        int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `coupon_id` int(11) NOT NULL COMMENT '优惠券Id',
    `goods_id`  int(11) NOT NULL COMMENT '商品id',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='优惠券关联商品';

CREATE TABLE `mall_feedback`
(
    `msg_id`    mediumint(8) unsigned               NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`   mediumint(8) unsigned               NOT NULL DEFAULT '0' COMMENT '会员Id',
    `user_name` varchar(60) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '会员会员名称',
    `mobile`    varchar(60) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '手机',
    `feed_Type` tinyint(1) unsigned                 NOT NULL DEFAULT '0' COMMENT '反馈类型',
    `content`   text COLLATE utf8_unicode_ci        NOT NULL COMMENT '详细内容',
    `status`    tinyint(1) unsigned                 NOT NULL DEFAULT '0' COMMENT '状态',
    `add_time`  timestamp                           NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '反馈时间',
    PRIMARY KEY (`msg_id`),
    KEY `user_id` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;

CREATE TABLE `mall_footprint`
(
    `id`       int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`  int(11) NOT NULL DEFAULT '0' COMMENT '会员Id',
    `goods_id` int(11) NOT NULL DEFAULT '0' COMMENT '商品id',
    `add_time` int(11) NOT NULL DEFAULT '0' COMMENT '记录时间',
    `referrer` int(11)          DEFAULT NULL COMMENT '转发人',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_goods`
(
    `id`                  int(11) unsigned NOT NULL AUTO_INCREMENT,
    `category_id`         int(11) unsigned          DEFAULT '0',
    `goods_sn`            varchar(60)               DEFAULT '',
    `name`                varchar(120)     NOT NULL DEFAULT '',
    `brand_id`            int(11) unsigned          DEFAULT '0',
    `goods_number`        mediumint(8) unsigned     DEFAULT '0',
    `keywords`            varchar(255)              DEFAULT '',
    `goods_brief`         varchar(255)              DEFAULT '',
    `goods_desc`          text,
    `is_on_sale`          tinyint(1) unsigned       DEFAULT '1',
    `add_time`            datetime                  DEFAULT NULL,
    `sort_order`          smallint(4) unsigned      DEFAULT '100',
    `is_delete`           tinyint(1) unsigned       DEFAULT '0',
    `attribute_category`  int(11) unsigned          DEFAULT '0',
    `counter_price`       decimal(10, 2) unsigned   DEFAULT '0.00' COMMENT '专柜价格',
    `extra_price`         decimal(10, 2) unsigned   DEFAULT '0.00' COMMENT '附加价格',
    `is_new`              tinyint(1) unsigned       DEFAULT '0',
    `goods_unit`          varchar(45)               DEFAULT NULL COMMENT '商品单位',
    `primary_pic_url`     varchar(255)              DEFAULT NULL COMMENT '商品主图',
    `list_pic_url`        varchar(255)              DEFAULT NULL COMMENT '商品列表图',
    `retail_price`        decimal(10, 2) unsigned   DEFAULT '0.00' COMMENT '零售价格',
    `sell_volume`         int(11) unsigned          DEFAULT '0' COMMENT '销售量',
    `primary_product_id`  int(11) unsigned          DEFAULT '0' COMMENT '主sku　product_id',
    `unit_price`          decimal(10, 2) unsigned   DEFAULT '0.00' COMMENT '单位价格，单价',
    `promotion_desc`      varchar(255)              DEFAULT NULL,
    `promotion_tag`       varchar(45)               DEFAULT NULL,
    `app_exclusive_price` decimal(10, 2) unsigned   DEFAULT NULL COMMENT 'APP专享价',
    `is_app_exclusive`    tinyint(1) unsigned       DEFAULT NULL COMMENT '是否是APP专属',
    `is_limited`          tinyint(1) unsigned       DEFAULT NULL,
    `is_hot`              tinyint(1) unsigned       DEFAULT '0',
    `market_price`        decimal(10, 2)            DEFAULT '0.00',
    `create_user_id`      bigint(20)                DEFAULT NULL COMMENT '创建人ID',
    `update_user_id`      bigint(20)                DEFAULT NULL COMMENT '修改人ID',
    `update_time`         datetime                  DEFAULT NULL COMMENT '修改时间',
    `create_user_dept_id` bigint(20)                DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `goods_sn` (`goods_sn`),
    KEY `cat_id` (`category_id`),
    KEY `brand_id` (`brand_id`),
    KEY `goods_number` (`goods_number`),
    KEY `sort_order` (`sort_order`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1181001
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_goods_attribute`
(
    `id`           int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `goods_id`     int(11) unsigned NOT NULL DEFAULT '0' COMMENT '商品Id',
    `attribute_id` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '属性Id',
    `value`        text             NOT NULL COMMENT '属性值',
    PRIMARY KEY (`id`),
    KEY `goods_id` (`goods_id`),
    KEY `attr_id` (`attribute_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 872
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_goods_gallery`
(
    `id`         int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `goods_id`   int(11)                              DEFAULT '0' COMMENT '商品id',
    `img_url`    longblob COMMENT '图片',
    `img_desc`   varchar(255) COLLATE utf8_unicode_ci DEFAULT '' COMMENT '描述',
    `sort_order` int(11)                              DEFAULT '5' COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `goods_id` (`goods_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 681
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;

CREATE TABLE `mall_goods_issue`
(
    `id`       int(11) NOT NULL AUTO_INCREMENT,
    `question` varchar(500) DEFAULT NULL,
    `answer`   varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 5
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_goods_specification`
(
    `id`               int(11) unsigned NOT NULL AUTO_INCREMENT,
    `goods_id`         int(11) unsigned NOT NULL DEFAULT '0',
    `specification_id` int(11) unsigned NOT NULL DEFAULT '0',
    `value`            varchar(50)               DEFAULT '',
    `pic_url`          varchar(255)              DEFAULT '',
    PRIMARY KEY (`id`),
    KEY `goods_id` (`goods_id`),
    KEY `specification_id` (`specification_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  DEFAULT CHARSET = utf8 COMMENT ='商品对应规格表值表';

CREATE TABLE `mall_keywords`
(
    `id`         int(11)                                 NOT NULL AUTO_INCREMENT COMMENT '主键',
    `keyword`    varchar(90) COLLATE utf8mb4_unicode_ci  NOT NULL DEFAULT '' COMMENT '关键字',
    `is_hot`     tinyint(1) unsigned                     NOT NULL DEFAULT '0' COMMENT '热搜',
    `is_default` tinyint(1) unsigned                     NOT NULL DEFAULT '0' COMMENT '默认',
    `is_show`    tinyint(1) unsigned                     NOT NULL DEFAULT '1' COMMENT '显示',
    `sort_order` int(11)                                 NOT NULL DEFAULT '100' COMMENT '排序',
    `scheme_url` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '关键词的跳转链接',
    `type`       int(11)                                 NOT NULL DEFAULT '0' COMMENT '类型',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='关键词热搜表';

CREATE TABLE `mall_order`
(
    `id`              mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
    `order_sn`        varchar(32)           DEFAULT '',
    `user_id`         mediumint(8) unsigned DEFAULT '0',
    `order_status`    int(3) unsigned       DEFAULT '0',
    `shipping_status` tinyint(1) unsigned   DEFAULT '0',
    `pay_status`      tinyint(1) unsigned   DEFAULT '0',
    `consignee`       varchar(60)           DEFAULT '',
    `country`         varchar(50)           DEFAULT NULL,
    `province`        varchar(50)           DEFAULT NULL,
    `city`            varchar(50)           DEFAULT NULL,
    `district`        varchar(50)           DEFAULT NULL,
    `address`         varchar(255)          DEFAULT '',
    `mobile`          varchar(60)           DEFAULT '',
    `postscript`      varchar(255)          DEFAULT '',
    `shipping_id`     tinyint(3)            DEFAULT '0',
    `shipping_name`   varchar(120)          DEFAULT '',
    `pay_id`          varchar(64)           DEFAULT '0',
    `pay_name`        varchar(120)          DEFAULT '',
    `shipping_fee`    decimal(10, 2)        DEFAULT '0.00',
    `actual_price`    decimal(10, 2)        DEFAULT '0.00' COMMENT '实际需要支付的金额',
    `integral`        int(10) unsigned      DEFAULT '0',
    `integral_money`  decimal(10, 2)        DEFAULT '0.00',
    `order_price`     decimal(10, 2)        DEFAULT '0.00' COMMENT '订单总价',
    `goods_price`     decimal(10, 2)        DEFAULT '0.00' COMMENT '商品总价',
    `add_time`        datetime              DEFAULT NULL,
    `confirm_time`    datetime              DEFAULT NULL,
    `pay_time`        datetime              DEFAULT NULL,
    `freight_price`   int(10) unsigned      DEFAULT '0' COMMENT '配送费用',
    `coupon_id`       mediumint(8) unsigned DEFAULT '0' COMMENT '使用的优惠券id',
    `parent_id`       mediumint(8) unsigned DEFAULT '0',
    `coupon_price`    decimal(10, 2)        DEFAULT NULL,
    `callback_status` enum ('true','false') DEFAULT 'true',
    `shipping_no`     varchar(32)           DEFAULT NULL,
    `full_cut_price`  decimal(10, 2)        DEFAULT NULL,
    `order_type`      varchar(1)            DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `order_sn` (`order_sn`),
    KEY `user_id` (`user_id`),
    KEY `order_status` (`order_status`),
    KEY `shipping_status` (`shipping_status`),
    KEY `pay_status` (`pay_status`),
    KEY `shipping_id` (`shipping_id`),
    KEY `pay_id` (`pay_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 21
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_order_goods`
(
    `id`                           mediumint(8) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`                     mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT '订单Id',
    `goods_id`                     mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT '商品id',
    `goods_name`                   varchar(120)          NOT NULL DEFAULT '' COMMENT '商品名称',
    `goods_sn`                     varchar(60)           NOT NULL DEFAULT '' COMMENT '商品序列号',
    `product_id`                   mediumint(8) unsigned NOT NULL DEFAULT '0' COMMENT '产品Id',
    `number`                       smallint(5) unsigned  NOT NULL DEFAULT '1' COMMENT '商品数量',
    `market_price`                 decimal(10, 2)        NOT NULL DEFAULT '0.00' COMMENT '市场价',
    `retail_price`                 decimal(10, 2)        NOT NULL DEFAULT '0.00' COMMENT '零售价格',
    `goods_specifition_name_value` text COMMENT '商品规格详情',
    `is_real`                      tinyint(1) unsigned            DEFAULT '0' COMMENT '虚拟商品',
    `goods_specifition_ids`        varchar(255)                   DEFAULT '' COMMENT '商品规格Ids',
    `list_pic_url`                 varchar(255)                   DEFAULT '' COMMENT '图片链接',
    PRIMARY KEY (`id`),
    KEY `order_id` (`order_id`),
    KEY `goods_id` (`goods_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 28
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_product`
(
    `id`                      mediumint(8) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `goods_id`                mediumint(8) unsigned   DEFAULT '0' COMMENT '商品Id',
    `goods_specification_ids` varchar(50)             DEFAULT '' COMMENT '商品规格ids',
    `goods_sn`                varchar(60)             DEFAULT '' COMMENT '商品序列号',
    `goods_number`            mediumint(8) unsigned   DEFAULT '0' COMMENT '商品编码',
    `retail_price`            decimal(10, 2) unsigned DEFAULT '0.00' COMMENT '零售价格',
    `market_price`            decimal(10, 2) unsigned DEFAULT '0.00' COMMENT '价格',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 247
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_related_goods`
(
    `id`               int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `goods_id`         int(11) unsigned NOT NULL DEFAULT '0' COMMENT '商品Id',
    `related_goods_id` int(11) unsigned NOT NULL DEFAULT '0' COMMENT '关联商品id',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_search_history`
(
    `id`       int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `keyword`  char(50)         NOT NULL COMMENT '关键字',
    `from`     varchar(45)               DEFAULT '' COMMENT '搜索来源，如PC、小程序、APP等',
    `add_time` int(11)          NOT NULL DEFAULT '0' COMMENT '搜索时间',
    `user_id`  varchar(45)               DEFAULT NULL COMMENT '会员Id',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 48
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_shipping`
(
    `id`     int(11)                  NOT NULL AUTO_INCREMENT,
    `code`   varchar(50)                       DEFAULT NULL,
    `name`   varchar(100)                      DEFAULT NULL,
    `status` int(1) unsigned zerofill NOT NULL DEFAULT '0' COMMENT '状态 1正常 0删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 103
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_sms_log`
(
    `id`          mediumint(8) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     int(11)               NOT NULL,
    `phone`       varchar(32)  DEFAULT NULL,
    `log_date`    bigint(20)   DEFAULT NULL,
    `sms_code`    varchar(12)  DEFAULT NULL,
    `send_status` bigint(20)   DEFAULT NULL,
    `sms_text`    varchar(255) DEFAULT NULL COMMENT '1成功 0失败',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_specification`
(
    `id`         int(11) unsigned    NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`       varchar(60)         NOT NULL DEFAULT '' COMMENT '规范名称',
    `sort_order` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '排序',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 5
  DEFAULT CHARSET = utf8 COMMENT ='规格表';

CREATE TABLE `mall_topic`
(
    `id`                int(10) unsigned        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`             varchar(255)            NOT NULL DEFAULT '''''' COMMENT '活动主题',
    `content`           text COMMENT '活动内容',
    `avatar`            varchar(255)            NOT NULL DEFAULT '' COMMENT '化名',
    `item_pic_url`      varchar(255)            NOT NULL DEFAULT '' COMMENT '活动条例图片',
    `subtitle`          varchar(255)            NOT NULL DEFAULT '''' COMMENT '子标题',
    `topic_category_id` int(11) unsigned        NOT NULL DEFAULT '0' COMMENT '活动类别',
    `price_info`        decimal(10, 2) unsigned NOT NULL DEFAULT '0.00' COMMENT '活动价格',
    `read_count`        varchar(255)            NOT NULL DEFAULT '0',
    `scene_pic_url`     varchar(255)            NOT NULL DEFAULT '' COMMENT '场景图片链接',
    `topic_template_id` int(11) unsigned        NOT NULL DEFAULT '0' COMMENT '活动模板Id',
    `topic_tag_id`      int(11) unsigned        NOT NULL DEFAULT '0' COMMENT '活动标签Id',
    KEY `topic_id` (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 315
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_topic_category`
(
    `id`      int(11)      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`   varchar(255) NOT NULL DEFAULT '' COMMENT '活动类别主题',
    `pic_url` varchar(255) NOT NULL DEFAULT '' COMMENT '活动类别图片链接',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_user`
(
    `id`              mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
    `username`        varchar(60)           NOT NULL DEFAULT '',
    `password`        varchar(32)                    DEFAULT '',
    `gender`          tinyint(1)                     DEFAULT NULL,
    `birthday`        datetime                       DEFAULT NULL,
    `register_time`   datetime                       DEFAULT NULL,
    `last_login_time` datetime                       DEFAULT NULL,
    `last_login_ip`   varchar(15)                    DEFAULT '',
    `user_level_id`   tinyint(3)                     DEFAULT NULL,
    `nickname`        varchar(60)                    DEFAULT '',
    `mobile`          varchar(20)                    DEFAULT '',
    `register_ip`     varchar(45)                    DEFAULT '',
    `avatar`          varchar(255)                   DEFAULT '',
    `weixin_openid`   varchar(50)                    DEFAULT '',
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_name` (`username`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 24
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_user_coupon`
(
    `id`            mediumint(8) unsigned NOT NULL AUTO_INCREMENT,
    `coupon_id`     tinyint(3) unsigned   DEFAULT '0',
    `coupon_number` varchar(20)           DEFAULT '',
    `user_id`       int(11) unsigned      DEFAULT '0',
    `used_time`     datetime              DEFAULT NULL,
    `add_time`      datetime              DEFAULT NULL,
    `order_id`      mediumint(8) unsigned DEFAULT '0',
    `coupon_status` TINYINT(3) UNSIGNED   DEFAULT '1' COMMENT '状态 1. 可用 2. 已用 3. 过期',
    `source_key`    varchar(64)           DEFAULT NULL COMMENT '来源key',
    `referrer`      int(32)               DEFAULT NULL COMMENT '发券人',
    PRIMARY KEY (`id`),
    KEY `user_id` (`user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 68
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_user_level`
(
    `id`          tinyint(3) unsigned NOT NULL AUTO_INCREMENT,
    `name`        varchar(30)         NOT NULL DEFAULT '',
    `description` varchar(255)        NOT NULL DEFAULT '',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 5
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_help_issue`
(
    `id`       int(11) NOT NULL AUTO_INCREMENT,
    `type_id`  int(11)      DEFAULT NULL COMMENT '问题分类',
    `question` varchar(500) DEFAULT NULL,
    `answer`   varchar(500) DEFAULT NULL,
    `sort`     int(4)       DEFAULT NULL COMMENT '排序',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 34
  DEFAULT CHARSET = utf8;

CREATE TABLE `mall_help_type`
(
    `id`        int(11) NOT NULL AUTO_INCREMENT,
    `type_name` varchar(50) DEFAULT NULL COMMENT '问题分类',
    `sort`      int(4)      DEFAULT NULL COMMENT '排序',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 7
  DEFAULT CHARSET = utf8;

-- 此桥接商品只供既有定制迁移使用, 后续迁移会转到1999105.
INSERT INTO `mall_category` (`id`, `name`, `parent_id`, `is_show`)
VALUES (1999000, '项目演示目录', 0, 1);
INSERT INTO `mall_goods`
(`id`, `category_id`, `goods_sn`, `name`, `goods_number`, `goods_brief`,
 `goods_desc`, `is_on_sale`, `is_delete`, `retail_price`, `primary_pic_url`, `list_pic_url`)
VALUES (1006002, 1999000, 'DEMO-BRIDGE-001', '演示 · 定制迁移占位商品', 100,
 '仅供初始化迁移使用, 不是真实售卖商品.', '<p>项目演示数据.</p>', 0, 0, 399.00,
 '/static/images/demo/demo-platinum-pearl-necklace.webp',
 '/static/images/demo/demo-platinum-pearl-necklace.webp');
INSERT INTO `mall_product` (`id`, `goods_id`, `goods_sn`, `goods_number`, `retail_price`)
VALUES (1006002, 1006002, 'DEMO-BRIDGE-001-STD', 100, 399.00);
INSERT INTO `mall_specification` (`id`, `name`, `sort_order`)
VALUES (1, '颜色', 1), (2, '规格', 2), (4, '重量', 3);
