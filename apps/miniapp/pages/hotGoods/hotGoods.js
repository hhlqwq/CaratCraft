const util = require('../../utils/util.js');
const api = require('../../config/api.js');

Page({
    data: {
        bannerInfo: {
            imgUrl: '',
            name: '',
        },
        categoryFilter: false,
        filterCategory: [],
        goodsList: [],
        categoryId: 0,
        currentSortType: 'default',
        currentSortOrder: 'desc',
        page: 1,
        size: 1000,
    },
    /** 读取热门商品的筛选条件和页面初始信息. */
    getData() {
        util.request(api.GoodsHot).then((res) => {
            if (res.code === 0) {
                this.setData({
                    bannerInfo: res.data.bannerInfo,
                });
                this.getGoodsList();
            }
        });
    },
    /** 按当前条件分页读取热门商品商品列表. */
    getGoodsList() {
        util.request(api.GoodsList, {
            isHot: 1,
            page: this.data.page,
            size: this.data.size,
            order: this.data.currentSortOrder,
            sort: this.data.currentSortType,
            categoryId: this.data.categoryId,
        }).then((res) => {
            if (res.code === 0) {
                this.setData({
                    goodsList: res.data.goodsList.records,
                    filterCategory: res.data.filterCategory,
                });
            }
        });
    },
    /** 初始化热门商品状态并读取页面所需数据. */
    onLoad(options) {
        this.getData();
    },

    /** 切换排序或筛选条件并重新加载商品列表. */
    openSortFilter(event) {
        const currentId = event.currentTarget.id;
        switch (currentId) {
            case 'categoryFilter':
                this.setData({
                    categoryFilter: !this.data.categoryFilter,
                    currentSortType: 'category',
                    currentSortOrder: 'asc',
                });
                break;
            case 'priceSort': {
                let tmpSortOrder = 'asc';
                if (this.data.currentSortOrder == 'asc') {
                    tmpSortOrder = 'desc';
                }
                this.setData({
                    currentSortType: 'price',
                    currentSortOrder: tmpSortOrder,
                    categoryFilter: false,
                });

                this.getData();
                break;
            }
            default:
                //综合排序
                this.setData({
                    currentSortType: 'default',
                    currentSortOrder: 'desc',
                    categoryFilter: false,
                });
                this.getData();
        }
    },
    /** 应用当前分类筛选并刷新商品列表. */
    selectCategory(event) {
        const currentIndex = event.target.dataset.categoryIndex;
        this.setData({
            categoryFilter: false,
            categoryId: this.data.filterCategory[currentIndex].id,
        });
        this.getData();
    },
});
