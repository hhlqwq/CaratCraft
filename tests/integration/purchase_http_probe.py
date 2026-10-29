"""串联真实后台聚合接口、小程序SKU选择器及购物车/立即购买计价."""

from decimal import Decimal
import json
import secrets
import subprocess
import time


def verify_purchase_http(root, fixtures, mysql, image, env, output, docker, sql, call):
    """只写本轮合成商品及账号, 使用实际应用包、HTTP和客户端选择器."""
    admin_token = secrets.token_urlsafe(32)
    sql("DELETE FROM SYS_USER_TOKEN WHERE USER_ID='1'")
    sql("INSERT INTO SYS_USER_TOKEN(USER_ID,TOKEN,EXPIRE_TIME,UPDATE_TIME) VALUES('1','"
        + admin_token + "',DATE_ADD(NOW(),INTERVAL 1 HOUR),NOW())")
    arguments = ["--network", "container:" + mysql, "--memory", "768m", "--mount",
                 "type=bind,source=" + str(root / "backend/platform-admin/target/platform-admin.jar")
                 + ",target=/app/admin.jar,readonly"]
    for name in ("JWT_SECRET", "MYSQL_HOST", "MYSQL_PORT", "MYSQL_DATABASE", "MYSQL_USERNAME",
                 "MYSQL_PASSWORD", "REDIS_HOST", "REDIS_PORT", "REDIS_PASSWORD", "SPRING_PROFILES_ACTIVE"):
        arguments.extend(["--env", name])
    admin = fixtures.start("caratcraft-admin-" + fixtures.run_id,
                           [*arguments, image, "java", "-Xmx384m", "-jar", "/app/admin.jar"], env)
    cases = []

    def request(path, body=None, expected=200, management=False):
        """同时检查传输状态和业务码, 不将HTTP成功误当业务成功."""
        options = ({"base_url": "http://127.0.0.1:8080/platform-framework", "auth_token": admin_token}
                   if management else {})
        status, response = call(path, body, **options)
        assert status == expected, (path, status, response.get("msg"))
        assert response["code"] == (0 if expected == 200 else expected), (path, response)
        cases.append({"path": path.split('?')[0], "status": status})
        return response.get("data")

    try:
        for attempt in range(90):
            try:
                status, _ = call("/internal/health/ready", authenticated=False,
                                 base_url="http://127.0.0.1:8080/platform-framework")
                if status == 200:
                    break
            except (ConnectionError, TimeoutError, json.JSONDecodeError):
                pass
            if attempt % 10 == 0:
                print("隔离管理端启动进度: " + str(attempt * 2) + "秒", flush=True)
            time.sleep(2)
        else:
            raise RuntimeError("隔离管理端未就绪, 详见admin.log")
        template = request("/mall/goods/aggregate/1999105", management=True)
        goods = template["goods"]
        goods.update(id=None, primaryProductId=None, name="隔离HTTP具名SKU商品", goodsSn="HTTP-ONLY",
                     goodsNumber=8, retailPrice="88.50", isOnSale=1, isDelete=0,
                     pearlSource="FRESHWATER", jewelryType="NECKLACE")
        sql("INSERT INTO mall_specification(id,name,sort_order) VALUES(19001,'隔离颜色',1)")
        aggregate = {"goods": goods, "attributes": [], "gallery": [],
                     "specs": [{"specificationId": 19001, "values": [{"value": "白色"}, {"value": "金色"}]}],
                     "skuList": [{"goodsSpecificationIds": "19001:白色", "goodsNumber": 8,
                                  "retailPrice": "88.50", "goodsSn": "HTTP-WHITE"},
                                 {"goodsSpecificationIds": "19001:金色", "goodsNumber": 0,
                                  "retailPrice": "99.00", "goodsSn": "HTTP-GOLD"}]}
        goods_id = request("/mall/goods/aggregate/save", aggregate, management=True)
        detail = request("/app/goods/detail?id=" + str(goods_id), {})
        script = """const fs=require('node:fs'); const sku=require(process.argv[1]);
const d=JSON.parse(fs.readFileSync(0,'utf8')); const groups=d.specificationList;
const index=sku.prepare(d.productList,groups);
const ids=groups.map(g=>String(g.valueList.find(v=>v.value==='白色').id));
const selected=sku.findProduct(index,ids);
process.stdout.write(JSON.stringify({id:selected.id,stock:sku.availableStock(selected),price:selected.retailPrice}));"""
        selected = json.loads(subprocess.run(
            ["node", "-e", script, str(root / "apps/miniapp/services/goods-sku.js")],
            input=json.dumps(detail), text=True, encoding="utf-8", capture_output=True, check=True).stdout)
        assert selected["stock"] == 8 and Decimal(str(selected["price"])) == Decimal("88.50")
        cases.append({"name": "actual_miniapp_sku_selector", "status": "passed"})
        product_id = selected["id"]
        purchase = {"goodsId": goods_id, "productId": product_id, "number": 2}
        request("/app/cart/add", purchase)
        edited = request("/mall/goods/aggregate/" + str(goods_id), management=True)
        for sku in edited["skuList"]:
            sku["goodsNumber"] = None
            if sku["id"] == product_id:
                sku["retailPrice"] = "95.25"
        request("/mall/goods/aggregate/update", edited, management=True)
        for kind in ("cart", "buy"):
            context = request("/app/buy/add", purchase) if kind == "buy" else {}
            checkout = request("/app/cart/checkout?type=" + kind + "&couponId=0&checkoutId="
                               + context.get("checkoutId", ""), {})
            line = checkout["checkedGoodsList"][0]
            assert line["productId"] == product_id and line["number"] == 2
            assert Decimal(str(line["retailPrice"])) * line["number"] == Decimal("190.50")
        request("/app/cart/add", {**purchase, "number": 9}, expected=400)
        request("/app/buy/add", {**purchase, "goodsId": 1999105}, expected=400)
        empty_sku = next(sku for sku in detail["productList"] if sku["id"] != product_id)
        request("/app/buy/add", {**purchase, "productId": empty_sku["id"]}, expected=400)
        assert sql("SELECT goods_number FROM mall_product WHERE id=" + str(product_id)) == "8"
        cases.append({"name": "cart_and_buy_do_not_consume_stock", "status": "passed"})
        (output / "purchase-http.json").write_text(json.dumps(
            {"tests": len(cases), "passed": len(cases), "cases": cases,
             "browserAcceptance": False, "realPayment": False}, ensure_ascii=False, indent=4), encoding="utf-8")
        print("真实商品维护到购买链路通过: " + str(len(cases)) + "项", flush=True)
    finally:
        result = docker(["logs", admin], check=False)
        (output / "admin.log").write_text(result.stdout + result.stderr, encoding="utf-8")
