/* 隔离的管理端浏览器回归服务, 全部数据为固定测试样例, 不连接业务后端. */
const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '../.tools/copyright/admin-ui-dist');
const port = Number(process.env.CARATCRAFT_FIXTURE_PORT || 18981);
const modules = [
    ['mall/ad', '广告管理'],
    ['oss/oss', '文件管理'],
    ['mall/address', '地址管理'],
    ['mall/order', '订单管理'],
    ['custom/configuration', '定制配置'],
    ['custom/work-order', '制作工单'],
    ['custom/component-stock', '材料库存'],
    ['mall/goods', '商品管理'],
    ['community/posts', '社区管理'],
    ['merchant/legal', '协议与隐私'],
    ['merchant/contact', '商家服务'],
];
const permissions = [
    'sys:org:list',
    'sys:org:save',
    'sys:menu:list',
    'mall:ad:list',
    'mall:ad:save',
    'mall:address:list',
    'mall:address:info',
    'mall:order:list',
    'mall:order:update',
    'mall:goods:list',
    'mall:goods:save',
    'custom:config:list',
    'custom:config:update',
    'custom:work:list',
    'custom:work:update',
    'custom:stock:list',
    'custom:stock:update',
    'sys:monitor:server',
    'community:post:list',
    'community:post:moderate',
    'merchant:legal:read',
    'merchant:legal:update',
    'merchant:legal:publish',
    'merchant:contact:read',
    'merchant:contact:update',
    'sys:oss:list',
];
const routes = {
    '/sys/org/queryAll': [
        {orgNo: 'test-root', parentNo: '0', orgName: '隔离总部', status: 1},
        {orgNo: 'test-child', parentNo: 'test-root', orgName: '隔离门店', status: 1},
    ],
    '/sys/menu/list': [
        {menuId: 'test-root', parentId: '0', name: '隔离目录', type: 0},
        {menuId: 'test-child', parentId: 'test-root', name: '隔离子菜单', type: 1},
    ],
    '/mall/ad/queryAll': [],
    '/mall/address/list': {
        records: [
            {
                id: 900001,
                nickname: '隔离测试会员',
                userName: '隔离收货人',
                mobile: '',
                detailInfo: '仅用于界面测试的地址',
                isDefault: 1,
            },
        ],
        total: 1,
    },
    '/mall/address/info/900001': {
        id: 900001,
        userName: '隔离收货人',
        mobile: '',
        detailInfo: '仅用于界面测试的地址',
        isDefault: 1,
    },
    '/merchant/legal/TERMS': {
        head: {revision: 1, published_version: 0},
        versions: [
            {
                kind: 'TERMS',
                version: 1,
                title: '隔离测试用户协议',
                content: '仅用于界面回归的测试正文, 不作为真实经营协议.',
                status: 'DRAFT',
            },
        ],
    },
    '/merchant/legal/PRIVACY': {
        head: {revision: 1, published_version: 1},
        versions: [
            {
                kind: 'PRIVACY',
                version: 1,
                title: '隔离测试隐私政策',
                content: '仅用于界面回归的隐私正文.',
                status: 'PUBLISHED',
            },
        ],
    },
    '/merchant/contact': {
        merchantName: '隔离测试商家',
        phone: '',
        wechatId: '',
        qrUrl: '',
        wxChatEnabled: false,
        version: 0,
    },
    '/merchant/contact/history': [],
    '/sys/oss/list': {records: [{id: 'test-image', url: '/static/img/avatar.png'}], total: 1},
    '/sys/menu/nav': {
        menuList: [
            {
                menuId: 'system-fixture',
                name: '系统设置',
                icon: 'system',
                list: [
                    {menuId: 'org-fixture', name: '组织管理', url: 'sys/org', list: []},
                    {menuId: 'menu-fixture', name: '菜单管理', url: 'sys/menu', list: []},
                ],
            },
            ...modules.map(([url, name], index) => ({menuId: index + 1, name, url, list: []})),
        ],
        permissions,
        dictList: [],
        orgList: [],
        userList: [],
    },
    '/sys/user/info': {userId: 1, userName: '界面测试账号'},
    '/mall/dashboard/summary': {
        unpaid: 8,
        toShip: 3,
        refundReview: 2,
        refundAttention: 1,
        workReview: 4,
        workMaking: 5,
        stockOut: 2,
    },
    '/custom/configuration/goods': [{id: 900001, name: '隔离测试珍珠项链'}],
    '/custom/configuration/products': [
        {
            goodsId: 900001,
            enabled: 0,
            requiredComponentTypes: 'pearl,chain,clasp',
            previewWidth: 600,
            previewHeight: 600,
        },
    ],
    '/custom/configuration/options': [],
    '/community/posts/list': {
        records: [
            {
                id: 900001,
                kind: 'DESIGN',
                title: '隔离测试: 大小珍珠交错排列',
                content:
                    '这是一份用于检查界面排版的模拟分享。材料大小与位置可在DIY中调整。\n不连接业务后端, 不会发布真实社区内容。',
                imagesJson: '[]',
                designJson: JSON.stringify({
                    arrangement: [
                        {position: 0, name: '测试淡水珍珠', sizeMm: 8},
                        {position: 1, name: '测试尾扣', sizeMm: 12},
                    ],
                }),
                status: 'PENDING',
                version: 0,
                likes: 0,
                moderationReason: '',
                createTime: '2026-10-19 12:00:00',
            },
        ],
        total: 1,
    },
    '/community/posts/logs': [
        {
            operatorName: '界面测试账号',
            fromStatus: 'PENDING',
            toStatus: 'HIDDEN',
            reason: '模拟审核记录',
            createTime: '2026-10-19 12:00:00',
        },
    ],
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
        return response.end(
            JSON.stringify({code: 0, data: routes[endpoint] || {records: [], total: 0, list: []}})
        );
    }
    if (url.pathname === '/static/config/index.js') {
        response.setHeader('Content-Type', 'text/javascript');
        return response.end(
            "window.SITE_CONFIG={baseUrl:location.origin+'/fixture-api',cdnUrl:'./',domain:'./',version:''}"
        );
    }
    const file = path.resolve(
        root,
        '.' + decodeURIComponent(url.pathname === '/' ? '/index.html' : url.pathname)
    );
    if (!file.startsWith(root + path.sep) || !fs.existsSync(file) || !fs.statSync(file).isFile()) {
        response.writeHead(404);
        return response.end('Not found');
    }
    const types = {
        '.html': 'text/html; charset=utf-8',
        '.js': 'text/javascript',
        '.css': 'text/css',
        '.svg': 'image/svg+xml',
        '.png': 'image/png',
        '.woff': 'font/woff',
        '.ttf': 'font/ttf',
    };
    response.setHeader('Content-Type', types[path.extname(file)] || 'application/octet-stream');
    if (path.extname(file) === '.html') {
        response.setHeader(
            'Set-Cookie',
            'token=fixture-only-no-business-access; Path=/; SameSite=Lax'
        );
        return response.end(
            fs
                .readFileSync(file, 'utf8')
                .replace(
                    '</body>',
                    '<aside style="position:fixed;bottom:0;right:0;z-index:99999;background:#fff3cd;color:#664d03;padding:5px">隔离界面回归 · 模拟数据</aside></body>'
                )
        );
    }
    fs.createReadStream(file).pipe(response);
}

/** 测试可在随机回环端口启动服务, 返回句柄以便只关闭本测试服务. */
function start(selectedPort = port) {
    const server = http.createServer(serve);
    return new Promise((resolve) =>
        server.listen(selectedPort, '127.0.0.1', () => resolve(server))
    );
}

if (require.main === module) {
    start().then((server) =>
        console.log('隔离界面回归: http://127.0.0.1:' + server.address().port + '/#/home')
    );
}
module.exports = {start};
