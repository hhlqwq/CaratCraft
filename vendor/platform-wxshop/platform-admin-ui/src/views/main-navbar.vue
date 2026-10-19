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
          <icon-svg name="zhedie"></icon-svg>
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
                <el-dropdown-item @click="updatePasswordHandle()">修改密码</el-dropdown-item>
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
  compatConfig: {MODE: 2},
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
      get() {
        return this.$store.state.common.navbarLayoutType;
      },
    },
    sidebarFold: {
      get() {
        return this.$store.state.common.sidebarFold;
      },
      set(val) {
        this.$store.commit('common/updateSidebarFold', val);
      },
    },
    mainTabs: {
      get() {
        return this.$store.state.common.mainTabs;
      },
      set(val) {
        this.$store.commit('common/updateMainTabs', val);
      },
    },
    userName: {
      get() {
        return this.$store.state.user.name;
      },
    },
  },
  methods: {
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
