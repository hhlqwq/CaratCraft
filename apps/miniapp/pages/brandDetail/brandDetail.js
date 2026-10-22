const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
    data: {
        id: 0,
        brand: {},
        goodsList: [],
        page: 1,
        size: 1000,
    },
    /** 初始化品牌详情状态并读取页面所需数据. */
    onLoad(options) {
        this.setData({
            id: parseInt(options.id),
        });
        this.getBrand();
    },
    /** 读取当前品牌介绍和展示信息. */
    getBrand() {
        util.request(api.BrandDetail, {id: this.data.id}).then((res) => {
            if (res.code === 0) {
                this.setData({
                    brand: res.data.brand,
                });

                this.getGoodsList();
            }
        });
    },
    /** 按当前条件分页读取品牌详情商品列表. */
    getGoodsList() {
        util.request(api.GoodsList, {
            brandId: this.data.id,
            page: this.data.page,
            size: this.data.size,
        }).then((res) => {
            if (res.code === 0) {
                this.setData({
                    goodsList: res.data.goodsList.records,
                });
            }
        });
    },
});
