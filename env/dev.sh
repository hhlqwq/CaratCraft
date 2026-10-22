#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT_DIR="$ROOT_DIR"
COMPOSE_FILE="$PROJECT_DIR/deploy/docker-compose.yml"
ENV_FILE="$PROJECT_DIR/deploy/.env"
ENV_EXAMPLE="$PROJECT_DIR/deploy/.env.example"

ACTION="start"
RESET_DATA="false"
NO_BUILD="false"

# 输出脚本使用方式 / Prints script usage.
usage() {
    echo "用法: ./env/dev.sh [start|stop|restart|status|logs] [--reset-data] [--no-build]"
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

# 启动前校验必要配置, 不修改已有数据库账号或密码.
assert_startup_configuration() {
    local key value
    for key in MYSQL_ROOT_PASSWORD MYSQL_APP_PASSWORD REDIS_PASSWORD JWT_SECRET; do
        value="$(sed -n "s/^${key}=//p" "$ENV_FILE" | tail -n 1)"
        [[ ${#value} -ge 24 ]] || { echo "$key 缺失或过弱, 请先按迁移说明配置独立应用账号和强密钥"; exit 1; }
    done
    value="$(sed -n 's/^MYSQL_APP_USER=//p' "$ENV_FILE" | tail -n 1)"
    [[ "$value" != root ]] || { echo "应用账号不得使用root"; exit 1; }
    BUILD_REVISION="$(git -C "$ROOT_DIR" rev-parse --short HEAD 2>/dev/null || echo workspace)"
    [[ -z "$(git -C "$ROOT_DIR" status --porcelain --untracked-files=normal 2>/dev/null)" ]] || BUILD_REVISION="${BUILD_REVISION}-dirty"
    export BUILD_REVISION
}

# 仅删除当前项目的MySQL和Redis卷, 不删除上传卷, 并要求执行者确认.
reset_test_data_volumes() {
    local mysql_volume redis_volume project_name volume owner confirmation
    mysql_volume="$(compose config --format json | python3 -c 'import json,sys; print(json.load(sys.stdin)["volumes"]["mysql-data"]["name"])')"
    redis_volume="$(compose config --format json | python3 -c 'import json,sys; print(json.load(sys.stdin)["volumes"]["redis-data"]["name"])')"
    project_name="$(compose config --format json | python3 -c 'import json,sys; print(json.load(sys.stdin)["name"])')"
    for volume in "$mysql_volume" "$redis_volume"; do
        [[ "$volume" =~ ^[a-zA-Z0-9][a-zA-Z0-9_.-]*$ ]] || exit 1
        owner="$(docker volume inspect --format '{{index .Labels "com.docker.compose.project"}}' "$volume" 2>/dev/null || true)"
        [[ -z "$owner" || "$owner" == "$project_name" ]] || exit 1
    done
    echo "仅删除数据库和缓存卷: $mysql_volume, $redis_volume; 上传卷保留。请先备份。"
    read -r -p "输入 RESET_DATA 确认删除数据库和缓存: " confirmation
    [[ "$confirmation" == RESET_DATA ]] || { echo "已取消数据重置"; exit 1; }
    compose down --remove-orphans
    for volume in "$mysql_volume" "$redis_volume"; do
        if docker volume inspect "$volume" >/dev/null 2>&1; then docker volume rm "$volume"; fi
    done
}

# 执行统一的 Compose 命令 / Runs a unified Compose command.
compose() {
    docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

# 校验免构建镜像的源码标识, 拒绝复用未提供新版探针的历史镜像.
assert_local_images() {
    local name revision
    for name in carat-craft-backend:local carat-craft-admin-ui:local; do
        revision="$(docker image inspect --format '{{index .Config.Labels "org.opencontainers.image.revision"}}' "$name" 2>/dev/null)" || {
            echo "缺少镜像 $name, 请移除--no-build重新运行。"; return 1;
        }
        [[ "$revision" == "$BUILD_REVISION" ]] || {
            echo "镜像 $name 与当前源码标识不一致, 请移除--no-build重新构建。"; return 1;
        }
    done
}

# 使用数据库容器自身凭据查询结构, 不输出密码或业务行.
database_query() {
    compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot --batch --skip-column-names "$MYSQL_DATABASE" "$@"' sh "--execute=$1"
}

# 在应用启动前识别部分初始化及缺少迁移的旧数据库.
assert_database_ready() {
    local tables name version
    tables="$(database_query "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME IN ('sys_region','mall_order','mall_user','mall_goods','CC_SCHEMA_MIGRATION');")" || return 1
    for name in sys_region mall_order mall_user mall_goods CC_SCHEMA_MIGRATION; do
        if ! grep -Fxq "$name" <<< "$tables"; then
            echo "数据库初始化或升级不完整, 缺少$name。请先备份并按docs/项目维护文档.md修复, 不要直接重置数据卷。"
            return 1
        fi
    done
    version="$(database_query "SELECT COUNT(*) FROM CC_SCHEMA_MIGRATION WHERE version='V20261022_01_sku_contract.sql';")" || return 1
    [[ "$version" == 1 ]] || { echo "请先按docs/项目维护文档.md备份并完成V20261022_01迁移。"; return 1; }
}

# 输出有限的容器状态和健康检查结果, 保留启动失败退出码.
show_startup_diagnostics() {
    compose ps --all || true
    local name
    for name in platform-admin platform-api; do
        docker inspect --format '{{json .State.Health}}' "$name" 2>/dev/null || true
    done
    echo "日志: ./env/dev.sh logs。缺表请先备份, 旧镜像请重新构建, 详见docs/项目维护文档.md。"
}

# 按镜像、数据服务、结构检查、应用就绪的顺序启动环境.
start_local_environment() {
    local result=0
    if [[ "$NO_BUILD" == true ]]; then
        assert_local_images || result=$?
    else
        compose build || result=$?
        if [[ "$result" != 0 ]]; then
            echo "镜像构建失败, 请查看上方Docker构建错误并检查镜像或依赖仓库网络。已有当前源码镜像可使用./env/dev.sh --no-build启动。"
            return "$result"
        fi
    fi
    if [[ "$result" == 0 ]]; then
        compose up --detach --wait --wait-timeout 300 mysql redis || result=$?
    fi
    if [[ "$result" == 0 ]]; then assert_database_ready || result=$?; fi
    if [[ "$result" == 0 ]]; then
        compose up --detach --no-build --wait --wait-timeout 300 || result=$?
    fi
    if [[ "$result" != 0 ]]; then show_startup_diagnostics; return "$result"; fi
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
    echo "  小程序 API: http://localhost:$port/platform-framework-api/app/"
}

if [[ $# -gt 0 && "$1" != --* ]]; then
    ACTION="$1"
    shift
fi

while [[ $# -gt 0 ]]; do
    case "$1" in
        --reset-data) RESET_DATA="true" ;;
        --no-build) NO_BUILD="true" ;;
        -h|--help) usage; exit 0 ;;
        *) echo "未知参数: $1"; usage; exit 1 ;;
    esac
    shift
done

case "$ACTION" in
    start|stop|restart|status|logs) ;;
    *) echo "未知操作: $ACTION"; usage; exit 1 ;;
esac

if [[ "$RESET_DATA" == "true" && "$ACTION" != "start" && "$ACTION" != "restart" ]]; then
    echo "--reset-data 只能与 start 或 restart 一起使用。"
    exit 1
fi

assert_docker_ready
initialize_environment

case "$ACTION" in
    stop)
        compose down --remove-orphans
        ;;
    status)
        compose ps
        ;;
    logs)
        compose logs --follow --tail=200
        ;;
    restart)
        assert_startup_configuration
        if [[ "$RESET_DATA" == "true" ]]; then reset_test_data_volumes; else compose down --remove-orphans; fi
        start_local_environment
        ;;
    start)
        assert_startup_configuration
        if [[ "$RESET_DATA" == "true" ]]; then reset_test_data_volumes; fi
        start_local_environment
        ;;
esac
