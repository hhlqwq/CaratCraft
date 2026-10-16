# 源码规范与检查

新增业务代码采用Google风格: Java缩进2空格, 新增JavaScript函数使用明确的输入和返回语义. 函数、构造函数及测试用例应有中文说明, 标点使用英文符号. 延续既有Vue文件时保留文件原有分号约定, 避免无关格式变动; Vue模板和脚本均由ESLint检查. 第三方源码、原始许可证和版权头不按项目习惯改写.

Java使用Google Java Format 1.28.0. 工具从[Maven Central官方发布包](https://repo.maven.apache.org/maven2/com/google/googlejavaformat/google-java-format/1.28.0/google-java-format-1.28.0-all-deps.jar)取得, 放在工作区`.tools/copyright/google-java-format.jar`, 使用前对照同地址`.sha256`文件核验. 工具仅用于开发, 不作为应用组件分发.

执行`powershell -File tools/Check-SourceStyle.ps1`, 检查当前增量Java文件、完整管理端业务源码及Git空白符. 检查指定提交范围时传入`-Base 提交号`. 管理端使用ESLint 10与Vue插件的基础正确性规则, 不使用旧版ESLint 3加载器. 为保留历史路由和组件引用, 不强制所有旧路由组件重命名; 新增复用组件仍使用多词名称.

前端通过`pnpm run lint`检查, 通过`pnpm run build`生成生产产物. 检查不启动业务服务、不修改数据库、不调用真实支付或AI接口. 函数说明及业务边界仍需代码审查, 不能只靠格式器判断正确性.
