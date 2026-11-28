package com.platform.modules.sys.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/** 只调用维护容器固定认证接口, 不将Docker接口或命令参数暴露给网页. */
@Service
public class MaintenanceClient {
    private final ObjectMapper json;
    private final URI base;
    private final Path tokenFile;
    private final HttpClient client =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    /** 从受信任部署配置读取内部地址与专用令牌文件. */
    public MaintenanceClient(
            ObjectMapper json,
            @Value("${caratcraft.maintenance-url:http://maintenance:8090}") String url,
            @Value("${caratcraft.maintenance-token-file:/run/platform-maintenance/token}")
                    String token) {
        this.json = json;
        base = URI.create(url);
        tokenFile = Path.of(token);
    }

    /** 获取固定的非敏感状态, 不接受客户端传入的URL. */
    public JsonNode read(String resource) {
        if (!java.util.Set.of("monitor", "release", "jobs").contains(resource)) {
            throw new IllegalArgumentException("无效维护查询");
        }
        return send(resource, HttpRequest.BodyPublishers.noBody(), false);
    }

    /** 只提交任务标识, 由维护服务核对任务状态及原安装身份. */
    public JsonNode action(String operation, String id) {
        if (!java.util.Set.of("confirm", "restore").contains(operation)) {
            throw new IllegalArgumentException("无效维护操作");
        }
        try {
            java.util.UUID.fromString(id);
            return send(
                    operation,
                    HttpRequest.BodyPublishers.ofString(json.writeValueAsString(Map.of("id", id))),
                    true);
        } catch (java.io.IOException error) {
            throw new IllegalStateException("维护请求编码失败");
        }
    }

    /** 流式转发发行包且明确长度, 避免在Java堆中保存镜像文件. */
    public JsonNode upload(InputStream input, long length) {
        if (length <= 0 || length > 4L * 1024 * 1024 * 1024) {
            throw new IllegalArgumentException("请选择不超过4 GiB的离线发行包");
        }
        return send(
                "packages",
                HttpRequest.BodyPublishers.fromPublisher(
                        HttpRequest.BodyPublishers.ofInputStream(() -> input), length),
                true);
    }

    /** 为固定目标添加内部认证, 失败只向业务界面返回受控摘要. */
    private JsonNode send(String resource, HttpRequest.BodyPublisher body, boolean post) {
        try {
            String token = Files.readString(tokenFile).strip();
            if (!token.matches("[A-Za-z0-9_-]{43}")) {
                throw new IllegalStateException("维护认证配置无效");
            }
            var request =
                    HttpRequest.newBuilder(base.resolve("/" + resource))
                            .timeout(post ? Duration.ofMinutes(15) : Duration.ofSeconds(10))
                            .header("Authorization", "Bearer " + token)
                            .header("Content-Type", "application/octet-stream")
                            .method(post ? "POST" : "GET", body)
                            .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.body().length() > 2 * 1024 * 1024) {
                throw new IllegalStateException("维护响应大小异常");
            }
            JsonNode result = json.readTree(response.body());
            if (response.statusCode() != 200) {
                throw new IllegalArgumentException(result.path("message").asText("维护操作未完成"));
            }
            return result;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("维护请求已中断");
        } catch (java.io.IOException error) {
            throw new IllegalStateException("维护服务暂不可用, 请核对标准启动结果");
        }
    }
}
