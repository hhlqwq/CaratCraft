package com.platform.common.exception;

import com.platform.common.utils.RestResponse;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** 保留业务响应结构, 同时让HTTP状态反映成功或失败. */
@RestControllerAdvice
public class RestStatusAdvice implements ResponseBodyAdvice<Object> {
    /** 响应实体也可能包裹业务结果, 最终按实际响应对象判断. */
    @Override
    public boolean supports(
            MethodParameter parameter, Class<? extends HttpMessageConverter<?>> converter) {
        return true;
    }

    /** 不改写支付XML、文件或健康探针, 仅统一业务JSON的HTTP状态. */
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter parameter,
            MediaType contentType,
            Class<? extends HttpMessageConverter<?>> converter,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (body instanceof RestResponse<?> result && result.getCode() != 0) {
            int code = result.getCode();
            response.setStatusCode(HttpStatusCode.valueOf(code >= 400 && code <= 599 ? code : 400));
        }
        return body;
    }
}
