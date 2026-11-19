package com.platform.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import com.platform.common.exception.BusinessException;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Locale;

/** 验证现用请求过滤器的放行、拒绝、固定区域和敏感参数日志边界. */
class RequestFilterTest {
    /** 普通中文及多值参数继续进入业务链, 不因泛型修复改变原请求. */
    @Test
    void normalParametersReachOriginalChain() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        request.addParameter("name", "珍珠项链");
        request.addParameter("options", "白色", "金色");
        FilterChain chain = mock();
        new AntiSqlInjectionFilter().doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    /** 拒绝请求不能把其他参数中的联系方式或凭据写入日志或异常. */
    @Test
    void rejectedRequestDoesNotExposeParameterValues() {
        var request = new MockHttpServletRequest();
        request.addParameter("query", "SELECT synthetic");
        String secret = "synthetic-private-contact-18200001111";
        request.addParameter("contact", secret);
        FilterChain chain = mock();
        Logger logger = (Logger) LoggerFactory.getLogger(AntiSqlInjectionFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            var failure =
                    assertThrows(
                            BusinessException.class,
                            () ->
                                    new AntiSqlInjectionFilter()
                                            .doFilter(
                                                    request, new MockHttpServletResponse(), chain));
            assertFalse(failure.getMessage().contains(secret));
            assertTrue(
                    appender.list.stream()
                            .noneMatch(event -> event.getFormattedMessage().contains(secret)));
            verifyNoInteractions(chain);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    /** 土耳其语区域不能把大写I转成无点小写字母而绕过INSERT规则. */
    @Test
    @ResourceLock("java.util.Locale.default")
    void keywordValidationDoesNotDependOnMachineLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertTrue(AntiSqlInjectionFilter.sqlValidate("INSERT synthetic"));
            assertFalse(AntiSqlInjectionFilter.sqlValidate("珍珠项链"));
        } finally {
            Locale.setDefault(original);
        }
    }
}
