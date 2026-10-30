<template>
    <main class="site-content" :class="{'site-content--tabs': $route.meta.isTab}">
        <!-- 主入口标签页 s -->
        <el-tabs
            v-if="$route.meta.isTab"
            v-model="mainTabsActiveName"
            :closable="true"
            @tab-click="selectedTabHandle"
            @tab-remove="removeTabHandle"
        >
            <el-dropdown class="site-tabs__tools" :show-timeout="0">
                <i class="el-icon-arrow-down el-icon--right"></i>
                <template #dropdown
                    ><el-dropdown-menu>
                        <el-dropdown-item @click="tabsCloseCurrentHandle"
                            >关闭当前标签页</el-dropdown-item
                        >
                        <el-dropdown-item @click="tabsCloseOtherHandle"
                            >关闭其它标签页</el-dropdown-item
                        >
                        <el-dropdown-item @click="tabsCloseAllHandle"
                            >关闭全部标签页</el-dropdown-item
                        >
                        <el-dropdown-item @click="tabsRefreshCurrentHandle"
                            >刷新当前标签页</el-dropdown-item
                        >
                    </el-dropdown-menu></template
                >
            </el-dropdown>
            <el-tab-pane
                v-for="item in mainTabs"
                :key="item.name"
                :label="item.title"
                :name="item.name"
            >
                <el-card :body-style="siteContentViewHeight">
                    <iframe
                        v-if="item.type === 'iframe'"
                        :src="item.iframeUrl"
                        width="100%"
                        height="100%"
                        frameborder="0"
                        scrolling="yes"
                    >
                    </iframe>
                    <router-view v-else v-slot="{Component}"
                        ><keep-alive
                            ><component
                                :is="Component"
                                v-if="item.name === mainTabsActiveName"
                                :key="item.name" /></keep-alive
                    ></router-view>
                </el-card>
            </el-tab-pane>
        </el-tabs>
        <!-- 主入口标签页 e -->
        <el-card v-else :body-style="siteContentViewHeight">
            <router-view v-slot="{Component}"
                ><keep-alive><component :is="Component" /></keep-alive
            ></router-view>
        </el-card>
        <div style="text-align: center">
            <el-link icon="el-icon-link" type="info" target="_blank" href="mailto:845367567@qq.com"
                >克拉匠造 · 商家工作台</el-link
            >
        </div>
    </main>
</template>

<script>
import {isURL} from '@/utils/validate';

export default {
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {};
    },
    computed: {
        documentClientHeight: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.common.documentClientHeight;
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
        /** 按窗口、导航和标签栏高度计算主内容区可用高度. */
        siteContentViewHeight() {
            let height = this.documentClientHeight - 50 - 30 - 2;
            if (this.$route.meta.isTab) {
                height -= 70;
                return isURL(this.$route.meta.iframeUrl)
                    ? {height: height + 'px'}
                    : {minHeight: height + 'px'};
            }
            return {minHeight: height + 'px'};
        },
    },
    methods: {
        // tabs, 选中tab
        selectedTabHandle(tab) {
            tab = this.mainTabs.filter((item) => item.name === (tab.paneName || tab.props.name));
            if (tab.length >= 1) {
                this.$router.push({name: tab[0].name, query: tab[0].query, params: tab[0].params});
            }
        },
        // tabs, 删除tab
        removeTabHandle(tabName) {
            this.$store.commit('common/removeTab', tabName);
        },
        // tabs, 关闭当前
        tabsCloseCurrentHandle() {
            this.removeTabHandle(this.mainTabsActiveName);
        },
        // tabs, 关闭其它
        tabsCloseOtherHandle() {
            this.mainTabs = this.mainTabs.filter((item) => item.name === this.mainTabsActiveName);
        },
        // tabs, 关闭全部
        tabsCloseAllHandle() {
            this.mainTabs = [];
            this.menuActiveName = '';
            this.$router.push({name: 'home'});
        },
        // tabs, 刷新当前
        tabsRefreshCurrentHandle() {
            const tab = this.$route;
            this.removeTabHandle(tab.paneName || tab.props.name);
            this.$nextTick(() => {
                this.$router.push({name: tab.name, query: tab.query, params: tab.params});
            });
        },
    },
};
</script>
