// src/main/java/com/cartapi/services/SessionService.java
package com.cartapi.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionService {

    public String createSession(HttpServletRequest request) {
        HttpSession session = request.getSession(true);  // true = tạo mới nếu chưa có
        session.setMaxInactiveInterval(30 * 60); // 30 phút hết hạn
        // Có thể lưu thêm thông tin tạm nếu cần
        session.setAttribute("createdAt", System.currentTimeMillis());
        return session.getId();
    }
}