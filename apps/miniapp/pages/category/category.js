const {readGoodsPage} = require('../../services/goods-feed.js');
const {readPage, withPageReads} = require('../../services/page-read.js');
const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page(
    withPageReads({
        data: {
            // text:"这是一个页面"
            navList: [],
            goodsList: [],
            id: 0,
            currentCategory: {},
            scrollLeft: 0,
            scrollTop: 0,
            scrollHeight: 0,
            page: 1,
            size: 10,
            loadmoreText: '正在加载更多数据',
            nomoreText: '全部加载完成',
            nomore: false,
            totalPages: 1,
        },
        /** 初始化分类商品状态并读取页面所需数据. */
        onLoad(options) {
            if (options.id) {
                this.setData({
                    id: parseInt(options.id),
                });
            }

            wx.getSystemInfo({
                success: (res) => {
                    this.setData({
                        scrollHeight: res.windowHeight,
                    });
                },
            });

            this.getCategoryInfo();
        },
        /** 读取当前分类详情与可切换的同级分类. */
        getCategoryInfo() {
            this.getGoodsList();
            readPage(
                this,
                'GoodsCategory',
                () => util.request(api.GoodsCategory, {id: this.data.id}),
                (res) => {
                    if (res.code == 0) {
                        this.setData({
                            navList: res.data.brotherCategory,
                            currentCategory: res.data.currentCategory,
                        });

                        //nav位置
                        let currentIndex = 0;
                        const navListCount = this.data.navList.length;
                        for (let i = 0; i < navListCount; i++) {
                            currentIndex += 1;
                            if (this.data.navList[i].id == this.data.id) {
                                break;
                            }
                        }
                        if (currentIndex > navListCount / 2 && navListCount > 5) {
                            this.setData({
                                scrollLeft: currentIndex * 60,
                            });
                        }
                    } else {
                        //显示错误信息
                    }
                }
            );
        },

        /**
         * 页面上拉触底事件的处理函数
         */
        onReachBottom() {
            this.getGoodsList(false);
        },

        /** 按当前条件分页读取分类商品商品列表. */
        getGoodsList(reset = true) {
            return readGoodsPage(this, {categoryId: this.data.id}, reset);
        },

        /** 根据点击的分类标识切换商品展示范围. */
        switchCate(event) {
            if (this.data.id == event.currentTarget.dataset.id) {
                return false;
            }

            const clientX = event.detail.x;
            const currentTarget = event.currentTarget;
            if (clientX < 60) {
                this.setData({
                    scrollLeft: currentTarget.offsetLeft - 60,
                });
            } else if (clientX > 330) {
                this.setData({
                    scrollLeft: currentTarget.offsetLeft,
                });
            }
            this.setData({
                id: event.currentTarget.dataset.id,
                page: 1,
                totalPages: 1,
                goodsList: [],
                nomore: false,
            });

            this.getCategoryInfo();
        },
    })
);
