import {createStore} from 'vuex';
import cloneDeep from 'lodash/cloneDeep';
import common from './modules/common';
import user from './modules/user';
import message from './modules/message';
import wxUserTags from './modules/wxUserTags';

export default createStore({
  modules: {
    common,
    user,
    message,
    wxUserTags,
  },
  mutations: {
    // 重置vuex本地储存状态
    resetStore(state) {
      Object.keys(state).forEach((key) => {
        state[key] = cloneDeep(window.SITE_CONFIG.storeState[key]);
      });
    },
  },
  strict: false,
});
