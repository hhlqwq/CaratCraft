import {createApp} from 'vue';
import App from '@/App';
// api: https://github.com/vuejs/vue-router
import router from '@/router';
// api: https://github.com/vuejs/vuex
import store from '@/store';
// api: https://github.com/alfhen/vue-cookie
import cookie from '@/utils/cookie';
// api: https://github.com/ElemeFE/element
import {ElMessageBox} from 'element-plus';
import {installElementComponents} from '@/utils/element-components';
import 'element-plus/dist/index.css';
// api: http://www.iconfont.cn/
import icons from '@/icons';
import IconSvg from '@/components/icon-svg';
import '@/element-ui-theme';
import '@/assets/scss/index.scss';
// api: https://github.com/axios/axios
import httpRequest from '@/utils/httpRequest';
import clipboard from '@/utils/clipboard';
import {watchBuildUpdates} from '@/utils/build-updates';

import {
    isAuth,
    treeDataTranslate,
    transOrg,
    transUser,
    transDict,
    openImg,
    transDate,
    subtractDate,
} from '@/utils';
import cloneDeep from 'lodash/cloneDeep';
import ElDict from './components/el-dict';
import ElImg from './components/el-img';
import RichTextEditor from './components/rich-text-editor';

// 业务组件和Element Plus统一使用原生Vue3运行时.
const app = createApp(App);
app.use(router);
app.use(store);
installElementComponents(app);
watchBuildUpdates(ElMessageBox);
app.directive('clipboard', clipboard);
app.component('icon-svg', IconSvg);
icons.install();
app.component('el-dict', ElDict);
app.component('el-img', ElImg);
app.component('rich-text-editor', RichTextEditor);
app.config.globalProperties.$cookie = cookie;

// 挂载全局

// ajax请求方法
app.config.globalProperties.$http = httpRequest;
// 权限方法
app.config.globalProperties.isAuth = isAuth;
// 树形数据转换
app.config.globalProperties.treeDataTranslate = treeDataTranslate;
// 机构翻译
app.config.globalProperties.transOrg = transOrg;
// 用户翻译
app.config.globalProperties.transUser = transUser;
// 数据字典翻译
app.config.globalProperties.transDict = transDict;
// 预览图片
app.config.globalProperties.openImg = openImg;
// 转时间
app.config.globalProperties.transDate = transDate;
app.config.globalProperties.subtractDate = subtractDate;

// 保存整站vuex本地储存初始状态
window.SITE_CONFIG.storeState = cloneDeep(store.state);

app.mount('#app');
