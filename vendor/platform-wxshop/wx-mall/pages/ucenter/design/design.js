const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

/** 我的设计页面 / My designs page. */
Page({
  data: {
    designList: [],
    loading: false,
    page: 1,
    total: 0,
    payingSnapshotId: 0,
  },

  /** 页面显示时读取当前用户的设计快照 / Loads the current user's design snapshots. */
  onShow() {
    this.setData({page: 1});
    this.loadDesigns();
  },

  /** 加载设计列表并转换为展示字段 / Loads designs and prepares display fields. */
  loadDesigns(nextPage) {
    const that = this;
    const requestedPage = nextPage || that.data.page;
    that.setData({loading: true});
    util
      .request(api.CustomizationList, {page: requestedPage, limit: 20})
      .then(function (res) {
        if (res.code !== 0) {
          wx.showToast({title: res.msg || '设计列表加载失败', icon: 'none'});
          return;
        }
        const designList = (res.data.records || []).map(function (item) {
          const sizing = that.getFrozenSizing(item.priceBreakdownJson);
          return {
            id: item.id,
            goodsId: item.goodsId,
            version: item.version,
            layers: that.getFrozenLayers(item.priceBreakdownJson),
            arrangement: that.getFrozenArrangement(item.priceBreakdownJson),
            sizing: sizing,
            previewUrl: item.previewUrl || '/static/images/logo.png',
            canContinue: item.status === 'DRAFT',
            canPay: item.status === 'DRAFT' && Boolean(sizing && sizing.ready === true),
            canViewOrder: item.status === 'ORDERED' && Boolean(item.orderId),
            orderId: Number(item.orderId || 0),
            statusText: that.getStatusText(item.status),
            createTime: item.createTime || '',
            priceText: that.getPriceText(item.priceBreakdownJson),
          };
        });
        that.setData({
          page: requestedPage,
          designList: requestedPage === 1 ? designList : that.data.designList.concat(designList),
          total: res.data.total,
        });
      })
      .catch(function () {
        wx.showToast({title: '设计列表加载失败', icon: 'none'});
      })
      .finally(function () {
        that.setData({loading: false});
      });
  },

  /** 加载下一页, 不重复请求已加载页. */
  loadMore() {
    if (this.data.loading || this.data.designList.length >= this.data.total) {
      return;
    }
    this.loadDesigns(this.data.page + 1);
  },

  /** 从历史报价读取冻结图层, 不依赖材料当前名称或配置. */
  getFrozenLayers(json) {
    try {
      return (JSON.parse(json || '{}').components || [])
        .filter(function (line) {
          return (
            line.assetUrl && (/^https:\/\//i.test(line.assetUrl) || /^\/(?!\/)/.test(line.assetUrl))
          );
        })
        .map(function (line) {
          return Object.assign({}, line, {
            assetUrl: /^\//.test(line.assetUrl) ? api.ApiOrigin + line.assetUrl : line.assetUrl,
          });
        })
        .sort(function (a, b) {
          return Number(a.zIndex || 0) - Number(b.zIndex || 0);
        });
    } catch (error) {
      return [];
    }
  },

  /** 使用保存时的排列顺序, 后续材料停用或改名不改变列表内容. */
  getFrozenArrangement(json) {
    try {
      return JSON.parse(json || '{}').arrangement || [];
    } catch (error) {
      return [];
    }
  },

  /** 展示保存时的尺寸与工艺校验状态, 不依据当前材料重新推算历史草稿. */
  getFrozenSizing(json) {
    try {
      return JSON.parse(json || '{}').sizing || null;
    } catch (error) {
      return null;
    }
  },

  /** 用户确认后归档本人版本, 下单设计不显示删除操作. */
  archiveDesign(event) {
    const that = this;
    wx.showModal({
      title: '移除草稿',
      content: '从我的设计中移除此草稿, 已下单设计不受影响。',
      success(res) {
        if (!res.confirm) {
          return;
        }
        util
          .request(api.CustomizationArchive, {
            snapshotId: event.currentTarget.dataset.id,
            version: event.currentTarget.dataset.version,
          })
          .then(function (response) {
            if (response.code !== 0) {
              throw new Error(response.msg || '移除失败');
            }
            that.setData({page: 1});
            that.loadDesigns();
          })
          .catch(function (error) {
            wx.showToast({title: error.message || '移除失败', icon: 'none'});
          });
      },
    });
  },

  /** 获取设计状态文案 / Gets design status text. */
  getStatusText(status) {
    return {DRAFT: '可继续编辑', LOCKED: '已锁定', ORDERED: '已下单'}[status] || '已保存';
  },

  /** 从报价 JSON 提取总价 / Extracts total price from quote JSON. */
  getPriceText(priceBreakdownJson) {
    try {
      const breakdown = JSON.parse(priceBreakdownJson || '{}');
      return breakdown.totalPrice === undefined ? '--' : '￥' + breakdown.totalPrice;
    } catch (error) {
      return '--';
    }
  },

  /** 跳转到设计器继续编辑 / Opens the designer for editing. */
  continueDesign(event) {
    const goodsId = Number(event.currentTarget.dataset.goodsId || 0);
    const snapshotId = Number(event.currentTarget.dataset.snapshotId || 0);
    if (!goodsId || !snapshotId) {
      wx.showToast({title: '缺少商品信息', icon: 'none'});
      return;
    }
    wx.setStorageSync('designerGoodsId', goodsId);
    wx.setStorageSync('designerSnapshotId', snapshotId);
    wx.switchTab({url: '/pages/designer/designer'});
  },

  /** 使用已保存设计创建购买上下文并进入结算 / Creates a buy context from a saved design and opens checkout. */
  payDesign(event) {
    const that = this;
    const goodsId = Number(event.currentTarget.dataset.goodsId || 0);
    const snapshotId = Number(event.currentTarget.dataset.snapshotId || 0);
    if (!goodsId || !snapshotId || that.data.payingSnapshotId) {
      return;
    }
    that.setData({payingSnapshotId: snapshotId});
    util
      .request(api.CustomizationConfig, {goodsId: goodsId}, 'GET')
      .then(function (configRes) {
        if (configRes.code !== 0 || !configRes.data || !configRes.data.primaryProductId) {
          throw new Error(configRes.msg || '商品规格未配置');
        }
        return util.request(
          api.BuyAdd,
          {
            goodsId: goodsId,
            productId: configRes.data.primaryProductId,
            number: 1,
            designSnapshotId: snapshotId,
          },
          'POST',
          'application/json'
        );
      })
      .then(function (buyRes) {
        if (buyRes.code !== 0) {
          throw new Error(buyRes.msg || '创建结算信息失败');
        }
        wx.navigateTo({
          url:
            '/pages/shopping/checkout/checkout?isBuy=true&checkoutId=' +
            encodeURIComponent(buyRes.data.checkoutId) +
            '&designSnapshotId=' +
            snapshotId,
        });
      })
      .catch(function (error) {
        wx.showToast({title: error.message || '无法进入结算', icon: 'none'});
      })
      .finally(function () {
        that.setData({payingSnapshotId: 0});
      });
  },

  /** 打开设计已经关联的订单 / Opens the order related to a submitted design. */
  viewOrder(event) {
    const orderId = Number(event.currentTarget.dataset.orderId || 0);
    if (!orderId) {
      wx.showToast({title: '订单信息不存在', icon: 'none'});
      return;
    }
    wx.navigateTo({url: '/pages/ucenter/orderDetail/orderDetail?id=' + orderId});
  },
});
