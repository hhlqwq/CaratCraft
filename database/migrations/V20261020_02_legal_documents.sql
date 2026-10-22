-- 协议正文由经营者维护, 不预置或自动发布示例法律文本.
CREATE TABLE CC_LEGAL_HEAD (
  kind VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL PRIMARY KEY,
  revision INT NOT NULL DEFAULT 0,
  published_version INT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO CC_LEGAL_HEAD(kind) VALUES('TERMS'),('PRIVACY');
CREATE TABLE CC_LEGAL_DOCUMENT (
  kind VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  version INT NOT NULL,
  title VARCHAR(120) NOT NULL,
  content MEDIUMTEXT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  operator_id VARCHAR(64) NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  published_at DATETIME NULL,
  PRIMARY KEY(kind,version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE CC_LEGAL_ACCEPTANCE (
  user_id BIGINT NOT NULL,
  kind VARCHAR(16) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  version INT NOT NULL,
  accepted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(user_id,kind,version),
  CONSTRAINT fk_acceptance_document FOREIGN KEY(kind,version)
    REFERENCES CC_LEGAL_DOCUMENT(kind,version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO SYS_MENU(menu_id,parent_id,name,url,perms,type,icon,order_num)
VALUES (9500,0,'协议与隐私','merchant/legal','merchant:legal:read',1,'menu',47),
       (950001,9500,'编辑协议草稿',NULL,'merchant:legal:update',2,NULL,1),
       (950002,9500,'发布协议版本',NULL,'merchant:legal:publish',2,NULL,2);
