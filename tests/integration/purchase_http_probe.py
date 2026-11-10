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

    def request(path, body=None, expected=200, management=False, auth_token=None):
        """同时检查传输状态和业务码, 不将HTTP成功误当业务成功."""
        options = ({"base_url": "http://127.0.0.1:8080/platform-framework", "auth_token": admin_token}
                   if management else {})
        if auth_token is not None:
            options["auth_token"] = auth_token
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
        address = request("/app/address/save", {
            "userName": "隔离收货人", "provinceName": "隔离省", "cityName": "隔离市",
            "countyName": "隔离区", "detailInfo": "仅供测试的地址", "telNumber": "13800000000"})
        assert address["userId"] == 9_000_001 and address["isDefault"] == 0
        address_id = address["id"]
        owned = request("/app/address/detail?id=" + str(address_id), {})
        assert owned["id"] == address_id
        request("/app/address/save", {"userName": "不完整输入"}, expected=400)
        oversized = {key: address[key] for key in (
            "id", "userName", "telNumber", "provinceName", "cityName", "countyName", "detailInfo")}
        request("/app/address/save", {**oversized, "userName": "测" * 51, "isDefault": False}, expected=400)
        sql("UPDATE mall_address SET user_id=9000002 WHERE id=" + str(address_id))
        request("/app/address/detail?id=" + str(address_id), {}, expected=403)
        request("/app/address/delete", {"id": address_id}, expected=404)
        sql("UPDATE mall_address SET user_id=9000001 WHERE id=" + str(address_id))
        request("/app/address/delete", {"id": address_id})
        assert sql("SELECT COUNT(*) FROM mall_address WHERE id=" + str(address_id)) == "0"
        # 合成正文只存在本轮随机测试库, 不作为商家正式协议发布.
        sql("INSERT INTO CC_LEGAL_DOCUMENT(kind,version,title,content,status,operator_id) VALUES"
            "('TERMS',1,'隔离协议','仅测试','PUBLISHED','test'),('PRIVACY',1,'隔离隐私','仅测试','PUBLISHED','test');"
            "UPDATE CC_LEGAL_HEAD SET published_version=1;"
            "INSERT INTO CC_LEGAL_ACCEPTANCE(user_id,kind,version) VALUES(9000001,'TERMS',1),(9000001,'PRIVACY',1);")
        address = request("/app/address/save", {"userName": "隔离订单收货人", "provinceName": "隔离省",
            "cityName": "隔离市", "countyName": "隔离区", "detailInfo": "仅供下单测试", "telNumber": "13800000000"})
        cart_checkout = request("/app/cart/checkout?type=cart&addressId=" + str(address["id"]), {})
        submit = {"addressId": address["id"], "type": "cart", "requestKey": "http_frozen_items_001",
                  "expectedActualPrice": cart_checkout["actualPrice"],
                  "expectedItems": [{"goodsId": goods_id, "productId": product_id, "number": purchase["number"]}]}
        changed = dict(submit, expectedItems=[{"goodsId": goods_id, "productId": empty_sku["id"], "number": purchase["number"]}])
        request("/app/order/submit", changed, expected=409)
        created = request("/app/order/submit", submit)
        retried = request("/app/order/submit", submit)
        assert created["orderInfo"]["id"] == retried["orderInfo"]["id"]
        request("/app/order/submit", changed, expected=400)
        assert int(sql("SELECT goods_number FROM mall_product WHERE id=" + str(product_id))) == 8 - purchase["number"]
        orders = request("/app/order/list?page=1&size=1", {})
        assert orders["total"] == 1 and orders["current"] == 1 and orders["size"] == 1 and len(orders["records"]) == 1
        request("/app/feedback/save", {"index": 1, "mobile": "13800000000", "content": "  隔离反馈😀  "})
        request("/app/feedback/save", {"index": 0, "mobile": "13800000000", "content": "无效分类"}, expected=400)
        request("/app/feedback/save", {"index": 1, "mobile": "123", "content": "无效号码"}, expected=400)
        assert sql("SELECT COUNT(*) FROM mall_feedback WHERE user_id=9000001 AND status=0 AND feed_type=1 AND content='隔离反馈😀'") == "1"
        sql("DELETE FROM mall_footprint WHERE user_id IN(9000001,9000002)")
        sql("INSERT INTO mall_footprint(id,user_id,goods_id,add_time) VALUES"
            f"(19950101,9000001,{goods_id},1735749000),(19950102,9000001,{goods_id},1735749000),"
            f"(19950103,9000001,1999105,1735749000),(19950104,9000002,{goods_id},1735749000)")
        footprints = request("/app/footprint/list?page=1&size=1", {})
        next_footprints = request("/app/footprint/list?page=2&size=1", {})
        assert footprints["total"] == next_footprints["total"] == 2
        assert footprints["records"][0]["id"] == 19950103 and next_footprints["records"][0]["id"] == 19950102
        sql("UPDATE mall_footprint SET referrer=9000001 WHERE id=19950102")
        shared = request("/app/footprint/sharelist?page=1&size=20", {})["data"]
        assert [(row["id"], row["nickname"]) for row in shared] == [(19950102, "隔离HTTP用户")]
        request("/app/footprint/list?page=1&size=101", {}, expected=400)
        request("/app/footprint/delete?footprintId=19950104", {}, expected=404)
        request("/app/footprint/delete?footprintId=19950102", {})
        assert sql(f"SELECT COUNT(*) FROM mall_footprint WHERE user_id=9000001 AND goods_id={goods_id}") == "0"
        assert sql("SELECT COUNT(*) FROM mall_footprint WHERE id=19950104") == "1"
        goods_page = request("/custom/configuration/goods?page=1&size=1", management=True)
        assert len(goods_page["records"]) == 1 and goods_page["total"] >= 1
        assert set(goods_page["records"][0]) == {"id", "name"}
        request("/custom/configuration/goods?page=1&size=101", expected=400, management=True)
        first = request("/custom/configuration/options?goodsId=1999105&page=1&size=100&keyword=client-", management=True)
        second = request("/custom/configuration/options?goodsId=1999105&page=2&size=100&keyword=client-", management=True)
        assert first["total"] == second["total"] == 112
        assert len(first["records"]) == 100 and len(second["records"]) == 12
        assert len({row["id"] for row in first["records"] + second["records"]}) == 112
        assert all(row["enabled"] == 0 for row in first["records"] + second["records"])
        request("/custom/configuration/options?goodsId=1999105&page=0&size=20", expected=400, management=True)
        (output / "purchase-http.json").write_text(json.dumps(
            {"tests": len(cases), "passed": len(cases), "cases": cases,
             "browserAcceptance": False, "realPayment": False}, ensure_ascii=False, indent=4), encoding="utf-8")
        print("真实商品维护到购买链路通过: " + str(len(cases)) + "项", flush=True)
    finally:
        result = docker(["logs", admin], check=False)
        (output / "admin.log").write_text(result.stdout + result.stderr, encoding="utf-8")
