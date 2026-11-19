const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');
const {createPagedFeed} = require('../../../services/paged-feed.js');
const {withPageReads} = require('../../../services/page-read.js');
const {performPageAction} = require('../../../services/page-action.js');

const feed = createPagedFeed({
    url: api.FootprintList,
    listField: 'records',
    responseField: 'records',
    refreshOnShow: true,
});

Page(
    withPageReads({
        ...feed,
        data: {...feed.data, footprintGroups: []},
        /** 清除旧日期分组, 读取和账号隔离继续复用共享分页. */
        resetFeed() {
            this.setData({footprintGroups: []});
            return feed.resetFeed.call(this);
        },
        /** 将当前已成功读取的分页记录按服务端明确日期分组. */
        readFeed(page) {
            return feed.readFeed.call(this, page).then((success) => {
                if (success) {
                    const groups = new Map();
                    for (const row of this.data.records) {
                        if (!groups.has(row.date)) {
                            groups.set(row.date, []);
                        }
                        groups.get(row.date).push(row);
                    }
                    this.setData({
                        footprintGroups: Array.from(groups, ([date, items]) => ({date, items})),
                    });
                }
                return success;
            });
        },
        /** 只从当前已加载列表解析事件身份, 不信任外部对象. */
        selectedRow(event) {
            const id = Number(event.currentTarget.dataset.id);
            return this.data.records.find((row) => row.id === id);
        },
        /** 普通点击进入当前商品, 确认和写入期间不跳转. */
        openItem(event) {
            const row = this.selectedRow(event);
            if (row && !this.data.actionBusy) {
                wx.navigateTo({url: '/pages/goods/goods?id=' + row.goodsId});
            }
        },
        /** 长按从确认开始防重, 删除成功重新读取第一页, 失败保留原列表. */
        deleteItem(event) {
            const row = this.selectedRow(event);
            if (!row) {
                return Promise.resolve(false);
            }
            return performPageAction(this, {
                confirmation: '删除该商品的全部浏览足迹？',
                canRun: () =>
                    !this.data.loading &&
                    !this.data.error &&
                    this.data.records.some((item) => item.id === row.id),
                request: () => util.request(api.FootprintDelete, {footprintId: row.id}),
                onSuccess: () => {
                    wx.showToast({title: '已删除', icon: 'success'});
                    this.resetFeed();
                },
            });
        },
    })
);
