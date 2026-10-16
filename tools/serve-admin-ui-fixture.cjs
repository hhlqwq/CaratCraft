/* 隔离的管理端浏览器回归服务, 全部数据为固定测试样例, 不连接业务后端. */
const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '../.tools/copyright/admin-ui-dist');
const port = Number(process.env.CARATCRAFT_FIXTURE_PORT || 18981);
const modules = [['mall/order', '订单管理'], ['custom/configuration', '定制配置'],
  ['custom/work-order', '制作工单'], ['custom/component-stock', '材料库存'], ['mall/goods', '商品管理']];
const permissions = ['mall:order:list', 'mall:order:update', 'mall:goods:list', 'mall:goods:save',
  'custom:config:list', 'custom:config:update', 'custom:work:list', 'custom:work:update',
  'custom:stock:list', 'custom:stock:update', 'sys:monitor:server'];
const routes = {
  '/sys/menu/nav': {menuList: modules.map(([url, name], index) => ({menuId: index + 1, name, url, list: []})),
    permissions, dictList: [], orgList: [], userList: []},
  '/sys/user/info': {userId: 1, userName: '界面测试账号'},
  '/mall/dashboard/summary': {unpaid: 8, toShip: 3, refundReview: 2, refundAttention: 1, workReview: 4, workMaking: 5, stockOut: 2},
  '/custom/configuration/goods': [{id: 900001, name: '隔离测试珍珠项链'}],
  '/custom/configuration/products': [{goodsId: 900001, enabled: 0, requiredComponentTypes: 'pearl,chain,clasp', previewWidth: 600, previewHeight: 600}],
  '/custom/configuration/options': [],
};

/** 固定回归数据只支持读取, 未实现的业务写入明确拒绝. */
function serve(request, response) {
  const url = new URL(request.url, 'http://127.0.0.1:18981');
  if (url.pathname.startsWith('/fixture-api/')) {
    const endpoint = url.pathname.replace('/fixture-api', '');
    response.setHeader('Content-Type', 'application/json; charset=utf-8');
    if (request.method !== 'GET') {
      response.writeHead(405);
      return response.end(JSON.stringify({code: 405, msg: '隔离界面回归不执行业务写入'}));
    }
    return response.end(JSON.stringify({code: 0, data: routes[endpoint] || {records: [], total: 0, list: []}}));
  }
  if (url.pathname === '/static/config/index.js') {
    response.setHeader('Content-Type', 'text/javascript');
    return response.end("window.SITE_CONFIG={baseUrl:location.origin+'/fixture-api',cdnUrl:'./',domain:'./',version:''}");
  }
  const file = path.resolve(root, '.' + decodeURIComponent(url.pathname === '/' ? '/index.html' : url.pathname));
  if (!file.startsWith(root + path.sep) || !fs.existsSync(file) || !fs.statSync(file).isFile()) {
    response.writeHead(404);
    return response.end('Not found');
  }
  const types = {'.html': 'text/html; charset=utf-8', '.js': 'text/javascript', '.css': 'text/css',
    '.svg': 'image/svg+xml', '.png': 'image/png', '.woff': 'font/woff', '.ttf': 'font/ttf'};
  response.setHeader('Content-Type', types[path.extname(file)] || 'application/octet-stream');
  if (path.extname(file) === '.html') {
    response.setHeader('Set-Cookie', 'token=fixture-only-no-business-access; Path=/; SameSite=Lax');
    return response.end(fs.readFileSync(file, 'utf8').replace('</body>',
      '<aside style="position:fixed;bottom:0;right:0;z-index:99999;background:#fff3cd;color:#664d03;padding:5px">隔离界面回归 · 模拟数据</aside></body>'));
  }
  fs.createReadStream(file).pipe(response);
}

http.createServer(serve).listen(port, '127.0.0.1', () => console.log('隔离界面回归: http://127.0.0.1:' + port + '/#/home'));
