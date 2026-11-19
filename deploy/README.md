# 部署配置入口

本目录集中Compose、Dockerfile、Nginx、健康探针和环境变量模板. 完整操作步骤统一见[项目维护文档](../docs/项目维护文档.md), 不在此复制另一套部署及升级说明.

Windows从仓库`deploy/scripts/`依次使用`build-local-test.cmd`和`start-local-test.cmd`. Linux/macOS使用`./scripts/dev.sh build`后使用`./scripts/dev.sh start`. 构建与启动行为和授权边界以维护文档为准.

Compose构建上下文为仓库根目录, 项目名固定为`platform-wxshop`, 保留原默认命名卷归属. 当前未上线, 空库初始化唯一来自`database/init/`的完整结构和种子, 不保留开发期历史升级链. 本地旧开发结构按现有启动规则重建; 已有真实数据环境不得执行初始化或使用开发重建入口.

私有`.env`及`secrets/`不进入Git、构建上下文或交付包. 旧运行挂载原件可能仍在本机忽略目录中, 不能在未核对运行实例和备份前清除. 目录调整不会自动升级已运行的镜像或数据库.
