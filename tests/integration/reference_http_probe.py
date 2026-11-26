"""在本次测试网络启动刚构建的API, 验证真实HTTP认证、规则及旧AI路由移除."""

import base64
import hashlib
import hmac
import json
import secrets
import subprocess
import time
import zipfile


def encoded(value):
    """按JWT标准编码合成测试身份, 不访问真实微信或读取业务用户令牌."""
    return base64.urlsafe_b64encode(json.dumps(value, separators=(",", ":")).encode()).rstrip(b"=")


def verify_reference_http(root, fixtures, mysql, image, env, output, docker):
    """只操作本轮随机夹具, 认证密钥随机生成且不进入日志或命令参数."""
    jar = root / "backend/platform-api/target/platform-api.jar"
    with zipfile.ZipFile(jar) as archive:
        if any("AiRecommendation" in name for name in archive.namelist()):
            raise AssertionError("干净交付包中仍存在旧AI类")
        if any(name.endswith("/ApiAddressSaveRequest.class") for name in archive.namelist()):
            raise AssertionError("干净交付包中仍存在已迁走的地址输入类")
        if not any(name.endswith("AppNecklaceReferenceController.class") for name in archive.namelist()):
            raise AssertionError("当前交付包缺少规则参考入口")
    user_id = 9_000_001

    def sql(statement):
        """仅在随机隔离库创建合成用户或读取验证计数."""
        return docker(["exec", "--env", "MYSQL_PWD", mysql, "mysql", "-uroot",
                       "--default-character-set=utf8mb4", "--batch", "--skip-column-names",
                       env["MYSQL_DATABASE"], "-e", statement], env).stdout.strip()

    sql("INSERT INTO WX_USER(ID,NICKNAME) VALUES(" + str(user_id) + ",'隔离HTTP用户')")
    before = sql("SELECT COUNT(*) FROM CC_DESIGN_SNAPSHOT WHERE user_id=" + str(user_id))
    key = secrets.token_bytes(32)
    application_env = env.copy()
    application_env.update({"JWT_SECRET": base64.b64encode(key).decode(), "MYSQL_HOST": "127.0.0.1",
                            "MYSQL_PORT": "3306", "MYSQL_USERNAME": env["MYSQL_USER"],
                            "REDIS_HOST": "127.0.0.1", "REDIS_PORT": "6379", "REDIS_PASSWORD": "",
                            "SPRING_PROFILES_ACTIVE": "docker"})
    arguments = ["--network", "container:" + mysql, "--memory", "768m",
                 "--mount", "type=bind,source=" + str(jar) + ",target=/app/platform-api.jar,readonly"]
    for name in ("JWT_SECRET", "MYSQL_HOST", "MYSQL_PORT", "MYSQL_DATABASE", "MYSQL_USERNAME",
                 "MYSQL_PASSWORD", "REDIS_HOST", "REDIS_PORT", "REDIS_PASSWORD", "SPRING_PROFILES_ACTIVE"):
        arguments.extend(["--env", name])
    application = fixtures.start("caratcraft-api-" + fixtures.run_id,
                                 [*arguments, image, "java", "-Xmx384m", "-jar", "/app/platform-api.jar"], application_env)
    base = "http://127.0.0.1:8081/platform-framework-api"
    header = encoded({"alg": "HS256", "typ": "JWT"})
    payload = encoded({"sub": str(user_id), "iat": int(time.time()), "exp": int(time.time()) + 900})
    unsigned = header + b"." + payload
    token = (unsigned + b"." + base64.urlsafe_b64encode(hmac.new(key, unsigned, hashlib.sha256).digest()).rstrip(b"=")).decode()

    def call(path, data=None, authenticated=True, *, base_url=base, auth_token=token,
             raw_body=None, content_type="application/json"):
        """从测试容器请求回环HTTP, 令牌仅经标准输入传递, 不发布端口或继承代理."""
        config = ['url = ' + json.dumps(base_url + path), 'header = ' + json.dumps('Content-Type: ' + content_type),
                  'silent', 'show-error', 'max-time = 5', 'noproxy = "*"',
                  'write-out = "\\n%{http_code}"']
        if authenticated:
            config.append('header = ' + json.dumps('token: ' + auth_token))
        if data is not None or raw_body is not None:
            config.append('data = ' + json.dumps(raw_body if raw_body is not None else json.dumps(data)))
        result = subprocess.run(['docker', 'exec', '-i', application, 'curl', '--config', '-'],
                                input='\n'.join(config), capture_output=True, text=True,
                                encoding='utf-8', timeout=15)
        if result.returncode:
            raise ConnectionError("隔离HTTP连接未就绪")
        body, status = result.stdout.rsplit('\n', 1)
        return int(status), json.loads(body)

    print("等待隔离API启动, 不替换业务运行实例", flush=True)
    ready = False
    try:
        for attempt in range(90):
            try:
                if call("/internal/health/ready", authenticated=False)[0] == 200:
                    ready = True
                    break
            except (TimeoutError, json.JSONDecodeError, ConnectionError):
                pass
            if attempt % 10 == 0:
                print("隔离API启动进度: " + str(attempt * 2) + "秒", flush=True)
            time.sleep(2)
        if not ready:
            raise RuntimeError("隔离API未就绪, 详见当前验证目录api.log")
        cases = []

        def check(name, path, body, expected, authenticated=True):
            """同时验证真实HTTP状态与业务码, 不将传输成功当作业务成功."""
            status, result = call(path, body, authenticated)
            assert status == expected, (name, status)
            if expected != 404:
                assert result["code"] == (0 if expected == 200 else expected), name
            cases.append({"name": name, "status": status})
            return result.get("data")

        check("feedback_requires_login", "/app/feedback/save", {"index": 1, "content": "匿名"}, 401, False)
        check("feedback_without_mobile", "/app/feedback/save", {"index": 1, "content": "隔离反馈选填"}, 200)
        assert sql("SELECT COUNT(*) FROM mall_feedback WHERE user_id=9000001 AND mobile='' AND content='隔离反馈选填'") == "1"
        check("question_requires_login", "/app/goods/questions", {"goodsId": 1999105, "question": "未登录"}, 401, False)
        check("question_pending", "/app/goods/questions", {"goodsId": 1999105, "question": "隔离待审提问"}, 200)
        public = check("pending_question_hidden", "/app/goods/questions?goodsId=1999105", None, 200, False)
        assert public["total"] == 0
        assert sql("SELECT COUNT(*) FROM CC_GOODS_QUESTION WHERE user_id=9000001 AND status='PENDING'") == "1"
        print("反馈选填及商品问答隔离HTTP通过", flush=True)

        materials = sql(
            "SELECT component_type,id FROM CC_COMPONENT_OPTION WHERE goods_id=1999105 "
            "AND enabled=1 ORDER BY id")
        grouped = {}
        for line in materials.splitlines():
            kind, identity = line.split("\t")
            grouped.setdefault(kind, []).append(int(identity))
        design = {"goodsId": 1999105, "arrangementOptionIds": [
            grouped["chain"][0], grouped["pearl"][0], grouped["pearl"][1],
            grouped["accessory"][0], grouped["clasp"][0]]}
        quote = check("mixed_necklace_quote", "/app/customization/quote", design, 200)
        assert quote["totalPrice"] > 0
        saved = check("save_mixed_necklace", "/app/customization/save", design, 200)
        snapshot_id = saved["snapshotId"]
        snapshot = check("restore_mixed_necklace", "/app/customization/detail?snapshotId=" + str(snapshot_id), {}, 200)
        assert json.loads(snapshot["configurationJson"])["arrangementOptionIds"] == design["arrangementOptionIds"]
        message = {"requestKey": "review_isolated_share", "kind": "DESIGN", "title": "隔离设计分享",
                   "content": "隔离验证", "mediaIds": [], "snapshotId": snapshot_id, "designConsent": True}
        posted = check("share_saved_necklace", "/app/community/publish", message, 200)
        dup = check("share_retry_idempotent", "/app/community/publish", message, 200)
        assert posted["id"] == dup["id"]
        public_status, _ = call("/app/community/detail?id=" + str(posted["id"]), authenticated=False)
        assert public_status in (400, 404)
        sql("UPDATE CC_COMMUNITY_POST SET status='PUBLISHED' WHERE id=" + str(int(posted["id"])))
        public = check("reviewed_necklace_public", "/app/community/detail?id=" + str(posted["id"]), None, 200, False)
        public_design = json.loads(public["designJson"])
        assert public_design["goodsId"] == 1999105 and len(public_design["arrangement"]) == 5
        assert not any(key in public_design for key in ("neckCircumferenceMm", "engraving", "snapshotId"))
        before = sql("SELECT COUNT(*) FROM CC_DESIGN_SNAPSHOT WHERE user_id=" + str(user_id))
        print("混合项链报价、保存、恢复及社区幂等隔离HTTP通过", flush=True)

        body = {"goodsId": 1999105, "preference": "MEDIUM", "neckCircumferenceMm": None}
        sql("UPDATE CC_CUSTOM_PRODUCT SET enabled=0 WHERE goods_id=1999105")
        check("authentication_required", "/app/customization/reference", body, 401, False)
        check("unpublished_template_rejected", "/app/customization/reference", body, 400)
        # 新材料目录默认停用; 仅在本轮隔离库配置独立测试材料, 不启用客户候选.
        sql("""
UPDATE CC_CUSTOM_PRODUCT SET enabled=1,length_rules=NULL WHERE goods_id=1999105;
UPDATE CC_COMPONENT_OPTION SET enabled=0 WHERE goods_id=1999105;
INSERT INTO CC_COMPONENT_OPTION(goods_id,component_type,option_code,name,price_delta,material_params,enabled)
VALUES(1999105,'pearl','reference-isolated-fixture','隔离规则参考材料',0,
       '{"stockQuantity":0,"stockUnit":"件"}',1);
""")
        assert sql("SELECT COUNT(*) FROM CC_COMPONENT_OPTION WHERE option_code LIKE 'client-%' AND enabled=1") == "0"
        generic = check("generic_reference", "/app/customization/reference", body, 200)
        assert generic["lengthsMm"] == [457.5, 507.5] and not generic["measured"]
        material_names = [item["name"] for item in generic["materials"]]
        assert material_names == ["隔离规则参考材料"], material_names
        assert generic["sources"][0]["url"].startswith("https://www.tiffany.com/")
        for content_type, expected in (("application/json; charset=UTF-8", 200), ("application/merge-patch+json", 415)):
            status, result = call("/app/customization/reference", body, content_type=content_type)
            assert status == expected and result["code"] == (0 if expected == 200 else expected)
            cases.append({"name": "json_media_type", "status": status})
        status, result = call("/app/customization/reference", raw_body='{"x":1,"x":2}', content_type="application/merge-patch+json")
        assert status == 400 and result["code"] == 400
        cases.append({"name": "suffix_json_validated_before_mvc", "status": status})
        for name, raw, expected in (("duplicate_json_key", '{"goodsId":1999105,"goodsId":1}', 400),
                                     ("trailing_json", '{}{}', 400),
                                     ("oversized_json", ' ' * (6 * 1024 * 1024 + 1), 413)):
            status, result = call("/app/customization/reference", raw_body=raw)
            assert status == expected and result["code"] == expected, (name, status)
            cases.append({"name": name, "status": status})
        measured = check("measured_filter", "/app/customization/reference", {**body, "neckCircumferenceMm": 480}, 200)
        assert measured["lengthsMm"] == [507.5]
        empty = check("no_intersection", "/app/customization/reference", {**body, "neckCircumferenceMm": 800}, 200)
        assert empty["lengthsMm"] == []
        check("invalid_measurement", "/app/customization/reference", {**body, "neckCircumferenceMm": 199}, 400)
        check("invalid_preference", "/app/customization/reference", {**body, "preference": "other"}, 400)
        check("unknown_goods", "/app/customization/reference", {**body, "goodsId": 2_000_000_000}, 400)
        for path in ("status", "prepare", "validate"):
            check("old_ai_" + path + "_removed", "/app/customization/ai/" + path,
                  None if path == "status" else {}, 404)
        assert before == sql("SELECT COUNT(*) FROM CC_DESIGN_SNAPSHOT WHERE user_id=" + str(user_id))
        cases.append({"name": "no_implicit_draft_write", "status": "passed"})
        (output / "http.json").write_text(json.dumps({"tests": len(cases), "passed": len(cases),
                                                     "cases": cases}, ensure_ascii=False, indent=4), encoding="utf-8")
        print("真实HTTP参考接口验证通过: " + str(len(cases)) + "项", flush=True)
        from purchase_http_probe import verify_purchase_http
        verify_purchase_http(root, fixtures, mysql, image, application_env, output, docker, sql, call)
    finally:
        result = docker(["logs", application], check=False)
        (output / "api.log").write_text(result.stdout + result.stderr, encoding="utf-8")
