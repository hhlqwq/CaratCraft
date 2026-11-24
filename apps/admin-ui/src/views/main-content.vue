<template>
    <main class="studio-content">
        <el-breadcrumb separator="/" v-if="$route.meta.parent">
            <el-breadcrumb-item v-if="$route.meta.group && $route.meta.group !== $route.meta.parentTitle">{{
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
        <h1 v-else-if="$route.meta.title && $route.name !== 'home'" class="studio-page-title">
            {{ $route.meta.title }}
        </h1>
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
    </main>
</template>

<script>
import {ElBreadcrumb, ElBreadcrumbItem} from 'element-plus';

export default {components: {ElBreadcrumb, ElBreadcrumbItem}};
</script>

<style scoped>
.studio-content {
    min-width: 0;
    padding: 20px 24px 24px;
}
.studio-content .el-breadcrumb {
    margin-bottom: 18px;
}
.studio-content iframe {
    width: 100%;
    min-height: calc(100vh - 140px);
    border: 0;
}
.studio-page-title {
    margin: 0 0 14px;
    color: var(--studio-text);
    font-size: 22px;
    font-weight: 650;
}
</style>
