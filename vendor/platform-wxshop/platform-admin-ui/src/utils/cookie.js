/** 管理端同源会话Cookie适配, 保留既有服务端token协议. */
export default {
  /** 按完整键名读取, 不将Cookie写入日志. */
  get(name) {
    const prefix = encodeURIComponent(name) + '=';
    const part = document.cookie.split(';').map(value => value.trim()).find(value => value.startsWith(prefix));
    return part ? decodeURIComponent(part.slice(prefix.length)) : null;
  },
  /** 写入同源Cookie, HTTPS时同时限定Secure. */
  set(name, value, days = 1) {
    document.cookie = encodeURIComponent(name) + '=' + encodeURIComponent(value) + ';path=/;SameSite=Lax;max-age=' + Math.max(0, Number(days)) * 86400 + (location.protocol === 'https:' ? ';Secure' : '');
  },
  /** 以相同作用域删除登录凭证. */
  delete(name) {
    this.set(name, '', 0);
  },
};
