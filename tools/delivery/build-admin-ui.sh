#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
UI_DIR="$ROOT_DIR/apps/admin-ui"
TARGET_DIR="$ROOT_DIR/deploy/packages/platform-admin-ui-dist"

rm -rf "$TARGET_DIR"
mkdir -p "$TARGET_DIR"

cd "$UI_DIR"
echo "开始使用项目指定的pnpm构建管理端..."
pnpm run build

cp -R "$UI_DIR/dist/." "$TARGET_DIR/"

echo "前端 dist 已输出到:"
echo "  $TARGET_DIR"

