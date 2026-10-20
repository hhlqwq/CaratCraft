const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
  data: {
    addressList: [],
  },

  /** 页面再次展示时刷新收货地址管理状态. */
  onShow() {
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
      url: '/pages/ucenter/addressAdd/addressAdd?id=' + event.currentTarget.dataset.addressId,
    });
  },
  /** 确认后删除本人指定的收货地址并刷新列表. */
  deleteAddress(event) {
    const that = this;
    wx.showModal({
      title: '',
      content: '确定要删除地址？',
      success(res) {
        if (res.confirm) {
          const addressId = event.target.dataset.addressId;
          util
            .request(api.AddressDelete, {id: addressId}, 'POST', 'application/json')
            .then(function (res) {
              if (res.code === 0) {
                that.getAddressList();
              }
            });
        }
      },
    });
    return false;
  },
});
