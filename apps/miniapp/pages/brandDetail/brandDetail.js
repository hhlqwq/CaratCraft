const {readGoodsPage} = require('../../services/goods-feed.js');
const {readPage, withPageReads} = require('../../services/page-read.js');
const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page(
    withPageReads({
        /** 触底时读取下一页, 不重复正在读取的页码. */
        onReachBottom() {
            return this.getGoodsList(false);
        },
        data: {
            id: 0,
            brand: {},
            goodsList: [],
            page: 1,
            size: 20,
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
            this.getGoodsList();
            readPage(
                this,
                'BrandDetail',
                () => util.request(api.BrandDetail, {id: this.data.id}),
                (res) => {
                    if (res.code === 0) {
                        this.setData({
                            brand: res.data.brand,
                        });
                    }
                }
            );
        },
        /** 按当前条件分页读取品牌详情商品列表. */
        getGoodsList(reset = true) {
            return readGoodsPage(this, {brandId: this.data.id}, reset);
        },
    })
);
