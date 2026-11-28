#!/usr/bin/env bash

# 复用统一启动流程, 自动识别全新与已有环境.
exec bash "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/dev.sh" start "$@"
