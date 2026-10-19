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

const pages = import.meta.glob('../views/**/*.vue');
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
        isTab: true,
      },
    },
    {
      path: '/theme',
      component: _import('common/theme'),
      name: 'theme',
      meta: {
        title: '主题',
        isTab: true,
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
  // 是否已经添加动态(菜单)路由
  isAddDynamicMenuRoutes: false,
  routes: globalRoutes.concat(mainRoutes),
});

router.beforeEach((to, from, next) => {
  // 添加动态(菜单)路由
  // 1. 已经添加 or 全局路由, 直接访问
  // 2. 获取菜单列表, 添加并保存本地存储
  if (router.options.isAddDynamicMenuRoutes || fnCurrentRouteType(to, globalRoutes) === 'global') {
    next();
  } else {
    http({
      url: '/sys/menu/nav',
      method: 'get',
    })
      .then(({data}) => {
        if (data && data.code === 0) {
          fnAddDynamicMenuRoutes(data.data.menuList);
          router.options.isAddDynamicMenuRoutes = true;
          sessionStorage.setItem('menuList', JSON.stringify(data.data.menuList || '[]'));
          sessionStorage.setItem('permissions', JSON.stringify(data.data.permissions || '[]'));
          sessionStorage.setItem('dictList', JSON.stringify(data.data.dictList || '[]'));
          sessionStorage.setItem('orgList', JSON.stringify(data.data.orgList || '[]'));
          sessionStorage.setItem('userList', JSON.stringify(data.data.userList || '[]'));
          next({
            ...to,
            replace: true,
          });
        } else {
          sessionStorage.setItem('menuList', '[]');
          sessionStorage.setItem('permissions', '[]');
          sessionStorage.setItem('dictList', '[]');
          sessionStorage.setItem('orgList', '[]');
          sessionStorage.setItem('userList', '[]');
          next();
        }
      })
      .catch((e) => {
        console.log(`%c${e} 请求菜单列表和权限失败，跳转至登录页！！`, 'color:blue');
        router.push({name: 'login'});
      });
  }
});

/**
 * 判断当前路由类型, global: 全局路由, main: 主入口路由
 * @param {*} route 当前路由
 */
function fnCurrentRouteType(route, globalRoutes = []) {
  let temp = [];
  for (let i = 0; i < globalRoutes.length; i++) {
    if (route.path === globalRoutes[i].path) {
      return 'global';
    } else if (globalRoutes[i].children && globalRoutes[i].children.length >= 1) {
      temp = temp.concat(globalRoutes[i].children);
    }
  }
  return temp.length >= 1 ? fnCurrentRouteType(route, temp) : 'main';
}

/**
 * 添加动态(菜单)路由
 * @param {*} menuList 菜单列表
 * @param {*} routes 递归创建的动态(菜单)路由
 */
function fnAddDynamicMenuRoutes(menuList = [], routes = []) {
  let temp = [];
  for (let i = 0; i < menuList.length; i++) {
    if (menuList[i].list && menuList[i].list.length >= 1) {
      temp = temp.concat(menuList[i].list);
    } else if (menuList[i].url && /\S/.test(menuList[i].url)) {
      menuList[i].url = menuList[i].url.replace(/^\//, '');
      const route = {
        path: menuList[i].url.replace('/', '-'),
        component: null,
        name: menuList[i].url.replace('/', '-'),
        meta: {
          menuId: menuList[i].menuId,
          title: menuList[i].name,
          isDynamic: true,
          isTab: true,
          iframeUrl: '',
        },
      };
      // url以http[s]://开头, 通过iframe展示
      if (isURL(menuList[i].url)) {
        route.path = `i-${menuList[i].menuId}`;
        route.name = `i-${menuList[i].menuId}`;
        route.meta.iframeUrl = menuList[i].url;
      } else {
        try {
          route.component = _import(`modules/${menuList[i].url}`) || null;
        } catch (e) {
          console.log(e);
        }
      }
      routes.push(route);
    }
  }
  if (temp.length >= 1) {
    fnAddDynamicMenuRoutes(temp, routes);
  } else {
    routes.forEach((route) => router.addRoute('main', route));
    router.addRoute({path: '/:pathMatch(.*)*', redirect: {name: '404'}});

    sessionStorage.setItem('dynamicMenuRoutes', JSON.stringify(routes || '[]'));
  }
}

export default router;
