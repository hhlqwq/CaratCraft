const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

Page({
    data: {
        typeId: 0,
        collectList: [],
        loading: false,
        loadError: '',
        changing: false,
    },
    /** 读取本人收藏的商品列表. */
    async getCollectList() {
        if (!this.contextCurrent() || this.data.changing) {
            return;
        }
        const generation = ++this.generation;
        this.setData({loading: true, loadError: '', collectList: []});
        try {
            const res = await util.request(api.CollectList, {typeId: 0});
            if (!this.contextCurrent(generation)) {
                return;
            }
            if (res.code !== 0 || !Array.isArray(res.data)) {
                throw new Error('收藏读取失败');
            }
            this.setData({collectList: res.data});
        } catch (error) {
            if (this.contextCurrent(generation)) {
                this.setData({loadError: '收藏加载失败, 请点击重试'});
            }
        } finally {
            if (this.contextCurrent(generation)) {
                this.setData({loading: false});
            }
        }
    },

    /** 页面再次展示时刷新商品收藏状态. */
    onShow() {
        this.active = true;
        this.token = wx.getStorageSync('token');
        this.generation = (this.generation || 0) + 1;
        this.setData({collectList: [], changing: false});
        this.getCollectList();
    },

    /** 离页时清除私人列表并废弃旧确认框及响应. */
    onHide() {
        this.active = false;
        this.generation++;
        this.setData({collectList: []});
    },

    /** 卸载使用统一离页规则. */
    onUnload() {
        this.onHide();
    },

    /** 核对页面代次与原账号身份. */
    contextCurrent(generation = this.generation) {
        return (
            this.active &&
            generation === this.generation &&
            !!this.token &&
            this.token === wx.getStorageSync('token')
        );
    },

    /** 进入所选收藏商品的详情页面. */
    openGoods(event) {
        if (
            !this.contextCurrent() ||
            this.data.loading ||
            this.data.loadError ||
            this.data.changing
        ) {
            return;
        }
        const item = this.data.collectList[event.currentTarget.dataset.index];
        if (!item || !item.valueId) {
            return;
        }
        const goodsId = item.valueId;

        //触摸时间距离页面打开的毫秒数
        const touchTime = this.data.touch_end - this.data.touch_start;
        //如果按下时间大于350为长按
        if (touchTime > 350) {
            const generation = ++this.generation;
            this.setData({changing: true});
            wx.showModal({
                title: '',
                content: '确定删除收藏吗？',
                success: async (res) => {
                    if (!this.contextCurrent(generation)) {
                        return;
                    }
                    if (!res.confirm) {
                        this.setData({changing: false});
                        return;
                    }
                    try {
                        const response = await util.request(
                            api.CollectAddOrDelete,
                            {typeId: 0, valueId: goodsId},
                            'POST',
                            'application/json'
                        );
                        if (!this.contextCurrent(generation)) {
                            return;
                        }
                        if (
                            response.code !== 0 ||
                            !response.data ||
                            !['add', 'delete'].includes(response.data.type)
                        ) {
                            throw new Error('收藏结果未确认');
                        }
                        wx.showToast({
                            title:
                                response.data.type === 'delete'
                                    ? '已取消收藏'
                                    : '已收藏, 请核对列表',
                            icon: 'none',
                        });
                    } catch (error) {
                        if (this.contextCurrent(generation)) {
                            util.showErrorToast('操作结果未确认, 请重新核对收藏');
                        }
                    } finally {
                        if (this.contextCurrent(generation)) {
                            this.setData({changing: false});
                            this.getCollectList();
                        }
                    }
                },
                fail: () => {
                    if (this.contextCurrent(generation)) {
                        this.setData({changing: false});
                        util.showErrorToast('无法打开确认框, 请重试');
                    }
                },
            });
        } else {
            wx.navigateTo({
                url: '/pages/goods/goods?id=' + goodsId,
            });
        }
    },
    /** 记录按下时间以识别长按操作. */
    touchStart(e) {
        this.setData({
            touch_start: e.timeStamp,
        });
    },
    /** 记录松开时间以区分详情导航与收藏操作. */
    touchEnd(e) {
        this.setData({
            touch_end: e.timeStamp,
        });
    },
});
