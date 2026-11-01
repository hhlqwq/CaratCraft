package com.platform.common.xss;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.platform.common.exception.BusinessExceptionHandler;
import com.platform.common.exception.RestStatusAdvice;
import com.platform.common.utils.JsonContract;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/** 真实JSON解析、Servlet流及并发净化回归, 不使用替身净化器. */
class XssRequestTest {
    /** 使用真实请求字节和带charset的媒体类型创建输入. */
    private MockHttpServletRequest request(String text) {
        var request = new MockHttpServletRequest();
        request.setContentType("application/json; charset=UTF-8");
        request.setContent(text.getBytes(StandardCharsets.UTF_8));
        return request;
    }

    /** JSON键名、数值、数组和嵌套JSON字符串不被当作HTML文档处理. */
    @Test
    void preservesStructureAndPlainText() throws Exception {
        var original =
                Map.of(
                        "plain",
                        "a & b \"引号\" \\路径",
                        "raw",
                        "{\"stockQuantity\":2}",
                        "html",
                        "<script>bad</script><b>好",
                        "number",
                        new BigDecimal("123456789.123456789"),
                        "<key>",
                        List.of(true, 7));
        var wrapped = new XssHttpServletRequestWrapper(request(JsonContract.write(original)));
        var tree =
                JsonContract.read(
                        new String(wrapped.getInputStream().readAllBytes(), StandardCharsets.UTF_8),
                        JsonNode.class);
        assertEquals(original.get("plain"), tree.path("plain").textValue());
        assertEquals(original.get("raw"), tree.path("raw").textValue());
        assertEquals(original.get("number"), tree.path("number").decimalValue());
        assertTrue(tree.path("<key>").isArray());
        assertTrue(tree.path("<key>").get(0).booleanValue());
        assertFalse(tree.path("html").textValue().contains("<script>"));
        assertTrue(tree.path("html").textValue().endsWith("</b>"));
        var skuInput =
                JsonContract.write(
                        Map.of(
                                "skuList",
                                Collections.nCopies(
                                        512, Map.of("goodsSpecificationIds", "珠".repeat(2048)))));
        var large = new XssHttpServletRequestWrapper(request(skuInput));
        var skuTree =
                JsonContract.read(
                        new String(large.getInputStream().readAllBytes(), StandardCharsets.UTF_8),
                        JsonNode.class);
        assertEquals(512, skuTree.path("skuList").size());
    }

    /** 每次读取拥有独立游标, EOF和长度准确, 非阻塞监听明确拒绝. */
    @Test
    void replaysCachedBodyAndReportsEof() throws Exception {
        var wrapped = new XssHttpServletRequestWrapper(request("{\"html\":\"<b>好\"}"));
        var first = wrapped.getInputStream();
        assertFalse(first.isFinished());
        byte[] bytes = first.readAllBytes();
        assertTrue(first.isFinished());
        assertEquals(-1, first.read());
        assertEquals(bytes.length, wrapped.getContentLength());
        assertEquals(bytes.length, wrapped.getContentLengthLong());
        assertArrayEquals(bytes, wrapped.getInputStream().readAllBytes());
        assertEquals(new String(bytes, StandardCharsets.UTF_8), wrapped.getReader().readLine());
        assertThrows(
                IllegalStateException.class, () -> first.setReadListener(mock(ReadListener.class)));
    }

    /** 空JSON保持可重复读取, 流和文本读取都准确报告EOF. */
    @Test
    void supportsEmptyAndSuffixJson() throws Exception {
        var empty = new XssHttpServletRequestWrapper(request(""));
        assertEquals(0, empty.getInputStream().readAllBytes().length);
        assertEquals(-1, empty.getReader().read());
    }

    /** 参数数组是副本, 参数名称及授权头不被HTML改写. */
    @Test
    void doesNotMutateParametersOrHeaders() {
        var request = new MockHttpServletRequest();
        request.addParameter("<key>", "<b>一", "A&B");
        request.addHeader("Authorization", "<opaque>&credential");
        var wrapped = new XssHttpServletRequestWrapper(request);
        assertEquals("<b>一</b>", wrapped.getParameter("<key>"));
        String[] values = wrapped.getParameterValues("<key>");
        assertNotSame(request.getParameterValues("<key>"), values);
        wrapped.getParameterMap().get("<key>")[0] = "changed";
        assertEquals("<b>一", request.getParameter("<key>"));
        assertEquals("A&B", values[1]);
        assertEquals("<opaque>&credential", wrapped.getHeader("Authorization"));
    }

    /** 结构错误和重复键在业务链之前400, 已知和未知长度超限均413. */
    @Test
    void rejectsMalformedAndOversizedBodies() throws Exception {
        for (String input :
                List.of(
                        "{bad",
                        "{}{}",
                        "{\"x\":1,\"x\":2}",
                        "[".repeat(65) + "0" + "]".repeat(65))) {
            FilterChain chain = mock();
            var response = new MockHttpServletResponse();
            new XssFilter().doFilter(request(input), response, chain);
            assertEquals(400, response.getStatus());
            assertEquals(
                    400,
                    JsonContract.object(response.getContentAsString()).path("code").intValue());
            verifyNoInteractions(chain);
        }
        for (boolean unknown : List.of(false, true)) {
            var request =
                    new MockHttpServletRequest() {
                        /** 模拟分块请求, 不信任Content-Length作为唯一边界. */
                        @Override
                        public long getContentLengthLong() {
                            return unknown ? -1 : super.getContentLengthLong();
                        }
                    };
            request.setContentType("application/json");
            request.setContent(new byte[XssHttpServletRequestWrapper.MAX_JSON_BYTES + 1]);
            FilterChain chain = mock();
            var response = new MockHttpServletResponse();
            new XssFilter().doFilter(request, response, chain);
            assertEquals(413, response.getStatus());
            assertEquals(
                    413,
                    JsonContract.object(response.getContentAsString()).path("code").intValue());
            verifyNoInteractions(chain);
        }
        var invalid = request("");
        invalid.setContent(new byte[] {(byte) 0xc3, 0x28});
        var response = new MockHttpServletResponse();
        FilterChain chain = mock();
        new XssFilter().doFilter(invalid, response, chain);
        assertEquals(400, response.getStatus());
        verifyNoInteractions(chain);
    }

    /** JSON真实MVC链接受charset和+json, XML回调正文保持原字节含义. */
    @Test
    void mvcSeesValidJsonAndUnchangedXml() throws Exception {
        var mvc =
                MockMvcBuilders.standaloneSetup(new Echo())
                        .setControllerAdvice(new BusinessExceptionHandler(), new RestStatusAdvice())
                        .addFilters(new XssFilter())
                        .build();
        for (String type :
                List.of("application/json; charset=UTF-8", "application/merge-patch+json")) {
            var result =
                    mvc.perform(
                                    post("/echo")
                                            .contentType(type)
                                            .content("{\"html\":\"<b>好\",\"empty\":null}"))
                            .andReturn();
            assertEquals(200, result.getResponse().getStatus());
            var data =
                    JsonContract.object(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
            assertEquals("<b>好</b>", data.path("html").textValue());
            assertTrue(data.path("empty").isNull());
        }
        String xml = "<xml><signature>A&B</signature></xml>";
        var result =
                mvc.perform(post("/xml").contentType("application/xml").content(xml)).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(xml, result.getResponse().getContentAsString());
        var unsupported =
                mvc.perform(
                                post("/json-only")
                                        .contentType("application/merge-patch+json")
                                        .content("{}"))
                        .andReturn();
        assertEquals(415, unsupported.getResponse().getStatus());
        assertEquals(
                415,
                JsonContract.object(
                                unsupported
                                        .getResponse()
                                        .getContentAsString(StandardCharsets.UTF_8))
                        .path("code")
                        .intValue());
    }

    /** 多线程交替处理未闭合标签时互不借用标签计数. */
    @Test
    void concurrentRequestsDoNotShareTagCounters() throws Exception {
        try (var executor = Executors.newFixedThreadPool(8)) {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int index = 0; index < 200; index++) {
                String tag = index % 2 == 0 ? "b" : "i";
                String input = "<" + tag + ">内容" + index;
                tasks.add(
                        () -> {
                            var request = new MockHttpServletRequest();
                            request.addParameter("text", input);
                            return (input + "</" + tag + ">")
                                    .equals(
                                            new XssHttpServletRequestWrapper(request)
                                                    .getParameter("text"));
                        });
            }
            for (var future : executor.invokeAll(tasks)) {
                assertTrue(future.get());
            }
        }
    }

    /** 仅供Servlet管线验证的真实JSON和XML绑定入口. */
    @RestController
    static class Echo {
        /** 明确限制JSON的业务入口使用统一415响应. */
        @PostMapping(value = "/json-only", consumes = "application/json")
        public Map<String, Object> jsonOnly(@RequestBody Map<String, Object> body) {
            return body;
        }

        /** 返回MVC解析结果, 不手工读取或伪造JSON. */
        @PostMapping("/echo")
        public Map<String, Object> echo(@RequestBody Map<String, Object> body) {
            return body;
        }

        /** 原始XML用于验证签名正文不经过JSON或HTML处理. */
        @PostMapping("/xml")
        public String xml(@RequestBody String body) {
            return body;
        }
    }
}
