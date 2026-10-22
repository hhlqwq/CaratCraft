-- 社区只提供发布和点赞, 不建立评论、私信或好友关系.
CREATE TABLE CC_COMMUNITY_POST (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  request_key VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  request_hash CHAR(64) CHARACTER SET ascii NOT NULL,
  kind VARCHAR(16) NOT NULL,
  title VARCHAR(60) NOT NULL,
  content VARCHAR(2000) NOT NULL,
  images_json JSON NOT NULL,
  design_json JSON NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  version BIGINT NOT NULL DEFAULT 0,
  moderation_reason VARCHAR(300) NOT NULL DEFAULT '',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_request (user_id, request_key),
  KEY ix_post_feed (status, id),
  KEY ix_post_owner (user_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE CC_COMMUNITY_LIKE (
  post_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (post_id, user_id),
  KEY ix_like_owner (user_id, post_id),
  CONSTRAINT fk_community_like_post FOREIGN KEY (post_id) REFERENCES CC_COMMUNITY_POST(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE CC_COMMUNITY_MEDIA (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  url VARCHAR(1000) NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_media_owner (user_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE CC_COMMUNITY_LOG (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  post_id BIGINT NOT NULL,
  operator_name VARCHAR(100) NOT NULL,
  from_status VARCHAR(16) NOT NULL,
  to_status VARCHAR(16) NOT NULL,
  reason VARCHAR(300) NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY ix_community_log (post_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO SYS_MENU (menu_id, parent_id, name, url, perms, type, icon, order_num)
VALUES (9300, 0, '社区管理', 'community/posts', 'community:post:list', 1, 'menu', 45),
       (930001, 9300, '审核与下架', NULL, 'community:post:moderate', 2, NULL, 1);
