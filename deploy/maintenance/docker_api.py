"""仅在独立维护容器内访问Docker, 所有目标必须属于固定项目及核心服务白名单."""

import http.client
import json
import os
import socket
import subprocess
from urllib.parse import quote

PROJECT = os.environ.get("CARATCRAFT_COMPOSE_PROJECT", "platform-wxshop")
NAMES = {"mysql": "platform-mysql", "redis": "platform-redis", "platform-admin": "platform-admin",
         "platform-api": "platform-api", "nginx": "platform-nginx"}
if PROJECT != "platform-wxshop":
    # 隔离验收使用独立项目及独立名称, 此配置只来自容器启动环境.
    if not PROJECT.startswith("caratcraft-platform-test-"):
        raise RuntimeError("维护项目配置不在允许范围")
    NAMES = {service: PROJECT + "-" + service for service in NAMES}


class DockerConnection(http.client.HTTPConnection):
    """通过本地Unix套接字连接Docker, 不开放守护进程网络端口."""

    def __init__(self):
        """固定连接位置与超时, 不接受外部URL."""
        super().__init__("localhost", timeout=12)

    def connect(self):
        """只连接部署时挂载的Docker套接字."""
        self.sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
        self.sock.settimeout(self.timeout)
        self.sock.connect("/var/run/docker.sock")


def request(endpoint):
    """读取固定Docker API路径, 控制响应大小并及时释放连接."""
    connection = DockerConnection()
    try:
        connection.request("GET", "/v1.47" + endpoint)
        response = connection.getresponse()
        if response.status != 200:
            raise RuntimeError("Docker读取失败, HTTP " + str(response.status))
        data = response.read(16 * 1024 * 1024 + 1)
        if len(data) > 16 * 1024 * 1024:
            raise RuntimeError("Docker响应超出限制")
        return json.loads(data)
    finally:
        connection.close()


def inspect(service):
    """先验证固定名称和Compose归属, 不向调用方返回环境变量或挂载秘密."""
    if service not in NAMES:
        raise ValueError("服务不在维护白名单")
    data = request("/containers/" + quote(NAMES[service], safe="") + "/json")
    labels = data.get("Config", {}).get("Labels", {})
    if labels.get("com.docker.compose.project") != PROJECT or labels.get("com.docker.compose.service") != service:
        raise RuntimeError("容器项目或服务归属异常")
    return data


def run(arguments, *, input_data=None, output=None, timeout=300):
    """执行程序内构造的固定参数列表, 不接收Shell片段或输出可能含秘密的原文."""
    result = subprocess.run(arguments, input=input_data, stdout=output or subprocess.PIPE,
                            stderr=subprocess.PIPE, timeout=timeout, check=False)
    if result.returncode:
        raise RuntimeError("维护子操作失败, 退出码 " + str(result.returncode))
    return result.stdout


def database(sql=None, output=None, restore=None):
    """通过已核验的MySQL容器执行发行清单SQL或一致性备份, 密码只来自容器内部环境."""
    inspect("mysql")
    if output is not None:
        command = 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysqldump -uroot --default-character-set=utf8mb4 --single-transaction --routines --triggers --no-tablespaces --set-gtid-purged=OFF "$MYSQL_DATABASE"'
    else:
        command = 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot --default-character-set=utf8mb4 --batch --skip-column-names "$MYSQL_DATABASE"'
    if restore is not None:
        with open(restore, "rb") as source:
            process = subprocess.run(["docker", "exec", "-i", NAMES["mysql"], "sh", "-c", command],
                                     stdin=source, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=600)
        if process.returncode:
            raise RuntimeError("数据库备份恢复失败, 应用保持维护状态")
        return ""
    data = run(["docker", "exec", "-i", NAMES["mysql"], "sh", "-c", command],
               input_data=sql.encode() if sql else None, output=output, timeout=600)
    return data.decode("utf-8") if data is not None else ""
