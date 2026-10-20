# 源码规范与检查

业务代码统一采用Google风格的基本约定: 两空格缩进, 明确命名, JavaScript使用单引号和分号, 对象括号内不增加空格. 新代码优先使用`const`, 确实需要重新赋值时使用`let`, 小程序不再使用`var`. 统一规则覆盖存量业务源码, 不只覆盖新增文件. 第三方源码、原始许可证和版权头不按项目习惯改写.

2026-10-20更新: C/C++采用Google风格并使用4空格缩进, 已在根目录EditorConfig为相关扩展名单独覆盖. Java及前端继续使用2空格.

Git属性同时固定业务源码扩展名的LF换行, 避免Windows的`core.autocrlf`在检出时反复改变格式检查结果. 编码和编辑器缩进仍由EditorConfig约束, 不对原始许可证内容作风格改写.

函数、构造函数及测试用例应有中文职责说明, 标点使用英文符号. 注释解释业务目的、输入边界、状态变化或异常处理, 不重复函数名称, 不保留空生命周期和模板提示语. 小程序静态检查要求具名工具函数和页面入口紧邻中文说明, 匿名回调由所属函数说明职责, 复杂回调仍应另作解释. Java重写方法优先参照接口契约, 改变边界或副作用时补充实现说明. 格式器不能证明注释内容准确或业务设计合理, 复杂逻辑仍须逐项审阅.

Java使用Google Java Format 1.28.0. 工具从[Maven Central官方发布包](https://repo.maven.apache.org/maven2/com/google/googlejavaformat/google-java-format/1.28.0/google-java-format-1.28.0-all-deps.jar)取得, 放在工作区`.tools/copyright/google-java-format.jar`, 使用前对照同地址`.sha256`文件核验. 工具仅用于开发, 不作为应用组件分发.

安装管理端锁定依赖后, 执行`pwsh -File tools/Check-SourceStyle.ps1`, 默认检查四个Java模块的全部`src`源码及测试、完整管理端业务源码、小程序全部脚本和前端格式、Git空白符. 执行`pwsh -File tools/Check-SourceStyle.ps1 -Format`可整理全量格式再检查; 不会自动修改业务逻辑或补写注释. 仅开发时快速检查可使用`-Incremental -Base 提交号`, 该模式只缩小Java范围, 不替代交付前的全量检查.

Java文件分批检查以适配Windows命令行长度限制, 每批输出进度. 前端由固定版本Prettier 3.6.2按根目录`.prettierrc.json`检查, 共同约定见[Prettier官方配置说明](https://prettier.io/docs/configuration). 管理端使用ESLint 10与Vue插件检查脚本及模板; 小程序另有微信全局对象配置和函数说明规则, 不以浏览器环境屏蔽真实未定义变量. 为保留历史路由和组件引用, 不强制旧路由组件重命名; 新增复用组件仍使用多词名称.

前端范围包含管理端`src`中的JS/Vue/CSS/SCSS与小程序JS/WXSS/JSON. WXML保留微信模板语法, 本轮不交给HTML格式器强行改写; 第三方DOMPurify、主题原件、依赖目录、构建输出和许可原件不进入格式化范围. Prettier只作为开发依赖, 不引入应用运行时包.

管理端通过`pnpm run lint`检查, `pnpm run format:check`检查两端业务格式, `pnpm run format`整理两端业务格式, `pnpm run build`生成生产产物. `tools/clean-miniprogram-source.cjs`是一次性模板清理辅助工具, 仅移除语法树确认的空生命周期和未使用局部绑定; 不移除分享入口或有业务语句的回调. 常规检查不会运行该清理工具. 以上检查不启动业务服务、不修改数据库、不调用真实支付或AI接口.
