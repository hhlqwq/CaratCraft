#!/usr/bin/env bash

# 复用统一构建流程, 保留参数和真实退出码.
exec bash "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/dev.sh" build "$@"
