const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
  data: {
    cartGoods: [],
    cartTotal: {
      goodsCount: 0,
      goodsAmount: 0.0,
      checkedGoodsCount: 0,
      checkedGoodsAmount: 0.0,
    },
    isEditCart: false,
    checkedAllStatus: true,
    editCartList: [],
  },

  /** 页面再次展示时刷新购物车状态. */
  onShow: function () {
    this.getCartList();
  },

  /** 读取当前购物车商品、选中状态和金额统计. */
  getCartList: function () {
    const that = this;
    util.request(api.CartList).then(function (res) {
      if (res.code === 0) {
        that.setData({
          cartGoods: res.data.cartList,
          cartTotal: res.data.cartTotal,
        });
      }

      that.setData({
        checkedAllStatus: that.isCheckedAll(),
      });
    });
  },
  /** 按当前购物车商品状态判断是否全部选中. */
  isCheckedAll: function () {
    //判断购物车商品已全选
    return this.data.cartGoods.every(function (element, index, array) {
      if (element.checked == true) {
        return true;
      } else {
        return false;
      }
    });
  },
  /** 切换单项勾选状态并同步购物车统计. */
  checkedItem: function (event) {
    const itemIndex = event.target.dataset.itemIndex;
    const that = this;

    if (!this.data.isEditCart) {
      util
        .request(
          api.CartChecked,
          {
            productIds: that.data.cartGoods[itemIndex].productId,
            isChecked: that.data.cartGoods[itemIndex].checked ? 0 : 1,
          },
          'POST',
          'application/json'
        )
        .then(function (res) {
          if (res.code === 0) {
            that.setData({
              cartGoods: res.data.cartList,
              cartTotal: res.data.cartTotal,
            });
          }

          that.setData({
            checkedAllStatus: that.isCheckedAll(),
          });
        });
    } else {
      //编辑状态
      const tmpCartData = this.data.cartGoods.map(function (element, index, array) {
        if (index == itemIndex) {
          element.checked = !element.checked;
        }

        return element;
      });

      that.setData({
        cartGoods: tmpCartData,
        checkedAllStatus: that.isCheckedAll(),
        'cartTotal.checkedGoodsCount': that.getCheckedGoodsCount(),
      });
    }
  },
  /** 统计当前勾选商品的总数量. */
  getCheckedGoodsCount: function () {
    let checkedGoodsCount = 0;
    this.data.cartGoods.forEach(function (v) {
      if (v.checked === true) {
        checkedGoodsCount += v.number;
      }
    });
    return checkedGoodsCount;
  },
  /** 统一切换购物车全部商品的勾选状态. */
  checkedAll: function () {
    const that = this;

    if (!this.data.isEditCart) {
      const productIds = this.data.cartGoods.map(function (v) {
        return v.productId;
      });
      util
        .request(
          api.CartChecked,
          {productIds: productIds.join(','), isChecked: that.isCheckedAll() ? 0 : 1},
          'POST',
          'application/json'
        )
        .then(function (res) {
          if (res.code === 0) {
            that.setData({
              cartGoods: res.data.cartList,
              cartTotal: res.data.cartTotal,
            });
          }

          that.setData({
            checkedAllStatus: that.isCheckedAll(),
          });
        });
    } else {
      //编辑状态
      const checkedAllStatus = that.isCheckedAll();
      const tmpCartData = this.data.cartGoods.map(function (v) {
        v.checked = !checkedAllStatus;
        return v;
      });

      that.setData({
        cartGoods: tmpCartData,
        checkedAllStatus: that.isCheckedAll(),
        'cartTotal.checkedGoodsCount': that.getCheckedGoodsCount(),
      });
    }
  },
  /** 切换购物车编辑状态并重置批量选择. */
  editCart: function () {
    const that = this;
    if (this.data.isEditCart) {
      this.getCartList();
      this.setData({
        isEditCart: !this.data.isEditCart,
      });
    } else {
      //编辑状态
      const tmpCartList = this.data.cartGoods.map(function (v) {
        v.checked = false;
        return v;
      });
      this.setData({
        editCartList: this.data.cartGoods,
        cartGoods: tmpCartList,
        isEditCart: !this.data.isEditCart,
        checkedAllStatus: that.isCheckedAll(),
        'cartTotal.checkedGoodsCount': that.getCheckedGoodsCount(),
      });
    }
  },
  /** 返回商城首页. */
  toIndexPage: function () {
    wx.switchTab({
      url: '/pages/index/index',
    });
  },
  /** 将修改后的购买数量同步到服务端购物车. */
  updateCart: function (productId, goodsId, number, id) {
    const that = this;

    util
      .request(api.CartUpdate, {
        productId: productId,
        goodsId: goodsId,
        number: number,
        id: id,
      })
      .then(function (res) {
        if (res.code === 0) {
          that.setData({
            //cartGoods: res.data.cartList,
            //cartTotal: res.data.cartTotal
          });
        }

        that.setData({
          checkedAllStatus: that.isCheckedAll(),
        });
      });
  },
  /** 减少当前购买数量, 保留最小购买数量限制. */
  cutNumber: function (event) {
    const itemIndex = event.target.dataset.itemIndex;
    const cartItem = this.data.cartGoods[itemIndex];
    const number = cartItem.number - 1 > 1 ? cartItem.number - 1 : 1;
    cartItem.number = number;
    this.setData({
      cartGoods: this.data.cartGoods,
    });
    this.updateCart(cartItem.productId, cartItem.goodsId, number, cartItem.id);
  },
  /** 增加当前购买数量, 由现有库存约束限制上限. */
  addNumber: function (event) {
    const itemIndex = event.target.dataset.itemIndex;
    const cartItem = this.data.cartGoods[itemIndex];
    const number = cartItem.number + 1;
    cartItem.number = number;
    this.setData({
      cartGoods: this.data.cartGoods,
    });
    this.updateCart(cartItem.productId, cartItem.goodsId, number, cartItem.id);
  },
  /** 校验已选商品后进入订单结算页面. */
  checkoutOrder: function () {
    //获取已选择的商品

    const checkedGoods = this.data.cartGoods.filter(function (element, index, array) {
      if (element.checked == true) {
        return true;
      } else {
        return false;
      }
    });

    if (checkedGoods.length <= 0) {
      return false;
    }

    wx.navigateTo({
      url: '../shopping/checkout/checkout',
    });
  },
  /** 删除当前勾选的购物车商品并刷新列表. */
  deleteCart: function () {
    //获取已选择的商品
    const that = this;

    let productIds = this.data.cartGoods.filter(function (element, index, array) {
      if (element.checked == true) {
        return true;
      } else {
        return false;
      }
    });

    if (productIds.length <= 0) {
      return false;
    }

    productIds = productIds.map(function (element, index, array) {
      if (element.checked == true) {
        return element.productId;
      }
    });

    util
      .request(
        api.CartDelete,
        {
          productIds: productIds.join(','),
        },
        'POST',
        'application/json'
      )
      .then(function (res) {
        if (res.code === 0) {
          const cartList = res.data.cartList.map((v) => {
            v.checked = false;
            return v;
          });

          that.setData({
            cartGoods: cartList,
            cartTotal: res.data.cartTotal,
          });
        }

        that.setData({
          checkedAllStatus: that.isCheckedAll(),
        });
      });
  },
});
