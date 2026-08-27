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

# 输出脚本使用方式 / Prints script usage.
usage() {
  echo "用法: ./dev.sh [start|stop|restart|status|logs] [--reset-data] [--no-build]"
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
    cp "$ENV_EXAMPLE" "$ENV_FILE"
    echo "已生成默认配置: $ENV_FILE"
  fi
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
    down_args=(down --remove-orphans)
    if [[ "$RESET_DATA" == "true" ]]; then
      echo "警告: 将删除 CaratCraft 的 MySQL 和 Redis 测试数据卷。"
      down_args+=(--volumes)
    fi
    compose "${down_args[@]}"
    up_args=(up --detach --wait --wait-timeout 300)
    [[ "$NO_BUILD" == "true" ]] || up_args+=(--build)
    compose "${up_args[@]}"
    show_access_info
    ;;
  start)
    if [[ "$RESET_DATA" == "true" ]]; then
      echo "警告: 将删除 CaratCraft 的 MySQL 和 Redis 测试数据卷。"
      compose down --volumes --remove-orphans
    fi
    up_args=(up --detach --wait --wait-timeout 300)
    [[ "$NO_BUILD" == "true" ]] || up_args+=(--build)
    compose "${up_args[@]}"
    show_access_info
    ;;
esac
