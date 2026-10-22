/* 微信云开发AI接入回归, 仅使用SDK与HTTP替身, 不消耗真实云额度. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');

/** 构建隔离SDK和后端, 观察模型通道、请求及一次性票据提交. */
function fixture({envId = 'test-env', generate, timers = {setTimeout, clearTimeout}} = {}) {
    const calls = {init: [], providers: [], generations: [], requests: []};
    const result = {
        lengthCm: 45,
        rangeMinCm: 43,
        rangeMaxCm: 47,
        optionIds: [1],
        reason: '估算建议',
    };
    const response = {
        choices: [{finish_reason: 'stop', message: {content: JSON.stringify(result)}}],
        note: '以上内容为AI生成, 不代表开发者立场, 请勿删除或修改本标记',
    };
    const config = {envId};
    const util = {
        request: async (url, data) => {
            calls.requests.push({url, data});
            if (url === '/prepare') {
                return {code: 0, data: {ticket: 'test-ticket', context: {catalog: [{id: 1}]}}};
            }
            return {code: 0, data: {goodsId: 10, ...result}};
        },
    };
    const wx = {
        cloud: {
            init: (options) => {
                calls.init.push(options);
            },
            extend: {
                AI: {
                    createModel: (provider) => {
                        calls.providers.push(provider);
                        return {
                            generateText: (options) => {
                                calls.generations.push(options);
                                return generate ? generate(options) : Promise.resolve(response);
                            },
                        };
                    },
                },
            },
        },
    };
    const sandbox = {module: {exports: {}}, wx, ...timers};
    sandbox.require = (name) => {
        if (name === '../config/cloud-ai.js') {
            return config;
        }
        if (name === '../utils/util.js') {
            return util;
        }
        if (name === '../config/api.js') {
            return {
                AiRecommendationPrepare: '/prepare',
                AiRecommendationValidate: '/validate',
            };
        }
        throw new Error('未知依赖: ' + name);
    };
    vm.runInNewContext(
        fs.readFileSync(
            path.resolve(
                __dirname,
                '../vendor/platform-wxshop/wx-mall/services/ai-recommendation.js'
            ),
            'utf8'
        ),
        sandbox
    );
    const input = {
        goodsId: 10,
        heightCm: 165,
        weightKg: 55,
        neckCm: null,
        preference: '锁骨位置',
        consent: true,
    };
    return {service: sandbox.module.exports, input, calls, wx, response};
}

/** 不配置云环境或基础库缺少AI时不调用任何外部服务. */
test('无云环境和旧微信明确降级到手动设计', async () => {
    const missing = fixture({envId: ''});
    await assert.rejects(
        missing.service.recommend(missing.input, () => true),
        /尚未配置/
    );
    assert.equal(missing.calls.requests.length, 0);
    const old = fixture();
    delete old.wx.cloud.extend;
    assert.match(old.service.availability(), /升级微信/);
});

/** 用户未同意时不向后端或微信提交身体参数. */
test('无同意不提交AI数据', async () => {
    const {service, input, calls} = fixture();
    input.consent = false;
    await assert.rejects(
        service.recommend(input, () => true),
        /同意/
    );
    assert.equal(calls.requests.length, 0);
});

/** 固定仅赠送额度通道, 完整传递微信生成标记并校验结果. */
test('仅使用hunyuan-v3和hy3并保留生成标记', async () => {
    const {service, input, calls, response} = fixture();
    const res = await service.recommend(input, () => true);
    assert.deepEqual(calls.providers, ['hunyuan-v3']);
    assert.equal(calls.generations[0].model, 'hy3');
    assert.equal(calls.generations[0].max_tokens, 1000);
    assert.deepEqual(
        calls.requests.map((call) => call.url),
        ['/prepare', '/validate']
    );
    assert.equal(calls.requests[1].data.ticket, 'test-ticket');
    assert.equal(calls.requests[1].data.result.lengthCm, 45);
    assert.equal(res.data.modelNotice, response.note);
    assert.equal(calls.init[0].env, 'test-env');
});

/** 额度不足或云服务失败不得切换到消耗套餐的通道或自动重试. */
test('云调用失败不重试且不切换付费通道', async () => {
    const {service, input, calls} = fixture({
        generate: () => Promise.reject({errMsg: 'quota exhausted'}),
    });
    await assert.rejects(
        service.recommend(input, () => true),
        /不会自动切换付费/
    );
    assert.deepEqual(calls.providers, ['hunyuan-v3']);
    assert.equal(calls.generations.length, 1);
    assert.equal(calls.requests.length, 1);
});

/** 失效请求不得继续生成, 已生成的失效结果也不能提交采用. */
test('输入变化后阻止模型调用或结果提交', async () => {
    const before = fixture();
    await assert.rejects(
        before.service.recommend(before.input, () => false),
        /参数已变化/
    );
    assert.equal(before.calls.generations.length, 0);
    const after = fixture();
    let checks = 0;
    await assert.rejects(
        after.service.recommend(after.input, () => ++checks === 1),
        /参数已变化/
    );
    assert.equal(after.calls.generations.length, 1);
    assert.equal(after.calls.requests.length, 1);
});

/** 不完整或非JSON输出不得交给后端材料校验. */
test('拒绝被截断、非JSON和超长模型内容', async () => {
    for (const choice of [
        {finish_reason: 'length', message: {content: '{}'}},
        {finish_reason: 'stop', message: {content: '```json\n{}\n```'}},
        {finish_reason: 'stop', message: {content: 'x'.repeat(16001)}},
    ]) {
        const {service, input, calls} = fixture({
            generate: () => Promise.resolve({choices: [choice]}),
        });
        await assert.rejects(
            service.recommend(input, () => true),
            /格式无效|未返回完整/
        );
        assert.equal(calls.requests.length, 1);
    }
});

/** 超时不取消底层微信调用时保持锁, 避免并行消耗额度; 结束后释放. */
test('超时保留调用锁直到底层调用结束', async () => {
    let timeout;
    let finish;
    const {service, input, calls} = fixture({
        generate: () =>
            new Promise((resolve) => {
                finish = resolve;
            }),
        timers: {
            setTimeout: (callback) => {
                timeout = callback;
                return 1;
            },
            clearTimeout: () => {},
        },
    });
    const pending = service.recommend(input, () => true);
    const rejected = assert.rejects(pending, /响应超时/);
    await new Promise(setImmediate);
    timeout();
    await rejected;
    await assert.rejects(
        service.recommend(input, () => true),
        /正在处理/
    );
    assert.equal(calls.generations.length, 1);
    finish({choices: []});
    await new Promise(setImmediate);
    const current = false;
    await assert.rejects(
        service.recommend(input, () => current),
        /参数已变化/
    );
    assert.equal(calls.requests.length, 2);
});
