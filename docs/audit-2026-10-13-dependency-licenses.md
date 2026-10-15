# 本地依赖许可声明索引

> 此附件为版权整改前的历史审查记录. 后续文件可能已替换或移除, 当前状态见[版权整改结果](copyright-remediation-2026-10-14.md). 哈希和问题行号不应当作整改后现状.

[权利与残留主文](audit-2026-10-13-rights-and-residue.md) / [机器可读明细](audit-2026-10-13-dependency-licenses.json).

当前已安装npm元数据1497个唯一name/version, API测试类路径296个JAR. 含开发/测试和间接依赖, 不是最终运行发布物SBOM. POM未直接声明不等于没有许可证, 要查父POM与JAR内声明; 多许可不等于必须同时承担所有候选条款. 所有实际分发范围/例外/素材授权仍待核验.

## npm安装树全部声明

| 包 | 版本 | package.json声明 |
|---|---|---|
| @babel/helper-module-imports | 7.0.0-beta.35 | MIT |
| @babel/helper-string-parser | 7.29.7 | MIT |
| @babel/helper-validator-identifier | 7.29.7 | MIT |
| @babel/parser | 7.29.8 | MIT |
| @babel/types | 7.0.0-beta.35 | MIT |
| @babel/types | 7.29.8 | MIT |
| @gar/promisify | 1.1.3 | MIT |
| @jridgewell/gen-mapping | 0.3.13 | MIT |
| @jridgewell/resolve-uri | 3.1.2 | MIT |
| @jridgewell/source-map | 0.3.11 | MIT |
| @jridgewell/sourcemap-codec | 1.5.5 | MIT |
| @jridgewell/trace-mapping | 0.3.31 | MIT |
| @leichtgewicht/ip-codec | 2.0.5 | MIT |
| @nodelib/fs.scandir | 2.1.5 | MIT |
| @nodelib/fs.stat | 2.0.5 | MIT |
| @nodelib/fs.walk | 1.2.8 | MIT |
| @npmcli/fs | 1.1.1 | ISC |
| @npmcli/move-file | 1.1.2 | MIT |
| @parcel/watcher | 2.6.0 | MIT |
| @parcel/watcher-win32-x64 | 2.6.0 | MIT |
| @rtsao/scc | 1.1.0 | MIT |
| @testim/chrome-version | 1.1.4 | MIT |
| @types/glob | 7.2.0 | MIT |
| @types/html-minifier-terser | 5.1.2 | MIT |
| @types/json-schema | 7.0.15 | MIT |
| @types/json5 | 0.0.29 | MIT |
| @types/minimatch | 6.0.0 | MIT |
| @types/node | 26.3.0 | MIT |
| @types/q | 1.5.8 | MIT |
| @types/source-list-map | 0.1.6 | MIT |
| @types/tapable | 1.0.12 | MIT |
| @types/uglify-js | 3.17.5 | MIT |
| @types/webpack | 4.41.40 | MIT |
| @types/webpack-sources | 3.2.3 | MIT |
| @types/yauzl | 2.10.3 | MIT |
| @vue/compiler-sfc | 2.7.16 | 未在package.json直接声明 |
| @vue/component-compiler-utils | 3.3.0 | MIT |
| @webassemblyjs/ast | 1.9.0 | MIT |
| @webassemblyjs/floating-point-hex-parser | 1.9.0 | MIT |
| @webassemblyjs/helper-api-error | 1.9.0 | MIT |
| @webassemblyjs/helper-buffer | 1.9.0 | MIT |
| @webassemblyjs/helper-code-frame | 1.9.0 | MIT |
| @webassemblyjs/helper-fsm | 1.9.0 | ISC |
| @webassemblyjs/helper-module-context | 1.9.0 | MIT |
| @webassemblyjs/helper-wasm-bytecode | 1.9.0 | MIT |
| @webassemblyjs/helper-wasm-section | 1.9.0 | MIT |
| @webassemblyjs/ieee754 | 1.9.0 | MIT |
| @webassemblyjs/leb128 | 1.9.0 | MIT |
| @webassemblyjs/utf8 | 1.9.0 | MIT |
| @webassemblyjs/wasm-edit | 1.9.0 | MIT |
| @webassemblyjs/wasm-gen | 1.9.0 | MIT |
| @webassemblyjs/wasm-opt | 1.9.0 | MIT |
| @webassemblyjs/wasm-parser | 1.9.0 | MIT |
| @webassemblyjs/wast-parser | 1.9.0 | MIT |
| @webassemblyjs/wast-printer | 1.9.0 | MIT |
| @xtuc/ieee754 | 1.2.0 | BSD-3-Clause |
| @xtuc/long | 4.2.2 | Apache-2.0 |
| accepts | 1.3.8 | MIT |
| acorn | 3.3.0 | MIT |
| acorn | 5.7.4 | MIT |
| acorn | 6.4.2 | MIT |
| acorn | 8.18.0 | MIT |
| acorn-jsx | 3.0.1 | MIT |
| agent-base | 2.1.1 | MIT |
| agent-base | 6.0.2 | MIT |
| aggregate-error | 3.1.0 | MIT |
| ajv | 6.15.0 | MIT |
| ajv | 4.11.8 | MIT |
| ajv-errors | 1.0.1 | MIT |
| ajv-keywords | 1.5.1 | MIT |
| ajv-keywords | 3.5.2 | MIT |
| alphanum-sort | 1.1.0 | MIT |
| ansi-colors | 1.1.0 | MIT |
| ansi-colors | 3.2.4 | MIT |
| ansi-cyan | 0.1.1 | MIT |
| ansi-escapes | 1.4.0 | MIT |
| ansi-gray | 0.1.1 | MIT |
| ansi-html-community | 0.0.8 | Apache-2.0 |
| ansi-red | 0.1.1 | MIT |
| ansi-regex | 2.1.1 | MIT |
| ansi-regex | 3.0.1 | MIT |
| ansi-regex | 4.1.1 | MIT |
| ansi-styles | 2.2.1 | MIT |
| ansi-styles | 3.2.1 | MIT |
| ansi-wrap | 0.1.0 | {"type":"MIT","url":"https://github.com/jonschlinkert/ansi-wrap/blob/master/LICENSE"} |
| anymatch | 3.1.3 | ISC |
| anymatch | 2.0.0 | ISC |
| append-buffer | 1.0.2 | MIT |
| aproba | 1.2.0 | ISC |
| archy | 1.0.0 | MIT |
| argparse | 1.0.10 | MIT |
| arr-diff | 1.1.0 | MIT |
| arr-diff | 4.0.0 | MIT |
| arr-filter | 1.1.2 | MIT |
| arr-flatten | 1.1.0 | MIT |
| arr-map | 2.0.2 | MIT |
| arr-union | 2.1.0 | MIT |
| arr-union | 3.1.0 | MIT |
| array-buffer-byte-length | 1.0.2 | MIT |
| array-each | 1.0.1 | MIT |
| array-flatten | 1.1.1 | MIT |
| array-flatten | 2.1.2 | MIT |
| array-includes | 3.1.9 | MIT |
| array-initial | 1.1.0 | MIT |
| array-last | 1.3.0 | MIT |
| array-slice | 0.2.3 | {"type":"MIT","url":"https://github.com/jonschlinkert/array-slice/blob/master/LICENSE"} |
| array-slice | 1.1.0 | MIT |
| array-sort | 1.0.0 | MIT |
| array-union | 1.0.2 | MIT |
| array-union | 2.1.0 | MIT |
| array-uniq | 1.0.3 | MIT |
| array-unique | 0.3.2 | MIT |
| array.prototype.findlastindex | 1.2.6 | MIT |
| array.prototype.flat | 1.3.3 | MIT |
| array.prototype.flatmap | 1.3.3 | MIT |
| array.prototype.reduce | 1.0.8 | MIT |
| arraybuffer.prototype.slice | 1.0.4 | MIT |
| asn1.js | 4.10.1 | MIT |
| assert | 1.5.1 | MIT |
| assertion-error | 1.0.0 | MIT |
| assign-symbols | 1.0.0 | MIT |
| ast-types | 0.14.2 | MIT |
| async | 2.6.4 | MIT |
| async | 3.2.6 | MIT |
| async-done | 1.3.2 | MIT |
| async-each | 1.0.6 | MIT |
| async-function | 1.0.0 | MIT |
| async-limiter | 1.0.1 | MIT |
| async-settle | 1.0.0 | MIT |
| async-validator | 1.8.5 | MIT |
| asynckit | 0.4.0 | MIT |
| atob | 2.1.2 | (MIT OR Apache-2.0) |
| autoprefixer | 6.7.7 | MIT |
| autoprefixer | 7.2.6 | MIT |
| available-typed-arrays | 1.0.7 | MIT |
| axios | 0.21.4 | MIT |
| axios | 1.19.0 | MIT |
| babel-code-frame | 6.26.0 | MIT |
| babel-core | 6.26.3 | MIT |
| babel-eslint | 7.2.3 | MIT |
| babel-generator | 6.26.1 | MIT |
| babel-helper-bindify-decorators | 6.24.1 | MIT |
| babel-helper-builder-binary-assignment-operator-visitor | 6.24.1 | MIT |
| babel-helper-call-delegate | 6.24.1 | MIT |
| babel-helper-define-map | 6.26.0 | MIT |
| babel-helper-explode-assignable-expression | 6.24.1 | MIT |
| babel-helper-explode-class | 6.24.1 | MIT |
| babel-helper-function-name | 6.24.1 | MIT |
| babel-helper-get-function-arity | 6.24.1 | MIT |
| babel-helper-hoist-variables | 6.24.1 | MIT |
| babel-helper-optimise-call-expression | 6.24.1 | MIT |
| babel-helper-regex | 6.26.0 | MIT |
| babel-helper-remap-async-to-generator | 6.24.1 | MIT |
| babel-helper-replace-supers | 6.24.1 | MIT |
| babel-helper-vue-jsx-merge-props | 2.0.3 | MIT |
| babel-helpers | 6.24.1 | MIT |
| babel-loader | 7.1.5 | MIT |
| babel-messages | 6.23.0 | MIT |
| babel-plugin-check-es2015-constants | 6.22.0 | MIT |
| babel-plugin-component | 1.1.1 | MIT |
| babel-plugin-dynamic-import-node | 1.2.0 | MIT |
| babel-plugin-syntax-async-functions | 6.13.0 | MIT |
| babel-plugin-syntax-async-generators | 6.13.0 | MIT |
| babel-plugin-syntax-class-properties | 6.13.0 | MIT |
| babel-plugin-syntax-decorators | 6.13.0 | MIT |
| babel-plugin-syntax-dynamic-import | 6.18.0 | MIT |
| babel-plugin-syntax-exponentiation-operator | 6.13.0 | MIT |
| babel-plugin-syntax-object-rest-spread | 6.13.0 | MIT |
| babel-plugin-syntax-trailing-function-commas | 6.22.0 | MIT |
| babel-plugin-transform-async-generator-functions | 6.24.1 | MIT |
| babel-plugin-transform-async-to-generator | 6.24.1 | MIT |
| babel-plugin-transform-class-properties | 6.24.1 | MIT |
| babel-plugin-transform-decorators | 6.24.1 | MIT |
| babel-plugin-transform-es2015-arrow-functions | 6.22.0 | MIT |
| babel-plugin-transform-es2015-block-scoped-functions | 6.22.0 | MIT |
| babel-plugin-transform-es2015-block-scoping | 6.26.0 | MIT |
| babel-plugin-transform-es2015-classes | 6.24.1 | MIT |
| babel-plugin-transform-es2015-computed-properties | 6.24.1 | MIT |
| babel-plugin-transform-es2015-destructuring | 6.23.0 | MIT |
| babel-plugin-transform-es2015-duplicate-keys | 6.24.1 | MIT |
| babel-plugin-transform-es2015-for-of | 6.23.0 | MIT |
| babel-plugin-transform-es2015-function-name | 6.24.1 | MIT |
| babel-plugin-transform-es2015-literals | 6.22.0 | MIT |
| babel-plugin-transform-es2015-modules-amd | 6.24.1 | MIT |
| babel-plugin-transform-es2015-modules-commonjs | 6.26.2 | MIT |
| babel-plugin-transform-es2015-modules-systemjs | 6.24.1 | MIT |
| babel-plugin-transform-es2015-modules-umd | 6.24.1 | MIT |
| babel-plugin-transform-es2015-object-super | 6.24.1 | MIT |
| babel-plugin-transform-es2015-parameters | 6.24.1 | MIT |
| babel-plugin-transform-es2015-shorthand-properties | 6.24.1 | MIT |
| babel-plugin-transform-es2015-spread | 6.22.0 | MIT |
| babel-plugin-transform-es2015-sticky-regex | 6.24.1 | MIT |
| babel-plugin-transform-es2015-template-literals | 6.22.0 | MIT |
| babel-plugin-transform-es2015-typeof-symbol | 6.23.0 | MIT |
| babel-plugin-transform-es2015-unicode-regex | 6.24.1 | MIT |
| babel-plugin-transform-exponentiation-operator | 6.24.1 | MIT |
| babel-plugin-transform-object-rest-spread | 6.26.0 | MIT |
| babel-plugin-transform-regenerator | 6.26.0 | MIT |
| babel-plugin-transform-runtime | 6.23.0 | MIT |
| babel-plugin-transform-strict-mode | 6.24.1 | MIT |
| babel-polyfill | 6.26.0 | MIT |
| babel-preset-env | 1.7.0 | MIT |
| babel-preset-stage-2 | 6.24.1 | MIT |
| babel-preset-stage-3 | 6.24.1 | MIT |
| babel-register | 6.26.0 | MIT |
| babel-runtime | 6.26.0 | MIT |
| babel-template | 6.26.0 | MIT |
| babel-traverse | 6.26.0 | MIT |
| babel-types | 6.26.0 | MIT |
| babylon | 6.18.0 | MIT |
| bach | 1.2.0 | MIT |
| balanced-match | 0.4.2 | MIT |
| balanced-match | 1.0.2 | MIT |
| balanced-match | 4.0.4 | MIT |
| base | 0.11.2 | MIT |
| base64-js | 1.5.1 | MIT |
| baseline-browser-mapping | 2.11.19 | Apache-2.0 |
| batch | 0.6.1 | MIT |
| bfj-node4 | 5.3.1 | MIT |
| big.js | 5.2.2 | MIT |
| binary-extensions | 1.13.1 | MIT |
| binary-extensions | 2.3.0 | MIT |
| binaryextensions | 1.0.1 | MIT |
| bluebird | 3.7.2 | MIT |
| bn.js | 4.12.5 | MIT |
| bn.js | 5.2.5 | MIT |
| body-parser | 1.20.6 | MIT |
| bonjour | 3.5.1 | MIT |
| boolbase | 1.0.0 | ISC |
| brace-expansion | 1.1.18 | MIT |
| brace-expansion | 5.0.9 | MIT |
| braces | 2.3.2 | MIT |
| braces | 3.0.3 | MIT |
| brorand | 1.1.0 | MIT |
| browser-stdout | 1.3.0 | ISC |
| browserify-aes | 1.2.0 | MIT |
| browserify-cipher | 1.0.1 | MIT |
| browserify-des | 1.0.2 | MIT |
| browserify-rsa | 4.1.1 | MIT |
| browserify-sign | 4.2.6 | ISC |
| browserify-zlib | 0.2.0 | MIT |
| browserslist | 1.7.7 | MIT |
| browserslist | 2.11.3 | MIT |
| browserslist | 3.2.8 | MIT |
| browserslist | 4.28.8 | MIT |
| buffer | 4.9.2 | MIT |
| buffer-crc32 | 0.2.13 | MIT |
| buffer-equal | 1.0.1 | MIT |
| buffer-from | 1.1.2 | MIT |
| buffer-indexof | 1.1.1 | MIT |
| buffer-xor | 1.0.3 | MIT |
| bufferutil | 4.1.0 | MIT |
| builtin-status-codes | 3.0.0 | MIT |
| bytes | 3.1.2 | MIT |
| cacache | 10.0.4 | ISC |
| cacache | 12.0.4 | ISC |
| cacache | 15.3.0 | ISC |
| cache-base | 1.0.1 | MIT |
| call-bind | 1.0.9 | MIT |
| call-bind-apply-helpers | 1.0.2 | MIT |
| call-bound | 1.0.4 | MIT |
| caller-callsite | 2.0.0 | MIT |
| caller-path | 0.1.0 | MIT |
| caller-path | 2.0.0 | MIT |
| callsites | 2.0.0 | MIT |
| callsites | 0.2.0 | MIT |
| camel-case | 4.1.2 | MIT |
| camelcase | 3.0.0 | MIT |
| camelcase | 5.3.1 | MIT |
| caniuse-api | 1.6.1 | MIT |
| caniuse-api | 3.0.0 | MIT |
| caniuse-db | 1.0.30001810 | CC-BY-4.0 |
| caniuse-lite | 1.0.30001810 | CC-BY-4.0 |
| chai-nightwatch | 0.1.1 | MIT |
| chalk | 1.1.3 | MIT |
| chalk | 2.4.2 | MIT |
| check-types | 7.4.0 | MIT |
| chokidar | 2.1.8 | MIT |
| chokidar | 3.6.0 | MIT |
| chokidar | 5.0.0 | MIT |
| chownr | 1.1.4 | ISC |
| chownr | 2.0.0 | ISC |
| chrome-trace-event | 1.0.4 | MIT |
| chromedriver | 110.0.0 | Apache-2.0 |
| cipher-base | 1.0.7 | MIT |
| circular-json | 0.3.3 | MIT |
| clap | 1.2.3 | MIT |
| class-utils | 0.3.6 | MIT |
| clean-css | 4.2.4 | MIT |
| clean-stack | 2.2.0 | MIT |
| cli-cursor | 1.0.2 | MIT |
| cli-cursor | 2.1.0 | MIT |
| cli-spinners | 1.3.1 | MIT |
| cli-width | 2.2.1 | ISC |
| clipboard | 2.0.11 | MIT |
| cliui | 3.2.0 | ISC |
| cliui | 5.0.0 | ISC |
| clone | 1.0.4 | MIT |
| clone | 2.1.2 | MIT |
| clone-buffer | 1.0.0 | MIT |
| clone-stats | 1.0.0 | MIT |
| cloneable-readable | 1.1.3 | MIT |
| co | 4.6.0 | MIT |
| co | 3.0.6 | MIT |
| coa | 1.0.4 | [{"type":"MIT"}] |
| coa | 2.0.2 | MIT |
| coalescy | 1.0.0 | MIT |
| code-point-at | 1.1.0 | MIT |
| collection-map | 1.0.0 | MIT |
| collection-visit | 1.0.0 | MIT |
| color | 0.11.4 | MIT |
| color | 3.2.1 | MIT |
| color-convert | 1.9.3 | MIT |
| color-name | 1.1.3 | MIT |
| color-name | 1.1.4 | MIT |
| color-string | 0.3.0 | MIT |
| color-string | 1.9.1 | MIT |
| color-support | 1.1.3 | ISC |
| colormin | 1.1.2 | MIT |
| colors | 1.1.2 | MIT |
| combined-stream | 1.0.8 | MIT |
| commander | 2.20.3 | MIT |
| commander | 2.9.0 | MIT |
| commander | 4.1.1 | MIT |
| commondir | 1.0.1 | MIT |
| compare-versions | 5.0.3 | MIT |
| component-emitter | 1.3.1 | MIT |
| compressible | 2.0.18 | MIT |
| compression | 1.8.1 | MIT |
| concat-map | 0.0.1 | MIT |
| concat-stream | 1.6.2 | MIT |
| concat-with-sourcemaps | 1.1.0 | ISC |
| connect-history-api-fallback | 1.6.0 | MIT |
| console-browserify | 1.2.0 | [{"type":"MIT","url":"http://github.com/browserify/console-browserify/raw/master/LICENSE"}] |
| consolidate | 0.15.1 | MIT |
| constants-browserify | 1.0.0 | MIT |
| content-disposition | 0.5.4 | MIT |
| content-type | 1.0.5 | MIT |
| convert-source-map | 1.9.0 | MIT |
| cookie | 0.7.2 | MIT |
| cookie-signature | 1.0.7 | MIT |
| copy-concurrently | 1.0.5 | ISC |
| copy-descriptor | 0.1.1 | MIT |
| copy-props | 2.0.5 | MIT |
| copy-webpack-plugin | 4.6.0 | MIT |
| core-js | 2.6.12 | MIT |
| core-util-is | 1.0.3 | MIT |
| cosmiconfig | 5.2.1 | MIT |
| create-ecdh | 4.0.4 | MIT |
| create-hash | 1.2.0 | MIT |
| create-hmac | 1.1.7 | MIT |
| cross-spawn | 5.1.0 | MIT |
| cross-spawn | 6.0.6 | MIT |
| crypto-browserify | 3.12.1 | MIT |
| crypto-js | 4.2.0 | MIT |
| css-color-names | 0.0.4 | MIT |
| css-declaration-sorter | 4.0.1 | MIT |
| css-loader | 0.28.11 | MIT |
| css-select | 2.1.0 | BSD-2-Clause |
| css-select | 4.3.0 | BSD-2-Clause |
| css-select-base-adapter | 0.1.1 | MIT |
| css-selector-tokenizer | 0.7.3 | MIT |
| css-tree | 1.0.0-alpha.37 | MIT |
| css-tree | 1.1.3 | MIT |
| css-what | 3.4.2 | BSD-2-Clause |
| css-what | 6.2.2 | BSD-2-Clause |
| cssesc | 3.0.0 | MIT |
| cssnano | 3.10.0 | MIT |
| cssnano | 4.1.11 | MIT |
| cssnano-preset-default | 4.0.8 | MIT |
| cssnano-util-get-arguments | 4.0.0 | MIT |
| cssnano-util-get-match | 4.0.0 | MIT |
| cssnano-util-raw-cache | 4.0.1 | MIT |
| cssnano-util-same-parent | 4.0.1 | MIT |
| csso | 2.3.2 | MIT |
| csso | 4.2.0 | MIT |
| csstype | 3.2.3 | MIT |
| cyclist | 1.0.2 | MIT |
| d | 1.0.2 | ISC |
| data-uri-to-buffer | 1.2.0 | MIT |
| data-view-buffer | 1.0.2 | MIT |
| data-view-byte-length | 1.0.2 | MIT |
| data-view-byte-offset | 1.0.1 | MIT |
| de-indent | 1.0.2 | MIT |
| debug | 4.4.3 | MIT |
| debug | 2.6.9 | MIT |
| debug | 2.2.0 | MIT |
| debug | 3.2.7 | MIT |
| debug | 4.3.1 | MIT |
| decamelize | 1.2.0 | MIT |
| decode-uri-component | 0.2.2 | MIT |
| deep-eql | 0.1.3 | MIT |
| deep-equal | 1.1.2 | MIT |
| deep-is | 0.1.4 | MIT |
| deepmerge | 1.3.2 | MIT |
| deepmerge | 1.5.2 | MIT |
| default-compare | 1.0.0 | MIT |
| default-gateway | 4.2.0 | BSD-2-Clause |
| default-resolution | 2.0.0 | MIT |
| define-data-property | 1.1.4 | MIT |
| define-properties | 1.2.1 | MIT |
| define-property | 1.0.0 | MIT |
| define-property | 0.2.5 | MIT |
| define-property | 2.0.2 | MIT |
| defined | 1.0.1 | MIT |
| degenerator | 1.0.4 | MIT |
| del | 4.1.1 | MIT |
| del | 6.1.1 | MIT |
| delayed-stream | 1.0.0 | MIT |
| delegate | 3.2.0 | MIT |
| depd | 2.0.0 | MIT |
| depd | 1.1.2 | MIT |
| des.js | 1.1.0 | MIT |
| destroy | 1.2.0 | MIT |
| detect-file | 1.0.0 | MIT |
| detect-indent | 4.0.0 | MIT |
| detect-libc | 2.1.2 | Apache-2.0 |
| detect-node | 2.1.0 | MIT |
| diff | 1.4.0 | [{"type":"BSD","url":"http://github.com/kpdecker/jsdiff/blob/master/LICENSE"}] |
| diffie-hellman | 5.0.3 | MIT |
| dir-glob | 2.2.2 | MIT |
| dir-glob | 3.0.1 | MIT |
| dns-equal | 1.0.0 | MIT |
| dns-packet | 5.6.1 | MIT |
| dns-txt | 2.0.2 | MIT |
| doctrine | 2.1.0 | Apache-2.0 |
| dom-converter | 0.2.0 | MIT |
| dom-serializer | 0.2.2 | MIT |
| dom-serializer | 1.4.1 | MIT |
| domain-browser | 1.2.0 | MIT |
| domelementtype | 2.3.0 | BSD-2-Clause |
| domelementtype | 1.3.1 | BSD-2-Clause |
| domhandler | 4.3.1 | BSD-2-Clause |
| domhandler | 2.4.2 | BSD-2-Clause |
| domready | 1.0.8 | 未在package.json直接声明 |
| domutils | 1.7.0 | BSD-2-Clause |
| domutils | 2.8.0 | BSD-2-Clause |
| dot-case | 3.0.4 | MIT |
| dot-prop | 5.3.0 | MIT |
| dunder-proto | 1.0.1 | MIT |
| duplexer | 0.1.2 | MIT |
| duplexify | 3.7.1 | MIT |
| each-props | 1.3.2 | MIT |
| echarts | 5.6.0 | Apache-2.0 |
| ee-first | 1.1.1 | MIT |
| ejs | 2.5.7 | Apache-2.0 |
| ejs | 2.7.4 | Apache-2.0 |
| electron-to-chromium | 1.5.414 | ISC |
| element-ui | 2.15.10 | MIT |
| elliptic | 6.6.1 | MIT |
| emoji-regex | 7.0.3 | MIT |
| emojis-list | 3.0.0 | MIT |
| encodeurl | 2.0.0 | MIT |
| end-of-stream | 1.4.5 | MIT |
| enhanced-resolve | 4.5.0 | [{"type":"MIT","url":"http://www.opensource.org/licenses/mit-license.php"}] |
| entities | 2.2.0 | BSD-2-Clause |
| entities | 1.1.2 | BSD-2-Clause |
| errno | 0.1.8 | MIT |
| error-ex | 1.3.4 | MIT |
| error-stack-parser | 2.1.4 | MIT |
| es-abstract | 1.24.2 | MIT |
| es-abstract-get | 1.0.0 | MIT |
| es-array-method-boxes-properly | 1.0.0 | MIT |
| es-define-property | 1.0.1 | MIT |
| es-errors | 1.3.0 | MIT |
| es-object-atoms | 1.1.2 | MIT |
| es-set-tostringtag | 2.1.0 | MIT |
| es-shim-unscopables | 1.1.0 | MIT |
| es-to-primitive | 1.3.4 | MIT |
| es5-ext | 0.10.64 | ISC |
| es6-iterator | 2.0.3 | MIT |
| es6-map | 0.1.5 | MIT |
| es6-set | 0.1.6 | ISC |
| es6-symbol | 3.1.4 | ISC |
| es6-weak-map | 2.0.3 | ISC |
| escalade | 3.2.0 | MIT |
| escape-html | 1.0.3 | MIT |
| escape-string-regexp | 1.0.5 | MIT |
| escodegen | 1.14.3 | BSD-2-Clause |
| escope | 3.6.0 | BSD-2-Clause |
| eslint | 3.19.0 | MIT |
| eslint-config-standard | 10.2.1 | MIT |
| eslint-friendly-formatter | 3.0.0 | MIT |
| eslint-import-resolver-node | 0.3.10 | MIT |
| eslint-loader | 2.2.1 | MIT |
| eslint-module-utils | 2.14.0 | MIT |
| eslint-plugin-html | 3.2.2 | ISC |
| eslint-plugin-import | 2.32.0 | MIT |
| eslint-plugin-node | 5.2.1 | MIT |
| eslint-plugin-promise | 3.8.0 | ISC |
| eslint-plugin-standard | 3.1.0 | MIT |
| eslint-scope | 4.0.3 | BSD-2-Clause |
| esniff | 2.0.1 | ISC |
| espree | 3.5.4 | BSD-2-Clause |
| esprima | 3.1.3 | BSD-2-Clause |
| esprima | 4.0.1 | BSD-2-Clause |
| esprima | 2.7.3 | BSD-2-Clause |
| esquery | 1.7.0 | BSD-3-Clause |
| esrecurse | 4.3.0 | BSD-2-Clause |
| estraverse | 4.3.0 | BSD-2-Clause |
| estraverse | 5.3.0 | BSD-2-Clause |
| esutils | 2.0.3 | BSD-2-Clause |
| etag | 1.8.1 | MIT |
| event-emitter | 0.3.5 | MIT |
| eventemitter3 | 4.0.7 | MIT |
| events | 3.3.0 | MIT |
| eventsource | 2.0.2 | MIT |
| eventsource-polyfill | 0.9.6 | MIT |
| evp_bytestokey | 1.0.3 | MIT |
| execa | 1.0.0 | MIT |
| exit-hook | 1.1.1 | MIT |
| expand-brackets | 2.1.4 | MIT |
| expand-tilde | 2.0.2 | MIT |
| express | 4.22.2 | MIT |
| ext | 1.7.0 | ISC |
| extend | 3.0.2 | MIT |
| extend-shallow | 2.0.1 | MIT |
| extend-shallow | 1.1.4 | MIT |
| extend-shallow | 3.0.2 | MIT |
| extglob | 2.0.4 | MIT |
| extract-zip | 2.0.1 | BSD-2-Clause |
| fancy-log | 1.3.3 | MIT |
| fancy-log | 2.0.0 | MIT |
| fast-deep-equal | 3.1.3 | MIT |
| fast-glob | 3.3.3 | MIT |
| fast-json-stable-stringify | 2.1.0 | MIT |
| fast-levenshtein | 1.1.4 | MIT |
| fast-levenshtein | 2.0.6 | MIT |
| fastparse | 1.1.2 | MIT |
| fastq | 1.20.1 | ISC |
| faye-websocket | 0.11.4 | Apache-2.0 |
| fd-slicer | 1.1.0 | MIT |
| figgy-pudding | 3.5.2 | ISC |
| figures | 1.7.0 | MIT |
| file-entry-cache | 2.0.0 | MIT |
| file-loader | 1.1.11 | MIT |
| file-uri-to-path | 1.0.0 | MIT |
| filesize | 3.6.1 | BSD-3-Clause |
| fill-range | 4.0.0 | MIT |
| fill-range | 7.1.1 | MIT |
| finalhandler | 1.3.2 | MIT |
| find-cache-dir | 1.0.0 | MIT |
| find-cache-dir | 0.1.1 | MIT |
| find-cache-dir | 2.1.0 | MIT |
| find-cache-dir | 3.3.2 | MIT |
| find-up | 1.1.2 | MIT |
| find-up | 2.1.0 | MIT |
| find-up | 3.0.0 | MIT |
| find-up | 4.1.0 | MIT |
| findup-sync | 2.0.0 | MIT |
| findup-sync | 3.0.0 | MIT |
| findup-sync | 5.0.0 | MIT |
| fined | 1.2.0 | MIT |
| flagged-respawn | 1.0.1 | MIT |
| flat-cache | 1.3.4 | MIT |
| flatten | 1.0.3 | MIT |
| flush-write-stream | 1.1.1 | MIT |
| follow-redirects | 1.16.0 | MIT |
| for-each | 0.3.5 | MIT |
| for-in | 1.0.2 | MIT |
| for-own | 1.0.0 | MIT |
| form-data | 4.0.6 | MIT |
| forwarded | 0.2.0 | MIT |
| fragment-cache | 0.2.1 | MIT |
| fresh | 0.5.2 | MIT |
| friendly-errors-webpack-plugin | 1.7.0 | MIT |
| from2 | 2.3.0 | MIT |
| fs-minipass | 2.1.0 | ISC |
| fs-mkdirp-stream | 1.0.0 | MIT |
| fs-write-stream-atomic | 1.0.10 | ISC |
| fs.realpath | 1.0.0 | ISC |
| ftp | 0.3.10 | [{"type":"MIT","url":"http://github.com/mscdex/node-ftp/raw/master/LICENSE"}] |
| function-bind | 1.1.2 | MIT |
| function.prototype.name | 1.2.0 | MIT |
| functions-have-names | 1.2.3 | MIT |
| generate-function | 2.3.1 | MIT |
| generate-object-property | 1.2.0 | MIT |
| generator-function | 2.0.1 | MIT |
| get-caller-file | 1.0.3 | ISC |
| get-caller-file | 2.0.5 | ISC |
| get-intrinsic | 1.3.0 | MIT |
| get-proto | 1.0.1 | MIT |
| get-stream | 4.1.0 | MIT |
| get-stream | 5.2.0 | MIT |
| get-symbol-description | 1.1.0 | MIT |
| get-uri | 2.0.4 | MIT |
| get-value | 2.0.6 | MIT |
| glob | 7.2.3 | ISC |
| glob | 7.0.5 | ISC |
| glob-parent | 3.1.0 | ISC |
| glob-parent | 5.1.2 | ISC |
| glob-stream | 6.1.0 | MIT |
| glob-watcher | 5.0.5 | MIT |
| global-modules | 1.0.0 | MIT |
| global-modules | 2.0.0 | MIT |
| global-prefix | 1.0.2 | MIT |
| global-prefix | 3.0.0 | MIT |
| globals | 9.18.0 | MIT |
| globalthis | 1.0.4 | MIT |
| globby | 7.1.1 | MIT |
| globby | 6.1.0 | MIT |
| globby | 11.1.0 | MIT |
| glogg | 1.0.2 | MIT |
| glogg | 2.2.0 | MIT |
| good-listener | 1.2.2 | MIT |
| gopd | 1.2.0 | MIT |
| graceful-fs | 4.2.11 | ISC |
| graceful-readlink | 1.0.1 | MIT |
| growl | 1.9.2 | MIT |
| growly | 1.3.0 | MIT |
| gulp | 4.0.2 | MIT |
| gulp-cli | 2.3.0 | MIT |
| gulp-concat | 2.6.1 | MIT |
| gulp-load-plugins | 2.0.8 | MIT |
| gulp-replace | 0.6.1 | MIT |
| gulp-shell | 0.6.5 | MIT |
| gulplog | 1.0.0 | MIT |
| gulplog | 2.2.0 | MIT |
| gzip-size | 4.1.0 | MIT |
| handle-thing | 2.0.1 | MIT |
| has | 1.0.4 | MIT |
| has-ansi | 2.0.0 | MIT |
| has-bigints | 1.1.0 | MIT |
| has-flag | 1.0.0 | MIT |
| has-flag | 3.0.0 | MIT |
| has-flag | 4.0.0 | MIT |
| has-gulplog | 1.0.0 | MIT |
| has-property-descriptors | 1.0.2 | MIT |
| has-proto | 1.2.0 | MIT |
| has-symbols | 1.1.0 | MIT |
| has-tostringtag | 1.0.2 | MIT |
| has-value | 1.0.0 | MIT |
| has-value | 0.3.1 | MIT |
| has-values | 0.1.4 | MIT |
| has-values | 1.0.0 | MIT |
| hash-base | 3.0.5 | MIT |
| hash-base | 3.1.2 | MIT |
| hash-sum | 1.0.2 | MIT |
| hash.js | 1.1.7 | MIT |
| hasown | 2.0.4 | MIT |
| he | 1.2.0 | MIT |
| hex-color-regex | 1.1.0 | MIT |
| hmac-drbg | 1.0.1 | MIT |
| home-or-tmp | 2.0.0 | MIT |
| homedir-polyfill | 1.0.3 | MIT |
| hosted-git-info | 2.8.9 | ISC |
| hpack.js | 2.1.6 | MIT |
| hsl-regex | 1.0.0 | MIT |
| hsla-regex | 1.0.0 | MIT |
| html-comment-regex | 1.1.2 | MIT |
| html-entities | 1.4.0 | MIT |
| html-minifier-terser | 5.1.1 | MIT |
| html-webpack-plugin | 4.5.2 | MIT |
| htmlparser2 | 3.10.1 | MIT |
| htmlparser2 | 6.1.0 | MIT |
| http-deceiver | 1.2.7 | MIT |
| http-errors | 2.0.1 | MIT |
| http-errors | 1.8.1 | MIT |
| http-parser-js | 0.5.10 | MIT |
| http-proxy | 1.18.1 | MIT |
| http-proxy-agent | 1.0.0 | MIT |
| http-proxy-middleware | 0.19.1 | MIT |
| https-browserify | 1.0.0 | MIT |
| https-proxy-agent | 5.0.1 | MIT |
| https-proxy-agent | 1.0.0 | MIT |
| iconv-lite | 0.4.24 | MIT |
| icss-replace-symbols | 1.1.0 | ISC |
| icss-utils | 2.1.0 | ISC |
| ieee754 | 1.2.1 | BSD-3-Clause |
| iferr | 0.1.5 | MIT |
| ignore | 3.3.10 | MIT |
| ignore | 5.3.2 | MIT |
| image-size | 0.5.5 | MIT |
| immutable | 5.1.9 | MIT |
| import-cwd | 2.1.0 | MIT |
| import-fresh | 2.0.0 | MIT |
| import-from | 2.1.0 | MIT |
| import-local | 2.0.0 | MIT |
| imurmurhash | 0.1.4 | MIT |
| indent-string | 4.0.0 | MIT |
| indexes-of | 1.0.1 | MIT |
| infer-owner | 1.0.4 | ISC |
| inflight | 1.0.6 | ISC |
| inherits | 2.0.4 | ISC |
| inherits | 2.0.3 | ISC |
| ini | 1.3.8 | ISC |
| inquirer | 0.12.0 | MIT |
| internal-ip | 4.3.0 | MIT |
| internal-slot | 1.1.0 | MIT |
| interpret | 1.4.0 | MIT |
| invariant | 2.2.4 | MIT |
| invert-kv | 1.0.0 | MIT |
| ip | 1.0.1 | MIT |
| ip | 1.1.9 | MIT |
| ip-regex | 2.1.0 | MIT |
| ipaddr.js | 1.9.1 | MIT |
| is-absolute | 1.0.0 | MIT |
| is-absolute-url | 2.1.0 | MIT |
| is-absolute-url | 3.0.3 | MIT |
| is-accessor-descriptor | 1.0.2 | MIT |
| is-arguments | 1.2.0 | MIT |
| is-array-buffer | 3.0.5 | MIT |
| is-arrayish | 0.2.1 | MIT |
| is-arrayish | 0.3.4 | MIT |
| is-async-function | 2.1.1 | MIT |
| is-bigint | 1.1.0 | MIT |
| is-binary-path | 1.0.1 | MIT |
| is-binary-path | 2.1.0 | MIT |
| is-boolean-object | 1.2.2 | MIT |
| is-buffer | 1.1.6 | MIT |
| is-callable | 1.2.7 | MIT |
| is-color-stop | 1.1.0 | MIT |
| is-core-module | 2.16.2 | MIT |
| is-data-descriptor | 1.0.1 | MIT |
| is-data-view | 1.0.2 | MIT |
| is-date-object | 1.1.0 | MIT |
| is-descriptor | 0.1.8 | MIT |
| is-descriptor | 1.0.4 | MIT |
| is-directory | 0.3.1 | MIT |
| is-document.all | 1.0.0 | MIT |
| is-extendable | 0.1.1 | MIT |
| is-extendable | 1.0.1 | MIT |
| is-extglob | 2.1.1 | MIT |
| is-finalizationregistry | 1.1.1 | MIT |
| is-finite | 1.1.0 | MIT |
| is-fullwidth-code-point | 1.0.0 | MIT |
| is-fullwidth-code-point | 2.0.0 | MIT |
| is-generator-function | 1.1.2 | MIT |
| is-glob | 4.0.3 | MIT |
| is-glob | 3.1.0 | MIT |
| is-map | 2.0.3 | MIT |
| is-my-ip-valid | 1.0.1 | MIT |
| is-my-json-valid | 2.20.6 | MIT |
| is-negated-glob | 1.0.0 | MIT |
| is-negative-zero | 2.0.3 | MIT |
| is-number | 4.0.0 | MIT |
| is-number | 3.0.0 | MIT |
| is-number | 7.0.0 | MIT |
| is-number-object | 1.1.1 | MIT |
| is-obj | 2.0.0 | MIT |
| is-path-cwd | 2.2.0 | MIT |
| is-path-in-cwd | 2.1.0 | MIT |
| is-path-inside | 3.0.3 | MIT |
| is-path-inside | 2.1.0 | MIT |
| is-plain-obj | 1.1.0 | MIT |
| is-plain-object | 5.1.0 | MIT |
| is-plain-object | 2.0.4 | MIT |
| is-property | 1.0.2 | MIT |
| is-regex | 1.2.1 | MIT |
| is-relative | 1.0.0 | MIT |
| is-resolvable | 1.1.0 | ISC |
| is-set | 2.0.3 | MIT |
| is-shared-array-buffer | 1.0.4 | MIT |
| is-stream | 1.1.0 | MIT |
| is-string | 1.1.1 | MIT |
| is-svg | 2.1.0 | MIT |
| is-symbol | 1.1.1 | MIT |
| is-typed-array | 1.1.15 | MIT |
| is-typedarray | 1.0.0 | MIT |
| is-unc-path | 1.0.0 | MIT |
| is-url | 1.2.4 | MIT |
| is-utf8 | 0.2.1 | MIT |
| is-valid-glob | 1.0.0 | MIT |
| is-weakmap | 2.0.2 | MIT |
| is-weakref | 1.1.1 | MIT |
| is-weakset | 2.0.4 | MIT |
| is-windows | 1.0.2 | MIT |
| is-wsl | 1.1.0 | MIT |
| is2 | 2.0.1 | MIT |
| isarray | 1.0.0 | MIT |
| isarray | 0.0.1 | MIT |
| isarray | 2.0.5 | MIT |
| isexe | 2.0.0 | ISC |
| isobject | 3.0.1 | MIT |
| isobject | 2.1.0 | MIT |
| istextorbinary | 1.0.2 | {"type":"MIT"} |
| jest-worker | 26.6.2 | MIT |
| js-base64 | 2.6.4 | BSD-3-Clause |
| js-tokens | 3.0.2 | MIT |
| js-tokens | 4.0.0 | MIT |
| js-yaml | 3.15.1 | MIT |
| js-yaml | 3.7.0 | MIT |
| jsesc | 1.3.0 | MIT |
| jsesc | 0.5.0 | [{"type":"MIT","url":"http://mths.be/mit"}] |
| json-parse-better-errors | 1.0.2 | MIT |
| json-schema-traverse | 0.4.1 | MIT |
| json-stable-stringify | 1.3.0 | MIT |
| json-stable-stringify-without-jsonify | 1.0.1 | MIT |
| json3 | 3.3.2 | [{"type":"MIT","url":"http://kit.mit-license.org/"}] |
| json5 | 0.5.1 | MIT |
| json5 | 1.0.2 | MIT |
| json5 | 2.2.3 | MIT |
| jsonify | 0.0.1 | Public Domain |
| jsonpointer | 5.0.1 | MIT |
| just-debounce | 1.1.0 | MIT |
| killable | 1.0.1 | ISC |
| kind-of | 5.1.0 | MIT |
| kind-of | 1.1.0 | {"type":"MIT","url":"https://github.com/jonschlinkert/kind-of/blob/master/LICENSE"} |
| kind-of | 6.0.3 | MIT |
| kind-of | 4.0.0 | MIT |
| kind-of | 3.2.2 | MIT |
| klona | 2.0.6 | MIT |
| last-call-webpack-plugin | 2.1.2 | MIT |
| last-run | 1.1.1 | MIT |
| lazystream | 1.0.1 | MIT |
| lcid | 1.0.0 | MIT |
| lead | 1.0.0 | MIT |
| levn | 0.3.0 | MIT |
| liftoff | 3.1.0 | MIT |
| load-json-file | 1.1.0 | MIT |
| loader-fs-cache | 1.0.3 | MIT |
| loader-runner | 2.4.0 | MIT |
| loader-utils | 1.4.2 | MIT |
| loader-utils | 2.0.4 | MIT |
| locate-path | 2.0.0 | MIT |
| locate-path | 3.0.0 | MIT |
| locate-path | 5.0.0 | MIT |
| lodash | 4.18.1 | MIT |
| lodash._arraycopy | 3.0.0 | MIT |
| lodash._arrayeach | 3.0.0 | MIT |
| lodash._baseassign | 3.2.0 | MIT |
| lodash._baseclone | 3.3.0 | MIT |
| lodash._baseclone | 4.5.7 | MIT |
| lodash._basecopy | 3.0.1 | MIT |
| lodash._basecreate | 3.0.3 | MIT |
| lodash._basefor | 3.0.3 | MIT |
| lodash._bindcallback | 3.0.1 | MIT |
| lodash._getnative | 3.9.1 | MIT |
| lodash._isiterateecall | 3.0.9 | MIT |
| lodash._reinterpolate | 3.0.0 | MIT |
| lodash._stack | 4.1.3 | MIT |
| lodash.camelcase | 4.3.0 | MIT |
| lodash.clone | 3.0.3 | MIT |
| lodash.create | 3.1.1 | MIT |
| lodash.defaultsdeep | 4.3.2 | MIT |
| lodash.isarguments | 3.1.0 | MIT |
| lodash.isarray | 3.0.4 | MIT |
| lodash.isplainobject | 4.0.6 | MIT |
| lodash.keys | 3.1.2 | MIT |
| lodash.keysin | 4.2.0 | MIT |
| lodash.memoize | 4.1.2 | MIT |
| lodash.mergewith | 4.6.2 | MIT |
| lodash.rest | 4.0.5 | MIT |
| lodash.template | 4.18.1 | MIT |
| lodash.templatesettings | 4.2.0 | MIT |
| lodash.uniq | 4.5.0 | MIT |
| log-symbols | 2.2.0 | MIT |
| loglevel | 1.9.2 | MIT |
| loose-envify | 1.4.0 | MIT |
| lower-case | 2.0.2 | MIT |
| lru-cache | 4.1.5 | ISC |
| lru-cache | 5.1.1 | ISC |
| lru-cache | 6.0.0 | ISC |
| lru-cache | 2.6.5 | ISC |
| make-dir | 1.3.0 | MIT |
| make-dir | 2.1.0 | MIT |
| make-dir | 3.1.0 | MIT |
| make-iterator | 1.0.1 | MIT |
| map-cache | 0.2.2 | MIT |
| map-visit | 1.0.0 | MIT |
| matchdep | 2.0.0 | MIT |
| math-expression-evaluator | 1.4.0 | MIT |
| math-intrinsics | 1.1.0 | MIT |
| md5.js | 1.3.5 | MIT |
| mdn-data | 2.0.4 | CC0-1.0 |
| mdn-data | 2.0.14 | CC0-1.0 |
| media-typer | 0.3.0 | MIT |
| memory-fs | 0.5.0 | MIT |
| memory-fs | 0.4.1 | MIT |
| merge-descriptors | 1.0.3 | MIT |
| merge-options | 1.0.1 | MIT |
| merge-source-map | 1.1.0 | MIT |
| merge-stream | 2.0.0 | MIT |
| merge2 | 1.4.1 | MIT |
| methods | 1.1.2 | MIT |
| micromatch | 3.1.10 | MIT |
| micromatch | 4.0.8 | MIT |
| micromatch | 3.1.0 | MIT |
| miller-rabin | 4.0.1 | MIT |
| mime | 1.3.6 | MIT |
| mime | 1.6.0 | MIT |
| mime | 2.6.0 | MIT |
| mime-db | 1.54.0 | MIT |
| mime-db | 1.52.0 | MIT |
| mime-types | 2.1.35 | MIT |
| mimic-fn | 1.2.0 | MIT |
| mini-css-extract-plugin | 1.6.2 | MIT |
| minimalistic-assert | 1.0.1 | ISC |
| minimalistic-crypto-utils | 1.0.1 | MIT |
| minimatch | 10.2.6 | BlueOak-1.0.0 |
| minimatch | 3.1.5 | ISC |
| minimatch | 3.0.3 | ISC |
| minimist | 1.2.8 | MIT |
| minimist | 0.0.10 | MIT |
| minimist | 0.0.8 | MIT |
| minipass | 3.3.6 | ISC |
| minipass | 5.0.0 | ISC |
| minipass-collect | 1.0.2 | ISC |
| minipass-flush | 1.0.7 | BlueOak-1.0.0 |
| minipass-pipeline | 1.2.4 | ISC |
| minizlib | 2.1.2 | MIT |
| mississippi | 2.0.0 | BSD-2-Clause |
| mississippi | 3.0.0 | BSD-2-Clause |
| mitt | 1.1.2 | MIT |
| mixin-deep | 1.3.2 | MIT |
| mkdirp | 1.0.4 | MIT |
| mkdirp | 0.5.6 | MIT |
| mkdirp | 0.5.1 | MIT |
| mkpath | 1.0.0 | MIT |
| mocha-nightwatch | 3.2.2 | MIT |
| move-concurrently | 1.0.1 | ISC |
| ms | 0.7.1 | 未在package.json直接声明 |
| ms | 2.0.0 | MIT |
| ms | 2.1.3 | MIT |
| ms | 2.1.2 | MIT |
| multicast-dns | 7.2.5 | MIT |
| multicast-dns-service-types | 1.1.0 | MIT |
| mute-stdout | 1.0.1 | MIT |
| mute-stream | 0.0.5 | ISC |
| nanoid | 3.3.18 | MIT |
| nanomatch | 1.2.13 | MIT |
| natural-compare | 1.4.0 | MIT |
| negotiator | 0.6.3 | MIT |
| negotiator | 0.6.4 | MIT |
| neo-async | 2.6.2 | MIT |
| netmask | 1.0.6 | MIT |
| next-tick | 1.1.0 | ISC |
| nice-try | 1.0.5 | MIT |
| nightwatch | 0.9.21 | MIT |
| no-case | 3.0.4 | MIT |
| node-addon-api | 7.1.1 | MIT |
| node-exports-info | 1.6.2 | MIT |
| node-forge | 0.10.0 | (BSD-3-Clause OR GPL-2.0) |
| node-gyp-build | 4.8.4 | MIT |
| node-libs-browser | 2.2.1 | MIT |
| node-notifier | 5.4.5 | MIT |
| node-releases | 2.0.53 | MIT |
| normalize-package-data | 2.5.0 | BSD-2-Clause |
| normalize-path | 2.1.1 | MIT |
| normalize-path | 3.0.0 | MIT |
| normalize-range | 0.1.2 | MIT |
| normalize-url | 1.9.1 | MIT |
| normalize-url | 3.3.0 | MIT |
| normalize-wheel | 1.0.1 | BSD-3-Clause |
| now-and-later | 2.0.1 | MIT |
| npm-run-path | 2.0.2 | MIT |
| nth-check | 1.0.2 | BSD-2-Clause |
| nth-check | 2.1.1 | BSD-2-Clause |
| num2fraction | 1.2.2 | MIT |
| number-is-nan | 1.0.1 | MIT |
| object-assign | 4.1.1 | MIT |
| object-copy | 0.1.0 | MIT |
| object-hash | 1.3.1 | MIT |
| object-inspect | 1.13.4 | MIT |
| object-is | 1.1.6 | MIT |
| object-keys | 1.1.1 | MIT |
| object-visit | 1.0.1 | MIT |
| object.assign | 4.1.7 | MIT |
| object.defaults | 1.1.0 | MIT |
| object.entries | 1.1.9 | MIT |
| object.fromentries | 2.0.8 | MIT |
| object.getownpropertydescriptors | 2.1.9 | MIT |
| object.groupby | 1.0.3 | MIT |
| object.map | 1.0.1 | MIT |
| object.pick | 1.3.0 | MIT |
| object.reduce | 1.0.1 | MIT |
| object.values | 1.2.1 | MIT |
| obuf | 1.1.2 | MIT |
| on-finished | 2.4.1 | MIT |
| on-headers | 1.1.0 | MIT |
| once | 1.4.0 | ISC |
| onetime | 1.1.0 | MIT |
| onetime | 2.0.1 | MIT |
| opener | 1.5.2 | (WTFPL OR MIT) |
| opn | 5.5.0 | MIT |
| optimist | 0.6.1 | MIT/X11 |
| optimize-css-assets-webpack-plugin | 3.2.1 | MIT |
| optionator | 0.8.3 | MIT |
| ora | 1.4.0 | MIT |
| ordered-read-streams | 1.0.1 | MIT |
| os-browserify | 0.3.0 | MIT |
| os-homedir | 1.0.2 | MIT |
| os-locale | 1.4.0 | MIT |
| os-tmpdir | 1.0.2 | MIT |
| own-keys | 1.0.2 | MIT |
| p-finally | 1.0.0 | MIT |
| p-limit | 1.3.0 | MIT |
| p-limit | 2.3.0 | MIT |
| p-limit | 3.1.0 | MIT |
| p-locate | 2.0.0 | MIT |
| p-locate | 3.0.0 | MIT |
| p-locate | 4.1.0 | MIT |
| p-map | 4.0.0 | MIT |
| p-map | 2.1.0 | MIT |
| p-retry | 3.0.1 | MIT |
| p-try | 1.0.0 | MIT |
| p-try | 2.2.0 | MIT |
| pac-proxy-agent | 1.1.0 | MIT |
| pac-resolver | 2.0.0 | MIT |
| pako | 1.0.11 | (MIT AND Zlib) |
| parallel-transform | 1.2.0 | MIT |
| param-case | 3.0.4 | MIT |
| parse-asn1 | 5.1.9 | ISC |
| parse-filepath | 1.0.2 | MIT |
| parse-json | 4.0.0 | MIT |
| parse-json | 2.2.0 | MIT |
| parse-node-version | 1.0.1 | MIT |
| parse-passwd | 1.0.0 | MIT |
| parseurl | 1.3.3 | MIT |
| pascal-case | 3.1.2 | MIT |
| pascalcase | 0.1.1 | MIT |
| path-browserify | 0.0.1 | MIT |
| path-dirname | 1.0.2 | MIT |
| path-exists | 2.1.0 | MIT |
| path-exists | 4.0.0 | MIT |
| path-exists | 3.0.0 | MIT |
| path-is-absolute | 1.0.1 | MIT |
| path-is-inside | 1.0.2 | (WTFPL OR MIT) |
| path-key | 2.0.1 | MIT |
| path-parse | 1.0.7 | MIT |
| path-root | 0.1.1 | MIT |
| path-root-regex | 0.1.2 | MIT |
| path-to-regexp | 0.1.13 | MIT |
| path-type | 3.0.0 | MIT |
| path-type | 4.0.0 | MIT |
| path-type | 1.1.0 | MIT |
| pbkdf2 | 3.1.6 | MIT |
| pend | 1.2.0 | MIT |
| picocolors | 0.2.1 | ISC |
| picocolors | 1.1.1 | ISC |
| picomatch | 4.0.7 | MIT |
| picomatch | 2.3.2 | MIT |
| pify | 4.0.1 | MIT |
| pify | 2.3.0 | MIT |
| pify | 3.0.0 | MIT |
| pinkie | 2.0.4 | MIT |
| pinkie-promise | 2.0.1 | MIT |
| pkg-dir | 1.0.0 | MIT |
| pkg-dir | 2.0.0 | MIT |
| pkg-dir | 3.0.0 | MIT |
| pkg-dir | 4.2.0 | MIT |
| plugin-error | 0.1.2 | MIT |
| pluralize | 1.2.1 | MIT |
| portfinder | 1.0.38 | MIT |
| posix-character-classes | 0.1.1 | MIT |
| possible-typed-array-names | 1.1.0 | MIT |
| postcss | 8.5.26 | MIT |
| postcss | 7.0.39 | MIT |
| postcss | 5.2.18 | MIT |
| postcss | 6.0.23 | MIT |
| postcss-calc | 7.0.5 | MIT |
| postcss-calc | 5.3.1 | MIT |
| postcss-colormin | 4.0.3 | MIT |
| postcss-colormin | 2.2.2 | MIT |
| postcss-convert-values | 4.0.1 | MIT |
| postcss-convert-values | 2.6.1 | MIT |
| postcss-discard-comments | 4.0.2 | MIT |
| postcss-discard-comments | 2.0.4 | MIT |
| postcss-discard-duplicates | 4.0.2 | MIT |
| postcss-discard-duplicates | 2.1.0 | MIT |
| postcss-discard-empty | 4.0.1 | MIT |
| postcss-discard-empty | 2.1.0 | MIT |
| postcss-discard-overridden | 4.0.1 | MIT |
| postcss-discard-overridden | 0.1.1 | MIT |
| postcss-discard-unused | 2.2.3 | MIT |
| postcss-filter-plugins | 2.0.3 | MIT |
| postcss-import | 11.1.0 | MIT |
| postcss-load-config | 2.1.2 | MIT |
| postcss-loader | 2.1.6 | MIT |
| postcss-merge-idents | 2.1.7 | MIT |
| postcss-merge-longhand | 4.0.11 | MIT |
| postcss-merge-longhand | 2.0.2 | MIT |
| postcss-merge-rules | 4.0.3 | MIT |
| postcss-merge-rules | 2.1.2 | MIT |
| postcss-message-helpers | 2.0.0 | MIT |
| postcss-minify-font-values | 4.0.2 | MIT |
| postcss-minify-font-values | 1.0.5 | MIT |
| postcss-minify-gradients | 4.0.2 | MIT |
| postcss-minify-gradients | 1.0.5 | MIT |
| postcss-minify-params | 4.0.2 | MIT |
| postcss-minify-params | 1.2.2 | MIT |
| postcss-minify-selectors | 4.0.2 | MIT |
| postcss-minify-selectors | 2.1.1 | MIT |
| postcss-modules-extract-imports | 1.2.1 | ISC |
| postcss-modules-local-by-default | 1.2.0 | MIT |
| postcss-modules-scope | 1.1.0 | ISC |
| postcss-modules-values | 1.3.0 | ISC |
| postcss-normalize-charset | 4.0.1 | MIT |
| postcss-normalize-charset | 1.1.1 | MIT |
| postcss-normalize-display-values | 4.0.2 | MIT |
| postcss-normalize-positions | 4.0.2 | MIT |
| postcss-normalize-repeat-style | 4.0.2 | MIT |
| postcss-normalize-string | 4.0.2 | MIT |
| postcss-normalize-timing-functions | 4.0.2 | MIT |
| postcss-normalize-unicode | 4.0.1 | MIT |
| postcss-normalize-url | 4.0.1 | MIT |
| postcss-normalize-url | 3.0.8 | MIT |
| postcss-normalize-whitespace | 4.0.2 | MIT |
| postcss-ordered-values | 4.1.2 | MIT |
| postcss-ordered-values | 2.2.3 | MIT |
| postcss-prefix-selector | 1.16.1 | MIT |
| postcss-reduce-idents | 2.4.0 | MIT |
| postcss-reduce-initial | 4.0.3 | MIT |
| postcss-reduce-initial | 1.0.1 | MIT |
| postcss-reduce-transforms | 4.0.2 | MIT |
| postcss-reduce-transforms | 1.0.4 | MIT |
| postcss-selector-parser | 6.1.4 | MIT |
| postcss-selector-parser | 2.2.3 | MIT |
| postcss-selector-parser | 3.1.2 | MIT |
| postcss-svgo | 4.0.3 | MIT |
| postcss-svgo | 2.1.6 | MIT |
| postcss-unique-selectors | 4.0.1 | MIT |
| postcss-unique-selectors | 2.0.2 | MIT |
| postcss-value-parser | 3.3.1 | MIT |
| postcss-value-parser | 4.2.0 | MIT |
| postcss-zindex | 2.2.0 | MIT |
| posthtml | 0.9.2 | MIT |
| posthtml-parser | 0.2.1 | MIT |
| posthtml-rename-id | 1.0.12 | MIT |
| posthtml-render | 1.4.0 | MIT |
| posthtml-svg-mode | 1.0.3 | MIT |
| prelude-ls | 1.1.2 | [{"type":"MIT","url":"https://raw.github.com/gkz/prelude-ls/master/LICENSE"}] |
| prepend-http | 1.0.4 | MIT |
| prettier | 2.8.8 | MIT |
| pretty-error | 2.1.2 | MIT |
| pretty-hrtime | 1.0.3 | MIT |
| private | 0.1.8 | MIT |
| process | 0.11.10 | MIT |
| process-nextick-args | 2.0.1 | MIT |
| progress | 1.1.8 | 未在package.json直接声明 |
| promise-inflight | 1.0.1 | ISC |
| proxy-addr | 2.0.7 | MIT |
| proxy-agent | 2.0.0 | MIT |
| proxy-from-env | 2.1.0 | MIT |
| proxy-from-env | 1.1.0 | MIT |
| prr | 1.0.1 | MIT |
| pseudomap | 1.0.2 | ISC |
| public-encrypt | 4.0.3 | MIT |
| pump | 3.0.4 | MIT |
| pump | 2.0.1 | MIT |
| pumpify | 1.5.1 | MIT |
| punycode | 1.4.1 | MIT |
| punycode | 2.3.1 | MIT |
| q | 1.5.1 | MIT |
| q | 1.4.1 | {"type":"MIT","url":"http://github.com/kriskowal/q/raw/master/LICENSE"} |
| qs | 6.15.3 | BSD-3-Clause |
| query-string | 4.3.4 | MIT |
| querystring-es3 | 0.2.1 | [{"type":"MIT","url":"https://github.com/Gozala/enchain/License.md"}] |
| querystringify | 2.2.0 | MIT |
| queue-microtask | 1.2.3 | MIT |
| randombytes | 2.1.0 | MIT |
| randomfill | 1.0.4 | MIT |
| range-parser | 1.2.1 | MIT |
| range-parser | 1.3.0 | MIT |
| raw-body | 2.5.3 | MIT |
| read-cache | 1.0.2 | MIT |
| read-pkg | 1.1.0 | MIT |
| read-pkg-up | 1.0.1 | MIT |
| readable-stream | 2.3.8 | MIT |
| readable-stream | 1.1.14 | MIT |
| readable-stream | 3.6.2 | MIT |
| readdirp | 2.2.1 | MIT |
| readdirp | 3.6.0 | MIT |
| readdirp | 5.1.1 | MIT |
| readline2 | 1.0.1 | MIT |
| rechoir | 0.6.2 | [{"type":"MIT","url":"https://github.com/tkellen/node-rechoir/blob/master/LICENSE"}] |
| reduce-css-calc | 1.3.0 | MIT |
| reduce-function-call | 1.0.3 | MIT |
| reflect.getprototypeof | 1.0.10 | MIT |
| regenerate | 1.4.2 | MIT |
| regenerator-runtime | 0.10.5 | MIT |
| regenerator-runtime | 0.11.1 | MIT |
| regenerator-transform | 0.10.1 | BSD |
| regex-not | 1.0.2 | MIT |
| regexp.prototype.flags | 1.5.4 | MIT |
| regexpu-core | 2.0.0 | MIT |
| regjsgen | 0.2.0 | MIT |
| regjsparser | 0.1.5 | BSD |
| relateurl | 0.2.7 | MIT |
| remove-bom-buffer | 3.0.0 | MIT |
| remove-bom-stream | 1.2.0 | MIT |
| remove-trailing-separator | 1.1.0 | ISC |
| renderkid | 2.0.7 | MIT |
| repeat-element | 1.1.4 | MIT |
| repeat-string | 1.6.1 | MIT |
| repeating | 2.0.1 | MIT |
| replace-ext | 1.0.1 | MIT |
| replace-homedir | 1.0.0 | MIT |
| replacestream | 4.0.3 | BSD-3-Clause |
| require-directory | 2.1.1 | MIT |
| require-main-filename | 1.0.1 | ISC |
| require-main-filename | 2.0.0 | ISC |
| require-uncached | 1.0.3 | MIT |
| requires-port | 1.0.0 | MIT |
| resize-observer-polyfill | 1.5.1 | MIT |
| resolve | 2.0.0-next.7 | MIT |
| resolve | 1.22.12 | MIT |
| resolve-cwd | 2.0.0 | MIT |
| resolve-dir | 1.0.1 | MIT |
| resolve-from | 3.0.0 | MIT |
| resolve-from | 1.0.1 | MIT |
| resolve-options | 1.1.0 | MIT |
| resolve-url | 0.2.1 | MIT |
| restore-cursor | 1.0.1 | MIT |
| restore-cursor | 2.0.0 | MIT |
| ret | 0.1.15 | MIT |
| retry | 0.12.0 | MIT |
| reusify | 1.1.0 | MIT |
| rgb-regex | 1.0.1 | MIT |
| rgba-regex | 1.0.0 | MIT |
| rimraf | 3.0.2 | ISC |
| rimraf | 2.7.1 | ISC |
| rimraf | 2.6.3 | ISC |
| ripemd160 | 2.0.3 | MIT |
| run-async | 0.1.0 | MIT |
| run-parallel | 1.2.0 | MIT |
| run-queue | 1.0.3 | ISC |
| rx-lite | 3.1.2 | [{"type":"Apache License, Version 2.0","url":"http://www.apache.org/licenses/LICENSE-2.0.html"}] |
| safe-array-concat | 1.1.4 | MIT |
| safe-buffer | 5.2.1 | MIT |
| safe-buffer | 5.1.2 | MIT |
| safe-push-apply | 1.0.0 | MIT |
| safe-regex | 1.1.0 | MIT |
| safe-regex-test | 1.1.0 | MIT |
| safer-buffer | 2.1.2 | MIT |
| sass | 1.103.1 | MIT |
| sass-loader | 10.2.0 | MIT |
| sax | 1.2.4 | ISC |
| schema-utils | 0.4.7 | MIT |
| schema-utils | 3.3.0 | MIT |
| schema-utils | 1.0.0 | MIT |
| select | 1.1.2 | MIT |
| select-hose | 2.0.0 | MIT |
| selenium-server | 3.141.59 | BSD-3-Clause |
| selfsigned | 1.10.14 | MIT |
| semver | 7.8.5 | ISC |
| semver | 5.0.3 | ISC |
| semver | 5.7.2 | ISC |
| semver | 6.3.1 | ISC |
| semver | 5.3.0 | ISC |
| semver-greatest-satisfied-range | 1.1.0 | MIT |
| send | 0.19.2 | MIT |
| serialize-javascript | 1.9.1 | BSD-3-Clause |
| serialize-javascript | 4.0.0 | BSD-3-Clause |
| serialize-javascript | 5.0.1 | BSD-3-Clause |
| serve-index | 1.9.2 | MIT |
| serve-static | 1.16.3 | MIT |
| set-blocking | 2.0.0 | ISC |
| set-function-length | 1.2.2 | MIT |
| set-function-name | 2.0.2 | MIT |
| set-proto | 1.0.0 | MIT |
| set-value | 2.0.1 | MIT |
| setimmediate | 1.0.5 | MIT |
| setprototypeof | 1.2.0 | ISC |
| sha.js | 2.4.12 | (MIT AND BSD-3-Clause) |
| shebang-command | 1.2.0 | MIT |
| shebang-regex | 1.0.0 | MIT |
| shelljs | 0.7.8 | BSD-3-Clause |
| shelljs | 0.8.5 | BSD-3-Clause |
| shellwords | 0.1.1 | MIT |
| side-channel | 1.1.1 | MIT |
| side-channel-list | 1.0.1 | MIT |
| side-channel-map | 1.0.1 | MIT |
| side-channel-weakmap | 1.0.2 | MIT |
| signal-exit | 3.0.7 | ISC |
| simple-swizzle | 0.2.4 | MIT |
| slash | 1.0.0 | MIT |
| slash | 3.0.0 | MIT |
| slice-ansi | 0.0.4 | MIT |
| smart-buffer | 1.1.15 | MIT |
| snapdragon | 0.8.2 | MIT |
| snapdragon-node | 2.1.1 | MIT |
| snapdragon-util | 3.0.1 | MIT |
| sockjs | 0.3.24 | MIT |
| sockjs-client | 1.6.1 | MIT |
| socks | 1.1.10 | MIT |
| socks-proxy-agent | 2.1.1 | MIT |
| sort-keys | 1.1.2 | MIT |
| source-list-map | 2.0.1 | MIT |
| source-map | 0.6.1 | BSD-3-Clause |
| source-map | 0.7.6 | BSD-3-Clause |
| source-map | 0.5.7 | BSD-3-Clause |
| source-map-js | 1.2.1 | BSD-3-Clause |
| source-map-resolve | 0.5.3 | MIT |
| source-map-support | 0.4.18 | MIT |
| source-map-support | 0.5.21 | MIT |
| source-map-url | 0.4.1 | MIT |
| sparkles | 1.0.1 | MIT |
| sparkles | 2.1.0 | MIT |
| spdx-correct | 3.2.0 | Apache-2.0 |
| spdx-exceptions | 2.5.0 | CC-BY-3.0 |
| spdx-expression-parse | 3.0.1 | MIT |
| spdx-license-ids | 3.0.23 | CC0-1.0 |
| spdy | 4.0.2 | MIT |
| spdy-transport | 3.0.0 | MIT |
| split-string | 3.1.0 | MIT |
| sprintf-js | 1.0.3 | BSD-3-Clause |
| ssri | 5.3.0 | ISC |
| ssri | 6.0.2 | ISC |
| ssri | 8.0.1 | ISC |
| stable | 0.1.8 | MIT |
| stack-trace | 0.0.10 | MIT |
| stackframe | 1.3.4 | MIT |
| static-extend | 0.1.2 | MIT |
| statuses | 2.0.2 | MIT |
| statuses | 1.5.0 | MIT |
| stompjs | 2.3.3 | Apache-2.0 |
| stop-iteration-iterator | 1.1.0 | MIT |
| stream-browserify | 2.0.2 | MIT |
| stream-each | 1.2.3 | MIT |
| stream-exhaust | 1.0.2 | MIT |
| stream-http | 2.8.3 | MIT |
| stream-shift | 1.0.3 | MIT |
| strict-uri-encode | 1.1.0 | MIT |
| string_decoder | 1.3.0 | MIT |
| string_decoder | 0.10.31 | MIT |
| string_decoder | 1.1.1 | MIT |
| string-width | 1.0.2 | MIT |
| string-width | 3.1.0 | MIT |
| string-width | 2.1.1 | MIT |
| string.prototype.trim | 1.2.11 | MIT |
| string.prototype.trimend | 1.0.10 | MIT |
| string.prototype.trimstart | 1.0.8 | MIT |
| strip-ansi | 3.0.1 | MIT |
| strip-ansi | 5.2.0 | MIT |
| strip-ansi | 4.0.0 | MIT |
| strip-bom | 3.0.0 | MIT |
| strip-bom | 2.0.0 | MIT |
| strip-eof | 1.0.0 | MIT |
| strip-json-comments | 2.0.1 | MIT |
| stylehacks | 4.0.3 | MIT |
| supports-color | 2.0.0 | MIT |
| supports-color | 5.5.0 | MIT |
| supports-color | 3.1.2 | MIT |
| supports-color | 6.1.0 | MIT |
| supports-color | 7.2.0 | MIT |
| supports-color | 3.2.3 | MIT |
| supports-preserve-symlinks-flag | 1.0.0 | MIT |
| sver-compat | 1.5.0 | MIT |
| svg-baker | 1.7.0 | MIT |
| svg-baker-runtime | 1.4.7 | MIT |
| svg-sprite-loader | 6.0.11 | MIT |
| svgo | 0.7.2 | MIT |
| svgo | 1.3.2 | MIT |
| table | 3.8.3 | BSD-3-Clause |
| tapable | 1.1.3 | MIT |
| tar | 6.2.1 | ISC |
| tcp-port-used | 1.0.3 | MIT |
| terser | 4.8.1 | BSD-2-Clause |
| terser | 5.50.0 | BSD-2-Clause |
| terser-webpack-plugin | 1.4.6 | MIT |
| terser-webpack-plugin | 4.2.3 | MIT |
| text-table | 0.2.0 | MIT |
| textextensions | 1.0.2 | MIT |
| throttle-debounce | 1.1.0 | MIT |
| through | 2.3.8 | MIT |
| through2 | 2.0.5 | MIT |
| through2-filter | 3.0.0 | MIT |
| thunkify | 2.1.2 | MIT |
| thunky | 1.1.0 | MIT |
| time-stamp | 1.1.0 | MIT |
| timers-browserify | 2.0.12 | MIT |
| timsort | 0.3.0 | MIT |
| tiny-cookie | 1.0.1 | [{"type":"MIT","url":"https://github.com/Alex1990/tiny-cookie/blob/master/LICENSE"}] |
| tiny-emitter | 2.1.0 | MIT |
| to-absolute-glob | 2.0.2 | MIT |
| to-arraybuffer | 1.0.1 | MIT |
| to-buffer | 1.2.2 | MIT |
| to-fast-properties | 2.0.0 | MIT |
| to-fast-properties | 1.0.3 | MIT |
| to-object-path | 0.3.0 | MIT |
| to-regex | 3.0.2 | MIT |
| to-regex-range | 2.1.1 | MIT |
| to-regex-range | 5.0.1 | MIT |
| to-through | 2.0.0 | MIT |
| toidentifier | 1.0.1 | MIT |
| traverse | 0.6.11 | MIT |
| trim-right | 1.0.1 | MIT |
| tryer | 1.0.1 | MIT |
| tsconfig-paths | 3.15.0 | MIT |
| tslib | 2.8.1 | 0BSD |
| tslib | 2.3.0 | 0BSD |
| tty-browserify | 0.0.0 | MIT |
| type | 2.7.3 | ISC |
| type-check | 0.3.2 | MIT |
| type-detect | 0.1.1 | MIT |
| type-is | 1.6.18 | MIT |
| typed-array-buffer | 1.0.3 | MIT |
| typed-array-byte-length | 1.0.3 | MIT |
| typed-array-byte-offset | 1.0.4 | MIT |
| typed-array-length | 1.0.8 | MIT |
| typedarray | 0.0.6 | MIT |
| typedarray-to-buffer | 3.1.5 | MIT |
| typedarray.prototype.slice | 1.0.5 | MIT |
| unbox-primitive | 1.1.0 | MIT |
| unc-path-regex | 0.1.2 | MIT |
| undertaker | 1.3.0 | MIT |
| undertaker-registry | 1.0.1 | MIT |
| undici-types | 8.3.0 | MIT |
| unidecode | 0.1.8 | [{"type":"BSD","url":"http://github.com/FGRibreau/node-unidecode/blob/master/LICENSE"}] |
| union-value | 1.0.1 | MIT |
| uniq | 1.0.1 | MIT |
| uniqs | 2.0.0 | MIT |
| unique-filename | 1.1.1 | ISC |
| unique-slug | 2.0.2 | ISC |
| unique-stream | 2.4.0 | MIT |
| unpipe | 1.0.0 | MIT |
| unquote | 1.1.1 | MIT |
| unset-value | 1.0.0 | MIT |
| upath | 1.2.0 | MIT |
| update-browserslist-db | 1.3.1 | MIT |
| uri-js | 4.4.1 | BSD-2-Clause |
| urix | 0.1.0 | MIT |
| url | 0.11.4 | MIT |
| url-loader | 0.5.9 | MIT |
| url-parse | 1.5.10 | MIT |
| url-slug | 2.0.0 | MIT |
| use | 3.1.1 | MIT |
| user-home | 2.0.0 | MIT |
| utf-8-validate | 5.0.10 | MIT |
| util | 0.10.4 | MIT |
| util | 0.11.1 | MIT |
| util-deprecate | 1.0.2 | MIT |
| util.promisify | 1.0.0 | MIT |
| util.promisify | 1.0.1 | MIT |
| utila | 0.4.0 | MIT |
| utils-merge | 1.0.1 | MIT |
| uuid | 8.3.2 | MIT |
| uuid | 3.4.0 | MIT |
| v8-compile-cache | 2.4.0 | MIT |
| v8flags | 3.2.0 | MIT |
| validate-npm-package-license | 3.0.4 | Apache-2.0 |
| value-or-function | 3.0.0 | MIT |
| vary | 1.1.2 | MIT |
| vendors | 1.0.4 | MIT |
| vinyl | 2.2.1 | MIT |
| vinyl-fs | 3.0.3 | MIT |
| vinyl-sourcemap | 1.1.0 | MIT |
| vm-browserify | 1.1.2 | MIT |
| vue | 2.7.16 | MIT |
| vue-baidu-analytics | 2.2.0 | MIT |
| vue-clipboard2 | 0.3.3 | MIT |
| vue-cookie | 1.1.4 | MIT |
| vue-hot-reload-api | 2.3.4 | MIT |
| vue-loader | 15.11.1 | MIT |
| vue-router | 3.6.5 | MIT |
| vue-style-loader | 4.1.3 | MIT |
| vue-style-loader | 3.1.2 | MIT |
| vue-template-compiler | 2.7.16 | MIT |
| vue-template-es2015-compiler | 1.9.1 | MIT |
| vuex | 3.6.2 | MIT |
| watchpack | 1.7.5 | MIT |
| watchpack-chokidar2 | 2.0.1 | MIT |
| wbuf | 1.7.3 | MIT |
| webpack | 4.47.0 | MIT |
| webpack-bundle-analyzer | 2.13.1 | MIT |
| webpack-cli | 3.3.12 | MIT |
| webpack-dev-middleware | 3.7.3 | MIT |
| webpack-dev-server | 3.11.3 | MIT |
| webpack-log | 2.0.0 | MIT |
| webpack-merge | 4.2.2 | MIT |
| webpack-sources | 1.4.3 | MIT |
| websocket | 1.0.35 | Apache-2.0 |
| websocket-driver | 0.7.5 | Apache-2.0 |
| websocket-extensions | 0.1.4 | Apache-2.0 |
| whet.extend | 0.9.9 | MIT |
| which | 1.3.1 | ISC |
| which-boxed-primitive | 1.1.1 | MIT |
| which-builtin-type | 1.2.1 | MIT |
| which-collection | 1.0.2 | MIT |
| which-module | 1.0.0 | ISC |
| which-module | 2.0.1 | ISC |
| which-typed-array | 1.1.22 | MIT |
| word-wrap | 1.2.5 | MIT |
| wordwrap | 0.0.3 | MIT |
| worker-farm | 1.7.0 | MIT |
| wrap-ansi | 2.1.0 | MIT |
| wrap-ansi | 5.1.0 | MIT |
| wrappy | 1.0.2 | ISC |
| write | 0.2.1 | MIT |
| ws | 4.1.0 | MIT |
| ws | 6.2.6 | MIT |
| xregexp | 2.0.0 | MIT |
| xtend | 4.0.2 | MIT |
| y18n | 4.0.3 | ISC |
| y18n | 3.2.2 | ISC |
| yaeti | 0.0.6 | MIT |
| yallist | 2.1.2 | ISC |
| yallist | 3.1.1 | ISC |
| yallist | 4.0.0 | ISC |
| yargs | 7.1.2 | MIT |
| yargs | 13.3.2 | MIT |
| yargs-parser | 13.1.2 | ISC |
| yargs-parser | 5.0.1 | ISC |
| yauzl | 2.10.0 | MIT |
| yocto-queue | 0.1.0 | MIT |
| zrender | 5.6.1 | BSD-3-Clause |

## Java测试类路径全部JAR声明

| JAR | 同名POM直接许可证声明 |
|---|---|
| spring-boot-starter-undertow-3.2.5.jar | Apache License, Version 2.0 |
| undertow-core-2.3.12.Final.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jboss-logging-3.5.3.Final.jar | Apache License 2.0 |
| xnio-api-3.8.8.Final.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| wildfly-common-1.5.4.Final.jar | Apache License 2.0 |
| wildfly-client-config-1.0.1.Final.jar | Apache License 2.0 |
| xnio-nio-3.8.8.Final.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jboss-threads-3.5.0.Final.jar | Apache License 2.0 |
| undertow-servlet-2.3.12.Final.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jakarta.servlet-api-6.0.0.jar | EPL 2.0; GPL2 w/ CPE |
| jakarta.annotation-api-2.1.1.jar | EPL 2.0; GPL2 w/ CPE |
| undertow-websockets-jsr-2.3.12.Final.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jakarta.websocket-api-2.1.1.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jakarta.websocket-client-api-2.1.1.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| tomcat-embed-el-10.1.20.jar | Apache License, Version 2.0 |
| knife4j-openapi3-jakarta-spring-boot-starter-4.5.0.jar | The Apache Software License, Version 2.0 |
| knife4j-core-4.5.0.jar | The Apache Software License, Version 2.0 |
| knife4j-openapi3-ui-4.5.0.jar | The Apache Software License, Version 2.0 |
| springdoc-openapi-starter-webmvc-ui-2.3.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| springdoc-openapi-starter-webmvc-api-2.3.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| springdoc-openapi-starter-common-2.3.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| swagger-core-jakarta-2.2.19.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| swagger-annotations-jakarta-2.2.19.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| swagger-models-jakarta-2.2.19.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jackson-dataformat-yaml-2.15.4.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| swagger-ui-5.10.3.jar | Apache 2.0 |
| spring-boot-starter-data-redis-3.2.5.jar | Apache License, Version 2.0 |
| spring-boot-starter-3.2.5.jar | Apache License, Version 2.0 |
| spring-boot-starter-logging-3.2.5.jar | Apache License, Version 2.0 |
| logback-classic-1.4.14.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| logback-core-1.4.14.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| log4j-to-slf4j-2.21.1.jar | Apache-2.0 |
| jul-to-slf4j-2.0.13.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| snakeyaml-2.2.jar | Apache License, Version 2.0 |
| lettuce-core-6.3.2.RELEASE.jar | Apache License, Version 2.0 |
| netty-common-4.1.109.Final.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| netty-handler-4.1.109.Final.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| netty-resolver-4.1.109.Final.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| netty-buffer-4.1.109.Final.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| netty-transport-native-unix-common-4.1.109.Final.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| netty-codec-4.1.109.Final.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| netty-transport-4.1.109.Final.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| reactor-core-3.6.5.jar | Apache License, Version 2.0 |
| reactive-streams-1.0.4.jar | MIT-0 |
| spring-data-redis-3.2.5.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| spring-data-keyvalue-3.2.5.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| spring-data-commons-3.2.5.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| spring-tx-6.1.6.jar | Apache License, Version 2.0 |
| spring-oxm-6.1.6.jar | Apache License, Version 2.0 |
| junit-4.13.2.jar | Eclipse Public License 1.0 |
| hamcrest-core-2.2.jar | BSD License 3 |
| spring-boot-starter-test-3.2.5.jar | Apache License, Version 2.0 |
| spring-boot-test-3.2.5.jar | Apache License, Version 2.0 |
| spring-boot-test-autoconfigure-3.2.5.jar | Apache License, Version 2.0 |
| json-path-2.9.0.jar | The Apache Software License, Version 2.0 |
| jakarta.xml.bind-api-4.0.2.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jakarta.activation-api-2.1.3.jar | EDL 1.0 |
| json-smart-2.5.1.jar | The Apache Software License, Version 2.0 |
| accessors-smart-2.5.1.jar | The Apache Software License, Version 2.0 |
| asm-9.6.jar | BSD-3-Clause |
| assertj-core-3.24.2.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| byte-buddy-1.14.13.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| awaitility-4.2.1.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| hamcrest-2.2.jar | BSD License 3 |
| junit-jupiter-5.10.2.jar | Eclipse Public License v2.0 |
| junit-jupiter-api-5.10.2.jar | Eclipse Public License v2.0 |
| opentest4j-1.3.0.jar | The Apache License, Version 2.0 |
| junit-platform-commons-1.10.2.jar | Eclipse Public License v2.0 |
| apiguardian-api-1.1.2.jar | The Apache License, Version 2.0 |
| junit-jupiter-params-5.10.2.jar | Eclipse Public License v2.0 |
| junit-jupiter-engine-5.10.2.jar | Eclipse Public License v2.0 |
| junit-platform-engine-1.10.2.jar | Eclipse Public License v2.0 |
| mockito-core-5.7.0.jar | MIT |
| byte-buddy-agent-1.14.13.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| objenesis-3.3.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| mockito-junit-jupiter-5.7.0.jar | MIT |
| jsonassert-1.5.1.jar | The Apache Software License, Version 2.0 |
| android-json-0.0.20131108.vaadin1.jar | Apache License 2.0 |
| spring-core-6.1.6.jar | Apache License, Version 2.0 |
| spring-jcl-6.1.6.jar | Apache License, Version 2.0 |
| spring-test-6.1.6.jar | Apache License, Version 2.0 |
| xmlunit-core-2.9.1.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| spring-boot-starter-web-3.2.5.jar | Apache License, Version 2.0 |
| spring-boot-starter-json-3.2.5.jar | Apache License, Version 2.0 |
| jackson-datatype-jdk8-2.15.4.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jackson-datatype-jsr310-2.15.4.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jackson-module-parameter-names-2.15.4.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| spring-web-6.1.6.jar | Apache License, Version 2.0 |
| micrometer-observation-1.12.5.jar | The Apache Software License, Version 2.0 |
| micrometer-commons-1.12.5.jar | The Apache Software License, Version 2.0 |
| spring-webmvc-6.1.6.jar | Apache License, Version 2.0 |
| spring-expression-6.1.6.jar | Apache License, Version 2.0 |
| spring-boot-starter-aop-3.2.5.jar | Apache License, Version 2.0 |
| spring-aop-6.1.6.jar | Apache License, Version 2.0 |
| aspectjweaver-1.9.22.jar | Eclipse Public License - v 2.0 |
| spring-context-support-6.1.6.jar | Apache License, Version 2.0 |
| spring-beans-6.1.6.jar | Apache License, Version 2.0 |
| spring-context-6.1.6.jar | Apache License, Version 2.0 |
| spring-boot-configuration-processor-3.2.5.jar | Apache License, Version 2.0 |
| spring-boot-devtools-3.2.5.jar | Apache License, Version 2.0 |
| spring-boot-3.2.5.jar | Apache License, Version 2.0 |
| spring-boot-autoconfigure-3.2.5.jar | Apache License, Version 2.0 |
| mysql-connector-j-8.0.33.jar | The GNU General Public License, v2 with Universal FOSS Exception, v1.0 |
| ojdbc11-21.1.0.0.jar | Oracle Free Use Terms and Conditions (FUTC) |
| dynamic-datasource-spring-boot3-starter-4.3.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| dynamic-datasource-spring-boot-common-4.3.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| dynamic-datasource-spring-4.3.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| dynamic-datasource-creator-4.3.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| druid-spring-boot-3-starter-1.2.25.jar | Apache 2 |
| druid-1.2.25.jar | Apache 2 |
| slf4j-api-2.0.13.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| mybatis-plus-spring-boot3-starter-3.5.10.jar | The Apache License, Version 2.0 |
| mybatis-plus-3.5.10.jar | The Apache License, Version 2.0 |
| mybatis-plus-core-3.5.10.jar | The Apache License, Version 2.0 |
| mybatis-plus-annotation-3.5.10.jar | The Apache License, Version 2.0 |
| mybatis-plus-spring-3.5.10.jar | The Apache License, Version 2.0 |
| mybatis-3.5.19.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| mybatis-spring-3.0.4.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| mybatis-plus-spring-boot-autoconfigure-3.5.10.jar | The Apache License, Version 2.0 |
| spring-boot-starter-jdbc-3.2.5.jar | Apache License, Version 2.0 |
| HikariCP-5.0.1.jar | The Apache Software License, Version 2.0 |
| spring-jdbc-6.1.6.jar | Apache License, Version 2.0 |
| mybatis-plus-jsqlparser-3.5.10.jar | The Apache License, Version 2.0 |
| jsqlparser-5.1.jar | GNU Library or Lesser General Public License (LGPL) V2.1; The Apache Software License, Version 2.0 |
| mybatis-plus-jsqlparser-common-3.5.10.jar | The Apache License, Version 2.0 |
| mybatis-plus-extension-3.5.10.jar | The Apache License, Version 2.0 |
| fastjson-2.0.53.jar | Apache 2 |
| fastjson2-extension-2.0.53.jar | Apache 2 |
| fastjson2-2.0.53.jar | Apache 2 |
| commons-io-2.13.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| commons-configuration-1.10.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| commons-lang-2.6.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| commons-logging-1.1.1.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| joda-time-2.12.2.jar | Apache License, Version 2.0 |
| lombok-1.18.44.jar | The MIT License |
| hutool-core-5.8.20.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| logstash-logback-encoder-7.4.jar | Apache License, Version 2.0; MIT License |
| jackson-databind-2.15.4.jar | The Apache Software License, Version 2.0 |
| easypoi-base-4.4.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| poi-4.1.1.jar | Apache License, Version 2.0 |
| commons-collections4-4.4.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| commons-math3-3.6.1.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| poi-ooxml-4.1.1.jar | Apache License, Version 2.0 |
| curvesapi-1.06.jar | BSD License |
| poi-ooxml-schemas-4.1.1.jar | Apache License, Version 2.0 |
| xmlbeans-3.1.0.jar | The Apache Software License, Version 2.0 |
| guava-16.0.1.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| commons-lang3-3.13.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| ognl-3.2.6.jar | The Apache Software License, Version 2.0 |
| javassist-3.20.0-GA.jar | MPL 1.1; LGPL 2.1; Apache License 2.0 |
| validation-api-1.1.0.Final.jar | The Apache Software License, Version 2.0 |
| easypoi-web-4.4.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| easypoi-annotation-4.4.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| qiniu-java-sdk-7.14.0.jar | MIT License |
| okhttp-4.12.0.jar | The Apache Software License, Version 2.0 |
| okio-3.6.0.jar | The Apache Software License, Version 2.0 |
| okio-jvm-3.6.0.jar | The Apache Software License, Version 2.0 |
| kotlin-stdlib-common-1.9.23.jar | The Apache License, Version 2.0 |
| kotlin-stdlib-jdk8-1.9.23.jar | The Apache License, Version 2.0 |
| kotlin-stdlib-1.9.23.jar | The Apache License, Version 2.0 |
| annotations-13.0.jar | The Apache Software License, Version 2.0 |
| kotlin-stdlib-jdk7-1.9.23.jar | The Apache License, Version 2.0 |
| gson-2.10.1.jar | Apache-2.0 |
| aliyun-sdk-oss-3.17.1.jar | The Apache License, Version 2.0 |
| httpclient-4.5.13.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| httpcore-4.4.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jdom2-2.0.6.1.jar | Similar to Apache License but with the acknowledgment clause removed |
| jettison-1.5.4.jar | Apache License, Version 2.0 |
| aliyun-java-sdk-core-4.5.10.jar |  |
| jaxb-api-2.3.1.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| javax.activation-api-1.2.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| org.jacoco.agent-0.8.5-runtime.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| ini4j-0.5.4.jar | Apache 2 |
| opentracing-api-0.33.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| opentracing-util-0.33.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| opentracing-noop-0.33.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| aliyun-java-sdk-ram-3.1.0.jar |  |
| aliyun-java-sdk-kms-2.11.0.jar |  |
| cos_api-5.6.169.jar | cos-java-sdk |
| commons-codec-1.16.1.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| tencentcloud-sdk-java-kms-3.1.213.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| tencentcloud-sdk-java-common-3.1.213.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| logging-interceptor-2.7.5.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| okhttp-2.7.5.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| bcprov-jdk15on-1.67.jar | Bouncy Castle Licence |
| xstream-1.4.20.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| mxparser-1.2.2.jar | Indiana University Extreme! Lab Software License |
| xmlpull-1.1.3.1.jar | Public Domain |
| minio-8.5.5.jar | The Apache License, Version 2.0 |
| simple-xml-safe-2.7.1.jar | The Apache Software License, Version 2.0 |
| jackson-annotations-2.15.4.jar | The Apache Software License, Version 2.0 |
| jackson-core-2.15.4.jar | The Apache Software License, Version 2.0 |
| bcprov-jdk18on-1.74.jar | Bouncy Castle Licence |
| commons-compress-1.23.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| snappy-java-1.1.10.1.jar | Apache-2.0 |
| esdk-obs-java-bundle-3.23.9.1.jar | Apache 2.0 License |
| log4j-core-2.21.1.jar | Apache-2.0 |
| log4j-api-2.21.1.jar | Apache-2.0 |
| velocity-tools-2.0.jar | The Apache Software License, Version 2.0 |
| commons-beanutils-1.7.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| commons-digester-1.8.jar | The Apache Software License, Version 2.0 |
| commons-chain-1.1.jar | The Apache Software License, Version 2.0 |
| commons-collections-3.2.jar | The Apache Software License, Version 2.0 |
| commons-validator-1.3.1.jar | The Apache Software License, Version 2.0 |
| dom4j-1.1.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| oro-2.0.8.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| sslext-1.2-0.jar | Apache Software License, Version 1.1 |
| struts-core-1.3.8.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| antlr-2.7.2.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| struts-taglib-1.3.8.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| struts-tiles-1.3.8.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| velocity-1.6.2.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| shiro-core-3.0.0.jar | Apache-2.0 |
| shiro-lang-3.0.0.jar | Apache-2.0 |
| shiro-cache-3.0.0.jar | Apache-2.0 |
| shiro-crypto-hash-3.0.0.jar | Apache-2.0 |
| shiro-crypto-core-3.0.0.jar | Apache-2.0 |
| shiro-hashes-argon2-3.0.0.jar | Apache-2.0 |
| shiro-hashes-bcrypt-3.0.0.jar | Apache-2.0 |
| shiro-crypto-cipher-3.0.0.jar | Apache-2.0 |
| shiro-config-core-3.0.0.jar | Apache-2.0 |
| shiro-config-ogdl-3.0.0.jar | Apache-2.0 |
| shiro-event-3.0.0.jar | Apache-2.0 |
| shiro-spring-3.0.0.jar | Apache-2.0 |
| shiro-web-3.0.0.jar | Apache-2.0 |
| encoder-1.4.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jjwt-api-0.11.5.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jjwt-impl-0.11.5.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jjwt-gson-0.11.5.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jwks-rsa-0.22.1.jar | The MIT License (MIT) |
| spring-boot-starter-validation-3.2.5.jar | Apache License, Version 2.0 |
| hibernate-validator-8.0.1.Final.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jakarta.validation-api-3.0.2.jar | Apache License 2.0 |
| classmate-1.6.0.jar | Apache License, Version 2.0 |
| weixin-java-mp-4.5.2.B.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| weixin-java-common-4.5.2.B.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jcl-over-slf4j-2.0.13.jar | Apache License, Version 2.0 |
| dom4j-2.1.3.jar | BSD 3-clause New License |
| weixin-java-miniapp-4.5.2.B.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| bcpkix-jdk15on-1.70.jar | Bouncy Castle Licence |
| bcutil-jdk15on-1.70.jar | Bouncy Castle Licence |
| weixin-java-pay-4.5.2.B.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| qrcode-utils-1.1.jar | The Apache License, Version 2.0 |
| core-3.2.1.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jodd-util-6.1.0.jar | BSD-2-Clause |
| javax.servlet-api-4.0.1.jar | CDDL + GPLv2 with classpath exception |
| weixin-java-cp-4.5.2.B.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| weixin-java-open-4.5.2.B.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jedis-5.1.0.jar | MIT |
| commons-pool2-2.12.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| json-20231013.jar | Public Domain |
| alipay-easysdk-2.2.3.jar | MIT License |
| easysdk-kernel-1.0.12.jar | MIT License |
| alipay-sdk-java-4.38.76.ALL.jar | The Apache License, Version 2.0 |
| batik-transcoder-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-anim-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-css-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-ext-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-parser-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-svg-dom-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-awt-util-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| xmlgraphics-commons-2.7.jar | The Apache Software License, Version 2.0 |
| batik-bridge-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-script-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-dom-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| xml-apis-1.4.01.jar | The Apache Software License, Version 2.0; The SAX License; The W3C License |
| batik-gvt-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-shared-resources-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-svggen-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-util-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-constants-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-i18n-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-xml-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| xml-apis-ext-1.3.04.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| batik-codec-1.16.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| pdfbox-3.0.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| pdfbox-io-3.0.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| fontbox-3.0.0.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jakarta.mail-2.0.1.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jakarta.activation-2.0.1.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| qcloudsms-1.0.6.jar | MIT License |
| httpmime-4.5.3.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| dysmsapi20170525-2.0.24.jar | The Apache License, Version 2.0 |
| tea-util-0.2.18.jar | The Apache License, Version 2.0 |
| endpoint-util-0.0.7.jar | The Apache License, Version 2.0 |
| tea-1.2.0.jar | The Apache License, Version 2.0 |
| tea-openapi-0.2.8.jar | The Apache License, Version 2.0 |
| credentials-java-0.2.4.jar | The Apache License, Version 2.0 |
| jaxb-core-4.0.5.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| angus-activation-2.0.2.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| jaxb-impl-4.0.5.jar | 未直接声明或无同名POM, 继承/JAR声明待核验 |
| alibabacloud-gateway-spi-0.0.1.jar | The Apache License, Version 2.0 |
| tea-xml-0.1.5.jar | The Apache License, Version 2.0 |
| openapiutil-0.2.1.jar | The Apache License, Version 2.0 |
| jodd-http-6.3.0.jar | BSD-2-Clause |
| commons-httpclient-3.1.jar | Apache License |
