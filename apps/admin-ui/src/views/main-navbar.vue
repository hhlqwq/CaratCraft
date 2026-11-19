<template>
    <nav class="site-navbar" :class="'site-navbar--' + navbarLayoutType">
        <div class="site-navbar__header">
            <h1 class="site-navbar__brand" @click="$router.push({name: 'home'})">
                <a class="site-navbar__brand-lg" href="javascript:;">
                    <span class="brand-en">CARAT CRAFT</span>
                    <span class="brand-cn">克拉匠造 · 商家工作台</span>
                </a>
                <a class="site-navbar__brand-mini" href="javascript:;">克拉</a>
            </h1>
        </div>
        <div class="site-navbar__body clearfix">
            <el-menu class="site-navbar__menu" mode="horizontal" :ellipsis="false">
                <el-menu-item
                    class="site-navbar__switch"
                    index="0"
                    aria-label="展开或折叠导航"
                    @click="sidebarFold = !sidebarFold"
                >
                    <!-- 折叠入口使用三横线图形, 避免旧几何图标看起来像文字. -->
                    <svg
                        viewBox="0 0 24 24"
                        width="18"
                        height="18"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="1.8"
                        stroke-linecap="round"
                        aria-hidden="true"
                    >
                        <path d="M4 6h16M4 12h16M4 18h16" />
                    </svg>
                </el-menu-item>
            </el-menu>
            <el-menu
                class="site-navbar__menu site-navbar__menu--right"
                mode="horizontal"
                :ellipsis="false"
            >
                <el-menu-item index="1" aria-label="布局设置" @click="showTheme()">
                    <el-badge is-dot value="new">
                        <icon-svg name="xitongpeizhi" class="el-icon-setting"></icon-svg>
                    </el-badge>
                </el-menu-item>
                <el-menu-item class="site-navbar__avatar" index="2">
                    <el-dropdown :show-timeout="0" placement="bottom">
                        <span class="el-dropdown-link">
                            <icon-svg name="admin" class="el-icon-setting"></icon-svg>{{ userName }}
                        </span>
                        <template #dropdown
                            ><el-dropdown-menu>
                                <el-dropdown-item @click="updatePasswordHandle()"
                                    >修改密码</el-dropdown-item
                                >
                                <el-dropdown-item @click="logoutHandle()">退出</el-dropdown-item>
                            </el-dropdown-menu></template
                        >
                    </el-dropdown>
                </el-menu-item>
            </el-menu>
        </div>
        <!-- 弹窗, 修改密码 -->
        <update-password v-if="updatePassowrdVisible" ref="updatePassowrd"></update-password>
        <theme v-if="themeVisible" ref="theme"></theme>
    </nav>
</template>

<script>
import Theme from './common/theme';
import UpdatePassword from './main-navbar-update-password';
import {clearLoginInfo} from '@/utils';

export default {
    /** 初始化当前组件的响应式界面和表单状态. */
    data() {
        return {
            updatePassowrdVisible: false,
            themeVisible: false,
        };
    },
    components: {
        UpdatePassword,
        Theme,
    },
    computed: {
        navbarLayoutType: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.common.navbarLayoutType;
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
        userName: {
            /** 读取当前控件或页面的响应式绑定值. */
            get() {
                return this.$store.state.user.name;
            },
        },
    },
    /** 从登录页发起改密时打开已认证的现有对话框. */
    mounted() {
        if (this.$route.query.changePassword === '1') {
            this.updatePasswordHandle();
        }
    },
    methods: {
        /** 打开主题设置面板并初始化其当前选项. */
        showTheme() {
            this.themeVisible = true;
            this.$nextTick(() => {
                this.$refs.theme.init();
            });
        },
        // 修改密码
        updatePasswordHandle() {
            this.updatePassowrdVisible = true;
            this.$nextTick(() => {
                this.$refs.updatePassowrd.init();
            });
        },
        // 退出
        logoutHandle() {
            this.$confirm(`确定进行[退出]操作?`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            })
                .then(() => {
                    this.$http({
                        url: '/sys/logout',
                        method: 'post',
                    }).then(({data}) => {
                        if (data && data.code === 0) {
                            clearLoginInfo();
                            this.$router.push({path: '/login'});
                            location.reload();
                        }
                    });
                })
                .catch(() => {});
        },
    },
};
</script>

<style lang="scss" scoped>
.site-navbar__brand-lg {
    align-items: flex-start;
    display: inline-flex;
    flex-direction: column;
    justify-content: center;
    line-height: 1.15;
    text-align: left;
}

.brand-en {
    color: #c9a86b;
    font-size: 10px;
    font-weight: 600;
    letter-spacing: 2.5px;
}

.brand-cn {
    color: #fff;
    font-size: 15px;
    font-weight: 500;
    margin-top: 4px;
}
</style>
