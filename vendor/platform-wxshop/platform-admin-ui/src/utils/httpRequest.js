import Vue from 'vue'
import axios from 'axios'
import router from '@/router'
import merge from 'lodash/merge'
import {clearLoginInfo} from '@/utils'
import {
  Message,
  Loading
} from 'element-ui'

// 超时时间
axios.defaults.timeout = 30000
// 跨域请求，允许保存cookie
axios.defaults.withCredentials = true
axios.defaults.headers = {'Content-Type': 'application/json; charset=utf-8'}
// 非生产环境 && 开启代理, 接口前缀统一使用[/platform]前缀做代理拦截!
const BASE_URL = process.env.NODE_ENV !== 'production' && process.env.OPEN_PROXY ? '/platform' : window.SITE_CONFIG.baseUrl
// 对面暴露的基础请求路径
axios.BASE_URL = BASE_URL

/**
 * 请求拦截
 */
axios.interceptors.request.use(config => {
  config.requestOptions = {
    showError: config.showError !== false,
    loadingInstance: null
  }
  if (config.loading !== false) {
    config.requestOptions.loadingInstance = Loading.service({
      text: 'Loading...',
      spinner: 'el-icon-loading',
      background: 'rgba(0, 0, 0, 0.7)'
    })
  }
  // 请求头带上token
  config.headers['token'] = Vue.cookie.get('token') || ''
  // 请求地址处理
  config.url = BASE_URL + config.url
  const type = config.method
  const defaults = {}
  if (type === 'post') {
    // post请求参数处理
    // json: 'application/json; charset=utf-8'
    // form: 'application/x-www-form-urlencoded; charset=utf-8'
    if (config.headers['Content-Type'] !== 'multipart/form-data') {
      config.data = JSON.stringify(config.data)
    }
  } else if (type === 'get') {
    // get请求参数处理
    config.params = merge(defaults, config.params)
  }
  return config
}, error => {
  return Promise.reject(error)
})

/**
 * 响应拦截
 */
axios.interceptors.response.use(response => {
  closeLoading(response.config)
  // 401, token失效
  if (response.data && response.data.code === 401) {
    clearLoginInfo()
    router.push({name: 'login'})
    // 跳转登录页后不再弹出错误提示, 避免长文本弹窗 /
    // Skip the error toast once redirected to the login page.
    return response
  }
  if (!(response.data && response.data.code === 0)) {
    if (response.config.requestOptions && response.config.requestOptions.showError) {
      let msg = (response.data && response.data.msg) || '操作失败'
      // 纯文本展示并截断超长内容, 防止整段堆栈刷屏 /
      // Render as plain text and truncate long payloads.
      if (msg.length > 200) {
        msg = msg.substring(0, 200) + '…'
      }
      Message({
        message: msg,
        type: 'error',
        showClose: true,
        duration: 5000
      })
    }
  }
  return response
}, error => {
  const config = error.config || {}
  closeLoading(config)
  if (!config.requestOptions || config.requestOptions.showError) {
    const status = error.response && error.response.status
    const message = status
      ? `请求失败（HTTP ${status}），请稍后重试`
      : '无法连接后台服务，请检查服务是否启动及网络配置'
    Message({message, type: 'error', showClose: true, duration: 3000})
  }
  return Promise.reject(error)
})

/**
 * 关闭当前请求的加载层 / Closes the loading overlay for the current request.
 * @param {Object} config Axios 请求配置 / Axios request configuration.
 */
function closeLoading (config) {
  const options = config && config.requestOptions
  if (options && options.loadingInstance) {
    options.loadingInstance.close()
  }
}

export default axios
