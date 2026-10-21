/* 使用内存文件替身验证前端离线缓存检查, 不安装依赖或写入临时文件. */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const {test} = require('node:test');
const source = fs.readFileSync(path.join(__dirname, 'check-cached-frontend.cjs'), 'utf8');

/** 在隔离上下文运行检查器, 按用例模拟完整、缺失或版本错误的依赖. */
function checkCache(installed) {
  const manifest = {dependencies: {vue: '3.5.44'}, devDependencies: {vite: '8.3.4'}};
  const cachedFs = {
    /** 根据依赖路径返回模拟清单, 缺失文件按真实文件系统报错. */
    readFileSync(file) {
      if (!file.includes('node_modules')) return JSON.stringify(manifest);
      const name = path.basename(path.dirname(file));
      if (!installed[name]) throw new Error('ENOENT');
      return JSON.stringify({version: installed[name]});
    },
  };
  vm.runInNewContext(source, {
    /** 只提供检查器声明的内置模块, 拒绝额外模块和网络入口. */
    require(name) {
      if (name === 'node:fs') return cachedFs;
      if (name === 'node:path') return path;
      throw new Error('Unexpected module: ' + name);
    },
    process: {argv: ['node', 'checker', '/frontend']},
    console: {log() {}},
  });
}

/** 所有直接依赖满足固定版本时允许离线编译. */
test('完整的前端依赖缓存通过', () => {
  assert.doesNotThrow(() => checkCache({vue: '3.5.44', vite: '8.3.4'}));
});

/** 缺少构建工具时必须指出包名, 不沿用旧产物. */
test('缺少前端依赖时指出缺失包', () => {
  assert.throws(() => checkCache({vue: '3.5.44'}), /vite: required=8\.3\.4, installed=missing/);
});

/** 已安装旧版本不能仅因为包存在而通过. */
test('版本不一致时拒绝缓存', () => {
  assert.throws(
    () => checkCache({vue: '3.5.43', vite: '8.3.4'}),
    /vue: required=3\.5\.44, installed=3\.5\.43/
  );
});
