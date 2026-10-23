/*
 *
 *      Copyright (c) 2018-2099, hehailong All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the openwtai.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: hehailong(845367567@qq.com)
 *
 */
package com.platform.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * jwt工具类
 *
 * @author 何海龙
 */
@Slf4j
@ConfigurationProperties(prefix = "platform-api.jwt")
@Component
public class JwtUtils {
    private String secret;
    private long expire;
    private String header;

    /** 启动时拒绝空值、非法Base64和不足256位的JWT密钥, 不输出密钥本身. */
    @PostConstruct
    public void validateConfiguration() {
        generateKey(secret);
        if (expire <= 0 || header == null || header.isBlank()) {
            throw new IllegalStateException("JWT有效期和请求头必须配置");
        }
    }

    /** 生成jwt token */
    public String generateToken(Long userId) {
        Date nowDate = new Date();
        // 过期时间
        Date expireDate = new Date(nowDate.getTime() + expire * 1000);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(String.valueOf(userId))
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(generateKey(secret), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 验证JWT签名并提取声明, 无效令牌返回空值. */
    public Claims getClaimByToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(generateKey(secret))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.debug("JWT已过期");
            return e.getClaims();
        } catch (Exception e) {
            log.debug("JWT验证失败: {}", e.getClass().getSimpleName());
            return null;
        }
    }

    /** 严格解码部署者提供的Base64随机密钥. */
    private static SecretKey generateKey(String secret) {
        byte[] encodedKey;
        try {
            encodedKey = java.util.Base64.getDecoder().decode(secret == null ? "" : secret);
        } catch (IllegalArgumentException error) {
            throw new IllegalStateException("JWT_SECRET必须为至少32字节随机值的Base64编码");
        }
        if (encodedKey.length < 32) {
            throw new IllegalStateException("JWT_SECRET必须为至少32字节随机值的Base64编码");
        }
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "HmacSHA256");
    }

    /**
     * token是否过期
     *
     * @return true：过期
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    /** 读取当前对象的Secret属性. */
    public String getSecret() {
        return secret;
    }

    /** 更新当前对象的Secret属性. */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /** 读取当前对象的Expire属性. */
    public long getExpire() {
        return expire;
    }

    /** 更新当前对象的Expire属性. */
    public void setExpire(long expire) {
        this.expire = expire;
    }

    /** 读取当前对象的Header属性. */
    public String getHeader() {
        return header;
    }

    /** 更新当前对象的Header属性. */
    public void setHeader(String header) {
        this.header = header;
    }
}
