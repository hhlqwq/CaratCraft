<template>
    <aside class="site-sidebar" :class="'site-sidebar--' + sidebarLayoutSkin">
        <div class="site-sidebar__inner">
            <el-menu
                unique-opened
                :default-active="menuActiveName || 'home'"
                :collapse="sidebarFold"
                :collapseTransition="false"
                class="site-sidebar__menu"
            >
                <el-menu-item index="home" aria-label="首页" @click="$router.push({name: 'home'})">
                    <icon-svg name="shouye" class="site-sidebar__menu-icon"></icon-svg>
                    <template #title><span>首页</span></template>
                </el-menu-item>
                <sub-menu
                    v-for="menu in menuList"
                    :key="menu.menuId"
                    :menu="menu"
                    :dynamicMenuRoutes="dynamicMenuRoutes"
                >
                </sub-menu>
            </el-menu>
        </div>
    </aside>
</template>

<script>
import SubMenu from './main-sidebar-sub-menu';
import {isURL} from '@/utils/validate';

export default {
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {
            dynamicMenuRoutes: [],
        };
    },
    components: {
        SubMenu,
    },
    computed: {
        sidebarLayoutSkin: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.common.sidebarLayoutSkin;
            },
        },
        sidebarFold: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.common.sidebarFold;
            },
        },
        menuList: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.common.menuList;
            },
            /** 将新值同步到当前控件或页面的绑定状态. */
            set(val) {
                this.$store.commit('common/updateMenuList', val);
            },
        },
        menuActiveName: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.common.menuActiveName;
            },
            /** 将新值同步到当前控件或页面的绑定状态. */
            set(val) {
                this.$store.commit('common/updateMenuActiveName', val);
            },
        },
        mainTabs: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.common.mainTabs;
            },
            /** 将新值同步到当前控件或页面的绑定状态. */
            set(val) {
                this.$store.commit('common/updateMainTabs', val);
            },
        },
        mainTabsActiveName: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.common.mainTabsActiveName;
            },
            /** 将新值同步到当前控件或页面的绑定状态. */
            set(val) {
                this.$store.commit('common/updateMainTabsActiveName', val);
            },
        },
    },
    watch: {
        $route: 'routeHandle',
    },
    /** 创建组件时准备所需的依赖数据. */
    created() {
        this.menuList = JSON.parse(sessionStorage.getItem('menuList') || '[]');
        this.dynamicMenuRoutes = JSON.parse(sessionStorage.getItem('dynamicMenuRoutes') || '[]');
        this.routeHandle(this.$route);
    },
    methods: {
        // 路由操作
        routeHandle(route) {
            if (route.meta.isTab) {
                // tab选中, 不存在先添加
                let tab = this.mainTabs.filter((item) => item.name === route.name)[0];
                if (!tab) {
                    if (route.meta.isDynamic) {
                        route = this.dynamicMenuRoutes.filter(
                            (item) => item.name === route.name
                        )[0];
                        if (!route) {
                            return console.error('未能找到可用标签页!');
                        }
                    }
                    tab = {
                        menuId: route.meta.menuId || route.name,
                        name: route.name,
                        title: route.meta.title,
                        type: isURL(route.meta.iframeUrl) ? 'iframe' : 'module',
                        iframeUrl: route.meta.iframeUrl || '',
                        params: route.params,
                        query: route.query,
                    };
                    this.mainTabs = this.mainTabs.concat(tab);
                }
                this.menuActiveName = tab.menuId + '';
                this.mainTabsActiveName = tab.name;
            }
        },
    },
};
</script>
