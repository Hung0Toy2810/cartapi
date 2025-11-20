// src/main/java/com/cartapi/controllers/CartController.java
package com.cartapi.controllers;

import com.cartapi.models.Cart;
import com.cartapi.models.LineItem;
import com.cartapi.utils.CartSessionManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import java.io.IOException;
import com.cartapi.models.Product;

@WebServlet({"/api/cart", "/api/cart/*"})
public class CartController extends HttpServlet {
    private final Gson gson = new Gson();

    private void addCorsHeaders(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        }
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Max-Age", "86400");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        addCorsHeaders(req, resp);

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.setStatus(401);
            resp.getWriter().write("{\"error\": \"No session\"}");
            return;
        }

        CartSessionManager.invalidateIfExpired(session);
        Cart cart = CartSessionManager.getCart(session);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        JsonObject result = new JsonObject();
        result.add("items", gson.toJsonTree(cart.getItems()));
        result.addProperty("totalPrice", cart.getTotalPrice());
        result.addProperty("itemCount", cart.getItems().size());
        resp.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        addCorsHeaders(req, resp);

        String path = req.getPathInfo();
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.setStatus(401);
            return;
        }

        CartSessionManager.invalidateIfExpired(session);
        Cart cart = CartSessionManager.getCart(session);

        if ("/add".equals(path)) {
            int productId = Integer.parseInt(req.getParameter("productId"));
            int quantity = Integer.parseInt(req.getParameter("quantity"));
            Product p = ProductController.getProductById(productId);
            if (p != null) {
                cart.addItem(p, quantity);
                resp.getWriter().write("{\"status\": \"added\"}");
            } else {
                resp.setStatus(404);
            }
        } else if ("/update".equals(path)) {
            int productId = Integer.parseInt(req.getParameter("productId"));
            int quantity = Integer.parseInt(req.getParameter("quantity"));
            cart.updateQuantity(productId, quantity);
            resp.getWriter().write("{\"status\": \"updated\"}");
        } else if ("/checkout".equals(path)) {
            cart.clear();
            session.invalidate();
            resp.getWriter().write("{\"status\": \"checked out, session cleared\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        addCorsHeaders(req, resp);

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.setStatus(401);
            return;
        }

        CartSessionManager.invalidateIfExpired(session);
        Cart cart = CartSessionManager.getCart(session);

        int productId = Integer.parseInt(req.getParameter("productId"));
        cart.removeItem(productId);
        resp.getWriter().write("{\"status\": \"removed\"}");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        addCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}