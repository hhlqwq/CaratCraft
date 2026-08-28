-- CaratCraft 管理后台定制模块菜单种子 / Admin customization module menu seed.
-- 幂等说明: 仅首次初始化执行一次, ID 段 20x 与底座 base.sql 不冲突 /
-- Run once on a fresh database; the 20x id range does not clash with base.sql.
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES
('20', '0', '定制管理', NULL, NULL, 0, 'system', 20),
('2001', '20', '生产工单', 'custom/work-order', 'custom:work-order:list', 1, 'menu', 1),
('200101', '2001', '查看', NULL, 'custom:work-order:list', 2, NULL, 0),
('200102', '2001', '流转状态', NULL, 'custom:work-order:update', 2, NULL, 0),
('2002', '20', '组件库存', 'custom/component-stock', 'custom:stock:list', 1, 'databases', 2),
('200201', '2002', '修改库存', NULL, 'custom:stock:update', 2, NULL, 0),
('2003', '20', '定制配置', 'custom/configuration', 'custom:config:list', 1, 'setting', 3),
('200301', '2003', '查看', NULL, 'custom:config:list', 2, NULL, 0),
('200302', '2003', '保存配置', NULL, 'custom:config:update', 2, NULL, 0)
ON DUPLICATE KEY UPDATE `NAME` = VALUES(`NAME`), `URL` = VALUES(`URL`), `PERMS` = VALUES(`PERMS`), `TYPE` = VALUES(`TYPE`), `ICON` = VALUES(`ICON`), `ORDER_NUM` = VALUES(`ORDER_NUM`);
