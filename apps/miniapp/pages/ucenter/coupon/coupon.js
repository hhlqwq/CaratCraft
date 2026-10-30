const {readPage, withPageReads} = require('../../../services/page-read.js');
const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page(
    withPageReads({
        data: {
            couponList: [],
        },
        /** 初始化个人优惠券状态并读取页面所需数据. */
        onLoad(options) {
            this.loadListData();
        },

        /** 读取当前用户可查看的优惠券列表. */
        loadListData() {
            readPage(
                this,
                'CouponList',
                () => util.request(api.CouponList),
                (res) => {
                    if (res.code === 0) {
                        this.setData({
                            couponList: res.data,
                        });
                    }
                }
            );
        },
    })
);
