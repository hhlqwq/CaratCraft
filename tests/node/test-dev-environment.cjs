const assert = require('node:assert/strict');
const test = require('node:test');
const {resolveMiniapp} = require('../../tools/maintenance/dev-env.cjs');

/** 用两个不同局域网地址模拟配置切换, 体验版与正式版选择独立配置. */
test('小程序 local、lan 和 production 选择独立地址', () => {
    const config = {
        NGINX_PORT: '9191',
        MINIAPP_LOCAL_ORIGIN: '',
        MINIAPP_LAN_ORIGIN: 'http://192.168.100.11:9191',
        MINIAPP_TRIAL_ORIGIN: 'https://trial.example.com',
        MINIAPP_PRODUCTION_ORIGIN: 'https://shop.example.com',
    };
    const suffix = '/platform-framework-api/app/';
    const first = resolveMiniapp({...config, CARATCRAFT_ENV: 'lan'});
    const second = resolveMiniapp({
        ...config,
        CARATCRAFT_ENV: 'lan',
        MINIAPP_LAN_ORIGIN: 'http://10.20.0.4:9191',
    });
    assert.equal(first.developBaseUrl, 'http://192.168.100.11:9191' + suffix);
    assert.equal(second.developBaseUrl, 'http://10.20.0.4:9191' + suffix);
    assert.equal(first.trialBaseUrl, second.trialBaseUrl);
    assert.equal(first.releaseBaseUrl, second.releaseBaseUrl);
    const local = resolveMiniapp({...config, CARATCRAFT_ENV: 'local'});
    assert.equal(local.developBaseUrl, 'http://127.0.0.1:9191' + suffix);
    const production = resolveMiniapp({...config, CARATCRAFT_ENV: 'production'});
    assert.equal(production.developBaseUrl, '');
    assert.equal(production.releaseBaseUrl, 'https://shop.example.com' + suffix);
});

/** 配置异常须明确指明缺失的变量, 禁止隐藏回退和非 HTTPS 发布. */
test('小程序拒绝缺失配置、设备回环及不安全发布地址', () => {
    const config = {NGINX_PORT: '8888'};
    assert.throws(() => resolveMiniapp(config), /CARATCRAFT_ENV/);
    assert.throws(() => resolveMiniapp({...config, CARATCRAFT_ENV: 'lan'}), /MINIAPP_LAN_ORIGIN/);
    assert.throws(
        () =>
            resolveMiniapp({
                ...config,
                CARATCRAFT_ENV: 'lan',
                MINIAPP_LAN_ORIGIN: 'http://127.0.0.1:8888',
            }),
        /MINIAPP_LAN_ORIGIN/
    );
    assert.throws(
        () =>
            resolveMiniapp({
                ...config,
                CARATCRAFT_ENV: 'production',
                MINIAPP_PRODUCTION_ORIGIN: 'http://shop.example.com',
            }),
        /MINIAPP_PRODUCTION_ORIGIN/
    );
});
