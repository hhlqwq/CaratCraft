#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$ROOT_DIR/vendor/platform-wxshop"
COMPOSE_FILE="$PROJECT_DIR/docker-compose.yml"
ENV_FILE="$PROJECT_DIR/deploy/.env"
ENV_EXAMPLE="$PROJECT_DIR/deploy/.env.example"

ACTION="start"
RESET_DATA="false"
NO_BUILD="false"
LEGACY_BUILD="false"

# 输出脚本使用方式 / Prints script usage.
usage() {
  echo "用法: ./dev.sh [start|stop|restart|status|logs] [--reset-data] [--no-build] [--legacy-build]"
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

# 启用传统构建器兼容模式 / Enables legacy builder compatibility mode.
enable_legacy_build() {
  export COMPOSE_BAKE=false
  export DOCKER_BUILDKIT=0
  echo "警告: 已启用传统构建器兼容模式，本次构建不会使用 BuildKit 缓存。"
}

# 执行统一的 Compose 命令 / Runs a unified Compose command.
compose() {
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
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
    --legacy-build) LEGACY_BUILD="true" ;;
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

if [[ "$LEGACY_BUILD" == "true" ]]; then
  enable_legacy_build
fi

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
    up_args=(up --detach --wait --wait-timeout 300)
    [[ "$NO_BUILD" == "true" ]] || up_args+=(--build)
    compose "${up_args[@]}"
    show_access_info
    ;;
  start)
    assert_startup_configuration
    if [[ "$RESET_DATA" == "true" ]]; then reset_test_data_volumes; fi
    up_args=(up --detach --wait --wait-timeout 300)
    [[ "$NO_BUILD" == "true" ]] || up_args+=(--build)
    compose "${up_args[@]}"
    show_access_info
    ;;
esac
