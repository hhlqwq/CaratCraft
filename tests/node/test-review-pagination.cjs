/* 购买评价遵守统一分页契约, 不使用旧字段或共享分类页码. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 装载真实页面并控制读取的完成顺序, 无需微信或后端服务. */
function fixture() {
    let page;
    const calls = [];
    vm.runInNewContext(
        fs.readFileSync(
            path.join(__dirname, '../../apps/miniapp/pages/comment/comment.js'),
            'utf8'
        ),
        {
            require: (name) =>
                name.endsWith('util.js')
                    ? {
                          request: (url, data) =>
                              new Promise((resolve, reject) =>
                                  calls.push({url, data, resolve, reject})
                              ),
                          formatTime: (date) => date.toISOString(),
                      }
                    : {CommentCount: 'count', CommentList: 'list'},
            Page: (definition) => {
                page = definition;
            },
        }
    );
    page.data = structuredClone(page.data);
    page.setData = (update) => Object.assign(page.data, update);
    page._version = 1;
    page.data.valueId = 10;
    return {page, calls};
}

/** 读取records并把秒转换成明确展示时间, 列表尾部不再请求空页. */
test('评价使用统一分页契约且结束后不重复加载', async () => {
    const {page, calls} = fixture();
    const pending = page.getCommentList();
    await page.getCommentList();
    assert.equal(calls.length, 1);
    calls[0].resolve({
        code: 0,
        data: {records: [{id: 1, addTime: 1000}], total: 1, current: 1, size: 20},
    });
    await pending;
    assert.equal(page.data.comments[0].id, 1);
    assert.equal(page.data.comments[0].addTimeText, '1970-01-01T00:16:40.000Z');
    assert.equal(page.data.hasMore, false);
    await page.onReachBottom();
    assert.equal(calls.length, 1);
});

/** 翻页失败不跳过数据, 只在成功时更新页码及数组. */
test('评价分页失败保持原页可重试', async () => {
    const {page, calls} = fixture();
    let pending = page.getCommentList();
    calls[0].reject(new Error('offline'));
    await pending;
    assert.equal(page.data.page, 1);
    assert.equal(page.data.error, 'offline');
    pending = page.getCommentList();
    assert.equal(calls[1].data.page, 1);
    calls[1].resolve({code: 0, data: {records: [], total: 0}});
    await pending;
    assert.equal(page.data.error, '');
    assert.equal(page.data.loading, false);
});

/** 切换分类和卸载使旧成功/失败失效, 不污染新分类或已销毁页面. */
test('评价切换分类及卸载隔离迟到响应', async () => {
    const {page, calls} = fixture();
    const first = page.getCommentList();
    page.switchTab({currentTarget: {dataset: {type: 1}}});
    calls[0].resolve({code: 0, data: {records: [{id: 99, addTime: 0}], total: 1}});
    await first;
    assert.equal(page.data.comments.length, 0);
    assert.equal(calls[1].data.page, 1);
    assert.equal(calls[1].data.showType, 1);
    page.onUnload();
    const snapshot = JSON.stringify(page.data);
    calls[1].reject(new Error('迟到失败'));
    calls[2].resolve({code: 0, data: {allCount: 99, hasPicCount: 99}});
    await new Promise(setImmediate);
    assert.equal(JSON.stringify(page.data), snapshot);
});
