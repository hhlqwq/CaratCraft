<template>
    <aside class="site-sidebar">
        <div class="site-sidebar__inner">
            <el-menu
                :default-active="$route.meta.menuName || $route.name"
                :collapse="$store.state.common.sidebarFold"
                :collapse-transition="false"
                class="site-sidebar__menu"
                router
            >
                <el-menu-item
                    v-if="!isAuth('platform:merchant:manage')"
                    index="home"
                    :route="{name: 'home'}"
                >
                    <icon-svg name="studio-home" class="site-sidebar__menu-icon" />
                    <template #title>工作台</template>
                </el-menu-item>
                <template v-for="group in groups" :key="group.title">
                    <el-menu-item
                        v-if="group.standalone"
                        :index="group.pages[0].name"
                        :route="{name: group.pages[0].name}"
                    >
                        <icon-svg :name="group.icon" class="site-sidebar__menu-icon" />
                        <template #title>{{ group.title }}</template>
                    </el-menu-item>
                    <el-sub-menu v-else :index="group.title">
                        <template #title>
                            <icon-svg :name="group.icon" class="site-sidebar__menu-icon" />
                            <span>{{ group.title }}</span>
                        </template>
                        <el-menu-item
                            v-for="page in group.pages"
                            :key="page.name"
                            :index="page.name"
                            :route="{name: page.name}"
                        >
                            {{ page.title }}
                        </el-menu-item>
                    </el-sub-menu>
                </template>
            </el-menu>
        </div>
    </aside>
</template>

<script>
import {workspaceGroups} from '@/router/workspace-navigation';

export default {
    /** 仅从当前已认证账号的服务端菜单构建分组. */
    data() {
        return {groups: workspaceGroups(JSON.parse(sessionStorage.getItem('menuList') || '[]'))};
    },
};
</script>
