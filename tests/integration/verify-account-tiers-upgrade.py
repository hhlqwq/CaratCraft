"""在禁网临时 MySQL 验证三级账号从旧库升级、冲突拒绝和回滚."""

from pathlib import Path
import secrets
import subprocess
import time

ROOT = Path(__file__).resolve().parents[2]
UPGRADE = ROOT / 'database/upgrade/account-tiers'
DATABASE = 'tier_fixture'


def run(arguments, content=None, check=True):
    """执行隔离测试命令, 保留错误用于断言."""
    return subprocess.run(arguments, input=content, cwd=ROOT, encoding='utf-8',
                          capture_output=True, timeout=120, check=check)


def audit(container):
    """在隔离库测试批准映射、唯一约束及回退边界."""

    def sql(statement, check=True, selected=True):
        """使用当前随机容器中的 MySQL 执行夹具语句."""
        command = ['docker', 'exec', '-i', container, 'mysql', '-uroot', '--batch',
                   '--skip-column-names', '--default-character-set=utf8mb4']
        return run(command + ([DATABASE] if selected else []), statement, check)

    old = run(['git', 'show', '963e4ff:database/init/010_schema.sql']).stdout
    sql(f'CREATE DATABASE {DATABASE}; USE {DATABASE};\n' + old, selected=False)
    sql("""INSERT INTO SYS_USER(USER_ID,USER_NAME,REAL_NAME,SEX,STATUS,PASSWORD,SALT,CREATE_TIME)
           VALUES('1','admin','Fixture',0,1,'oldAdminHash','oldSalt',NOW()),
                 ('old-staff','staff','Fixture',0,1,'oldStaffHash','oldSalt',NOW());
           INSERT INTO SYS_ORG(ORG_NO,ORG_NAME,STATUS,CREATE_TIME) VALUES('QA','Fixture',1,NOW());
           INSERT INTO SYS_USER_TOKEN(USER_ID,TOKEN,EXPIRE_TIME,UPDATE_TIME) VALUES('1','fixture',NOW(),NOW());""")
    prefix = """SET @cc_confirm_account_tiers='APPLY_ACCOUNT_TIERS'; SET @cc_default_org='QA';
                CREATE TEMPORARY TABLE cc_tier_mapping(USER_ID varchar(32) PRIMARY KEY,
                    ACCOUNT_TYPE varchar(16) NOT NULL, ORG_NO varchar(32));
                INSERT INTO cc_tier_mapping VALUES('1','PLATFORM_ADMIN',NULL);"""
    apply = (UPGRADE / 'apply.sql').read_text(encoding='utf-8')
    failure = sql(prefix + apply, check=False)
    assert failure.returncode != 0 and 'no approved mapping' in failure.stderr, failure.stderr
    sql(prefix + "INSERT INTO cc_tier_mapping VALUES('old-staff','EMPLOYEE','QA');" + apply)
    records = sql("SELECT CONCAT_WS(':',USER_ID,ACCOUNT_TYPE,COALESCE(ORG_NO,''),STATUS,"
                  "PASSWORD_CHANGE_REQUIRED,COALESCE(PASSWORD,'null')) FROM SYS_USER ORDER BY USER_ID;")
    assert records.stdout.strip().splitlines() == ['1:PLATFORM_ADMIN::0:1:null', 'old-staff:EMPLOYEE:QA:1:0:oldStaffHash']
    assert sql('SELECT COUNT(*) FROM SYS_USER_TOKEN;').stdout.strip() == '0'
    assert sql("UPDATE SYS_USER SET ACCOUNT_TYPE='PLATFORM_ADMIN' WHERE USER_ID='old-staff'", check=False).returncode != 0
    rollback = ("SET @cc_confirm_account_tier_rollback='ROLLBACK_ACCOUNT_TIERS'; "
                + (UPGRADE / 'rollback.sql').read_text(encoding='utf-8'))
    rejected = sql(rollback, check=False)
    assert rejected.returncode != 0 and 'Rollback requires' in rejected.stderr, rejected.stderr
    sql("UPDATE SYS_USER SET STATUS=1,PASSWORD='fixtureFinalCredential',PASSWORD_CHANGE_REQUIRED=0 WHERE USER_ID='1';")
    sql(rollback)
    columns = sql('SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() '
                  "AND COLUMN_NAME IN ('ACCOUNT_TYPE','DIRECT_USER_ID','TEMPORARY_PASSWORD_EXPIRES_AT');")
    assert columns.stdout.strip() == '0'
    assert sql("SELECT PASSWORD FROM SYS_USER WHERE USER_ID='old-staff';").stdout.strip() == 'oldStaffHash'
    print('迁移、冲突拒绝、撤销旧令牌、唯一超管约束和回滚全部通过。')


def main():
    """只启动和销毁本次随机、禁网、内存存储的 MySQL."""
    name = 'caratcraft-tier-sql-' + secrets.token_hex(5)
    result = run(['docker', 'run', '--pull=never', '--rm', '-d', '--network', 'none', '--name', name,
                  '--tmpfs', '/var/lib/mysql', '-e', 'MYSQL_ALLOW_EMPTY_PASSWORD=yes', 'mysql:8.0.36'])
    container = result.stdout.strip()
    try:
        for attempt in range(30):
            ready = run(['docker', 'exec', container, 'mysqladmin', '-h', '127.0.0.1', '-uroot', 'ping'], check=False)
            if ready.returncode == 0:
                break
            time.sleep(2)
        else:
            raise RuntimeError('隔离 MySQL 初始化超时')
        audit(container)
    finally:
        run(['docker', 'stop', container], check=False)


if __name__ == '__main__':
    try:
        main()
    except subprocess.CalledProcessError as error:
        raise SystemExit(error.stderr)
