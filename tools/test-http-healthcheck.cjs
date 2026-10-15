/* 验证容器HTTP探针只接受就绪响应, 不连接业务应用. */
const assert = require('node:assert/strict');
const {execFile} = require('node:child_process');
const fs = require('node:fs');
const http = require('node:http');
const path = require('node:path');
const {promisify} = require('node:util');
const {test} = require('node:test');
const execute = promisify(execFile);
const root = path.resolve(__dirname, '..');

/** 编译并针对独立HTTP桩验证200成功与503失败, 完成后只关闭本测试服务器. */
test('容器探针区分HTTP200与依赖未就绪503', async () => {
  const home = process.env.JAVA_HOME || path.join(root, '.tools/jdk21/jdk-21.0.8+9');
  const suffix = process.platform === 'win32' ? '.exe' : '';
  const output = path.join(root, '.tools/copyright/healthcheck');
  fs.mkdirSync(output, {recursive: true});
  await execute(path.join(home, 'bin/javac' + suffix), ['-d', output,
    path.join(root, 'vendor/platform-wxshop/deploy/HealthCheck.java')]);
  let status = 200;
  const server = http.createServer((request, response) => {
    response.writeHead(status);
    response.end('{}');
  });
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  const url = 'http://127.0.0.1:' + server.address().port + '/ready';
  try {
    await execute(path.join(home, 'bin/java' + suffix), ['-cp', output, 'HealthCheck', url]);
    status = 503;
    await assert.rejects(execute(path.join(home, 'bin/java' + suffix),
        ['-cp', output, 'HealthCheck', url]), error => error.code === 1);
  } finally {
    await new Promise(resolve => server.close(resolve));
  }
});
