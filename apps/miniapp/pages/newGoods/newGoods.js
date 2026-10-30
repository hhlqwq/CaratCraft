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
            size: 20,
        },
        /** 读取新品列表的筛选条件和页面初始信息. */
        getData() {
            this.getGoodsList();
            readPage(
                this,
                'GoodsNew',
                () => util.request(api.GoodsNew),
                (res) => {
                    if (res.code === 0) {
                        this.setData({
                            bannerInfo: res.data.bannerInfo,
                        });
                    }
                }
            );
        },
        /** 按当前条件分页读取新品列表商品列表. */
        getGoodsList(reset = true) {
            return readGoodsPage(
                this,
                {
                    isNew: 1,
                    order: this.data.currentSortOrder,
                    sort: this.data.currentSortType,
                    categoryId: this.data.categoryId,
                },
                reset
            );
        },
        /** 初始化新品列表状态并读取页面所需数据. */
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
    })
);
