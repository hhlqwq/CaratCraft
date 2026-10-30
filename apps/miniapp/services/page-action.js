const util = require('../utils/util.js');

/** 锁定一次页面写入, 确认后复核账号及页面归属, 失败只提示而不自动重放. */
async function performPageAction(page, {confirmation, canRun, request, onSuccess}) {
    const token = wx.getStorageSync('token') || '';
    if (
        page.data.actionBusy ||
        page.data.readLoading ||
        page.data.readError ||
        page._readToken !== token ||
        !page._pageReads?.alive ||
        !canRun()
    ) {
        return false;
    }
    const context = page._pageReads;
    /** 防止弹窗期间切换账号、离开页面或换掉当前操作对象. */
    function isCurrent() {
        return (
            page._pageReads === context &&
            context?.alive &&
            token === (wx.getStorageSync('token') || '')
        );
    }
    page.setData({actionBusy: true});
    try {
        if (confirmation) {
            const confirmed = await new Promise((resolve, reject) => {
                wx.showModal({
                    title: '请确认',
                    content: confirmation,
                    success: (result) => resolve(result.confirm),
                    fail: reject,
                });
            });
            if (!confirmed) {
                return false;
            }
        }
        if (!isCurrent() || !canRun()) {
            return false;
        }
        const response = await request();
        if (!isCurrent()) {
            return false;
        }
        if (!response || response.code !== 0) {
            throw new Error(response?.msg || '操作失败, 请刷新后确认状态');
        }
        onSuccess(response);
        return true;
    } catch (error) {
        if (isCurrent()) {
            util.showErrorToast(error?.message || '操作失败, 请刷新后确认状态');
        }
        return false;
    } finally {
        if (isCurrent()) {
            page.setData({actionBusy: false});
        }
    }
}

module.exports = {performPageAction};
