package com.platform.common.controller;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

/** 对外只返回状态和构建标识, 不泄露数据库地址、账号或依赖错误详情. */
@RestController
@RequestMapping("/internal/health")
public class ReadinessController {
    private final ObjectProvider<DataSource> databases;
    private final ObjectProvider<RedisConnectionFactory> caches;
    private final String version;
    private final String revision;

    /** 注入依赖提供者和构建元数据, 存活探针不触发数据库或缓存连接. */
    public ReadinessController(
            ObjectProvider<DataSource> databases,
            ObjectProvider<RedisConnectionFactory> caches,
            @Value("${BUILD_VERSION:${projectVersion:unknown}}") String version,
            @Value("${BUILD_REVISION:workspace}") String revision) {
        this.databases = databases;
        this.caches = caches;
        this.version = version;
        this.revision = revision;
    }

    /** 存活检查只确认HTTP应用可以响应. */
    @GetMapping("live")
    public ResponseEntity<Map<String, String>> live() {
        return ResponseEntity.ok(metadata("alive"));
    }

    /** 就绪检查验证数据库连接、当前关键表结构和Redis响应. */
    @GetMapping("ready")
    public ResponseEntity<Map<String, String>> ready() {
        try {
            DataSource source = databases.getIfAvailable();
            RedisConnectionFactory redis = caches.getIfAvailable();
            if (source == null || redis == null) {
                return ResponseEntity.status(503).body(metadata("not_ready"));
            }
            try (Connection connection = source.getConnection();
                    var statement =
                            connection.prepareStatement(
                                    "SELECT COUNT(*) FROM information_schema.tables WHERE"
                                        + " table_schema=DATABASE() AND table_name IN"
                                        + " ('CC_ORDER_PRICE_LOG','CC_DESIGN_SNAPSHOT','CC_SCHEMA_VERSION','CC_REFUND','CC_STOCK_LOG','CC_COMMUNITY_POST','CC_COMMUNITY_LIKE','CC_COMMUNITY_MEDIA','CC_COMMUNITY_LOG','CC_MERCHANT_CONTACT','CC_MERCHANT_CONTACT_LOG','CC_LEGAL_HEAD','CC_LEGAL_DOCUMENT','CC_LEGAL_ACCEPTANCE','CC_LOGIN_IDENTITY_LOCK','CC_ORDER_LEGAL')")) {
                statement.setQueryTimeout(3);
                try (var rows = statement.executeQuery()) {
                    if (!rows.next() || rows.getInt(1) != 16) {
                        return ResponseEntity.status(503).body(metadata("schema_required"));
                    }
                }
                try (var versionQuery =
                                connection.prepareStatement(
                                        "SELECT version FROM CC_SCHEMA_VERSION WHERE"
                                                + " id=1 AND version=1");
                        var versions = versionQuery.executeQuery()) {
                    if (!versions.next()) {
                        return ResponseEntity.status(503).body(metadata("schema_required"));
                    }
                }
            }
            try (var connection = redis.getConnection()) {
                if (!"PONG".equalsIgnoreCase(connection.ping())) {
                    return ResponseEntity.status(503).body(metadata("not_ready"));
                }
            }
            return ResponseEntity.ok(metadata("ready"));
        } catch (Exception error) {
            return ResponseEntity.status(503).body(metadata("not_ready"));
        }
    }

    /** 保持固定的非敏感响应字段, 不序列化异常对象. */
    private Map<String, String> metadata(String status) {
        return Map.of(
                "status", status, "version", version, "revision", revision, "requiredSchema", "1");
    }
}
