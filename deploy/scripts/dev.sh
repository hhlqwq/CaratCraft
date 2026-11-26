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
    if [[ ! -f "$ENV_FILE" ]]; then
        command -v openssl >/dev/null || { echo "请安装openssl或手动创建deploy/.env并配置独立强密钥"; exit 1; }
        umask 077
        local line key
        while IFS= read -r line || [[ -n "$line" ]]; do
            line="${line%$'\r'}"
            key="${line%%=*}"
            case "$key" in
                MYSQL_ROOT_PASSWORD|MYSQL_APP_PASSWORD|REDIS_PASSWORD|JWT_SECRET)
                    printf '%s=%s\n' "$key" "$(openssl rand -base64 32)" ;;
                *) printf '%s\n' "$line" ;;
            esac
        done < "$ENV_EXAMPLE" > "$ENV_FILE"
        echo "已生成独立随机配置: $ENV_FILE, 密钥不在终端输出。"
    fi
}

# 读取简单的键值配置, 拒绝重复键.
config_value() {
    awk -v key="$1" '
        NR==1{sub(/^\357\273\277/,"")}
        index($0,key"=")==1 {sub(/^[^=]+=/,""); sub(/^[[:space:]]*/,""); sub(/[[:space:]]*$/,""); print}
    ' "$ENV_FILE"
}

# 读取公开启动规则, 汇总出错项, 不记录配置值.
assert_startup_configuration() {
    local key value jwt_size canonical number kind rule="" line
    local -a failures=() bound_ports=() port_keys=()
    if ! awk 'NR==1{sub(/^\357\273\277/,"")} /^[[:space:]]*($|#)/ {next} !/^[A-Z][A-Z0-9_]*=/ {print "deploy/.env 第 " NR " 行无效。"; exit 1} {key=$0; sub(/=.*/, "", key); if (seen[key]++) {print "重复配置项 " key "。"; exit 1}}' "$ENV_FILE"; then
        return 1
    fi
    while IFS= read -r line || [[ -n "$line" ]]; do
        line="${line%$'\r'}"
        if [[ "$line" =~ ^\#\ @startup\ (default|port|required|secret)$ ]]; then
            rule="${BASH_REMATCH[1]}"
            continue
        fi
        kind="$rule"
        rule=""
        if [[ -z "$kind" || ! "$line" =~ ^([A-Z][A-Z0-9_]*)= ]]; then continue; fi
        key="${BASH_REMATCH[1]}"
        value="$(config_value "$key")"
        if [[ -z "$value" ]]; then
            if [[ "$kind" =~ ^(default|port)$ ]] && ! grep -q "^$key=" "$ENV_FILE"; then
                failures+=("$key 缺失。可执行 repair-config 补齐安全默认值。")
            else
                failures+=("$key 缺失。请在 deploy/.env 手工填写，沿用现有环境配置。")
            fi
        elif [[ "$kind" == secret && ${#value} -lt 24 ]]; then
            failures+=("$key 不足24个字符。请核对原有凭据。")
        elif [[ "$kind" == port ]]; then
            if [[ ! "$value" =~ ^[0-9]{1,5}$ ]] || (( 10#$value < 1 || 10#$value > 65535 )); then
                failures+=("$key 端口无效。")
                continue
            fi
            number=$((10#$value))
            local index
            for index in "${!bound_ports[@]}"; do
                if [[ "${bound_ports[$index]}" == "$number" ]]; then
                    failures+=("$key 与 ${port_keys[$index]} 端口重复。")
                fi
            done
            bound_ports+=("$number")
            port_keys+=("$key")
        fi
    done < "$ENV_EXAMPLE"
    value="$(config_value JWT_SECRET)"
    if [[ ${#value} -ge 24 ]]; then
        jwt_size="$(printf '%s' "$value" | openssl base64 -d -A | wc -c)"
        canonical="$(printf '%s' "$value" | openssl base64 -d -A | openssl base64 -A)"
        if [[ "$jwt_size" -lt 32 || "$canonical" != "$value" ]]; then
            failures+=("JWT_SECRET 应为至少32字节随机数的标准 Base64 编码。")
        fi
    fi
    if [[ "$(config_value MYSQL_APP_USER)" == [Rr][Oo][Oo][Tt] ]]; then
        failures+=("MYSQL_APP_USER 不得使用 root。")
    fi
    case "$(config_value CARATCRAFT_ENV)" in
        local|'') ;;
        lan) if [[ -z "$(config_value MINIAPP_LAN_ORIGIN)" ]]; then
            failures+=("MINIAPP_LAN_ORIGIN 缺失。请填写手机可访问的局域网地址。")
        fi ;;
        *) failures+=("CARATCRAFT_ENV 必须是 local 或 lan。") ;;
    esac
    if [[ ${#failures[@]} -gt 0 ]]; then
        printf '%s\n' "${failures[@]}"
        return 1
    fi
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
    if command -v sha256sum >/dev/null 2>&1; then
        sha256sum | awk '{print toupper($1)}'
    else
        shasum -a 256 | awk '{print toupper($1)}'
    fi
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

# 人工确认后只删除归属正确的本项目数据库和缓存卷, 保留上传卷.
reset_test_data_volumes() {
    local mysql_volume redis_volume project_name volume owner confirmation config metadata
    config="$(compose config --format json)" || return 1
    mysql_volume="$(printf '%s' "$config" | python3 -c 'import json,sys; print(json.load(sys.stdin)["volumes"]["mysql-data"]["name"])')" || return 1
    redis_volume="$(printf '%s' "$config" | python3 -c 'import json,sys; print(json.load(sys.stdin)["volumes"]["redis-data"]["name"])')" || return 1
    project_name="$(printf '%s' "$config" | python3 -c 'import json,sys; print(json.load(sys.stdin)["name"])')" || return 1
    for volume in "$mysql_volume" "$redis_volume"; do
        [[ "$volume" =~ ^[a-zA-Z0-9][a-zA-Z0-9_.-]*$ ]] || return 1
        metadata="$(docker volume inspect "$volume" 2>/dev/null)" || return 1
        owner="$(printf '%s' "$metadata" | python3 -c 'import json,sys; print(json.load(sys.stdin)[0].get("Labels",{}).get("com.docker.compose.project",""))')" || return 1
        [[ -n "$owner" && "$owner" == "$project_name" ]] || return 1
    done
    echo "仅重建本项目MySQL及Redis开发数据, 上传卷保留。"
    read -r -p "输入 RESET_DATA 确认重建开发库和缓存: " confirmation
    [[ "$confirmation" == RESET_DATA ]] || { echo "已取消数据重置"; return 1; }
    compose down --remove-orphans || return 1
    for volume in "$mysql_volume" "$redis_volume"; do
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
            echo "本地开发库不是当前结构, 缺少$name。"
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
    local previous=0 checksum check apply stage
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
        for stage in "$registry/before.sql" "$directory/check.sql" "$registry/apply.sql" \
            "$directory/apply.sql" "$directory/check.sql" "$registry/after.sql"; do
            sed -e "s/__ID__/$id/g" -e "s/__SHA256__/$checksum/g" "$stage" || return 1
        done
    done
}

# 在同一MySQL连接内完成加锁、校验与幂等迁移.
apply_automatic_migrations() {
    local batch output
    batch="$(migration_batch)" || return 1
    output="$(database_query "$batch")" || { echo '安全结构增量失败，请核对迁移编号和数据库结构。' >&2; return 1; }
    while IFS= read -r line; do
        case "$line" in
            *': verified') printf '数据库迁移 %s：已应用，结构复核通过\n' "${line%:*}" ;;
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
    if [[ "$result" == 0 ]]; then assert_database_ready || result=$?; fi
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
    assert_data_volumes || return $?
    if [[ "$NO_BUILD" != true ]]; then build_local_environment || return $?; fi
    assert_local_images || return $?
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

    local image actual expected_id probe
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
        echo "$service：镜像一致，健康"
    done
    probe="$(docker exec platform-nginx sh -ec '
        wget -q -T 5 -t 1 -O /dev/null http://127.0.0.1/
        status=$(wget -S -T 5 -t 1 -O /dev/null http://127.0.0.1/platform-framework/sys/user/info 2>&1) || :
        case $status in *401\ Unauthorized*) ;; *) exit 1;; esac
        wget -q -T 5 -t 1 -O - http://127.0.0.1/platform-framework-api/app/customization/styles')" || return 1
    if ! grep -Eq '"code"[[:space:]]*:[[:space:]]*0([,}[:space:]])' <<< "$probe"; then
        echo '小程序款式接口未返回成功状态。' >&2
        return 1
    fi
    echo '代理验证通过：管理台、后台认证入口、DIY 款式接口。'
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
    echo "  小程序配置: 执行 dev.sh miniapp 生成。"
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
    configure) initialize_environment; exit ;;
    miniapp|diagnose|check-config|repair-config) exec node "$ROOT_DIR/tools/maintenance/dev-env.cjs" "$ACTION" ;;
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
[[ -f "$ENV_FILE" ]] || { echo '缺少 deploy/.env, 请先执行 dev.sh configure。'; exit 1; }

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
