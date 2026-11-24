/**
 * 全站路由配置
 *
 * 建议:
 * 1. 代码中路由统一使用name属性跳转(不使用path属性)
 */
import cookie from '@/utils/cookie';
import {createRouter, createWebHashHistory} from 'vue-router';
import http from '@/utils/httpRequest';
import {isURL} from '@/utils/validate';
import {clearLoginInfo} from '@/utils';
import {authorizedPages, pagePresentation} from './workspace-navigation';

const pages = import.meta.glob(['../views/**/*.vue', '!../views/modules/oss/oss-upload.vue']);
/** 只允许加载构建时存在的页面, 不执行服务端提供的任意路径. */
function _import(name) {
    const page = pages['../views/' + name + '.vue'];
    if (!page) {
        throw new Error('页面不存在: ' + name);
    }
    return page;
}

// 全局路由(无需嵌套上左右整体布局)
const globalRoutes = [
    {
        path: '/404',
        component: _import('common/404'),
        name: '404',
        meta: {title: '404未找到'},
    },
    {
        path: '/login',
        component: _import('common/login'),
        name: 'login',
        meta: {title: '登录'},
    },
    {
        path: '/password-change',
        component: _import('main-navbar-update-password'),
        name: 'password-change',
        props: {requiredChange: true},
        meta: {title: '修改临时密码'},
    },
    {
        path: '/password-recovery',
        component: _import('common/password-recovery'),
        name: 'password-recovery',
        meta: {title: '找回密码'},
    },
];

// 主入口路由(需嵌套上左右整体布局)
const mainRoutes = {
    path: '/',
    component: _import('main'),
    name: 'main',
    redirect: {name: 'home'},
    meta: {title: '主入口整体布局'},
    children: [
        // 通过meta对象设置路由展示方式
        // 1. isTab: 是否通过tab展示内容, true: 是, false: 否
        // 2. iframeUrl: 是否通过iframe嵌套展示内容, '以http[s]://开头': 是, '': 否
        // 提示: 如需要通过iframe嵌套展示内容, 但不通过tab打开, 请自行创建组件使用iframe处理!
        {
            path: '/home',
            component: _import('common/home'),
            name: 'home',
            meta: {
                title: '首页',
            },
        },
        {
            path: '/theme',
            component: _import('common/theme'),
            name: 'theme',
            meta: {
                title: '主题',
            },
        },
    ],
    beforeEnter(to, from, next) {
        const token = cookie.get('token');
        if (!token || !/\S/.test(token)) {
            clearLoginInfo();
            return next({name: 'login'});
        }
        next();
    },
};

const router = createRouter({
    history: createWebHashHistory(),
    scrollBehavior: () => ({top: 0}),
    routes: globalRoutes.concat(mainRoutes),
});

const removeRoutes = [];
let loadedFor = '';
let pendingNavigation;

/** 注销或切换账号时清除上一账号注册的动态路由. */
router.clearWorkspaceRoutes = () => {
    removeRoutes.splice(0).forEach((remove) => remove());
    loadedFor = '';
    pendingNavigation = undefined;
};

/** 用服务端返回的页面构建访问路由, 附属页还需对应的动作权限. */
function registerMenus(menus, grants) {
    for (const page of authorizedPages(menus)) {
        if (page.url === 'sys/menu') {
            continue;
        }
        const external = isURL(page.url);
        const name = external ? `i-${page.menuId}` : page.url.replace('/', '-');
        const meta = {
            ...pagePresentation(page),
            menuName: name,
            iframeUrl: external ? page.url : '',
        };
        const component = external
            ? _import('common/404')
            : pages[`../views/modules/${page.url}.vue`];
        if (!component || router.hasRoute(name)) {
            continue;
        }
        removeRoutes.push(router.addRoute('main', {path: name, name, component, meta}));
        if (name === 'mall-order' && grants.includes('mall:order:info')) {
            removeRoutes.push(
                router.addRoute('main', {
                    path: 'mall-order/:id(\\d+)',
                    name: 'mall-order-detail',
                    component: _import('modules/mall/order-detail'),
                    meta: {...meta, parent: name, parentTitle: meta.title, title: '订单详情'},
                })
            );
        }
        if (name === 'mall-goods') {
            const editor = _import('modules/mall/goods-add-or-update');
            const destinations = [
                {
                    suffix: '/new',
                    name: 'mall-goods-new',
                    title: '新增商品',
                    grants: ['mall:goods:save'],
                },
                {
                    suffix: '/:id(\\d+)',
                    name: 'mall-goods-detail',
                    title: '商品详情',
                    grants: ['mall:goods:info'],
                },
                {
                    suffix: '/:id(\\d+)/edit',
                    name: 'mall-goods-edit',
                    title: '编辑商品',
                    grants: ['mall:goods:info', 'mall:goods:update'],
                },
            ];
            for (const destination of destinations) {
                if (destination.grants.every((grant) => grants.includes(grant))) {
                    removeRoutes.push(
                        router.addRoute('main', {
                            path: name + destination.suffix,
                            name: destination.name,
                            component: editor,
                            meta: {
                                ...meta,
                                title: destination.title,
                                parent: name,
                                parentTitle: '商品列表',
                            },
                        })
                    );
                }
            }
        }
        if (name === 'sys-user') {
            if (grants.includes('sys:user:save')) {
                removeRoutes.push(
                    router.addRoute('main', {
                        path: 'sys-user/new',
                        name: 'sys-user-new',
                        component: _import('modules/sys/user-add-or-update'),
                        meta: {...meta, parent: name, parentTitle: meta.title, title: '新建员工'},
                    })
                );
            }
            if (grants.includes('sys:user:update') && grants.includes('sys:user:info')) {
                removeRoutes.push(
                    router.addRoute('main', {
                        path: 'sys-user/:id/edit',
                        name: 'sys-user-edit',
                        component: _import('modules/sys/user-add-or-update'),
                        meta: {...meta, parent: name, parentTitle: meta.title, title: '编辑员工'},
                    })
                );
            }
        }
    }
    removeRoutes.push(router.addRoute({path: '/:pathMatch(.*)*', redirect: {name: '404'}}));
}

/** 同一会话只加载一次授权菜单, 首次直达时按新注册的路径重新解析. */
router.beforeEach(async (to) => {
    if (['login', '404', 'password-recovery'].includes(to.name)) {
        return true;
    }
    const token = cookie.get('token');
    if (!token) {
        clearLoginInfo();
        return {name: 'login'};
    }
    if (to.name === 'password-change') {
        return true;
    }
    if (loadedFor !== token) {
        if (!pendingNavigation || pendingNavigation.token !== token) {
            router.clearWorkspaceRoutes();
            pendingNavigation = {
                token,
                request: http({url: '/sys/menu/nav', method: 'get', showError: false}),
            };
        }
        try {
            const {data} = await pendingNavigation.request;
            if (cookie.get('token') !== token) {
                return {name: 'login'};
            }
            if (data?.code === 428) {
                router.clearWorkspaceRoutes();
                return {name: 'password-change'};
            }
            if (data?.code !== 0) {
                clearLoginInfo();
                return {name: 'login'};
            }
            if (loadedFor !== token) {
                const navigation = data.data;
                for (const key of ['menuList', 'permissions', 'dictList', 'orgList', 'userList']) {
                    sessionStorage.setItem(key, JSON.stringify(navigation[key] || []));
                }
                registerMenus(navigation.menuList || [], navigation.permissions || []);
                loadedFor = token;
                pendingNavigation = undefined;
            }
            return {path: to.path, query: to.query, hash: to.hash, replace: true};
        } catch {
            clearLoginInfo();
            return {name: 'login'};
        }
    }
    if (to.name === 'home' && router.hasRoute('platform-merchant-accounts')) {
        return {name: 'platform-merchant-accounts', replace: true};
    }
    return to.matched.length > 0 || {name: '404'};
});

export default router;
