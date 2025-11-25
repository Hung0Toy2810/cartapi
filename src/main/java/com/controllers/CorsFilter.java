// src/main/java/com/controllers/CorsFilter.java
package com.controllers;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

// Áp dụng cho tất cả các URL (/*)
@WebFilter("/*")
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) res;

        // CHO PHÉP TẤT CẢ DOMAIN TRUY CẬP (localhost, 192.168.x.x, vercel.app, netlify.app, v.v.)
        response.setHeader("Access-Control-Allow-Origin", "*");

        // Cho phép gửi token trong header
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Cache preflight 1 ngày
        response.setHeader("Access-Control-Max-Age", "86400");

        // Xử lý request OPTIONS (preflight) ngay lập tức, không đi vào servlet
        if ("OPTIONS".equalsIgnoreCase(((jakarta.servlet.http.HttpServletRequest) req).getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }

    @Override public void init(FilterConfig filterConfig) {}
    @Override public void destroy() {}
}