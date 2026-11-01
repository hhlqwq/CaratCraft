/** 初始化页面读取上下文, 每个接口分别记录代次和失败状态. */
function contextFor(page) {
    if (!page._pageReads) {
        page._pageReads = {alive: true, requests: new Map()};
    }
    return page._pageReads;
}

/** 汇总当前读取状态, 独立区块失败不会抹掉其他成功内容. */
function renderReadState(page, context) {
    if (!context.alive || page._pageReads !== context) {
        return;
    }
    const requests = Array.from(context.requests.values());
    page.setData({
        readLoading: requests.some((request) => request.loading),
        readError: requests.some((request) => request.failed) ? '数据读取失败, 请重试' : '',
    });
}

/** 执行只读请求并隔离旧响应; 写入和支付不得通过此入口自动重试. */
async function readPage(page, key, requestFactory, apply) {
    const context = contextFor(page);
    if (!context.alive) {
        return false;
    }
    const token = wx.getStorageSync('token') || '';
    const request = {loading: true, failed: false, requestFactory, apply};
    context.requests.set(key, request);
    renderReadState(page, context);
    /** 只有当前账号、当前页面和同接口的最后一次读取可以更新视图. */
    function isCurrent() {
        return (
            context.alive &&
            page._pageReads === context &&
            context.requests.get(key) === request &&
            (wx.getStorageSync('token') || '') === token
        );
    }
    try {
        const response = await requestFactory();
        if (!isCurrent()) {
            return false;
        }
        if (!response || response.code !== 0) {
            throw new Error('读取失败');
        }
        apply(response);
        return true;
    } catch (error) {
        if (isCurrent()) {
            request.failed = true;
        }
        return false;
    } finally {
        request.loading = false;
        if (isCurrent()) {
            renderReadState(page, context);
            if (!page.data.readLoading) {
                wx.stopPullDownRefresh();
            }
        }
    }
}

/** 为只读页面接入卸载隔离、账号清理和显式失败重试, 不重放写操作. */
function withPageReads(definition) {
    const initial = JSON.stringify(definition.data || {});
    const {onLoad, onShow, onUnload} = definition;
    return {
        ...definition,
        data: {...definition.data, readLoading: false, readError: '', actionBusy: false},
        /** 保存路由参数用于账号切换后的只读重新初始化. */
        onLoad(options) {
            this._readOptions = options || {};
            this._readToken = wx.getStorageSync('token') || '';
            contextFor(this);
            return onLoad?.call(this, this._readOptions);
        },
        /** 账号切换先清除旧账号内容和在途请求, 再重新读取. */
        onShow() {
            const token = wx.getStorageSync('token') || '';
            const changed = this._readToken !== undefined && token !== this._readToken;
            this._readToken = token;
            if (changed) {
                contextFor(this).alive = false;
                this._pageReads = null;
                contextFor(this);
                this.setData({
                    ...JSON.parse(initial),
                    readLoading: false,
                    readError: '',
                    actionBusy: false,
                });
                onLoad?.call(this, this._readOptions || {});
            }
            return onShow?.call(this);
        },
        /** 卸载后禁止在途读取修改页面. */
        onUnload() {
            contextFor(this).alive = false;
            return onUnload?.call(this);
        },
        /** 仅重试失败的只读区块, 不重复已成功的请求或任何写入. */
        retryRead() {
            const context = contextFor(this);
            if (
                !context.alive ||
                this.data.readLoading ||
                (wx.getStorageSync('token') || '') !== this._readToken
            ) {
                return Promise.resolve([]);
            }
            return Promise.all(
                Array.from(context.requests.entries())
                    .filter(([, request]) => request.failed)
                    .map(([key, request]) =>
                        readPage(this, key, request.requestFactory, request.apply)
                    )
            );
        },
    };
}

/** 用户更换输入时废弃对应读取, 防止旧搜索结果覆盖新的输入状态. */
function invalidatePageRead(page, key) {
    const context = contextFor(page);
    context.requests.delete(key);
    renderReadState(page, context);
}

module.exports = {readPage, withPageReads, invalidatePageRead};
