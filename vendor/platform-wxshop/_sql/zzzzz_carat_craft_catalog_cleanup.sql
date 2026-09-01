-- 清理原底座商城目录和外链 / Removes legacy storefront catalog data and remote assets.
-- 全新数据库初始化入口, 与 migrations/20260901_cleanup_legacy_catalog.sql 保持一致。
SOURCE /docker-entrypoint-initdb.d/migrations/20260901_cleanup_legacy_catalog.sql;
