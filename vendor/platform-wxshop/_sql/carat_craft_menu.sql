-- CaratCraft 管理后台生产工单菜单种子 / Admin work-order menu seed.
-- 幂等说明: 仅首次初始化执行一次, ID 段 20x 与底座 base.sql 不冲突 /
-- Run once on a fresh database; the 20x id range does not clash with base.sql.
INSERT INTO `SYS_MENU` (`MENU_ID`, `PARENT_ID`, `NAME`, `URL`, `PERMS`, `TYPE`, `ICON`, `ORDER_NUM`) VALUES
('20', '0', '定制管理', NULL, NULL, 0, 'system', 20),
('2001', '20', '生产工单', 'custom/work-order', 'custom:work-order:list', 1, 'menu', 1),
('200101', '2001', '查看', NULL, 'custom:work-order:list', 2, NULL, 0),
('200102', '2001', '流转状态', NULL, 'custom:work-order:update', 2, NULL, 0);
