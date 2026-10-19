const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
  data: {
    addressList: [],
  },

  /** 页面再次展示时刷新结算收货地址状态. */
  onShow: function () {
    this.getAddressList();
  },
  /** 读取本人保存的收货地址列表. */
  getAddressList() {
    const that = this;
    util.request(api.AddressList).then(function (res) {
      if (res.code === 0) {
        that.setData({
          addressList: res.data,
        });
      }
    });
  },
  /** 携带地址标识进入新增或编辑页面. */
  addressAddOrUpdate(event) {
    wx.navigateTo({
      url: '/pages/shopping/addressAdd/addressAdd?id=' + event.currentTarget.dataset.addressId,
    });
  },
  /** 记录当前选择的收货地址并返回结算流程. */
  selectAddress(event) {
    try {
      wx.setStorageSync('addressId', event.currentTarget.dataset.addressId);
    } catch (e) {
      util.showErrorToast('无法保存收货地址, 请重试');
      return;
    }

    //选择该收货地址
    wx.navigateBack({
      url: '/pages/shopping/checkout/checkout',
    });
  },
});
