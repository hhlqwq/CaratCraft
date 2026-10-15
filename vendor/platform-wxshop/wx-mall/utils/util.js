var api = require('../config/api.js')

function formatTime (date) {
  var year = date.getFullYear()
  var month = date.getMonth() + 1
  var day = date.getDate()

  var hour = date.getHours()
  var minute = date.getMinutes()
  var second = date.getSeconds()

  return [year, month, day].map(formatNumber).join('-') + ' ' + [hour, minute, second].map(formatNumber).join(':')
}

function formatNumber (n) {
  n = n.toString()
  return n[1] ? n : '0' + n
}

let pendingRequests = 0;
let authRedirecting = false;

/** 失效会话统一清理, 忽略旧令牌的迟到响应并避免重复登录导航. */
function expireSession(token) {
  if ((wx.getStorageSync('token') || '') !== token) return;
  ['token', 'userInfo', 'addressId', 'designSnapshotId', 'designQuote'].forEach(function(key) {
    wx.removeStorageSync(key);
  });
  const app = typeof getApp === 'function' ? getApp() : null;
  if (app && app.globalData) {
    app.globalData.token = '';
    app.globalData.userInfo = {};
    app.globalData.userCoupon = 'NO_USE_COUPON';
    app.globalData.courseCouponCode = {};
  }
  if (!authRedirecting) {
    authRedirecting = true;
    const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : [];
    if (!pages.length || pages[pages.length - 1].route !== 'pages/auth/btnAuth/btnAuth') {
      wx.navigateTo({url: '/pages/auth/btnAuth/btnAuth', fail: function() { authRedirecting = false; }});
    }
  }
}

/** 统一请求、并发加载计数及失败收尾, 所有分支均返回可完成的Promise. */
function request(url, data = {}, method = 'POST', header = 'application/x-www-form-urlencoded') {
  const token = wx.getStorageSync('token') || '';
  if (token) authRedirecting = false;
  if (pendingRequests++ === 0) wx.showLoading({title: '加载中...'});
  let completed = false;
  /** 每个请求仅释放一次加载计数. */
  function finish() {
    if (completed) return;
    completed = true;
    pendingRequests = Math.max(0, pendingRequests - 1);
    if (pendingRequests === 0) wx.hideLoading();
  }
  return new Promise(function(resolve, reject) {
    try {
      wx.request({
        url: url,
        data: data,
        method: method,
        header: {'Content-Type': header, token: token},
        success: function(res) {
          if (res.statusCode === 401 || (res.data && res.data.code === 401)) {
            expireSession(token);
            reject(new Error('登录已失效, 请重新登录'));
          } else if (res.data && typeof res.data.code !== 'undefined') {
            resolve(res.data);
          } else if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(res.data);
          } else {
            reject(new Error('请求失败, HTTP ' + res.statusCode));
          }
        },
        fail: function(error) { reject(new Error(error.errMsg || '网络连接失败')); },
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
function checkSession () {
  return new Promise(function (resolve, reject) {
    wx.checkSession({
      success: function () {
        resolve(true)
      },
      fail: function () {
        reject(false)
      }
    })
  })
}

/**
 * 调用微信登录
 */
function login () {
  return new Promise(function (resolve, reject) {
    wx.login({
      success: function (res) {
        if (res.code) {
          resolve(res)
        } else {
          reject(res)
        }
      },
      fail: function (err) {
        reject(err)
      }
    })
  })
}

function redirect (url) {

  //判断页面是否需要登录
  if (false) {
    wx.redirectTo({
      url: '/pages/auth/login/login'
    })
    return false
  } else {
    wx.redirectTo({
      url: url
    })
  }
}

function showErrorToast (msg) {
  wx.showToast({
    title: msg,
    image: '/static/images/icon_error.png'
  })
}

function showSuccessToast (msg) {
  wx.showToast({
    title: msg,
  })
}

module.exports = {
  formatTime,
  request,
  redirect,
  showErrorToast,
  showSuccessToast,
  checkSession,
  login,
}
