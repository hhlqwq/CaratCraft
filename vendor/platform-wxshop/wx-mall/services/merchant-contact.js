const util = require('../utils/util.js');
const api = require('../config/api.js');

let cached = null;
let expiresAt = 0;
let pending = null;

/** 客服资料最多缓存5分钟, 并发点击共享一个请求, 失败后允许下一次重试. */
function load() {
  if (cached && Date.now() < expiresAt) {
    return Promise.resolve(cached);
  }
  if (pending) {
    return pending;
  }
  pending = util
    .request(api.MerchantContact)
    .then((response) => {
      if (response.code !== 0 || !response.data) {
        throw new Error(response.msg || '客服资料暂不可用');
      }
      cached = response.data;
      expiresAt = Date.now() + 5 * 60 * 1000;
      return cached;
    })
    .finally(() => {
      pending = null;
    });
  return pending;
}

module.exports = {load};
