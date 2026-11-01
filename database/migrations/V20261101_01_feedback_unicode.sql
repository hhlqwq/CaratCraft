-- 保留Unicode反馈正文和昵称, 不改变既有业务值或用户ID范围.
SET NAMES utf8mb4;
ALTER TABLE mall_feedback
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
