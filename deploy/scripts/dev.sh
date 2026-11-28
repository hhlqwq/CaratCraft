#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
PROJECT_DIR="$ROOT_DIR"
COMPOSE_FILE="$PROJECT_DIR/deploy/docker-compose.yml"
ENV_FILE="$PROJECT_DIR/deploy/.env"
ENV_EXAMPLE="$PROJECT_DIR/deploy/.env.example"

ACTION="start"
NO_BUILD="false"

# 输出脚本使用方式 / Prints script usage.
usage() {
    echo "用法: ./deploy/scripts/dev.sh [configure|build|init-db|upgrade-db|reset-db|start|stop|restart|status|logs|miniapp|diagnose|check-config|repair-config] [--no-build]"
}

# 校验 Docker Engine 和 Compose / Validates Docker Engine and Compose.
assert_docker_ready() {
    command -v docker >/dev/null 2>&1 || {
        echo "未检测到 Docker。macOS 请安装 Docker Desktop，Ubuntu 请安装 Docker Engine 和 Compose Plugin。"
        exit 1
    }
    docker compose version >/dev/null
    docker info >/dev/null || {
        echo "Docker 服务未运行，请先启动 Docker。"
        exit 1
    }
}

# 创建本地环境变量文件 / Creates the local environment file.
initialize_environment() {
    run_environment_tool configure
}

# 使用现有前端基础镜像运行维护工具, 宿主只需要Docker和Shell.
run_environment_tool() {
    local image=m.daocloud.io/docker.io/library/node:24-alpine
    local tool_user="$(id -u):$(id -g)"
    if [[ "$1" == configure ]]; then tool_user=0:0; fi
    if ! docker image inspect "$image" >/dev/null 2>&1; then
        echo '准备配置工具镜像...'
        docker pull "$image" || return 1
    fi
    docker run --rm --network none --user "$tool_user" \
        --mount "type=bind,source=$ROOT_DIR,target=/workspace" --workdir /workspace \
        --env CARATCRAFT_TOOL_UID="$(id -u)" --env CARATCRAFT_TOOL_GID="$(id -g)" \
        --env CARATCRAFT_CONTAINER_TOOL=1 "$image" node tools/maintenance/dev-env.cjs "$@"
}

# 仅完全没有本项目卷和容器时允许首次初始化, 部分状态停止并保留现场.
prepare_local_start() {
    local resources all_volumes all_containers kind owner found=0
    all_volumes="$(docker volume ls --format '{{.Name}}')" || return 1
    all_containers="$(docker container ls -a --format '{{.Names}}')" || return 1
    resources="$(docker container ls -aq --filter label=com.docker.compose.project=platform-wxshop)" || return 1
    [[ ! -e "$ROOT_DIR/deploy/.initializing" && ! -L "$ROOT_DIR/deploy/.initializing" ]] || {
        echo '检测到未完成的首次初始化 deploy/.initializing, 请核对日志与数据后人工恢复。' >&2; return 1;
    }
    for kind in mysql-data redis-data upload-data; do
        if grep -Fxq "platform-wxshop_$kind" <<< "$all_volumes"; then
            owner="$(docker volume inspect --format '{{index .Labels "com.docker.compose.project"}}' "platform-wxshop_$kind")" || return 1
            [[ "$owner" == platform-wxshop ]] || { echo "数据卷 $kind 归属异常。" >&2; return 1; }
            found=$((found + 1))
        fi
    done
    if [[ -e "$ROOT_DIR/deploy/.initialized" && "$found" != 3 ]]; then
        echo '检测到已初始化记录但持久卷缺失, 请恢复数据卷, 不会自动重新建库。' >&2
        return 1
    fi
    if [[ "$found" == 0 && -z "$resources" ]] &&
        ! grep -Eq '^platform-(mysql|redis|admin|api|nginx)$' <<< "$all_containers"; then
        if [[ -e "$ENV_FILE" && ( ! -f "$ROOT_DIR/deploy/secrets/platform-bootstrap.txt" ||
            ! -f "$ROOT_DIR/deploy/secrets/platform-bootstrap.runtime.txt" ) ]]; then
            echo '已有配置但无持久卷且缺少首次引导文件, 请核对是否丢失数据, 停止自动建库。' >&2
            return 1
        fi
        FRESH_ENVIRONMENT=true
        initialize_environment || return 1
    elif [[ "$found" == 3 && -f "$ENV_FILE" ]]; then
        FRESH_ENVIRONMENT=false
    else
        echo '配置丢失、数据卷不完整或存在残留容器, 已停止自动初始化。请恢复原配置并核对三只持久卷。' >&2
        return 1
    fi
}

# 读取公开启动规则, 汇总出错项, 不记录配置值.
assert_startup_configuration() {
    run_environment_tool check-config
}

# 清除继承的 Compose 同名变量, 仅从指定的配置文件取值.
clear_compose_overrides() {
    local key
    export CARATCRAFT_DB_INIT=false
    while IFS= read -r key; do
        unset "$key"
    done < <(sed -n 's/^\([A-Z][A-Z0-9_]*\)=.*/\1/p' "$ENV_EXAMPLE")
}

# 检查仓库盘可用空间, 端口冲突由 Compose 返回具体占用端口.
assert_host_disk() {
    local available
    available="$(df -Pk "$ROOT_DIR" | awk 'NR==2 {print $4}')"
    [[ "$available" =~ ^[0-9]+$ && "$available" -ge 2097152 ]] || { echo '仓库所在磁盘剩余空间不足 2 GiB。'; return 1; }
}

# 记录当前源码的 Git 提交, 离线源码包明确标记 archive.
set_git_revision() {
    BUILD_GIT_SHA=archive
    if [[ -e "$ROOT_DIR/.git" ]] && command -v git >/dev/null 2>&1; then
        local sha
        if sha="$(git -C "$ROOT_DIR" rev-parse --verify HEAD)" && [[ "$sha" =~ ^[0-9a-f]{40}$ ]]; then
            BUILD_GIT_SHA="$sha"
        fi
    fi
    export BUILD_GIT_SHA
    echo "Git: $BUILD_GIT_SHA"
}

# 计算单个文件的SHA256, 兼容Linux及macOS自带工具.
sha256_file() {
    local sum
    if command -v sha256sum >/dev/null 2>&1; then
        sum="$(sha256sum -- "$@")" || return $?
    else
        sum="$(shasum -a 256 -- "$@")" || return $?
    fi
    [[ "$sum" =~ ^([0-9a-fA-F]{64})[[:space:]] ]] || return 1
    printf '%s\n' "${BASH_REMATCH[1]}" | tr 'a-f' 'A-F'
}

# 收集唯一清单中的构建输入, 拒绝链接且排除缓存及私人配置.
build_source_files() {
    local item="$1" name child
    [[ ! -L "$item" ]] || { echo "构建输入不允许链接: $item" >&2; return 1; }
    if [[ -d "$item" ]]; then
        while IFS= read -r -d '' child; do
            [[ ! -L "$child" ]] || { echo "构建输入不允许链接: $child" >&2; return 1; }
            name="${child##*/}"
            case "$name" in
                application-local*|environment.local.js|project.config.json|project.private.config.json|.env|.env.*|*.log|*.tmp|*.key|*.pem|*.p12)
                    [[ "$child" == "$ENV_EXAMPLE" ]] || continue ;;
            esac
            printf '%s\n' "${child#"$ROOT_DIR/"}"
        done < <(find "$item" \( -type d \( -name node_modules -o -name target -o -name dist -o -name .git -o -name .idea -o -name .vscode -o -name .vite -o -name .cache -o -name coverage -o -name .tools -o -name .pnpm-store -o -name __pycache__ \) \) -prune -o \( -type f -o -type l \) -print0)
    else
        [[ -f "$item" ]] || { echo "构建输入缺失: $item" >&2; return 1; }
        printf '%s\n' "${item#"$ROOT_DIR/"}"
    fi
}

# 根据输入路径和文件摘要计算源码标识, 不依赖Git元数据.
build_revision() {
    local scope="$1" entry files name manifest
    files="$(while IFS= read -r entry || [[ -n "$entry" ]]; do
        entry="${entry%$'\r'}"
        [[ "$entry" == "$scope "* ]] || continue
        build_source_files "$ROOT_DIR/${entry#* }" || exit 1
    done < "$ROOT_DIR/deploy/build-inputs.txt")" || return 1
    [[ -n "$files" ]] || return 1
    files="$(printf '%s\n' "$files" | LC_ALL=C sort -u)"
    manifest="$(while IFS= read -r name; do
        printf '%s\0' "$ROOT_DIR/$name"
    done <<< "$files" | if command -v sha256sum >/dev/null 2>&1; then
        xargs -0 sha256sum
    else
        xargs -0 shasum -a 256
    fi | awk -v root="$ROOT_DIR/" '{print substr($0, 67 + length(root)) ":" toupper(substr($0, 1, 64))}')" || return 1
    printf '%s\n' "$manifest" | sha256_file | tr 'A-F' 'a-f'
}

# 两个镜像分别记录各自的构建输入摘要.
set_build_revisions() {
    BACKEND_BUILD_REVISION="$(build_revision backend)" || return 1
    ADMIN_UI_BUILD_REVISION="$(build_revision admin-ui)" || return 1
    export BACKEND_BUILD_REVISION ADMIN_UI_BUILD_REVISION
}

# 动态选择需要构建的镜像, 不启动服务.
build_local_environment() {
    local service image expected digest
    while read -r service image expected; do
        if digest="$(docker image inspect --format '{{index .Config.Labels "io.caratcraft.source-digest"}}' "$image" 2>/dev/null)"; then
            if [[ -z "$expected" || "$digest" == "$expected" ]]; then
                echo "复用镜像 $image, 构建输入未变更。"
                continue
            fi
        fi
        if [[ -z "$expected" ]]; then
            compose pull "$service" || return $?
        else
            compose build "$service" || return $?
        fi
    done < <(local_images)
    echo "镜像构建完成, 请运行 ./deploy/scripts/dev.sh start。"
}

# 人工确认后只删除已核对归属的固定项目数据库和缓存卷.
reset_test_data_volumes() {
    local confirmation volume
    assert_data_volumes || return 1
    echo "仅重建本项目MySQL及Redis开发数据, 上传卷保留。"
    read -r -p "输入 RESET_DATA 确认重建开发库和缓存: " confirmation
    [[ "$confirmation" == RESET_DATA ]] || { echo "已取消数据重置"; return 1; }
    compose down --remove-orphans || return 1
    for volume in platform-wxshop_mysql-data platform-wxshop_redis-data; do
        docker volume rm "$volume" || return 1
    done
}

# 执行统一的 Compose 命令 / Runs a unified Compose command.
compose() {
    docker compose -p platform-wxshop --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

# 两个Java服务共用同一镜像, 只选一个服务构建.
local_images() {
    printf '%s\n' \
        'mysql m.daocloud.io/docker.io/library/mysql:8.0.36' \
        'redis m.daocloud.io/docker.io/library/redis:7.2-alpine' \
        "platform-admin carat-craft-backend:local $BACKEND_BUILD_REVISION" \
        "nginx carat-craft-admin-ui:local $ADMIN_UI_BUILD_REVISION"
}

# 按构建输入摘要校验运行镜像, Git提交仅用于追溯.
assert_local_images() {
    local service image expected digest
    while read -r service image expected; do
        digest="$(docker image inspect --format '{{index .Config.Labels "io.caratcraft.source-digest"}}' "$image" 2>/dev/null)" || {
            echo "缺少镜像 $image, 请先运行 ./deploy/scripts/dev.sh build。"; return 1;
        }
        [[ -z "$expected" || "$digest" == "$expected" ]] || {
            echo "镜像 $image 的构建输入已变更, 请先运行 ./deploy/scripts/dev.sh build。"; return 1;
        }
    done < <(local_images)
}

# 使用数据库容器自身凭据查询结构, 不输出密码或业务行.
database_query() {
    local output result=0
    if [[ "${2:-root}" == application ]]; then
        output="$(compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_PASSWORD" exec mysql -u"$MYSQL_USER" --default-character-set=utf8mb4 --batch --skip-column-names "$MYSQL_DATABASE" "$@"' sh "--execute=$1" 2>&1)" || result=$?
    else
        output="$(compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot --default-character-set=utf8mb4 --batch --skip-column-names "$MYSQL_DATABASE" "$@"' sh "--execute=$1" 2>&1)" || result=$?
    fi
    if [[ "$result" != 0 ]]; then
        printf '%s\n' "$output" >&2
        return 1
    fi
    printf '%s\n' "$output"
}

# 核对唯一完整初始化版本, 遇空库或旧结构停止.
assert_database_schema() {
    local version
    version="$(database_query 'SELECT version FROM CC_SCHEMA_VERSION WHERE id=1;')" || return $?
    case "$version" in
        1) return 0 ;;
        ''|0) echo '本地开发库尚未完成当前完整初始化。'; return 1 ;;
        *) echo '未知数据库结构版本, 停止启动。'; return 1 ;;
    esac
}

# 在应用启动前识别部分初始化及未完成初始化的旧数据库.
assert_database_ready() {
    local tables name version
    tables="$(database_query "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('sys_region','mall_order','mall_user','mall_goods','CC_SCHEMA_VERSION');")" || return $?
    for name in sys_region mall_order mall_user mall_goods CC_SCHEMA_VERSION; do
        if ! grep -Fxq "$name" <<< "$tables"; then
            echo "本地开发库不是当前结构, 缺少${name}。"
            return 1
        fi
    done
    assert_database_schema || return $?
    local missing tiers
    missing="$(database_query "$(cat "$ROOT_DIR/deploy/schema-preflight.sql")")" || return $?
    if [[ -n "$missing" ]]; then
        printf '本地库缺少必需字段, 请参照项目维护文档备份并执行对应增量, 数据卷已保留:\n%s\n' "$missing" >&2
        return 1
    fi
    tiers="$(database_query "SELECT IF(EXISTS(SELECT 1 FROM SYS_USER WHERE USER_ID='1' AND ACCOUNT_TYPE='PLATFORM_ADMIN') AND NOT EXISTS(SELECT 1 FROM SYS_USER WHERE ACCOUNT_TYPE IS NULL OR (USER_ID<>'1' AND ACCOUNT_TYPE NOT IN ('MERCHANT_ADMIN','EMPLOYEE'))),1,0);")" || return $?
    [[ "$tiers" == 1 ]] || { echo '本地库的账号层级尚未完成迁移, 停止应用启动。' >&2; return 1; }
    version="$(database_query "SELECT 1;" application)" || return $?
    [[ "$version" == 1 ]] || return 1
}

# 生成来自显式清单的SQL批次, 两端使用相同的结构检查和记录格式.
migration_batch() {
    local registry="$ROOT_DIR/database/upgrade/local" entry sequence directory id ids=""
    local previous=0 checksum check apply stage complete staged
    local completed="${1:-}" mode="${2:-}" pending=""
    local entries=()
    while IFS= read -r entry || [[ -n "$entry" ]]; do
        entry="${entry%$'\r'}"
        [[ "$entry" =~ ^([0-9]{4})\ ([a-z][a-z0-9-]{0,48})$ ]] || {
            echo "本机迁移清单条目无效: $entry" >&2; return 1;
        }
        sequence="${BASH_REMATCH[1]}"
        (( 10#$sequence > previous )) || { echo '本机迁移清单顺序错误。' >&2; return 1; }
        previous=$((10#$sequence))
        id="$sequence-${BASH_REMATCH[2]}"
        ids="${ids:+$ids,}'$id'"
        entries+=("$entry")
    done < "$registry/manifest.txt"
    [[ -n "$ids" ]] || { echo '本机迁移清单不能为空。' >&2; return 1; }
    sed "s/__IDS__/$ids/g" "$registry/begin.sql" || return 1
    for entry in "${entries[@]}"; do
        id="${entry/ /-}"
        directory="$ROOT_DIR/database/upgrade/${entry#* }"
        check="$(sha256_file "$directory/check.sql")" || return 1
        apply="$(sha256_file "$directory/apply.sql")" || return 1
        checksum="$(printf '%s%s' "$check" "$apply" | sha256_file -)" || return 1
        complete=0
        if grep -Fxq "$id" <<< "$completed"; then complete=1; fi
        sed -e "s/__ID__/$id/g" -e "s/__SHA256__/$checksum/g" -e "s/__COMPLETED__/$complete/g" "$registry/before.sql" || return 1
        if [[ "$complete" == 1 ]]; then
            printf "SELECT '%s: verified';\n" "$id"
            continue
        fi
        if [[ "$mode" == baseline ]]; then
            printf -v staged "INSERT INTO CC_SCHEMA_MIGRATION VALUES ('%s', '%s', CURRENT_TIMESTAMP);\nSELECT '%s: baseline';" "$id" "$checksum" "$id"
            pending+="$staged"$'\n'
            continue
        fi
        staged="$(
            printf '%s\n' 'SET @cc_compatible = NULL, @cc_complete = NULL;'
            for stage in "$directory/check.sql" "$registry/apply.sql" "$directory/apply.sql" \
                "$directory/check.sql" "$registry/after.sql"; do
                sed -e "s/__ID__/$id/g" -e "s/__SHA256__/$checksum/g" "$stage" || exit 1
            done
        )" || return 1
        pending+="$staged"$'\n'
    done
    if [[ "$mode" == baseline ]]; then
        cat "$registry/current-schema.sql" || return 1
        printf 'START TRANSACTION;\n%sCOMMIT;\n' "$pending"
    else
        printf '%s' "$pending"
        cat "$registry/current-schema.sql"
    fi
}

# 在同一MySQL连接内完成加锁、校验与幂等迁移.
apply_automatic_migrations() {
    local snapshot completed batch output
    snapshot="$(cat "$ROOT_DIR/database/upgrade/local/snapshot.sql")" || return 1
    completed="$(database_query "$snapshot")" || return 1
    batch="$(migration_batch "$completed" "${1:-}")" || return 1
    output="$(database_query "$batch")" || { echo '安全结构增量失败，请核对迁移编号和数据库结构。' >&2; return 1; }
    while IFS= read -r line; do
        case "$line" in
            *': verified') printf '数据库迁移 %s：已应用，指纹核对通过\n' "${line%:*}" ;;
            *': baseline') printf '数据库迁移 %s：结构已齐全，已登记\n' "${line%:*}" ;;
            *': applying') printf '数据库迁移 %s：增量已执行并核对结构\n' "${line%:*}" ;;
        esac
    done <<< "$output"
}

# 输出有限的容器状态和健康检查结果, 保留启动失败退出码.
show_startup_diagnostics() {
    compose ps --all || true
    local name
    for name in platform-admin platform-api; do
        docker inspect --format '{{json .State.Health}}' "$name" 2>/dev/null || true
    done
    echo "日志: ./deploy/scripts/dev.sh logs。请按实际失败阶段排查本地开发环境。"
}

# 核对卷的存在及归属, 标准启动只接受已有卷.
assert_data_volumes() {
    local kind metadata
    for kind in mysql-data redis-data; do
        if metadata="$(docker volume inspect --format '{{index .Labels "com.docker.compose.project"}}' "platform-wxshop_$kind" 2>/dev/null)"; then
            if [[ "${1:-existing}" == new || "$metadata" != platform-wxshop ]]; then
                echo "数据卷 $kind 已存在或归属不匹配。"; return 1
            fi
        elif [[ "${1:-existing}" != new ]]; then
            echo "缺少数据卷 $kind, 新环境请先执行 init-db。"; return 1
        fi
    done
}

# 独立初始化新数据卷, 已有卷全部保留.
initialize_local_database() {
    local result=0
    assert_data_volumes new
    CARATCRAFT_DB_INIT=true compose up --detach --no-build --pull never --wait --wait-timeout 300 mysql redis || result=$?
    if [[ "$result" == 0 ]]; then
        assert_database_ready && apply_automatic_migrations baseline || result=$?
    fi
    # 只移除本次初始化容器, 保留数据库卷和初始化结果.
    compose rm --stop --force mysql
    return "$result"
}

# 只执行已选择的增量 SQL, 不代替备份和归属预检.
upgrade_local_database() {
    local confirmation
    case "$UPGRADE" in admin-jobs|staff-password) ;; *) echo '请选择 admin-jobs 或 staff-password。'; return 1 ;; esac
    assert_data_volumes
    read -r -p "核对目标库及备份后, 输入 UPGRADE_DB 执行 $UPGRADE: " confirmation
    [[ "$confirmation" == UPGRADE_DB ]] || { echo '已取消数据库升级。'; return 1; }
    compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql --default-character-set=utf8mb4 -uroot "$MYSQL_DATABASE"' < "$ROOT_DIR/database/upgrade/$UPGRADE/apply.sql"
}

# 验证镜像、数据服务和运行版本, 遇到任何错误时保留数据卷.
start_local_environment() {
    local service expected
    if [[ "${FRESH_ENVIRONMENT:-false}" == true ]]; then
        assert_data_volumes new || return 1
    else
        assert_data_volumes || return 1
    fi
    if [[ "$NO_BUILD" != true ]]; then build_local_environment || return $?; fi
    assert_local_images || return $?
    if [[ "${FRESH_ENVIRONMENT:-false}" == true ]]; then
        mkdir "$ROOT_DIR/deploy/.initializing" || return 1
        echo '首次初始化MySQL及Redis, 正在等待完整结构和种子...'
        initialize_local_database || return 1
    fi
    if ! compose up --detach --no-recreate --no-build --pull never --wait --wait-timeout 300 mysql redis ||
        ! assert_database_ready || ! apply_automatic_migrations; then
        show_startup_diagnostics
        return 1
    fi
    if [[ "$ACTION" == restart ]]; then compose restart platform-admin platform-api nginx; fi
    if ! compose up --detach --no-deps --no-build --pull never --wait --wait-timeout 300 platform-admin platform-api nginx; then
        show_startup_diagnostics
        return 1
    fi

    local image actual expected_id
    for service in platform-admin platform-api platform-nginx; do
        image=carat-craft-backend:local
        expected="$BACKEND_BUILD_REVISION"
        if [[ "$service" == platform-nginx ]]; then
            image=carat-craft-admin-ui:local
            expected="$ADMIN_UI_BUILD_REVISION"
        fi
        expected_id="$(docker image inspect --format '{{.Id}}' "$image")" || return 1
        actual="$(docker container inspect --format '{{.Image}} {{index .Config.Labels "io.caratcraft.source-digest"}} {{.State.Health.Status}}' "$service")" || return 1
        if [[ "$actual" != "$expected_id $expected healthy" ]]; then
            echo "$service 的运行镜像或健康状态不符合预期。" >&2
            return 1
        fi
        echo "${service}：镜像一致，健康"
    done
    if ! docker exec platform-nginx sh -ec '
        wget -q -T 5 -t 1 -O /dev/null http://127.0.0.1/
        status=$(wget -S -T 5 -t 1 -O /dev/null http://127.0.0.1/platform-framework/ 2>&1) || :
        case $status in *401\ Unauthorized*) ;; *) exit 1;; esac
        api=$(wget -q -T 5 -t 1 -O - http://127.0.0.1/platform-framework-api/)
        case $api in *platform-framework-api/doc.html) ;; *) exit 1;; esac'; then
        echo 'Nginx 静态入口、后台根路径或 API 根路径探测失败。' >&2
        return 1
    fi
    echo '代理验证通过：管理台、后台与 API 根路径。'
    mkdir -p "$ROOT_DIR/deploy/.initialized" || return 1
    if [[ "${FRESH_ENVIRONMENT:-false}" == true ]]; then
        rmdir "$ROOT_DIR/deploy/.initializing" || return 1
        echo '超管账号 admin 的临时密码位于 deploy/secrets/platform-bootstrap.txt, 请在30分钟内登录并设置新密码。'
    fi
    show_access_info
}

# 输出本地访问地址 / Prints local access URLs.
show_access_info() {
    local port
    port="$(sed -n 's/^NGINX_PORT=//p' "$ENV_FILE" | tail -n 1)"
    port="${port:-8888}"
    echo
    echo "CaratCraft 已启动:"
    echo "  管理台: http://localhost:$port"
    echo "  后台接口: http://localhost:$port/platform-framework"
    echo "  商城接口: http://localhost:$port/platform-framework-api"
    echo "  小程序配置: 执行 bash deploy/scripts/dev.sh miniapp 生成。"
}

if [[ $# -gt 0 && "$1" != --* ]]; then
    ACTION="$1"
    shift
fi

UPGRADE=""
if [[ "$ACTION" == upgrade-db && $# -gt 0 && "$1" != --* ]]; then
    UPGRADE="$1"
    shift
fi

while [[ $# -gt 0 ]]; do
    case "$1" in
        --no-build) NO_BUILD="true" ;;
        -h|--help) usage; exit 0 ;;
        *) echo "未知参数: $1"; usage; exit 1 ;;
    esac
    shift
done

case "$ACTION" in
    configure|miniapp|diagnose|check-config|repair-config)
        assert_docker_ready
        if [[ "$ACTION" == diagnose && -f "$ENV_FILE" ]]; then compose ps --all; fi
        run_environment_tool "$ACTION"
        exit ;;
    build|start|stop|restart|status|logs|init-db|upgrade-db|reset-db) ;;
    *) echo "未知操作: $ACTION"; usage; exit 1 ;;
esac
if [[ "$ACTION" == build && "$NO_BUILD" == true ]]; then
    echo "build不能与--no-build同时使用。"; exit 1
fi
if [[ "$ACTION" == build || "$ACTION" == start || "$ACTION" == restart ]]; then
    SECONDS=0
    trap 'printf "操作耗时：%s 秒\\n" "$SECONDS"' EXIT
fi
assert_docker_ready
clear_compose_overrides
if [[ "$ACTION" == build ]]; then
    ENV_FILE="$ENV_EXAMPLE"
    assert_host_disk
    set_build_revisions
    set_git_revision
    build_local_environment
    exit
fi
if [[ "$ACTION" == start || "$ACTION" == restart ]]; then prepare_local_start; fi
[[ -f "$ENV_FILE" ]] || { echo '缺少 deploy/.env, 请恢复原配置或运行标准启动入口。'; exit 1; }

case "$ACTION" in
    stop) compose down --remove-orphans ;;
    status) compose ps ;;
    logs) compose logs --follow --tail=200 ;;
    upgrade-db) assert_startup_configuration; upgrade_local_database ;;
    reset-db) assert_startup_configuration; reset_test_data_volumes ;;
    init-db) assert_startup_configuration; initialize_local_database ;;
    start|restart)
        assert_startup_configuration
        assert_host_disk
        set_build_revisions
        set_git_revision
        start_local_environment
        ;;
esac
