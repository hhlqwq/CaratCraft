const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
  data: {
    documents: [],
    history: [],
    error: '',
    loading: false,
    saving: false,
    acceptance: null,
    token: '',
  },
  /** 保存可选的已发布历史版本参数, 不允许任意路径跳转. */
  onLoad(options) {
    this.options = options || {};
  },
  /** 每次返回重新读取公开正文和本人记录, 页面代次隔离旧账号响应. */
  onShow() {
    this.load();
  },
  /** 离开页面时清除本人记录并废弃未完成请求. */
  onHide() {
    this.generation = (this.generation || 0) + 1;
    this.setData({history: [], acceptance: null, loading: false, saving: false});
  },
  /** 页面销毁沿用离开时的账号隔离处理. */
  onUnload() {
    this.onHide();
  },
  /** 公开正文按纯文本展示, 历史版本由公开接口再次校验发布状态. */
  async load() {
    const generation = (this.generation = (this.generation || 0) + 1);
    const token = wx.getStorageSync('token') || '';
    this.setData({loading: true, error: '', documents: [], history: [], token, acceptance: null});
    const current = () =>
      generation === this.generation && token === (wx.getStorageSync('token') || '');
    try {
      const {kind, version} = this.options || {};
      const historical = ['TERMS', 'PRIVACY'].includes(kind) && /^[1-9]\d*$/.test(version || '');
      const response = await util.request(
        api.LegalDocuments + (historical ? '/' + kind + '/' + version : '')
      );
      if (response.code !== 0) {
        throw new Error(response.msg || '协议加载失败');
      }
      if (!current()) {
        return;
      }
      this.setData({documents: historical ? [response.data] : response.data});
      if (token) {
        const records = await util.request(api.LegalDocuments + '/history');
        if (records.code !== 0) {
          throw new Error(records.msg || '确认记录加载失败');
        }
        if (current()) {
          this.setData({history: records.data});
        }
      }
    } catch (error) {
      if (current()) {
        this.setData({error: error.message || '加载失败, 请重试'});
      }
    } finally {
      if (current()) {
        this.setData({loading: false});
      }
    }
  },
  /** 确认组件只传递本次主动勾选的两个当前版本. */
  consentChanged(event) {
    this.setData({acceptance: event.detail});
  },
  /** 登录用户确认当前版本, 失败保留正文并支持重新加载. */
  async accept() {
    if (!this.data.acceptance || this.data.saving || !this.data.token) {
      return;
    }
    const generation = this.generation;
    const token = this.data.token;
    this.setData({saving: true, error: ''});
    try {
      const response = await util.request(
        api.LegalDocuments + '/accept',
        this.data.acceptance,
        'POST',
        'application/json'
      );
      if (generation !== this.generation || token !== wx.getStorageSync('token')) {
        return;
      }
      if (response.code !== 0) {
        throw new Error(response.msg || '确认失败');
      }
      wx.showToast({title: '已记录确认', icon: 'success'});
      await this.load();
    } catch (error) {
      if (generation === this.generation) {
        this.setData({error: error.message || '确认失败'});
      }
    } finally {
      if (generation === this.generation) {
        this.setData({saving: false});
      }
    }
  },
});
