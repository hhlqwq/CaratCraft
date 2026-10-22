const util = require('../../../utils/util.js');
const api = require('../../../config/api.js');

const statuses = {
    PENDING_REVIEW: ['等待商家审核', '申请已提交, 如需退货请先与商家确认寄回安排'],
    APPROVED: ['审核已通过', '正在向支付渠道提交退款, 尚未确认到账'],
    PROCESSING: ['支付渠道处理中', '已受理不等于已到账, 请以支付渠道查询结果为准'],
    SUCCESS: ['退款成功', '服务端已核对支付渠道返回的退款成功状态'],
    RETRY: ['退款核对中', '系统将继续核对, 无需重复提交申请'],
    MANUAL_REVIEW: ['待人工核对', '请联系商家核实退款进度, 当前未确认退款成功'],
    FAILED: ['退款未成功', '请联系商家处理, 当前未确认退款成功'],
    REJECTED: ['申请未通过', '如需补充说明, 请联系商家沟通'],
};

Page({
    data: {records: [], page: 1, total: 0, hasMore: true, loading: false, error: ''},

    /** 初始化请求代次, 列表由页面展示事件刷新以接收订单详情中的状态变化. */
    onLoad() {
        this.requestVersion = 0;
    },

    /** 从订单详情返回时重新读取售后状态, 不把旧状态当作实时到账结果. */
    onShow() {
        this.reload();
    },

    /** 开始新一轮分页并废弃此前未完成的响应. */
    reload() {
        this.requestVersion++;
        this.setData({records: [], page: 1, total: 0, hasMore: true, loading: false, error: ''});
        return this.loadMore();
    },

    /** 仅成功响应推进页码, 失败保留原页并提供明确重试入口. */
    async loadMore() {
        if (this.data.loading || !this.data.hasMore) {
            return;
        }
        const version = this.requestVersion;
        const page = this.data.page;
        this.setData({loading: true, error: ''});
        try {
            const response = await util.request(api.RefundList, {page, limit: 20});
            if (version !== this.requestVersion) {
                return;
            }
            if (response.code !== 0) {
                throw new Error(response.msg || '售后资料加载失败');
            }
            if (!response.data || !Array.isArray(response.data.records)) {
                throw new Error('售后资料格式无效, 请重试');
            }
            const rows = response.data.records.map((item) => {
                const display = statuses[item.status] || [
                    '状态待核对',
                    '当前状态未知, 请联系商家核对进度',
                ];
                const amount = Number(item.amountFen);
                return {
                    ...item,
                    statusText: display[0],
                    hint: display[1],
                    amountText:
                        Number.isSafeInteger(amount) && amount >= 0
                            ? (amount / 100).toFixed(2)
                            : '待核对',
                };
            });
            const records = this.data.records.concat(rows);
            this.setData({
                records,
                total: response.data.total,
                page: page + 1,
                hasMore: rows.length > 0 && records.length < response.data.total,
            });
        } catch (error) {
            if (version === this.requestVersion) {
                this.setData({error: error.message || '售后资料加载失败, 请重试'});
            }
        } finally {
            if (version === this.requestVersion) {
                this.setData({loading: false});
            }
        }
    },

    /** 触底按当前登录身份继续加载下一页售后申请. */
    onReachBottom() {
        this.loadMore();
    },

    /** 手动刷新完成后总是结束微信刷新提示. */
    async onPullDownRefresh() {
        try {
            await this.reload();
        } finally {
            wx.stopPullDownRefresh();
        }
    },

    /** 离开私人页面时清除订单资料并废弃请求, 避免切换账号后短暂展示旧列表. */
    onHide() {
        this.requestVersion++;
        this.setData({records: [], total: 0, loading: false, error: ''});
    },

    /** 页面卸载使未完成请求失效, 不再更新已销毁页面. */
    onUnload() {
        this.requestVersion++;
    },
});
