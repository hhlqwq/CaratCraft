# 私有支付证书

通过只读目录挂载微信支付 APIv3 的 `apiclient_key.pem` 及 `wechatpay_public_key.pem`. 商户证书序列号、公钥 ID 与 APIv3 密钥由部署环境配置. 不提交真实密钥或证书.
证书不提交Git、不复制到前端、不通过图片上传接口或Nginx公开目录分发.
当前目录已忽略除本文档之外的文件. 证书和支付账户须由实际运营商家提供并保持匹配.

标准首次启动还生成超管临时凭据. `platform-bootstrap.txt` 供宿主当前用户读取, `platform-bootstrap.runtime.txt` 为相同凭据的容器副本, 仅供后端固定用户 UID/GID 999 读取. 两份文件均为600权限, 宿主副本与 `.env` 归宿主用户所有. 首次改密后按维护文档移除引导配置和两份文件, 不将其放入上传卷或公开目录. Windows 文件访问同时遵循项目目录的本机ACL.
