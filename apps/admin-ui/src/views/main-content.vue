<template>
    <main class="studio-content">
        <el-breadcrumb separator="/" v-if="$route.meta.group || $route.meta.parent">
            <el-breadcrumb-item v-if="$route.meta.group">{{
                $route.meta.group
            }}</el-breadcrumb-item>
            <el-breadcrumb-item
                v-if="$route.meta.parent"
                :to="{name: $route.meta.parent, query: $route.query}"
            >
                {{ $route.meta.parentTitle }}
            </el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
        </el-breadcrumb>
        <iframe
            v-if="$route.meta.iframeUrl"
            :src="$route.meta.iframeUrl"
            :title="$route.meta.title"
        />
        <el-card v-else shadow="never">
            <router-view v-slot="{Component}">
                <keep-alive :max="1">
                    <component
                        :is="Component"
                        :key="$route.name === 'home' ? 'home' : $route.path"
                    />
                </keep-alive>
            </router-view>
        </el-card>
        <footer class="studio-footer">CaratCraft</footer>
    </main>
</template>

<script>
import {ElBreadcrumb, ElBreadcrumbItem} from 'element-plus';

export default {components: {ElBreadcrumb, ElBreadcrumbItem}};
</script>

<style scoped>
.studio-content {
    padding: 16px 24px 24px;
}
.studio-content .el-breadcrumb {
    margin-bottom: 18px;
}
.studio-content iframe {
    width: 100%;
    min-height: calc(100vh - 140px);
    border: 0;
}
.studio-footer {
    padding-top: 18px;
    color: #69746f;
    text-align: center;
}
</style>
