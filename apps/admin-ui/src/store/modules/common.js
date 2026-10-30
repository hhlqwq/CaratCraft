import router from '@/router';

export default {
    namespaced: true,
    state: {
        // 页面文档可视高度(随窗口改变大小)
        documentClientHeight: 0,
        // 导航条, 布局风格, defalut(默认) / inverse(反向)
        navbarLayoutType: 'default',
        // 侧边栏, 布局皮肤, light(浅色) / dark(黑色)
        sidebarLayoutSkin: 'dark',
        // 侧边栏, 折叠状态
        sidebarFold: false,
        // 侧边栏, 菜单
        menuList: [],
        menuActiveName: '',
        // 内容, 是否需要刷新
        contentIsNeedRefresh: false,
        // 主入口标签页
        mainTabs: [],
        mainTabsActiveName: '',
    },
    mutations: {
        /** 更新共享布局记录的可视区域高度. */
        updateDocumentClientHeight(state, height) {
            state.documentClientHeight = height;
        },
        /** 更新导航栏的布局类型. */
        updateNavbarLayoutType(state, type) {
            state.navbarLayoutType = type;
        },
        /** 更新侧栏使用的配色主题. */
        updateSidebarLayoutSkin(state, skin) {
            state.sidebarLayoutSkin = skin;
        },
        /** 更新侧栏展开或折叠状态. */
        updateSidebarFold(state, fold) {
            state.sidebarFold = fold;
        },
        /** 替换当前账号可见的导航菜单列表. */
        updateMenuList(state, list) {
            state.menuList = list;
        },
        /** 更新当前激活的菜单名称. */
        updateMenuActiveName(state, name) {
            state.menuActiveName = name;
        },
        /** 设置主内容区是否需要重新加载. */
        updateContentIsNeedRefresh(state, status) {
            state.contentIsNeedRefresh = status;
        },
        /** 同步主界面已打开的标签页列表. */
        updateMainTabs(state, tabs) {
            state.mainTabs = tabs;
        },
        /** 更新主界面当前激活的标签页名称. */
        updateMainTabsActiveName(state, name) {
            state.mainTabsActiveName = name;
        },
        /** 删除指定标签页并调整当前激活页. */
        removeTab(state, tabName) {
            state.mainTabs = state.mainTabs.filter((item) => item.name !== tabName);
            if (state.mainTabs.length >= 1) {
                // 当前选中tab被删除
                if (tabName === state.mainTabsActiveName) {
                    const tab = state.mainTabs[state.mainTabs.length - 1];
                    router.push({name: tab.name, query: tab.query, params: tab.params}, () => {
                        state.mainTabsActiveName = tab.name;
                    });
                }
            } else {
                state.menuActiveName = '';
                router.push({name: 'home'});
            }
        },
        /** 关闭当前激活的主界面标签页. */
        closeCurrentTab(state) {
            this.commit('common/removeTab', state.mainTabsActiveName);
        },
    },
};
