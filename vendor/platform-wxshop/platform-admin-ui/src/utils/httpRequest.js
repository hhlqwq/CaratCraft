import cookie from '@/utils/cookie';
import axios from 'axios';
import router from '@/router';
import merge from 'lodash/merge';
import {clearLoginInfo} from '@/utils';
import {ElMessage as Message, ElLoading as Loading} from 'element-plus';

// 超时时间
axios.defaults.timeout = 30000;
// 跨域请求，允许保存cookie
axios.defaults.withCredentials = true;
axios.defaults.headers = {'Content-Type': 'application/json; charset=utf-8'};
// 非生产环境 && 开启代理, 接口前缀统一使用[/platform]前缀做代理拦截!
const BASE_URL =
  process.env.NODE_ENV !== 'production' && process.env.OPEN_PROXY
    ? '/platform'
    : window.SITE_CONFIG.baseUrl;
// 对面暴露的基础请求路径
axios.BASE_URL = BASE_URL;
let authExpiredNotified = false;
const loadingLayers = new Map();
let loadingSequence = 0;

/**
 * 请求拦截
 */
axios.interceptors.request.use(
  (config) => {
    config.requestOptions = {
      showError: config.showError !== false,
    };
    if (config.loading !== false) {
      config.loadingId = ++loadingSequence;
      loadingLayers.set(
        config.loadingId,
        Loading.service({
          text: 'Loading...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)',
        })
      );
    }
    // 记录本次请求实际使用的令牌，避免旧请求的 401 清除新登录令牌 / Records the request token to isolate stale 401 responses.
    config.authToken = cookie.get('token') || '';
    config.headers['token'] = config.authToken;
    // 请求地址处理
    config.url = BASE_URL + config.url;
    const type = config.method;
    const defaults = {};
    if (type === 'post') {
      // post请求参数处理
      // json: 'application/json; charset=utf-8'
      // form: 'application/x-www-form-urlencoded; charset=utf-8'
      if (config.headers['Content-Type'] !== 'multipart/form-data') {
        config.data = JSON.stringify(config.data);
      }
    } else if (type === 'get') {
      // get请求参数处理
      config.params = merge(defaults, config.params);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * 响应拦截
 */
axios.interceptors.response.use(
  (response) => {
    closeLoading(response.config);
    if (response.data && response.data.code === 0 && cookie.get('token')) {
      authExpiredNotified = false;
    }
    // 401, token失效
    if (response.data && response.data.code === 401) {
      handleUnauthorized(response.config, response.data.msg);
      return response;
    }
    if (!(response.data && response.data.code === 0)) {
      if (response.config.requestOptions && response.config.requestOptions.showError) {
        let msg = (response.data && response.data.msg) || '操作失败';
        // 纯文本展示并截断超长内容, 防止整段堆栈刷屏 /
        // Render as plain text and truncate long payloads.
        if (msg.length > 200) {
          msg = msg.substring(0, 200) + '…';
        }
        Message({
          message: msg,
          type: 'error',
          showClose: true,
          duration: 5000,
        });
      }
    }
    return response;
  },
  (error) => {
    const config = error.config || {};
    closeLoading(config);
    if (error.response && error.response.status === 401) {
      const responseData = error.response.data || {};
      handleUnauthorized(config, responseData.msg);
      return Promise.reject(error);
    }
    const status = error.response && error.response.status;
    const payload = error.response && error.response.data;
    const businessFailure =
      status >= 400 && status < 500 && payload && typeof payload.code === 'number';
    if (!config.requestOptions || config.requestOptions.showError) {
      const message =
        payload && typeof payload.msg === 'string'
          ? payload.msg.slice(0, 200)
          : status
            ? `请求失败（HTTP ${status}），请稍后重试`
            : '无法连接后台服务，请检查网络配置';
      Message({message, type: 'error', showClose: true, duration: 3000});
    }
    // 旧页面仍按code处理可恢复的业务错误, 真实网络/服务故障保持拒绝.
    if (businessFailure) {
      return error.response;
    }
    return Promise.reject(error);
  }
);

/**
 * 关闭当前请求的加载层 / Closes the loading overlay for the current request.
 * @param {Object} config Axios 请求配置 / Axios request configuration.
 */
function closeLoading(config) {
  const instance = config && loadingLayers.get(config.loadingId);
  if (instance) {
    instance.close();
    loadingLayers.delete(config.loadingId);
  }
}

/**
 * 处理登录失效，并忽略旧令牌请求的迟到响应 / Handles authentication expiry while ignoring stale-token responses.
 * @param {Object} config Axios 请求配置 / Axios request configuration.
 * @param {string} reason 服务端返回的失效原因 / Expiration reason returned by the server.
 */
function handleUnauthorized(config, reason) {
  const requestToken = (config && config.authToken) || '';
  const currentToken = cookie.get('token') || '';
  if (requestToken !== currentToken) {
    return;
  }

  clearLoginInfo();
  if ((router.currentRoute.value || router.currentRoute).name === 'login') {
    return;
  }

  if (!authExpiredNotified) {
    authExpiredNotified = true;
    Message.closeAll();
    const failureReason = String(reason || '服务端拒绝了当前令牌，请重新登录').substring(0, 120);
    Message({
      message: `登录状态已失效：${failureReason}`,
      type: 'warning',
      showClose: true,
      duration: 5000,
    });
  }
  router.replace({name: 'login'}).catch(() => {});
}

export default axios;
