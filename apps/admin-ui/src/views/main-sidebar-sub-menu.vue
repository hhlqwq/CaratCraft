<template>
    <el-sub-menu
        v-if="menu.list && menu.list.length >= 1"
        :index="menu.menuId + ''"
        :popper-class="'site-sidebar--' + sidebarLayoutSkin + '-popper'"
    >
        <template #title>
            <icon-svg :name="menu.icon || 'wechat'" class="site-sidebar__menu-icon"></icon-svg>
            <span>{{ menu.name }}</span>
        </template>
        <sub-menu
            v-for="item in menu.list"
            :key="item.menuId"
            :menu="item"
            :dynamicMenuRoutes="dynamicMenuRoutes"
        >
        </sub-menu>
    </el-sub-menu>
    <el-menu-item
        v-else
        :index="menu.menuId + ''"
        :aria-label="menu.name"
        @click="gotoRouteHandle(menu)"
    >
        <icon-svg :name="menu.icon || 'wechat'" class="site-sidebar__menu-icon"></icon-svg>
        <span>{{ menu.name }}</span>
    </el-menu-item>
</template>

<script>
import SubMenu from './main-sidebar-sub-menu';

export default {
    name: 'sub-menu',
    props: {
        menu: {
            type: Object,
            required: true,
        },
        dynamicMenuRoutes: {
            type: Array,
            required: true,
        },
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
    },
    methods: {
        // 通过menuId与动态(菜单)路由进行匹配跳转至指定路由
        gotoRouteHandle(menu) {
            const route = this.dynamicMenuRoutes.filter((item) => item.meta.menuId === menu.menuId);
            if (route.length >= 1) {
                this.$router.push({name: route[0].name});
            }
        },
    },
};
</script>
