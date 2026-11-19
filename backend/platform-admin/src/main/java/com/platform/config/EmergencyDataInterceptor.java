package com.platform.config;

import com.platform.datascope.StaffAuthority;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.web.servlet.HandlerInterceptor;

/** 将缺少业务归属的管理旧接口限制在应急账号, 防止经旧列表获得全量数据. */
final class EmergencyDataInterceptor implements HandlerInterceptor {
    private final StaffAuthority authority;

    /** 注入实时身份及岗位权限服务. */
    EmergencyDataInterceptor(StaffAuthority authority) {
        this.authority = authority;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"OPTIONS".equals(request.getMethod()) && !authority.emergency()) {
            throw new UnauthorizedException("该入口仅供应急管理");
        }
        return true;
    }
}
