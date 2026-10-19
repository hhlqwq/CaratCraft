const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
  data: {
    id: 0,
    goodsDetailHtml: '',
    goods: {},
    gallery: [],
    attribute: [],
    issueList: [],
    comment: [],
    brand: {},
    specificationList: [],
    productList: [],
    relatedGoods: [],
    cartGoodsCount: 0,
    userHasCollect: 0,
    number: 1,
    checkedSpecText: '请选择规格数量',
    openAttr: false,
    noCollectImage: '/static/images/icon_collect.png',
    hasCollectImage: '/static/images/icon_collect_checked.png',
    collectBackImage: '/static/images/icon_collect.png',
    isCustom: false,
  },
  /** 读取商品详情、规格和购物车数量并更新展示状态. */
  getGoodsInfo: function () {
    const that = this;
    util.request(api.GoodsDetail, {id: that.data.id}).then(function (res) {
      if (res.code === 0) {
        that.setData({
          goods: res.data.info,
          gallery: res.data.gallery,
          attribute: res.data.attribute,
          issueList: res.data.issue,
          comment: res.data.comment,
          brand: res.data.brand,
          specificationList: res.data.specificationList,
          productList: res.data.productList,
          userHasCollect: res.data.userHasCollect,
        });
        //设置默认值
        that.setDefSpecInfo(that.data.specificationList);
        if (res.data.userHasCollect == 1) {
          that.setData({
            collectBackImage: that.data.hasCollectImage,
          });
        } else {
          that.setData({
            collectBackImage: that.data.noCollectImage,
          });
        }

        /* 使用微信原生富文本组件显示HTML, 不再分发旧解析库. */
        that.setData({goodsDetailHtml: res.data.info.goodsDesc || ''});

        that.getGoodsRelated();
      }
    });
  },
  /** 图片加载失败时替换为本地占位图 / Replaces failed product images with a local placeholder. */
  handleImageError: function (event) {
    const imageType = event.currentTarget.dataset.imageType;
    const index = Number(event.currentTarget.dataset.index || 0);
    let dataPath = '';
    if (imageType === 'gallery') {
      dataPath = 'gallery[' + index + '].imgUrl';
    }
    if (imageType === 'related') {
      dataPath = 'relatedGoods[' + index + '].listPicUrl';
    }
    if (imageType === 'goods') {
      dataPath = 'goods.listPicUrl';
    }
    if (!dataPath) {
      return;
    }
    const update = {};
    update[dataPath] = '/static/images/logo.png';
    this.setData(update);
  },
  /** 读取当前商品的关联推荐列表. */
  getGoodsRelated: function () {
    const that = this;
    util.request(api.GoodsRelated, {id: that.data.id}).then(function (res) {
      if (res.code === 0) {
        that.setData({
          relatedGoods: res.data.goodsList,
        });
      }
    });
  },
  /** 更新当前点击的规格选项并重新匹配SKU. */
  clickSkuValue: function (event) {
    const specNameId = event.currentTarget.dataset.nameId;
    const specValueId = event.currentTarget.dataset.valueId;

    //判断是否可以点击

    //TODO 性能优化，可在wx:for中添加index，可以直接获取点击的属性名和属性值，不用循环
    const _specificationList = this.data.specificationList;
    for (let i = 0; i < _specificationList.length; i++) {
      if (_specificationList[i].specificationId == specNameId) {
        for (let j = 0; j < _specificationList[i].valueList.length; j++) {
          if (_specificationList[i].valueList[j].id == specValueId) {
            //如果已经选中，则反选
            if (_specificationList[i].valueList[j].checked) {
              _specificationList[i].valueList[j].checked = false;
            } else {
              _specificationList[i].valueList[j].checked = true;
            }
          } else {
            _specificationList[i].valueList[j].checked = false;
          }
        }
      }
    }
    this.setData({
      specificationList: _specificationList,
    });
    //重新计算spec改变后的信息
    this.changeSpecInfo();

    //重新计算哪些值不可以点击
  },

  //获取选中的规格信息
  getCheckedSpecValue: function () {
    const checkedValues = [];
    const _specificationList = this.data.specificationList;
    for (let i = 0; i < _specificationList.length; i++) {
      const _checkedObj = {
        nameId: _specificationList[i].specificationId,
        valueId: 0,
        valueText: '',
      };
      for (let j = 0; j < _specificationList[i].valueList.length; j++) {
        if (_specificationList[i].valueList[j].checked) {
          _checkedObj.valueId = _specificationList[i].valueList[j].id;
          _checkedObj.valueText = _specificationList[i].valueList[j].value;
        }
      }
      checkedValues.push(_checkedObj);
    }

    return checkedValues;
  },
  //根据已选的值，计算其它值的状态
  setSpecValueStatus: function () {},
  //判断规格是否选择完整
  isCheckedAllSpec: function () {
    return !this.getCheckedSpecValue().some(function (v) {
      if (v.valueId == 0) {
        return true;
      }
    });
  },
  /** 按规格顺序生成用于匹配SKU的选中标识. */
  getCheckedSpecKey: function () {
    const checkedValue = this.getCheckedSpecValue().map(function (v) {
      return v.valueId;
    });

    return checkedValue.join('_');
  },
  /** 根据当前规格选择更新价格、库存及说明. */
  changeSpecInfo: function () {
    const checkedNameValue = this.getCheckedSpecValue();

    //设置选择的信息
    const checkedValue = checkedNameValue
      .filter(function (v) {
        if (v.valueId != 0) {
          return true;
        } else {
          return false;
        }
      })
      .map(function (v) {
        return v.valueText;
      });
    if (checkedValue.length > 0) {
      this.setData({
        checkedSpecText: checkedValue.join('　'),
      });
    } else {
      this.setData({
        checkedSpecText: '请选择规格数量',
      });
    }
  },
  /** 从商品SKU中查找与当前规格组合一致的记录. */
  getCheckedProductItem: function (key) {
    return this.data.productList.filter(function (v) {
      if (v.goodsSpecificationIds.indexOf(key) > -1) {
        return true;
      } else {
        return false;
      }
    });
  },
  /** 初始化商品详情状态并读取页面所需数据. */
  onLoad: function (options) {
    this.setData({
      id: parseInt(options.id),
      // id: 1181000
    });
    const that = this;
    this.getGoodsInfo();
    // 已开启定制的商品展示"定制这款"入口 / Shows the designer entry for custom-enabled goods.
    util.request(api.CustomizationConfig, {goodsId: that.data.id}, 'GET').then(function (res) {
      if (res.code === 0) {
        that.setData({isCustom: true});
      }
    });
    util.request(api.CartGoodsCount).then(function (res) {
      if (res.code === 0) {
        that.setData({
          cartGoodsCount: res.data.cartTotal.goodsCount,
        });
      }
    });
  },

  /** 跳转到 DIY 定制器 / Navigates to the DIY designer tab. */
  openDesigner: function () {
    // tabBar 页不支持带参跳转, 通过 storage 传递商品 ID / Tab pages take no query; pass via storage.
    wx.setStorageSync('designerGoodsId', this.data.id);
    wx.switchTab({
      url: '/pages/designer/designer',
    });
  },

  /** 切换规格选择面板并保留当前购买意图. */
  switchAttrPop: function () {
    if (this.data.openAttr == false) {
      this.setData({
        openAttr: !this.data.openAttr,
        collectBackImage: '/static/images/detail_back.png',
      });
    }
  },
  /** 优先关闭规格面板, 无面板时切换商品收藏状态. */
  closeAttrOrCollect: function () {
    const that = this;
    if (this.data.openAttr) {
      this.setData({
        openAttr: false,
      });
      if (that.data.userHasCollect == 1) {
        that.setData({
          collectBackImage: that.data.hasCollectImage,
        });
      } else {
        that.setData({
          collectBackImage: that.data.noCollectImage,
        });
      }
    } else {
      //添加或是取消收藏
      util
        .request(
          api.CollectAddOrDelete,
          {typeId: 0, valueId: this.data.id},
          'POST',
          'application/json'
        )
        .then(function (res) {
          const _res = res;
          if (_res.code == 0) {
            if (_res.data.type == 'add') {
              that.setData({
                collectBackImage: that.data.hasCollectImage,
              });
            } else {
              that.setData({
                collectBackImage: that.data.noCollectImage,
              });
            }
          } else {
            wx.showToast({
              image: '/static/images/icon_error.png',
              title: _res.msg,
              mask: true,
            });
          }
        });
    }
  },
  /** 通过独立购物车入口进入购物车页面. */
  openCartPage: function () {
    wx.navigateTo({
      url: '/pages/cart/cart',
    });
  },

  /**
   * 直接购买
   */
  buyGoods: function () {
    const that = this;
    if (this.data.openAttr == false) {
      //打开规格选择窗口
      this.setData({
        openAttr: !this.data.openAttr,
        collectBackImage: '/static/images/detail_back.png',
      });
    } else {
      //提示选择完整规格
      if (!this.isCheckedAllSpec()) {
        return false;
      }

      //根据选中的规格，判断是否有对应的sku信息
      const checkedProduct = this.getCheckedProductItem(this.getCheckedSpecKey());
      if (!checkedProduct || checkedProduct.length <= 0) {
        //找不到对应的product信息，提示没有库存
        return false;
      }

      //验证库存
      if (checkedProduct.goodsNumber < this.data.number) {
        //找不到对应的product信息，提示没有库存
        return false;
      }

      // 直接购买商品
      util
        .request(
          api.BuyAdd,
          {goodsId: this.data.goods.id, number: this.data.number, productId: checkedProduct[0].id},
          'POST',
          'application/json'
        )
        .then(function (res) {
          const _res = res;
          if (_res.code == 0) {
            that.setData({
              openAttr: !that.data.openAttr,
            });
            wx.navigateTo({
              url:
                '/pages/shopping/checkout/checkout?isBuy=true&checkoutId=' +
                encodeURIComponent(res.data.checkoutId),
            });
          } else {
            wx.showToast({
              image: '/static/images/icon_error.png',
              title: _res.msg,
              mask: true,
            });
          }
        });
    }
  },

  /**
   * 添加到购物车
   */
  addToCart: function () {
    const that = this;
    if (this.data.openAttr == false) {
      //打开规格选择窗口
      this.setData({
        openAttr: !this.data.openAttr,
        collectBackImage: '/static/images/detail_back.png',
      });
    } else {
      //提示选择完整规格
      if (!this.isCheckedAllSpec()) {
        wx.showToast({
          title: '请选择完整规格',
        });
        return false;
      }

      //根据选中的规格，判断是否有对应的sku信息
      const checkedProduct = this.getCheckedProductItem(this.getCheckedSpecKey());
      if (!checkedProduct || checkedProduct.length <= 0) {
        //找不到对应的product信息，提示没有库存
        return false;
      }

      //验证库存
      if (checkedProduct.goodsNumber < this.data.number) {
        //找不到对应的product信息，提示没有库存
        return false;
      }

      //添加到购物车
      util
        .request(
          api.CartAdd,
          {goodsId: this.data.goods.id, number: this.data.number, productId: checkedProduct[0].id},
          'POST',
          'application/json'
        )
        .then(function (res) {
          const _res = res;
          if (_res.code == 0) {
            wx.showToast({
              title: '添加成功',
            });
            that.setData({
              openAttr: !that.data.openAttr,
              cartGoodsCount: _res.data.cartTotal.goodsCount,
            });
            if (that.data.userHasCollect == 1) {
              that.setData({
                collectBackImage: that.data.hasCollectImage,
              });
            } else {
              that.setData({
                collectBackImage: that.data.noCollectImage,
              });
            }
          } else {
            wx.showToast({
              image: '/static/images/icon_error.png',
              title: _res.msg,
              mask: true,
            });
          }
        });
    }
  },
  /** 减少当前购买数量, 保留最小购买数量限制. */
  cutNumber: function () {
    this.setData({
      number: this.data.number - 1 > 1 ? this.data.number - 1 : 1,
    });
  },
  /** 增加当前购买数量, 由现有库存约束限制上限. */
  addNumber: function () {
    this.setData({
      number: this.data.number + 1,
    });
  },
  /** 初始化默认规格展示与可购买状态. */
  setDefSpecInfo: function (specificationList) {
    //未考虑规格联动情况
    const that = this;
    if (!specificationList) {
      return;
    }
    for (let i = 0; i < specificationList.length; i++) {
      const specification = specificationList[i];
      const specNameId = specification.specificationId;
      //规格只有一个时自动选择规格
      if (specification.valueList && specification.valueList.length == 1) {
        const specValueId = specification.valueList[0].id;
        that.clickSkuValue({currentTarget: {dataset: {nameId: specNameId, valueId: specValueId}}});
      }
    }
    specificationList.map(function (item) {});
  },
});
