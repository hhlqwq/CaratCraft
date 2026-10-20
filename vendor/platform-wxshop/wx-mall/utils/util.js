const api = require('../config/api.js');

/** 将日期格式化为本地年月日和时分秒文本. */
function formatTime(date) {
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();

  const hour = date.getHours();
  const minute = date.getMinutes();
  const second = date.getSeconds();

  return (
    [year, month, day].map(formatNumber).join('-') +
    ' ' +
    [hour, minute, second].map(formatNumber).join(':')
  );
}

/** 将不足两位的日期数字补齐前导零. */
function formatNumber(n) {
  n = n.toString();
  return n[1] ? n : '0' + n;
}

let pendingRequests = 0;
let authRedirecting = false;

/** 将同源上传图片地址转换为当前API来源, 小程序包内静态图片路径保持原样. */
function normalizeAssetUrls(value, key = '') {
  if (
    typeof value === 'string' &&
    /^\/upload\//.test(value) &&
    (key === 'data' || /(?:url|pic|image|src)$/i.test(key)) &&
    api.ApiOrigin
  ) {
    return api.ApiOrigin + value;
  }
  if (Array.isArray(value)) {
    return value.map(function (item) {
      return normalizeAssetUrls(item, key);
    });
  }
  if (value && typeof value === 'object') {
    Object.keys(value).forEach(function (name) {
      if (name !== '__proto__' && name !== 'constructor' && name !== 'prototype') {
        value[name] = normalizeAssetUrls(value[name], name);
      }
    });
  }
  return value;
}

/** 失效会话统一清理, 忽略旧令牌的迟到响应并避免重复登录导航. */
function expireSession(token) {
  if ((wx.getStorageSync('token') || '') !== token) {
    return;
  }
  ['token', 'userInfo', 'addressId', 'designSnapshotId', 'designQuote'].forEach(function (key) {
    wx.removeStorageSync(key);
  });
  const app = typeof getApp === 'function' ? getApp() : null;
  if (app && app.globalData) {
    app.globalData.token = '';
    app.globalData.userInfo = {};
    app.globalData.userCoupon = 'NO_USE_COUPON';
    app.globalData.selectedCoupon = {};
  }
  if (!authRedirecting) {
    authRedirecting = true;
    const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : [];
    if (!pages.length || pages[pages.length - 1].route !== 'pages/auth/btnAuth/btnAuth') {
      wx.navigateTo({
        url: '/pages/auth/btnAuth/btnAuth',
        fail() {
          authRedirecting = false;
        },
      });
    }
  }
}

/** 统一请求、并发加载计数及失败收尾, 所有分支均返回可完成的Promise. */
function request(url, data = {}, method = 'POST', header = 'application/x-www-form-urlencoded') {
  const token = wx.getStorageSync('token') || '';
  if (token) {
    authRedirecting = false;
  }
  if (pendingRequests++ === 0) {
    wx.showLoading({title: '加载中...'});
  }
  let completed = false;
  /** 每个请求仅释放一次加载计数. */
  function finish() {
    if (completed) {
      return;
    }
    completed = true;
    pendingRequests = Math.max(0, pendingRequests - 1);
    if (pendingRequests === 0) {
      wx.hideLoading();
    }
  }
  return new Promise(function (resolve, reject) {
    try {
      wx.request({
        url: url,
        data: data,
        method: method,
        header: {'Content-Type': header, token: token},
        success(res) {
          if (res.statusCode === 401 || (res.data && res.data.code === 401)) {
            expireSession(token);
            reject(new Error('登录已失效, 请重新登录'));
          } else if (res.data && typeof res.data.code !== 'undefined') {
            resolve(normalizeAssetUrls(res.data));
          } else if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(normalizeAssetUrls(res.data));
          } else {
            reject(new Error('请求失败, HTTP ' + res.statusCode));
          }
        },
        fail(error) {
          reject(new Error(error.errMsg || '网络连接失败'));
        },
        complete: finish,
      });
    } catch (error) {
      finish();
      reject(error);
    }
  });
}

/**
 * 检查微信会话是否过期
 */
function checkSession() {
  return new Promise(function (resolve, reject) {
    wx.checkSession({
      success() {
        resolve(true);
      },
      fail() {
        reject(false);
      },
    });
  });
}

/**
 * 调用微信登录
 */
function login() {
  return new Promise(function (resolve, reject) {
    wx.login({
      success(res) {
        if (res.code) {
          resolve(res);
        } else {
          reject(res);
        }
      },
      fail(err) {
        reject(err);
      },
    });
  });
}

/** 跳转到指定页面, 登录校验由目标业务入口负责. */
function redirect(url) {
  wx.redirectTo({url: url});
}

/** 使用统一错误图标展示失败提示. */
function showErrorToast(msg) {
  wx.showToast({
    title: msg,
    image: '/static/images/icon_error.png',
  });
}

/** 使用统一成功样式展示完成提示. */
function showSuccessToast(msg) {
  wx.showToast({
    title: msg,
  });
}

module.exports = {
  normalizeAssetUrls,
  formatTime,
  request,
  redirect,
  showErrorToast,
  showSuccessToast,
  checkSession,
  login,
};
