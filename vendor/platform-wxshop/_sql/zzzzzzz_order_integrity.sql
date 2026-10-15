-- 新建数据库初始化改价审计表, 已有数据库通过迁移入口执行对应版本.
SOURCE /docker-entrypoint-initdb.d/migrations/V20261015_01_order_integrity.sql;
