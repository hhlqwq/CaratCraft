# CaratCraft应用发布许可材料

2026-10-14版权整改后的实际应用依赖见[运行组件索引](generated/runtime-components.json). 该索引记录256个不同的后端运行JAR、36个前端/复制样式组件, 不混入未发布的测试和构建工具. 原始声明、父POM和NOTICE在`generated/evidence/`, 对应库源码及原始MIT发布包在`sources/`.

## 保留、替换与义务

- Highcharts、旧UEditor静态插件、EXE/SWF、旧wxParse、Oracle JDBC和aizuda-monitor已从当前应用源码/依赖中移除. 商品详情改用项目HTML编辑组件及微信原生rich-text, 监控改为直接使用OSHI.
- 当前JDBC为MariaDB Connector/J3.5.10, 连接现有MySQL服务. 选择LGPL-2.1-or-later, 保留许可证、对应源码, 并允许接收者替换库及为调试该库进行必要的分析. 这不把应用其他代码转为该库的版权.
- JNA、JSqlParser、Javassist等多许可组件选择其Apache2分支. Logback、AspectJ、JaCoCo及相应Jakarta库选择适用EPL分支; 老Java API库按所附CDDL分支处理. 不以出现GPL候选字样就认定应用必须全部开源.
- `sources/`提供13份需要随应用交付处理的对应Java库源码. 不能在商业交付时仅复制应用JAR而遗漏此目录及许可原文.
- 若旧MIT npm包未附独立LICENSE, 保留同版本官方MIT元数据、经完整性校验的原始发布包及标准MIT文本. 原包内版权声明原样保留, 不编造版权日期或把作者改为项目作者.
- CSS内复制的normalize.css7.0.0另有MIT证据. ElementUI主题字体与已安装官方2.15.10字体哈希一致, 保留其MIT文本; 这些库字体不是项目自有字体.
- [项目素材来源](project-assets.json)记录161个重新绘制的资源, 由`tools/generate-project-assets.py`中的几何图形生成. 珠宝图片是标注DEMO的功能示意, 商家上线必须使用其有权发布的真实商品图与文案.

源码原有BSD/Apache版权头保留, 包括只出现在法律声明里的历史名称. [源码声明集合](SOURCE-NOTICES.txt)同时随应用提供. 无关品牌的产品展示、推广、回调默认值和统计账号已清理. 许可材料中出现第三方名字不表示其为本项目提供背书.

## 接收者替换JDBC库

把修改后的兼容JDBC JAR放到`overrides/`, 可以使用应用中已有的Spring Boot PropertiesLauncher启动:

```sh
java -Dloader.path=./overrides -cp platform-api.jar org.springframework.boot.loader.launch.PropertiesLauncher
java -Dloader.path=./overrides -cp platform-admin.jar org.springframework.boot.loader.launch.PropertiesLauncher
```

本轮已用实际API发布JAR验证外部JDBC优先加载机制. 独立数据库连接、提交、回滚、分页和字符集另已通过. 接收者还可修改Maven依赖并使用完整项目源码重新构建. 商业交付条款不得禁止依法替换这些库及必要的调试.

## 应用交付与基础服务的边界

本次核验和交付对象是项目源码、应用JAR、前端/小程序资产及上述材料. MySQL、Redis、JRE、Nginx和基础操作系统镜像由部署者从各自官方渠道取得, 不包含在应用交付包内. 开发用Docker Compose中的MySQL可以保留自用, 不能据此把基础镜像作为没有分发义务的闭源/OEM安装包出售.

若另行交付完整OCI镜像、数据库服务器或整机安装包, 必须针对确切镜像摘要核验OS/JRE/数据库内全部软件的许可、声明、对应源码和提供方式. 本轮没有构建、上传或批准这种整包交付. 不需要安装付费组件即可运行应用, 但允许使用仍有各组件的分发条件.

## 重新构建与检查

在项目根目录使用既有依赖环境, 顺序执行:

```sh
node --openssl-legacy-provider tools/build-copyright-ui.cjs
python tools/build-license-bundle.py
python tools/complete-license-evidence.py
python tools/prepare-license-sources.py
python tools/check-copyright-distribution.py
```

后端应先按当前Maven配置打包admin/API. 补齐证据的脚本只从官方仓库读取小型文件, 不会静默批准未知许可. 更新依赖后必须重新生成并审核索引, 不能把本次索引当成所有未来版本的授权.

完整修改和验证记录见[版权整改结果](../../../docs/copyright-remediation-2026-10-14.md). 本材料不是对未知权利主张的零风险保证; 商家另外添加的商品摄影、字体、文案、商标和数据也须有自己的来源与授权.
