const fs = require('node:fs');
const path = require('node:path');
const {spawnSync} = require('node:child_process');

const root = path.resolve(__dirname, '../..');
const envFile = path.join(root, 'deploy/.env');
const target = path.join(root, 'apps/miniapp/config/environment.local.js');
const apiPath = '/platform-framework-api/app/';

/** 读取项目唯一配置, 拒绝重复键, 保留秘密供本机进程使用. */
function readConfig(file) {
    return parseConfig(fs.readFileSync(file, 'utf8'));
}

/** 解析 UTF-8 键值, 区分缺失键和空值. */
function parseConfig(text) {
    const config = Object.create(null);
    const lines = text.replace(/^\uFEFF/, '').split(/\r?\n/);
    for (const line of lines) {
        if (!line.trim() || line.trimStart().startsWith('#')) {
            continue;
        }
        const pair = /^([A-Z][A-Z0-9_]*)=(.*)$/.exec(line);
        if (!pair || Object.hasOwn(config, pair[1])) {
            throw new Error(`配置包含无效或重复的键 ${pair?.[1] || ''}`);
        }
        config[pair[1]] = pair[2].trim();
    }
    return config;
}

const envExample = path.join(root, 'deploy/.env.example');

/** 读取公开模板声明的启动规则及安全默认值. */
function configurationPolicy(template) {
    const defaults = parseConfig(template);
    const rules = new Map();
    for (const match of template.matchAll(
        /^# @startup (default|port|required|secret)\r?\n([A-Z][A-Z0-9_]*)=/gm
    )) {
        rules.set(match[2], match[1]);
    }
    return {defaults, rules};
}

/** 只对标记了安全策略的缺失项提供默认值, 保留所有已有值. */
function safeDefault(key, config, {rules, defaults}) {
    const rule = rules.get(key);
    const value = defaults[key];
    if (!value || (rule !== 'default' && rule !== 'port')) {
        return false;
    }
    if (key === 'CARATCRAFT_ENV' && config.MINIAPP_LAN_ORIGIN) {
        return false;
    }
    return (
        rule !== 'port' ||
        (/^[0-9]{1,5}$/.test(value) &&
            Number(value) >= 1 &&
            Number(value) <= 65535 &&
            !Array.from(rules).some(
                ([other, type]) =>
                    other !== key && type === 'port' && Number(config[other]) === Number(value)
            ))
    );
}

/** 汇总配置错误, 只返回键名和操作提示. */
function inspectStartup(config, policy, allowProduction = false) {
    const issues = [];
    const ports = new Map();
    for (const [key, rule] of policy.rules) {
        const value = config[key];
        if (!value) {
            const repairable = !Object.hasOwn(config, key) && safeDefault(key, config, policy);
            issues.push(
                key +
                    ' 缺失。' +
                    (repairable
                        ? '可执行 repair-config 补齐安全默认值。'
                        : '请手工核对并填写原配置。')
            );
        } else if (rule === 'secret' && value.length < 24) {
            issues.push(key + ' 不足24个字符。请核对原有凭据。');
        } else if (rule === 'port') {
            const number = Number(value);
            if (!/^[0-9]{1,5}$/.test(value) || number < 1 || number > 65535) {
                issues.push(key + ' 端口无效。');
            } else if (ports.has(number)) {
                issues.push(key + ' 与 ' + ports.get(number) + ' 端口重复。');
            } else {
                ports.set(number, key);
            }
        }
    }
    const jwt = config.JWT_SECRET;
    if (jwt && jwt.length >= 24) {
        const bytes = Buffer.from(jwt, 'base64');
        if (bytes.length < 32 || bytes.toString('base64') !== jwt) {
            issues.push('JWT_SECRET 应为至少32字节随机数的标准 Base64 编码。');
        }
    }
    if (config.MYSQL_APP_USER?.toLowerCase() === 'root') {
        issues.push('MYSQL_APP_USER 不得使用 root。');
    }
    const modes = allowProduction ? ['local', 'lan', 'production'] : ['local', 'lan'];
    if (config.CARATCRAFT_ENV && !modes.includes(config.CARATCRAFT_ENV)) {
        issues.push('CARATCRAFT_ENV 必须是 ' + modes.join('、') + '。');
    }
    if (config.CARATCRAFT_ENV === 'lan' && !config.MINIAPP_LAN_ORIGIN) {
        issues.push('MINIAPP_LAN_ORIGIN 缺失。请填写手机可访问的局域网地址。');
    }
    return issues;
}

/** 串行追加可安全修复的缺项, 既有内容保持原样. */
function repairConfig(file, policy) {
    if (!fs.lstatSync(file).isFile()) {
        throw new Error('deploy/.env 必须是普通文件。');
    }
    const lock = file + '.compat.lock';
    const handle = fs.openSync(lock, 'wx', 0o600);
    try {
        const raw = fs.readFileSync(file);
        const text = raw.toString('utf8');
        const config = parseConfig(text);
        const added = [];
        for (const key of policy.rules.keys()) {
            if (!Object.hasOwn(config, key) && safeDefault(key, config, policy)) {
                config[key] = policy.defaults[key];
                added.push(key);
            }
        }
        if (added.length) {
            const newline = text.includes('\r\n') ? '\r\n' : '\n';
            if (!raw.equals(fs.readFileSync(file))) {
                throw new Error('deploy/.env 已被其他进程修改，请重新执行。');
            }
            const prefix = text.endsWith('\n') || !text ? '' : newline;
            fs.appendFileSync(
                file,
                prefix + added.map((key) => key + '=' + config[key]).join(newline) + newline
            );
        }
        return added;
    } finally {
        fs.closeSync(handle);
        fs.unlinkSync(lock);
    }
}

/** 同时显示旧文件遗漏的可选键, 缺失值及秘密不进入日志. */
function reportConfiguration(config, policy, allowProduction) {
    const missing = Object.keys(policy.defaults).filter((key) => !Object.hasOwn(config, key));
    if (missing.length) {
        console.log('配置文件缺少这些键（含可选项） ' + missing.join('、'));
    }
    const issues = inspectStartup(config, policy, allowProduction);
    for (const issue of issues) {
        console.error(issue);
    }
    return issues.length === 0;
}

/** 校验独立环境的站点源, 拒绝凭据、子路径及发布环境的非 HTTPS 地址. */
function origin(value, name, mode) {
    let url;
    try {
        url = new URL(value);
    } catch {
        throw new Error(`${name} 缺失或无效, 请填写完整站点地址`);
    }
    const loopback = /^(localhost|127\.|\[::1\]$)/i.test(url.hostname);
    const dnsName = /^(?:[a-z0-9-]+\.)+[a-z][a-z0-9-]*$/i.test(url.hostname);
    if (
        !['http:', 'https:'].includes(url.protocol) ||
        url.username ||
        url.password ||
        url.pathname !== '/' ||
        url.search ||
        url.hash ||
        url.hostname === '0.0.0.0' ||
        (mode === 'lan' && loopback) ||
        (mode === 'production' && (url.protocol !== 'https:' || !dnsName || loopback))
    ) {
        throw new Error(`${name} 与 ${mode} 环境要求不符, 请检查协议、域名及路径`);
    }
    return url.origin + apiPath;
}

/** 从模式选择单一开发地址, 体验版和正式版分别配置. */
function resolveMiniapp(config) {
    const mode = config.CARATCRAFT_ENV;
    if (!['local', 'lan', 'production'].includes(mode)) {
        throw new Error('CARATCRAFT_ENV 必须是 local、lan 或 production');
    }
    const port = Number(config.NGINX_PORT);
    if (!Number.isInteger(port) || port < 1 || port > 65535) {
        throw new Error('NGINX_PORT 缺失或无效');
    }
    const production = config.MINIAPP_PRODUCTION_ORIGIN;
    return {
        developBaseUrl:
            mode === 'local'
                ? origin(
                      config.MINIAPP_LOCAL_ORIGIN || `http://127.0.0.1:${port}`,
                      'MINIAPP_LOCAL_ORIGIN',
                      mode
                  )
                : mode === 'lan'
                  ? origin(config.MINIAPP_LAN_ORIGIN, 'MINIAPP_LAN_ORIGIN', mode)
                  : '',
        trialBaseUrl: config.MINIAPP_TRIAL_ORIGIN
            ? origin(config.MINIAPP_TRIAL_ORIGIN, 'MINIAPP_TRIAL_ORIGIN', 'production')
            : '',
        releaseBaseUrl:
            production || mode === 'production'
                ? origin(production, 'MINIAPP_PRODUCTION_ORIGIN', 'production')
                : '',
    };
}

/** 保存本机生成物, 公共源码和应用镜像不包含此文件. */
function generate(config, destination) {
    const addresses = resolveMiniapp(config);
    fs.writeFileSync(
        destination,
        `/* 本机生成文件, 来源为 deploy/.env. */\nmodule.exports = ${JSON.stringify(addresses, null, 4)};\n`,
        {mode: 0o600}
    );
    return addresses;
}

/** 调用只读命令, 隐藏可能带服务器凭据的原生命令错误. */
function capture(executable, args) {
    const result = spawnSync(executable, args, {cwd: root, encoding: 'utf8', timeout: 8000});
    return result.status === 0 ? result.stdout.trim() : '';
}

/** 显示当前机器的应用链路、运行版本和持久化状态. */
function diagnose(config, addresses) {
    console.log(`仓库 ${root}`);
    console.log(
        `Git ${capture('git', ['branch', '--show-current']) || '分支不可用'} ${capture('git', ['rev-parse', 'HEAD']) || '提交不可用'}`
    );
    console.log(`配置 deploy/.env · ${config.CARATCRAFT_ENV}`);
    console.log(`小程序预期地址 ${JSON.stringify(addresses)}`);
    try {
        const actual = JSON.parse(
            fs.readFileSync(target, 'utf8').split('module.exports = ')[1].trim().replace(/;$/, '')
        );
        console.log(
            `小程序已生成地址 ${JSON.stringify(actual)} · ${JSON.stringify(actual) === JSON.stringify(addresses) ? '一致' : '需重新生成'}`
        );
    } catch {
        console.log('小程序本机配置尚未生成或无法识别');
    }
    const docker = (...args) => capture('docker', args);
    for (const image of ['carat-craft-backend:local', 'carat-craft-admin-ui:local']) {
        const labels = docker('image', 'inspect', '--format', '{{json .Config.Labels}}', image);
        const metadata = labels ? JSON.parse(labels) : {};
        console.log(
            `${image} Git=${metadata['org.opencontainers.image.revision'] || '-'} 摘要=${metadata['io.caratcraft.source-digest'] || '-'}`
        );
    }
    for (const service of [
        'platform-mysql',
        'platform-redis',
        'platform-admin',
        'platform-api',
        'platform-nginx',
    ]) {
        const status = docker(
            'container',
            'inspect',
            '--format',
            '{{.State.Status}} {{if .State.Health}}{{.State.Health.Status}}{{end}} {{.Image}}',
            service
        );
        console.log(`${service} ${status || '无法读取或尚未启动'}`);
    }
    const nginx = docker('exec', 'platform-nginx', 'nginx', '-T');
    console.log(
        `Nginx upstream ${
            nginx
                .split('\n')
                .map((line) => line.trim())
                .filter((line) =>
                    /^set \$(admin|api)_upstream |^proxy_pass http:\/\/platform-/.test(line)
                )
                .join(' | ') || '无法读取'
        }`
    );
    const mysql = docker(
        'exec',
        'platform-mysql',
        'sh',
        '-c',
        'MYSQL_PWD="$MYSQL_PASSWORD" exec mysql -u"$MYSQL_USER" -D"$MYSQL_DATABASE" -Nse "SELECT VERSION FROM CC_SCHEMA_VERSION WHERE ID=1"'
    );
    console.log(`MySQL 应用账号结构版本 ${/^\d+$/.test(mysql) ? mysql : '无法读取'}`);
    const redis = docker(
        'exec',
        'platform-redis',
        'sh',
        '-c',
        'REDISCLI_AUTH="$REDIS_PASSWORD" exec redis-cli ping'
    );
    console.log(`Redis ${redis === 'PONG' ? 'PONG' : '无法读取'}`);
    const requestBase =
        config.CARATCRAFT_ENV === 'production'
            ? addresses.releaseBaseUrl
            : addresses.developBaseUrl;
    for (const [method, endpoint] of [
        ['POST', 'index/newGoods'],
        ['POST', 'goods/count'],
        ['GET', 'community/list?page=1&size=1'],
        ['GET', 'customization/config?goodsId=1999105'],
    ]) {
        const result = capture('curl', [
            '--silent',
            '--show-error',
            '--connect-timeout',
            '3',
            '--max-time',
            '5',
            '--request',
            method,
            '--output',
            process.platform === 'win32' ? 'NUL' : '/dev/null',
            '--write-out',
            'HTTP %{http_code} · IP %{remote_ip} · TLS %{ssl_verify_result}',
            requestBase + endpoint,
        ]);
        console.log(`${method} ${endpoint} ${result || '网络或 TLS 检查未通过'}`);
    }
    for (const service of ['platform-admin', 'platform-nginx']) {
        const mounts = docker('inspect', '--format', '{{json .Mounts}}', service);
        const uploads = (mounts ? JSON.parse(mounts) : []).find(
            (mount) =>
                mount.Destination === '/data/uploads' ||
                mount.Destination === '/usr/share/nginx/html/upload'
        );
        console.log(
            `${service} 上传 ${uploads ? `${uploads.Destination} · ${uploads.Type} · ${uploads.Name || '宿主挂载'}` : '未发现持久挂载'}`
        );
    }
    for (const name of ['apiclient_key.pem', 'wechatpay_public_key.pem']) {
        console.log(
            `deploy/secrets/${name} ${fs.existsSync(path.join(root, 'deploy/secrets', name)) ? '存在' : '未配置'}`
        );
    }
    for (const name of [
        'WX_MINIAPP_APPID',
        'WX_MINIAPP_SECRET',
        'WX_PAY_API_V3_KEY',
        'CARATCRAFT_DEFAULT_BUSINESS_ORG',
    ]) {
        console.log(`${name} ${config[name] ? '已配置' : '未配置'}`);
    }
}

/** 从仓库定位文件并运行生成或只读诊断. */
function main(command) {
    if (!fs.existsSync(envFile)) {
        throw new Error('缺少 deploy/.env, 请先按维护文档执行 configure');
    }
    if (command === 'miniapp') {
        const config = readConfig(envFile);
        console.log(`已生成 ${target} ${JSON.stringify(generate(config, target))}`);
    } else if (['diagnose', 'check-config', 'repair-config'].includes(command)) {
        const policy = configurationPolicy(fs.readFileSync(envExample, 'utf8'));
        if (command === 'repair-config') {
            const added = repairConfig(envFile, policy);
            console.log(added.length ? '已追加 ' + added.join('、') : '没有可安全追加的配置项');
        }
        const config = readConfig(envFile);
        if (!reportConfiguration(config, policy, command === 'diagnose')) {
            process.exitCode = 1;
            return;
        }
        if (command === 'diagnose') {
            diagnose(config, resolveMiniapp(config));
        } else {
            console.log('启动配置检查通过');
        }
    } else {
        throw new Error('请选择 miniapp、diagnose、check-config 或 repair-config');
    }
}

if (require.main === module) {
    try {
        main(process.argv[2]);
    } catch (error) {
        console.error(error.message);
        process.exitCode = 1;
    }
}

module.exports = {
    readConfig,
    resolveMiniapp,
    generate,
    configurationPolicy,
    inspectStartup,
    repairConfig,
};
