// src/main/java/com/cartapi/controllers/SessionController.java
package com.cartapi.controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.cartapi.models.SessionResponse;
import com.cartapi.services.SessionService;
import com.google.gson.Gson;
import java.io.IOException;

@WebServlet("/api/session/create")
public class SessionController extends HttpServlet {

    private final SessionService sessionService = new SessionService();
    private final Gson gson = new Gson();

    // CHO PHÉP TẤT CẢ MÁY TRONG MẠNG LAN + GIỮ COOKIE
    private void addCorsHeaders(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        }
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Max-Age", "86400");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        addCorsHeaders(request, response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String sessionId = sessionService.createSession(request);
            SessionResponse resp = new SessionResponse(sessionId, "Session created successfully");
            response.getWriter().write(gson.toJson(resp));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Server error\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        addCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}