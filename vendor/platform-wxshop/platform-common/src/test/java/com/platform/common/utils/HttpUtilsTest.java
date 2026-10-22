package com.platform.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/** 只连接回环HTTP桩, 验证中文请求、响应和无正文响应. */
class HttpUtilsTest {
    /** UTF-8正文保持原文, 无响应实体时返回空串而不是空指针异常. */
    @Test
    void postPreservesUtf8AndAcceptsEmptyResponse() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicReference<String> request = new AtomicReference<>();
        server.createContext(
                "/echo",
                exchange -> {
                    request.set(
                            new String(
                                    exchange.getRequestBody().readAllBytes(),
                                    StandardCharsets.UTF_8));
                    byte[] response = "中文响应".getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, response.length);
                    try (var output = exchange.getResponseBody()) {
                        output.write(response);
                    }
                });
        server.createContext(
                "/empty",
                exchange -> {
                    exchange.sendResponseHeaders(204, -1);
                    exchange.close();
                });
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            assertEquals("中文响应", HttpUtils.sendPost(base + "/echo", "中文请求"));
            assertEquals("中文请求", request.get());
            assertEquals("", HttpUtils.sendPost(base + "/empty", ""));
        } finally {
            server.stop(0);
        }
    }
}
