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
package com.platform.common.xss;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.platform.common.utils.JsonContract;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 同步请求的有界JSON和HTML值净化, 不改写协议头或JSON字段名称.
 *
 * @author hehailong
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    static final int MAX_JSON_BYTES = 6 * 1024 * 1024;
    private final boolean json;
    private byte[] body;

    /** 保留原始请求, 不在构造阶段读取上传流或复用有状态净化器. */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        json = isJson(request.getContentType());
    }

    /** 识别带charset的JSON及标准+json媒体类型, 错误媒体类型交由MVC处理. */
    private boolean isJson(String contentType) {
        if (contentType == null) {
            return false;
        }
        try {
            var type = MediaType.parseMediaType(contentType);
            String subtype = type.getSubtype().toLowerCase(Locale.ROOT);
            return "application".equalsIgnoreCase(type.getType())
                    && ("json".equals(subtype) || subtype.endsWith("+json"));
        } catch (IllegalArgumentException error) {
            return false;
        }
    }

    /** 沿用Nginx普通请求6MiB边界, 固定UTF-8并拒绝重复键及尾随JSON. */
    void prepareJsonBody() throws IOException {
        if (!json || body != null) {
            return;
        }
        var charset = MediaType.parseMediaType(super.getContentType()).getCharset();
        if (charset != null && !StandardCharsets.UTF_8.equals(charset)) {
            throw new IllegalArgumentException("JSON须使用UTF-8");
        }
        if (super.getContentLengthLong() > MAX_JSON_BYTES) {
            throw new JsonBodyTooLargeException();
        }
        byte[] raw = super.getInputStream().readNBytes(MAX_JSON_BYTES + 1);
        if (raw.length > MAX_JSON_BYTES) {
            throw new JsonBodyTooLargeException();
        }
        final String text;
        try {
            text =
                    StandardCharsets.UTF_8
                            .newDecoder()
                            .onMalformedInput(CodingErrorAction.REPORT)
                            .onUnmappableCharacter(CodingErrorAction.REPORT)
                            .decode(ByteBuffer.wrap(raw))
                            .toString();
        } catch (CharacterCodingException error) {
            throw new IllegalArgumentException("JSON须使用有效UTF-8", error);
        }
        if (text.isBlank()) {
            body = raw;
            return;
        }
        JsonNode root = JsonContract.read(text, JsonNode.class);
        body = JsonContract.write(sanitize(root, 0)).getBytes(StandardCharsets.UTF_8);
        if (body.length > MAX_JSON_BYTES) {
            throw new JsonBodyTooLargeException();
        }
    }

    /** 只处理文本值, 保留数组、键名、数值及null, 并限制递归深度. */
    private JsonNode sanitize(JsonNode node, int depth) {
        if (depth > 64) {
            throw new IllegalArgumentException("JSON嵌套层数超过限制");
        }
        if (node.isTextual()) {
            return TextNode.valueOf(xssEncode(node.textValue()));
        }
        if (node instanceof ObjectNode object) {
            var fields = object.fields();
            while (fields.hasNext()) {
                var field = fields.next();
                object.set(field.getKey(), sanitize(field.getValue(), depth + 1));
            }
        } else if (node instanceof ArrayNode array) {
            for (int index = 0; index < array.size(); index++) {
                array.set(index, sanitize(array.get(index), depth + 1));
            }
        }
        return node;
    }

    /** JSON每次返回独立的已缓存流, 其他上传及回调流保持原协议. */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (!json) {
            return super.getInputStream();
        }
        prepareJsonBody();
        return new CachedInputStream(body);
    }

    /** 文本读取与JSON字节读取使用同一份缓存及编码. */
    @Override
    public BufferedReader getReader() throws IOException {
        return json
                ? new BufferedReader(
                        new InputStreamReader(getInputStream(), StandardCharsets.UTF_8))
                : super.getReader();
    }

    /** JSON固定为UTF-8, 不影响非JSON请求的编码. */
    @Override
    public String getCharacterEncoding() {
        return json ? StandardCharsets.UTF_8.name() : super.getCharacterEncoding();
    }

    /** 净化前长度未知, 净化后报告真实缓存长度而非旧Content-Length. */
    @Override
    public int getContentLength() {
        return json ? (body == null ? -1 : body.length) : super.getContentLength();
    }

    /** 长整数长度遵循与普通长度相同的缓存契约. */
    @Override
    public long getContentLengthLong() {
        return json ? (body == null ? -1 : body.length) : super.getContentLengthLong();
    }

    /** 仅净化参数值, 不改写查找名称. */
    @Override
    public String getParameter(String name) {
        return xssEncode(super.getParameter(name));
    }

    /** 多值参数先复制, 不修改容器持有的原始数组. */
    @Override
    public String[] getParameterValues(String name) {
        String[] original = super.getParameterValues(name);
        if (original == null) {
            return null;
        }
        String[] values = original.clone();
        for (int index = 0; index < values.length; index++) {
            values[index] = xssEncode(values[index]);
        }
        return values;
    }

    /** 参数映射使用独立数组, 不在多个读取入口重复编码原始值. */
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> values = new LinkedHashMap<>();
        for (String name : super.getParameterMap().keySet()) {
            values.put(name, getParameterValues(name));
        }
        return values;
    }

    /** 没有HTML起始符的文本保留原值, HTML净化器每次独立创建以隔离标签计数. */
    private String xssEncode(String input) {
        return input == null || input.indexOf('<') < 0 ? input : new HtmlFilter().filter(input);
    }

    /** 只向明确需要原始协议的内部调用方提供原请求. */
    public HttpServletRequest getOrgRequest() {
        return (HttpServletRequest) getRequest();
    }

    /** 解包本类包装器, 不猜测其他包装器的内部状态. */
    public static HttpServletRequest getOrgRequest(HttpServletRequest request) {
        return request instanceof XssHttpServletRequestWrapper wrapper
                ? wrapper.getOrgRequest()
                : request;
    }

    /** 请求体边界异常由过滤器明确映射为413. */
    static final class JsonBodyTooLargeException extends IOException {
        /** 错误不携带正文或其他用户资料. */
        JsonBodyTooLargeException() {
            super("JSON请求体不能超过6MiB");
        }
    }

    /** 内存缓存只提供当前同步Servlet链所需的读取协议. */
    private static final class CachedInputStream extends ServletInputStream {
        private final ByteArrayInputStream input;

        /** 每个读取入口拥有独立游标, 不共享已消费位置. */
        private CachedInputStream(byte[] bytes) {
            input = new ByteArrayInputStream(bytes);
        }

        /** EOF状态与实际剩余字节一致. */
        @Override
        public boolean isFinished() {
            return input.available() == 0;
        }

        /** 内存读取不会等待网络数据. */
        @Override
        public boolean isReady() {
            return true;
        }

        /** 当前过滤器注册为同步链, 非阻塞读取不得静默成功. */
        @Override
        public void setReadListener(ReadListener listener) {
            throw new IllegalStateException("同步请求不支持非阻塞读取");
        }

        /** 单字节读取遵循标准EOF语义. */
        @Override
        public int read() {
            return input.read();
        }

        /** 批量读取直接使用缓存, 避免逐字节复制. */
        @Override
        public int read(byte[] target, int offset, int length) {
            return input.read(target, offset, length);
        }
    }
}
