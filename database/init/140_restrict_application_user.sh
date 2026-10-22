#!/usr/bin/env bash
set -euo pipefail

# 新建库初始化完成后收窄应用账号权限, 迁移操作使用单独管理账号.
restrict_application_user() {
  [[ "${MYSQL_DATABASE:-}" =~ ^[a-zA-Z0-9_]+$ ]] || exit 1
  [[ "${MYSQL_USER:-}" =~ ^[a-zA-Z0-9_]+$ ]] || exit 1
  [[ "$MYSQL_USER" != root ]] || exit 1
  MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --protocol=socket -uroot <<SQL
REVOKE ALL PRIVILEGES, GRANT OPTION FROM '$MYSQL_USER'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON \`$MYSQL_DATABASE\`.* TO '$MYSQL_USER'@'%';
SQL
  echo "应用数据库账号已限制为当前库的读写权限, 不包含建库或修改表权限."
}

restrict_application_user
