<template>
    <div
        class="site-wrapper"
        :class="{'site-sidebar--fold': sidebarFold}"
        v-loading.fullscreen.lock="loading"
        element-loading-text="拼命加载中"
    >
        <template v-if="!loading">
            <main-navbar />
            <main-sidebar />
            <div class="site-content__wrapper">
                <main-content v-if="!$store.state.common.contentIsNeedRefresh" />
            </div>
        </template>
    </div>
</template>

<script>
import MainNavbar from './main-navbar';
import MainSidebar from './main-sidebar';
import MainContent from './main-content';

export default {
    /** 向后代组件提供主界面的刷新能力. */
    provide() {
        return {
            // 刷新
            refresh() {
                this.$store.commit('common/updateContentIsNeedRefresh', true);
                this.$nextTick(() => {
                    this.$store.commit('common/updateContentIsNeedRefresh', false);
                });
            },
        };
    },
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {
            loading: true,
        };
    },
    components: {
        MainNavbar,
        MainSidebar,
        MainContent,
    },
    computed: {
        documentClientHeight: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.common.documentClientHeight;
            },
            /** 将新值同步到当前控件或页面的绑定状态. */
            set(val) {
                this.$store.commit('common/updateDocumentClientHeight', val);
            },
        },
        sidebarFold: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.common.sidebarFold;
            },
            /** 将新值同步到当前控件或页面的绑定状态. */
            set(val) {
                this.$store.commit('common/updateSidebarFold', val);
            },
        },
        userId: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.user.id;
            },
            /** 将新值同步到当前控件或页面的绑定状态. */
            set(val) {
                this.$store.commit('user/updateId', val);
            },
        },
        userName: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.user.name;
            },
            /** 将新值同步到当前控件或页面的绑定状态. */
            set(val) {
                this.$store.commit('user/updateName', val);
            },
        },
    },
    /** 创建组件时准备所需的依赖数据. */
    created() {
        this.getUserInfo();
    },
    /** 挂载组件后初始化控件并读取所需数据. */
    mounted() {
        this.resetDocumentClientHeight();
    },
    methods: {
        // 重置窗口可视高度
        resetDocumentClientHeight() {
            this.sidebarFold = document.documentElement['clientWidth'] <= 1200 || false;
            this.documentClientHeight = document.documentElement['clientHeight'];
            window.onresize = () => {
                this.sidebarFold = document.documentElement['clientWidth'] <= 1200 || false;
                this.documentClientHeight = document.documentElement['clientHeight'];
            };
        },
        // 获取当前管理员信息
        getUserInfo() {
            this.$http({
                url: '/sys/user/info',
                method: 'get',
            }).then(({data}) => {
                if (data && data.code === 0) {
                    this.loading = false;
                    this.userId = data.data.userId;
                    this.userName = data.data.userName;
                }
            });
        },
    },
};
</script>
