/* 由 deploy/.env 生成本机专用模块, 微信项目不保存任何服务地址副本. */
try {
    module.exports = require('./environment.local.js');
} catch (error) {
    throw new Error(
        '缺少小程序本机配置, 请先运行 deploy/scripts/dev.ps1 -Action miniapp 或 dev.sh miniapp'
    );
}
