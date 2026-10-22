/* 在隔离页面验证HTML编辑组件, 不连接业务API或保存商品. */
const fs = require('fs');
const http = require('http');
const path = require('path');
const root = path.resolve(__dirname, '../..');
const ui = path.join(root, 'apps/admin-ui');
const parser = require(path.join(ui, 'node_modules/@vue/compiler-sfc'));
const vue = fs.readFileSync(path.join(ui, 'node_modules/vue/dist/vue.global.js'));
const component = parser.parse(
    fs.readFileSync(path.join(ui, 'src/components/rich-text-editor/index.vue'), 'utf8')
).descriptor;
const script = component.script.content
    .replace(/import DOMPurify[^;]+;/, "import DOMPurify from '/purify.mjs';")
    .replace('export function sanitizeHtml', 'function sanitizeHtml')
    .replace('export default {', 'const Editor = {');
const image = fs.readFileSync(
    path.join(root, 'apps/miniapp/static/images/demo/demo-platinum-pearl-necklace.webp')
);
const html = `<!doctype html><html lang="zh-CN"><meta charset="utf-8">
<title>HTML编辑组件隔离验证</title><style>
body{font:16px sans-serif;max-width:1050px;margin:30px auto;color:#34524c}
textarea{box-sizing:border-box}button{cursor:pointer}pre{white-space:pre-wrap;background:#f5f5f5;padding:12px}
${component.styles.map((style) => style.content.replace(/ >>> /g, ' ')).join('\n')}
</style><div id="app"><h1>商品详情编辑验证</h1>
<p>隔离演示页面, 操作仅更新内存中的HTML.</p>
<button @click="readonly=!readonly">切换只读</button>
<p>模式: {{readonly ? '只读' : '可编辑'}}</p>
<rich-text-editor v-model="html" :disabled="readonly" placeholder="商品详情"></rich-text-editor>
<h2>同步的HTML</h2><pre id="result">{{html}}</pre>
</div><script src="/vue.js"></script><script type="module">
${script}
Editor.template=${JSON.stringify(component.template.content)};
const app = Vue.createApp({data: () => ({readonly:false,html:'<h2>演示商品详情</h2><p>仅用于隔离编辑器回归.</p>'})});
app.component('rich-text-editor',Editor);
app.component('el-button',{props:['disabled','type'],template:${JSON.stringify('<button :disabled="disabled" @click="$emit(\'click\')"><slot></slot></button>')}});
app.component('el-dialog',{props:['modelValue','title'],template:${JSON.stringify('<div v-if="modelValue" role="dialog"><h2>{{title}}</h2><slot></slot><slot name="footer"></slot></div>')}});
app.component('el-img',{props:['modelValue'],template:${JSON.stringify("<button @click=\"$emit('update:modelValue','/demo.webp')\">选择演示图片</button>")}});
app.mount('#app');
</script></html>`;

/** 返回内存测试页面和明确允许的资源, 不暴露工作区目录. */
function serve(request, response) {
    const routes = {
        '/': ['text/html; charset=utf-8', html],
        '/vue.js': ['text/javascript', vue],
        '/purify.mjs': [
            'text/javascript',
            fs.readFileSync(path.join(ui, 'src/third-party/dompurify/purify.es.mjs')),
        ],
        '/demo.webp': ['image/webp', image],
    };
    const result = routes[request.url];
    if (!result) {
        response.writeHead(404).end();
        return;
    }
    response.writeHead(200, {'Content-Type': result[0], 'Cache-Control': 'no-store'});
    response.end(result[1]);
}

const server = http.createServer(serve);

/** 输出本次测试端口, 仅绑定本机回环地址. */
function ready() {
    console.log(`编辑器隔离验证页面: http://127.0.0.1:${server.address().port}/`);
}

/** 测试窗口到期后自动关闭本脚本创建的服务器. */
function finish() {
    console.log('编辑器隔离验证窗口已到期.');
    server.close();
}

server.listen(0, '127.0.0.1', ready);
setTimeout(finish, 20 * 60 * 1000).unref();
