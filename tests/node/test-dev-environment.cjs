const assert = require('node:assert/strict');
const test = require('node:test');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const {spawnSync} = require('node:child_process');
const {
    resolveMiniapp,
    readConfig,
    configurationPolicy,
    inspectStartup,
    repairConfig,
} = require('../../tools/maintenance/dev-env.cjs');
const configureSkip =
    process.platform !== 'linux' || process.getuid?.() !== 0
        ? '首次配置文件属主测试需要Docker内的Linux root, 按维护文档执行容器专项'
        : false;

/** 独立配置副本验证首次安全配置、旧配置保留和残缺凭据拦截. */
test('全新环境生成独立密钥及超管凭据, 已有配置逐字保留', {skip: configureSkip}, () => {
    const fixture = fs.mkdtempSync(path.join(os.tmpdir(), 'cc-first-config-'));
    const source = path.resolve(__dirname, '../../tools/maintenance/dev-env.cjs');
    const command = path.join(fixture, 'tools/maintenance/dev-env.cjs');
    const config = path.join(fixture, 'deploy/.env');
    const bootstrap = path.join(fixture, 'deploy/secrets/platform-bootstrap.txt');
    fs.mkdirSync(path.dirname(command), {recursive: true});
    fs.mkdirSync(path.dirname(config), {recursive: true});
    fs.copyFileSync(source, command);
    fs.copyFileSync(
        path.resolve(__dirname, '../../deploy/.env.example'),
        path.join(fixture, 'deploy/.env.example')
    );
    try {
        const first = spawnSync(process.execPath, [command, 'configure'], {
            encoding: 'utf8',
            env: {...process.env, CARATCRAFT_TOOL_UID: '1000', CARATCRAFT_TOOL_GID: '1000'},
        });
        assert.equal(first.status, 0, first.stderr);
        const initial = fs.readFileSync(config);
        const values = readConfig(config);
        const keys = ['MYSQL_ROOT_PASSWORD', 'MYSQL_APP_PASSWORD', 'REDIS_PASSWORD', 'JWT_SECRET'];
        assert.equal(new Set(keys.map((key) => values[key])).size, 4);
        assert.equal(Buffer.from(values.JWT_SECRET, 'base64').length, 32);
        assert.equal(
            values.CARATCRAFT_SUPER_ADMIN_BOOTSTRAP_FILE,
            '/run/secrets/platform-bootstrap.runtime.txt'
        );
        const secret = fs.readFileSync(bootstrap, 'utf8');
        const runtime = path.join(fixture, 'deploy/secrets/platform-bootstrap.runtime.txt');
        assert.equal(fs.readFileSync(runtime, 'utf8'), secret);
        assert.match(secret, /^[A-Za-z0-9_-]{32}$/);
        if (process.platform !== 'win32') {
            assert.equal(fs.statSync(bootstrap).mode & 0o777, 0o600);
            assert.equal(fs.statSync(config).mode & 0o777, 0o600);
            assert.equal(fs.statSync(runtime).mode & 0o777, 0o600);
            assert.equal(fs.statSync(runtime).uid, 999);
        }
        assert.equal(fs.statSync(config).uid, 1000);
        fs.chmodSync(fixture, 0o755);
        const access = 'require("node:fs").accessSync(process.argv[1], 4)';
        assert.equal(
            spawnSync(process.execPath, ['-e', access, runtime], {uid: 999, gid: 999}).status,
            0
        );
        assert.notEqual(
            spawnSync(process.execPath, ['-e', access, bootstrap], {uid: 999, gid: 999}).status,
            0
        );
        assert.notEqual(
            spawnSync(process.execPath, ['-e', access, runtime], {uid: 998, gid: 998}).status,
            0
        );
        assert.equal(first.stdout.includes(secret), false);
        assert.equal(spawnSync(process.execPath, [command, 'configure']).status, 0);
        assert.deepEqual(fs.readFileSync(config), initial);
        fs.unlinkSync(config);
        assert.notEqual(spawnSync(process.execPath, [command, 'configure']).status, 0);
        assert.equal(fs.existsSync(config), false);
    } finally {
        fs.rmSync(fixture, {recursive: true, force: true});
    }
});

/** 异常公开模板必须在生成任何私有文件前停止, CRLF模板仅替换指定项. */
test('配置生成兼容CRLF, 拒绝缺失或重复密钥项', {skip: configureSkip}, () => {
    const source = path.resolve(__dirname, '../../tools/maintenance/dev-env.cjs');
    const template = fs.readFileSync(path.resolve(__dirname, '../../deploy/.env.example'), 'utf8');
    for (const text of [
        template.replace(/^JWT_SECRET=.*\n/m, ''),
        template + '\nJWT_SECRET=\n',
        template.replace(/\r?\n/g, '\r\n'),
    ]) {
        const fixture = fs.mkdtempSync(path.join(os.tmpdir(), 'cc-config-template-'));
        const command = path.join(fixture, 'tools/maintenance/dev-env.cjs');
        const config = path.join(fixture, 'deploy/.env');
        const bootstrap = path.join(fixture, 'deploy/secrets/platform-bootstrap.txt');
        try {
            fs.mkdirSync(path.dirname(command), {recursive: true});
            fs.mkdirSync(path.dirname(config), {recursive: true});
            fs.copyFileSync(source, command);
            fs.writeFileSync(path.join(fixture, 'deploy/.env.example'), text);
            const result = spawnSync(process.execPath, [command, 'configure'], {encoding: 'utf8'});
            if (!text.includes('\r\n')) {
                assert.notEqual(result.status, 0);
                assert.equal(fs.existsSync(config), false);
                assert.equal(fs.existsSync(bootstrap), false);
                continue;
            }
            assert.equal(result.status, 0, result.stderr);
            const generated = fs.readFileSync(config, 'utf8');
            let expected = text;
            const values = readConfig(config);
            for (const key of [
                'MYSQL_ROOT_PASSWORD',
                'MYSQL_APP_PASSWORD',
                'REDIS_PASSWORD',
                'JWT_SECRET',
            ]) {
                expected = expected.replace(
                    new RegExp(`^${key}=[^\\r\\n]*`, 'm'),
                    `${key}=${values[key]}`
                );
            }
            expected = expected.replace(
                /^CARATCRAFT_SUPER_ADMIN_BOOTSTRAP_FILE=[^\r\n]*/m,
                'CARATCRAFT_SUPER_ADMIN_BOOTSTRAP_FILE=/run/secrets/platform-bootstrap.runtime.txt'
            );
            assert.equal(generated, expected);
        } finally {
            fs.rmSync(fixture, {recursive: true, force: true});
        }
    }
});

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
