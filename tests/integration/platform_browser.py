"""使用独立无界面Chrome验证平台页面和正常改密入口, 不操作用户桌面或浏览器资料."""

import base64
import json
import os
from pathlib import Path
import socket
import shutil
import struct
import subprocess
import time
from urllib.request import urlopen
from urllib.parse import urlsplit


class Devtools:
    """用标准库连接本轮无界面Chrome的本地调试协议, 不引入运行依赖."""

    def __init__(self, address):
        """仅连接回环调试端口, 握手后按请求标识处理事件与响应."""
        url = urlsplit(address)
        if url.hostname not in ('127.0.0.1', 'localhost'):
            raise ValueError('调试连接必须使用本机回环')
        self.socket = socket.create_connection((url.hostname, url.port), timeout=15)
        key = base64.b64encode(os.urandom(16)).decode()
        request = 'GET ' + url.path + ' HTTP/1.1\r\nHost: ' + url.netloc + '\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Key: ' + key + '\r\nSec-WebSocket-Version: 13\r\n\r\n'
        self.socket.sendall(request.encode())
        header = b''
        while b'\r\n\r\n' not in header:
            header += self.socket.recv(1)
        if not header.startswith(b'HTTP/1.1 101'):
            raise RuntimeError('本机调试协议握手失败')
        self.sequence = 0
        self.session = None

    def read(self, length):
        """按帧长度读取, 不将凭据或页面内容写入日志."""
        content = b''
        while len(content) < length:
            block = self.socket.recv(length - len(content))
            if not block:
                raise RuntimeError('无界面浏览器连接中断')
            content += block
        return content

    def call(self, method, params=None, session=True):
        """发送一个有界JSON请求, 跳过异步事件并等待同标识响应."""
        self.sequence += 1
        message = {'id': self.sequence, 'method': method, 'params': params or {}}
        if session and self.session:
            message['sessionId'] = self.session
        body = json.dumps(message).encode()
        mask = os.urandom(4)
        header = bytes([0x81, 0x80 | len(body)]) if len(body) < 126 else bytes([0x81, 0xfe]) + struct.pack('!H', len(body))
        self.socket.sendall(header + mask + bytes(value ^ mask[index % 4] for index, value in enumerate(body)))
        while True:
            frame = self.read(2)
            length = frame[1] & 127
            if length == 126:
                length = struct.unpack('!H', self.read(2))[0]
            elif length == 127:
                length = struct.unpack('!Q', self.read(8))[0]
            if length > 16 * 1024 * 1024:
                raise RuntimeError('调试响应超出限制')
            data = self.read(length)
            if frame[0] & 15 == 8:
                raise RuntimeError('调试连接已关闭')
            result = json.loads(data)
            if result.get('id') != self.sequence:
                continue
            if 'error' in result:
                raise RuntimeError('浏览器操作失败: ' + method)
            return result.get('result', {})

    def evaluate(self, expression):
        """仅读取DOM状态或触发正常控件, 不调用Vue组件私有业务方法."""
        result = self.call('Runtime.evaluate', {'expression': expression, 'returnByValue': True})
        if 'exceptionDetails' in result:
            raise RuntimeError('浏览器DOM操作失败')
        return result.get('result', {}).get('value')

    def wait(self, expression):
        """等待实际页面条件, 每次等待都有明确上限."""
        deadline = time.monotonic() + 20
        while time.monotonic() < deadline:
            if self.evaluate('Boolean(' + expression + ')'):
                return
            time.sleep(0.2)
        raise TimeoutError('平台页面条件等待超时')


def verify(base, token, folder, checked, login):
    """截图五个实际页面, 并从账号菜单完成本地密码可见性与改密验证."""
    executable = Path('/Applications/Google Chrome.app/Contents/MacOS/Google Chrome')
    if not executable.exists():
        print('未验证: 当前机器没有可用的无界面Chrome', flush=True)
        return
    profile = folder / 'chrome-profile'
    screenshots = folder / 'screenshots'
    screenshots.mkdir()
    with open(folder / 'chrome.log', 'w') as output:
        process = subprocess.Popen([str(executable), '--headless=new', '--remote-debugging-port=0',
            '--user-data-dir=' + str(profile), '--no-first-run', '--no-default-browser-check',
            '--disable-background-networking', '--disable-sync', '--disable-extensions', '--disable-component-update',
            '--window-size=1440,1000', 'about:blank'], stdout=output, stderr=output)
        client = None
        try:
            for attempt in range(50):
                if (profile / 'DevToolsActivePort').exists():
                    break
                time.sleep(0.2)
            port = (profile / 'DevToolsActivePort').read_text().splitlines()[0]
            with urlopen('http://127.0.0.1:' + port + '/json/version', timeout=5) as response:
                address = json.load(response)['webSocketDebuggerUrl']
            client = Devtools(address)
            target = client.call('Target.createTarget', {'url': 'about:blank'}, session=False)['targetId']
            client.session = client.call('Target.attachToTarget', {'targetId': target, 'flatten': True}, session=False)['sessionId']
            client.call('Page.enable')
            client.call('Page.bringToFront')
            client.call('Network.enable')
            client.call('Network.setCookie', {'name': 'token', 'value': token, 'url': base, 'path': '/'})
            for route, title in (('overview', '平台总览'), ('merchant-accounts', '商家管理员'), ('monitor', '系统监控'), ('releases', '版本与更新'), ('logs', '系统日志')):
                client.call('Page.navigate', {'url': base + '/#/platform-' + route})
                client.wait('document.querySelector(".studio-page-title")?.textContent.trim()===' + json.dumps(title))
                client.wait('!document.querySelector(".el-loading-mask")')
                time.sleep(0.3)
                checked('无界面浏览器真实页面 ' + title, client.evaluate('document.body.innerText.includes("平台管理")'))
                image = client.call('Page.captureScreenshot', {'format': 'png'})['data']
                (screenshots / ('platform-' + route + '.png')).write_bytes(base64.b64decode(image))
            box = client.evaluate('JSON.stringify(document.querySelector(".el-dropdown-link").getBoundingClientRect().toJSON())')
            rectangle = json.loads(box)
            client.call('Input.dispatchMouseEvent', {'type': 'mouseMoved', 'x': rectangle['x'] + 10, 'y': rectangle['y'] + 10})
            client.wait('[...document.querySelectorAll(".el-dropdown-menu__item")].some(item=>item.innerText.includes("修改密码"))')
            client.evaluate('[...document.querySelectorAll(".el-dropdown-menu__item")].find(item=>item.innerText.includes("修改密码")).click()')
            client.wait('document.querySelectorAll(".el-dialog input").length===3')
            client.wait('document.querySelector(".el-dialog").innerText.includes("6") && document.querySelector(".el-dialog").innerText.includes("32")')
            time.sleep(0.3)
            checked('改密三个密码框默认隐藏', client.evaluate('[...document.querySelectorAll(".el-dialog input")].every(input=>input.type==="password")'))
            for index, value in enumerate(('123456', '654321', '654321')):
                client.evaluate('document.querySelectorAll(".el-dialog input")[' + str(index) + '].focus()')
                client.call('Input.insertText', {'text': value})
            checked('正常输入事件更新三个密码框', client.evaluate('[...document.querySelectorAll(".el-dialog input")].every(input=>input.value.length===6)'))
            client.wait('document.querySelector(".el-dialog .el-input__password")')
            client.evaluate('document.querySelector(".el-dialog .el-input__password").click()')
            client.wait('document.querySelector(".el-dialog input").type==="text"')
            checked('Element Plus密码显示切换真实生效', True)
            client.evaluate('[...document.querySelectorAll(".el-dialog__footer button")].find(button=>button.innerText.trim()==="确定").click()')
            client.wait('location.hash.includes("login")')
            checked('从正常账号菜单提交改密并重新登录', bool(login(base, 'admin', '654321')))
        finally:
            if client:
                client.socket.close()
            process.terminate()
            try:
                process.wait(timeout=8)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait()
            shutil.rmtree(profile, ignore_errors=True)
