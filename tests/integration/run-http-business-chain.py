"""用最新发布JAR和一次性数据库验证真实HTTP商品到结算链路, 不访问业务服务."""

import argparse
import base64
from datetime import datetime
from decimal import Decimal
import hashlib
import hmac
import json
import os
from pathlib import Path
import re
import secrets
import subprocess
import time
import urllib.error
import urllib.parse
import urllib.request

ROOT = Path(__file__).resolve().parents[2]
UPLOAD_LABEL = "org.caratcraft.fixture.upload"


def docker(*arguments, input_text=None, environment=None):
    """执行明确的Docker参数, 凭据仅通过环境和标准输入传递."""
    result = subprocess.run(["docker", *arguments], input=input_text, text=True,
                            encoding="utf-8", capture_output=True, env=environment)
    if result.returncode:
        raise RuntimeError("Docker操作失败: " + arguments[0] + ": " + result.stderr[-1500:])
    return result.stdout.strip()


def wait_until(action, timeout=120):
    """有限等待本轮服务就绪, 失败保留最后一个错误且不重启业务程序."""
    deadline = time.monotonic() + timeout
    last_error = None
    while time.monotonic() < deadline:
        try:
            return action()
        except (RuntimeError, OSError, ValueError) as error:
            last_error = error
            time.sleep(1)
    raise RuntimeError("隔离服务等待超时: " + str(last_error))


def sql(container, statement):
    """仅在本轮创建的隔离容器执行SQL, 不使用宿主机或业务数据库配置."""
    if not re.fullmatch(r"[a-f0-9]{64}", container):
        raise ValueError("无效隔离容器身份")
    return docker("exec", "-i", container, "sh", "-c",
                  'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql --protocol=TCP -h127.0.0.1 '
                  '-uroot --default-character-set=utf8mb4 --batch --skip-column-names "$MYSQL_DATABASE"',
                  input_text=statement)


def request(base, route, token=None, payload=None, form=None, method="POST", success=True):
    """向本轮回环地址发送真实HTTP, 失败只报告协议状态及业务码."""
    if urllib.parse.urlparse(base).hostname != "127.0.0.1":
        raise ValueError("HTTP验收只允许本轮回环服务")
    headers = {"token": token} if token else {}
    body = None
    if payload is not None:
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        headers["Content-Type"] = "application/json"
    elif form is not None:
        body = urllib.parse.urlencode(form).encode("utf-8")
        headers["Content-Type"] = "application/x-www-form-urlencoded"
    try:
        with urllib.request.urlopen(urllib.request.Request(base + route, data=body,
                                    headers=headers, method=method), timeout=15) as response:
            status, data = response.status, json.load(response)
    except urllib.error.HTTPError as error:
        status, data = error.code, json.load(error)
    if route == "/internal/health/ready":
        if status != 200 or data.get("status") != "ready":
            raise RuntimeError("隔离应用尚未就绪")
        return data
    if success:
        if status != 200 or data.get("code") != 0:
            raise RuntimeError(f"HTTP步骤失败: {route}, status={status}, code={data.get('code')}, msg={data.get('msg')}")
    elif status == 200 and data.get("code") == 0:
        raise RuntimeError("应拒绝的HTTP请求被接受: " + route)
    return data.get("data")


def user_token(user_id, secret):
    """给合成用户签发与真实认证器相同的HS256测试JWT, 不调用微信登录."""
    def encode(value):
        """按JWT要求生成无填充的URL安全Base64."""
        return base64.urlsafe_b64encode(value).rstrip(b"=")
    now = int(time.time())
    header = encode(b'{"alg":"HS256","typ":"JWT"}')
    payload = encode(json.dumps({"sub": str(user_id), "iat": now, "exp": now + 1800}).encode())
    unsigned = header + b"." + payload
    return (unsigned + b"." + encode(hmac.new(secret, unsigned, hashlib.sha256).digest())).decode()


def verify_upload_storage(container, admin_container, admin, admin_token, nginx_args, containers):
    """上传到真实应用并用原 Nginx 图片规则验证重启和替换后的原始地址."""
    boundary = "caratcraft-storage-fixture"
    image = (ROOT / "apps/miniapp/static/images/address_right.png").read_bytes()
    prefix = ("--" + boundary + '\r\nContent-Disposition: form-data; name="file"; '
              'filename="fixture.png"\r\nContent-Type: image/png\r\n\r\n').encode("ascii")
    body = prefix + image + ("\r\n--" + boundary + "--\r\n").encode("ascii")
    headers = {"token": admin_token, "Content-Type": "multipart/form-data; boundary=" + boundary}
    with urllib.request.urlopen(urllib.request.Request(admin + "/sys/oss/upload", data=body,
                                headers=headers, method="POST"), timeout=15) as response:
        payload = json.load(response)
    assert payload["code"] == 0, payload.get("msg")
    uploaded = payload["data"]["url"]
    assert re.fullmatch(r"/upload/[A-Za-z0-9/_-]+\.png", uploaded), uploaded
    mapping = docker("port", container, "8082/tcp")
    assert re.fullmatch(r"127\.0\.0\.1:[0-9]+", mapping)
    origin = "http://" + mapping

    def read_image():
        """直接读取回环 Nginx 的上传图片并计算内容摘要."""
        with urllib.request.urlopen(origin + uploaded, timeout=8) as response:
            assert response.headers.get_content_type() == "image/png"
            data = response.read()
            assert data.startswith(b"\x89PNG\r\n\x1a\n")
            return hashlib.sha256(data).hexdigest()

    nginx = docker(*nginx_args)
    containers.append(nginx)
    expected = wait_until(read_image)
    print("真实后台上传及 Nginx 图片读取通过", flush=True)
    docker("restart", nginx)
    assert wait_until(read_image) == expected
    docker("restart", admin_container)
    wait_until(lambda: request(admin, "/internal/health/ready", method="GET"), timeout=150)
    assert wait_until(read_image) == expected
    docker("rm", "-f", nginx)
    containers.remove(nginx)
    containers.append(docker(*nginx_args))
    assert wait_until(read_image) == expected
    print("独立 Nginx 重启和替换后原地址与内容摘要一致", flush=True)

    client = """
const assert = require('node:assert/strict');
const crypto = require('node:crypto');
const [origin, relative, expected] = process.argv.slice(1);
const api = require.resolve('./apps/miniapp/config/api.js');
require.cache[api] = {exports: {ApiOrigin: origin}};
const {normalizeAssetUrls} = require('./apps/miniapp/utils/util.js');
const {listPicUrl} = normalizeAssetUrls({listPicUrl: relative});
assert.equal(listPicUrl, origin + relative);
fetch(listPicUrl).then(async (response) => {
    assert.equal(response.status, 200);
    assert.equal(crypto.createHash('sha256').update(Buffer.from(await response.arrayBuffer())).digest('hex'), expected);
}).catch((error) => {process.stderr.write(String(error)); process.exitCode = 1;});
"""
    subprocess.run(["node", "-e", client, origin, uploaded, expected], cwd=ROOT, check=True)
    print("小程序资源地址解析代码能从同一规则后的地址读取该图片", flush=True)


def select_product(detail, value):
    """让真实小程序SKU模块解析HTTP返回数据, 不在测试中复制组合选择算法."""
    script = """const fs=require('node:fs');
const sku=require('./apps/miniapp/services/goods-sku.js');
const {detail,value}=JSON.parse(fs.readFileSync(0,'utf8'));
const selected=detail.specificationList.map(group=>{
    const item=group.valueList.find(item=>item.value===value);
    if(!item)throw new Error('HTTP规格值缺失');
    return String(item.id);
});
const product=sku.findProduct(sku.prepare(detail.productList,detail.specificationList),selected);
if(!product)throw new Error('客户端无法唯一选中HTTP规格');
process.stdout.write(JSON.stringify(product));
"""
    result = subprocess.run(["node", "-e", script], cwd=ROOT,
                            input=json.dumps({"detail": detail, "value": value}),
                            capture_output=True, text=True, encoding="utf-8", check=True)
    return json.loads(result.stdout)


def verify_chain(admin, api, admin_token, token_a, token_b, category_id):
    """串联实际后台保存、客户端选规格、加购、结算及账号与库存失败边界."""
    checks = []
    request(admin, "/mall/goods/aggregate/1", method="GET", success=False)
    request(api, "/app/cart/index", success=False)
    checks.append("真实认证器拒绝无令牌请求")
    aggregate = {
        "goods": {"name": "隔离HTTP链路商品", "categoryId": category_id,
                  "retailPrice": 80, "goodsNumber": 8, "isOnSale": 1,
                  "isDelete": 0, "pearlSource": "FRESHWATER", "jewelryType": "NECKLACE"},
        "specs": [{"specificationId": 800001, "specificationName": "隔离颜色",
                   "values": [{"value": "白色"}, {"value": "金色"}]}],
        "skuList": [{"goodsSpecificationIds": "800001:白色", "goodsNumber": 8, "retailPrice": 80},
                    {"goodsSpecificationIds": "800001:金色", "goodsNumber": 2, "retailPrice": 95}],
    }
    goods_id = request(admin, "/mall/goods/aggregate/save", admin_token, payload=aggregate)
    saved = request(admin, f"/mall/goods/aggregate/{goods_id}", admin_token, method="GET")
    detail = request(api, "/app/goods/detail", form={"id": goods_id})
    white = select_product(detail, "白色")
    gold = select_product(detail, "金色")
    assert white["id"] != gold["id"] and Decimal(str(white["retailPrice"])) == Decimal("80")
    checks.append("后台具名组合经真实HTTP由小程序SKU模块唯一选择")
    cart = request(api, "/app/cart/add", token_a,
                   payload={"goodsId": goods_id, "productId": white["id"], "number": 2})
    row = next(item for item in cart["cartList"] if item["productId"] == white["id"])
    assert Decimal(str(cart["cartTotal"]["checkedGoodsAmount"])) == Decimal("160")
    other = request(api, "/app/cart/index", token_b)
    assert not other["cartList"]
    request(api, "/app/cart/update", token_b,
            payload={"id": row["id"], "goodsId": goods_id, "productId": white["id"], "number": 1}, success=False)
    checks.append("加购金额及购物车跨账号读写隔离")
    checkout = request(api, "/app/cart/checkout", token_a, form={"type": "cart"})
    assert Decimal(str(checkout["goodsTotalPrice"])) == Decimal("160")
    for sku in saved["skuList"]:
        if sku["id"] == white["id"]:
            sku["retailPrice"] = 90
    request(admin, "/mall/goods/aggregate/update", admin_token, payload=saved)
    updated = request(admin, f"/mall/goods/aggregate/{goods_id}", admin_token, method="GET")
    assert next(s for s in updated["skuList"] if s["goodsSpecificationIds"] == "800001:白色")["id"] == white["id"]
    checkout = request(api, "/app/cart/checkout", token_a, form={"type": "cart"})
    assert Decimal(str(checkout["goodsTotalPrice"])) == Decimal("180")
    checks.append("后台改价保留SKU身份且结算采用当前价格")
    request(api, "/app/cart/add", token_a,
            payload={"goodsId": goods_id, "productId": gold["id"], "number": 3}, success=False)
    unchanged = request(api, "/app/cart/index", token_a)
    assert len(unchanged["cartList"]) == 1 and unchanged["cartList"][0]["number"] == 2
    checks.append("越库存加购失败且购物车保持原内容")
    buy = request(api, "/app/buy/add", token_a,
                  payload={"goodsId": goods_id, "productId": gold["id"], "number": 1})
    buy_checkout = request(api, "/app/cart/checkout", token_a,
                           form={"type": "buy", "checkoutId": buy["checkoutId"]})
    assert Decimal(str(buy_checkout["goodsTotalPrice"])) == Decimal("95")
    request(api, "/app/cart/checkout", token_b,
            form={"type": "buy", "checkoutId": buy["checkoutId"]}, success=False)
    checks.append("真实Redis立即购买上下文及跨账号拒绝")
    updated["goods"]["isOnSale"] = 0
    request(admin, "/mall/goods/aggregate/update", admin_token, payload=updated)
    request(api, "/app/cart/checkout", token_a, form={"type": "cart"}, success=False)
    checks.append("下架后旧购物车结算重新复核可售状态")
    request(admin, f"/custom/configuration/catalog-template?goodsId={goods_id}", method="GET", success=False)
    template = request(admin, f"/custom/configuration/catalog-template?goodsId={goods_id}", admin_token, method="GET")
    assert len(template["options"]) == 112
    assert {row["componentType"] for row in template["options"]} == {"pearl", "clasp", "accessory", "chain"}
    assert all(row["enabled"] == 0 and Decimal(str(row["priceDelta"])) == 0 for row in template["options"])
    imported = request(admin, "/custom/configuration/import", admin_token, payload=template)
    assert imported["optionCount"] == 112
    options = request(admin, f"/custom/configuration/options?goodsId={goods_id}", admin_token, method="GET")
    assert len(options) == 112 and all(row["enabled"] == 0 for row in options)
    checks.append("真实权限下下载112项客户四类目录并以停用状态完整导入")
    legacy = dict(template["options"][0], componentType="material", optionCode="http-legacy-type")
    request(admin, "/custom/configuration/option/save", admin_token, payload=legacy, success=False)
    unconfirmed_accessory = dict(next(row for row in template["options"] if row["componentType"] == "accessory"), enabled=1)
    request(admin, "/custom/configuration/option/save", admin_token, payload=unconfirmed_accessory, success=False)
    product = dict(template["product"], enabled=1)
    request(admin, "/custom/configuration/product/save", admin_token, payload=product, success=False)
    checks.append("HTTP拒绝旧材料分类、未确认计长的配饰及未补齐真实材料库存的候选发布")
    return checks


def main():
    """只管理本轮创建的容器, 完成或失败均清理, 输出不含令牌的机器证据."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--date", required=True, help="客户端日期, YYYY-MM-DD")
    parser.add_argument("--backend-image", help="验证已封装镜像中的应用JAR, 不挂载宿主机JAR")
    parser.add_argument("--storage-only", action="store_true", help="隔离验证本地上传卷和服务替换后的资源访问")
    args = parser.parse_args()
    day = datetime.strptime(args.date, "%Y-%m-%d").strftime("%Y%m%d")
    suffix = secrets.token_hex(3)
    database = f"caratcraft_init_audit_{day}_{suffix}"
    containers = []
    upload_volume = None
    env = os.environ.copy()
    secret = secrets.token_bytes(32)
    admin_token = secrets.token_hex(32)
    env.update(MYSQL_ROOT_PASSWORD=secrets.token_hex(24), MYSQL_PASSWORD=secrets.token_hex(24),
               MYSQL_DATABASE=database, MYSQL_USER="http_regression", MYSQL_USERNAME="http_regression",
               MYSQL_HOST="127.0.0.1", REDIS_HOST="127.0.0.1", REDIS_PASSWORD=secrets.token_hex(24),
               JWT_SECRET=base64.b64encode(secret).decode(), SPRING_PROFILES_ACTIVE="docker")
    try:
        ports = ["--publish", "127.0.0.1::8082"] if args.storage_only else []
        mysql = docker("run", "--detach", "--rm", "--pull=never", "--name", "caratcraft-http-mysql-" + suffix,
                       "--tmpfs", "/var/lib/mysql", "--publish", "127.0.0.1::8080", "--publish", "127.0.0.1::8081",
                       *ports,
                       "--env", "MYSQL_ROOT_PASSWORD", "--env", "MYSQL_PASSWORD", "--env", "MYSQL_DATABASE",
                       "--env", "MYSQL_USER", "--mount", f"type=bind,source={ROOT / 'database/init'},target=/docker-entrypoint-initdb.d,readonly",
                       "mysql:8.0.36", "--default-authentication-plugin=mysql_native_password", environment=env)
        containers.append(mysql)
        wait_until(lambda: sql(mysql, "SELECT version FROM CC_SCHEMA_VERSION WHERE id=1;"))
        print("完整隔离数据库初始化完成", flush=True)
        sql(mysql, f"INSERT INTO SYS_USER_TOKEN(USER_ID,TOKEN,EXPIRE_TIME,UPDATE_TIME) VALUES('1','{admin_token}',DATE_ADD(NOW(),INTERVAL 1 HOUR),NOW());")
        category_id = None
        if not args.storage_only:
            sql(mysql, "INSERT INTO WX_USER(ID,MA_OPENID,NICKNAME) VALUES(900001,'http_synthetic_a','隔离用户A'),(900002,'http_synthetic_b','隔离用户B');"
                "INSERT INTO mall_specification(id,name) VALUES(800001,'隔离颜色');")
            category_id = int(sql(mysql, "SELECT id FROM mall_category ORDER BY id LIMIT 1;"))
        redis = docker("run", "--detach", "--rm", "--pull=never", "--network", "container:" + mysql,
                       "--name", "caratcraft-http-redis-" + suffix, "--read-only", "--tmpfs", "/data",
                       "--env", "REDIS_PASSWORD", "redis:7.2-alpine", "sh", "-c",
                       'exec redis-server --save "" --appendonly no --requirepass "$REDIS_PASSWORD"', environment=env)
        containers.append(redis)
        bases = {}
        application_containers = {}
        modules = [("admin", 8080, "platform-framework"), ("api", 8081, "platform-framework-api")]
        if args.storage_only:
            upload_volume = docker("volume", "create", "--label", f"{UPLOAD_LABEL}={suffix}",
                                   "caratcraft-upload-test-" + suffix)
        for module, port, context in modules:
            jar = ROOT / f"backend/platform-{module}/target/platform-{module}.jar"
            if not jar.is_file():
                raise FileNotFoundError("需要先构建最新应用JAR: " + str(jar))
            command = ["run", "--detach", "--rm", "--pull=never", "--network", "container:" + mysql,
                       "--name", "caratcraft-http-" + module + "-" + suffix]
            if not args.backend_image:
                command.extend(["--mount", f"type=bind,source={jar},target=/verify/application.jar,readonly"])
            if upload_volume:
                command.remove("--rm")
                command.extend(["--mount", f"type=volume,source={upload_volume},target=/data/uploads"])
            for key in ("MYSQL_DATABASE", "MYSQL_USERNAME", "MYSQL_PASSWORD", "MYSQL_HOST", "REDIS_HOST", "REDIS_PASSWORD", "JWT_SECRET", "SPRING_PROFILES_ACTIVE"):
                command.extend(["--env", key])
            command.extend([args.backend_image or "carat-craft-backend:local", "java", "-Xmx384m", "-jar",
                            f"/app/platform-{module}.jar" if args.backend_image else "/verify/application.jar"])
            application_containers[module] = docker(*command, environment=env)
            containers.append(application_containers[module])
            mapping = docker("port", mysql, str(port) + "/tcp")
            if not re.fullmatch(r"127\.0\.0\.1:[0-9]+", mapping):
                raise ValueError("测试服务必须只绑定回环端口")
            bases[module] = "http://" + mapping + "/" + context
            wait_until(lambda: request(bases[module], "/internal/health/ready", method="GET"), timeout=150)
            print(module + "最新JAR已通过真实HTTP就绪检查", flush=True)
        if upload_volume:
            production = (ROOT / "deploy/nginx/default.conf").read_text(encoding="utf-8")
            location = production.split("    location /upload/ {", 1)[1].split("\n    }", 1)[0]
            conf = "server { listen 8082; root /usr/share/nginx/html;\nlocation /upload/ {" + location + "\n}}"
            nginx = ["run", "--detach", "--pull=never", "--network", "container:" + mysql,
                     "--name", "caratcraft-http-upload-" + suffix,
                     "--mount", f"type=volume,source={upload_volume},target=/usr/share/nginx/html/upload,readonly",
                     "--entrypoint", "sh", "nginx:1.27-alpine", "-ec",
                     'printf "%s\\n" "$1" > /etc/nginx/conf.d/default.conf; exec nginx -g "daemon off;"', "sh", conf]
            verify_upload_storage(mysql, application_containers["admin"], bases["admin"], admin_token, nginx, containers)
            return
        checks = verify_chain(bases["admin"], bases["api"], admin_token,
                              user_token(900001, secret), user_token(900002, secret), category_id)
        evidence = {"date": args.date, "database": database, "checks": checks, "passed": True,
                    "backendImage": args.backend_image,
                    "scope": "最新两应用真实HTTP、认证器、MyBatis/MySQL及Redis; 合成身份, 不调用微信或支付"}
        output = ROOT / "docs/资料/HTTP业务链路验证.json"
        output.write_text(json.dumps(evidence, ensure_ascii=False, indent=4) + "\n", encoding="utf-8", newline="\n")
        print(f"真实HTTP业务链路通过: {len(checks)}组边界", flush=True)
    finally:
        for container in reversed(containers):
            if upload_volume:
                docker("rm", "-f", container)
            else:
                docker("stop", "--time", "5", container)
        if upload_volume:
            labels = json.loads(docker("volume", "inspect", "--format", "{{json .Labels}}", upload_volume))
            if labels.get(UPLOAD_LABEL) != suffix:
                raise RuntimeError("上传测试卷归属变化, 拒绝清理")
            docker("volume", "rm", upload_volume)
        print("本轮隔离测试容器已清理", flush=True)


if __name__ == "__main__":
    main()
