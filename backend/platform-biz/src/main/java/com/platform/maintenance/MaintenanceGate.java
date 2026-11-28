package com.platform.maintenance;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/** 独立维护器通过只读挂载控制停写, 不读取客户端传入的维护状态. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class MaintenanceGate extends OncePerRequestFilter {
    private static final Path MARKER = Path.of("/run/platform-control/maintenance");
    private static final Set<String> SESSION =
            Set.of("/sys/login", "/captcha.jpg", "/sys/logout", "/sys/user/info", "/sys/menu/nav");

    /** 供退款补偿和Quartz复用同一个停写判断. */
    public static boolean active() {
        return Files.exists(MARKER);
    }

    /** 维护期间只保留内部健康及平台管理认证入口, 业务查询和写入均暂缓. */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        boolean read = "GET".equals(request.getMethod());
        boolean recovery = "POST".equals(request.getMethod()) && path.equals("/platform/restore");
        if (!active()
                || path.startsWith("/internal/health/")
                || path.equals("/")
                || (read
                        && (path.startsWith("/platform/")
                                || SESSION.contains(path)
                                || path.equals("/sys/password-policy")))
                || recovery) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(503);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"code\":503,\"msg\":\"系统正在维护, 请稍后重试\"}");
    }
}
