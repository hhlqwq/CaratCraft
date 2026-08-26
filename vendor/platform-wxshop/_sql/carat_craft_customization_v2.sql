-- 仅供已部署旧版定制表的数据库执行一次 / Run once only for existing customization databases.
ALTER TABLE `CC_CUSTOM_PRODUCT`
  ADD COLUMN `required_component_types` varchar(512) DEFAULT NULL AFTER `model_url`;

ALTER TABLE `CC_DESIGN_SNAPSHOT`
  ADD COLUMN `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP AFTER `create_time`;
