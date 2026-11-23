const assert = require('node:assert/strict');
const test = require('node:test');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const {
    resolveMiniapp,
    readConfig,
    configurationPolicy,
    inspectStartup,
    repairConfig,
} = require('../../tools/maintenance/dev-env.cjs');

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

/** 旧配置保持原字节, 显式补齐缺项时保留已有密码. */
test('旧配置升级只补安全默认值并可重复运行', (t) => {
    const temp = fs.mkdtempSync(path.join(os.tmpdir(), 'caratcraft-config-'));
    t.after(() => fs.rmSync(temp, {recursive: true}));
    const file = path.join(temp, '.env');
    const policy = configurationPolicy(
        fs.readFileSync(path.resolve(__dirname, '../../deploy/.env.example'), 'utf8')
    );
    const secret = Buffer.alloc(32, 49).toString('base64');
    const original = [
        '# 老配置',
        'MYSQL_DATABASE=existing',
        'MYSQL_APP_USER=existing_app',
        'MYSQL_ROOT_PASSWORD=' + secret,
        'REDIS_PORT=8888',
        'MYSQL_PORT=3306',
        '',
    ].join('\r\n');
    fs.writeFileSync(file, original);
    const problems = inspectStartup(readConfig(file), policy).join('\n');
    assert.match(problems, /CARATCRAFT_ENV 缺失/);
    assert.match(problems, /MYSQL_APP_PASSWORD 缺失/);
    assert.match(problems, /JWT_SECRET 缺失/);
    assert(!problems.includes(secret));
    assert.deepEqual(repairConfig(file, policy), ['CARATCRAFT_ENV']);
    assert.equal(fs.readFileSync(file, 'utf8'), original + 'CARATCRAFT_ENV=local\r\n');
    assert.deepEqual(repairConfig(file, policy), []);
    assert.equal(readConfig(file).MYSQL_APP_USER, 'existing_app');
    assert.equal(readConfig(file).MYSQL_APP_PASSWORD, undefined);
    assert.equal(readConfig(file).NGINX_PORT, undefined);
    fs.writeFileSync(file, original + 'MINIAPP_LAN_ORIGIN=http://192.168.1.7:8888\r\n');
    assert.deepEqual(repairConfig(file, policy), []);
    assert.equal(readConfig(file).CARATCRAFT_ENV, undefined);
});
