// src/main/java/com/cartapi/controllers/ProductController.java
package com.cartapi.controllers;

import com.cartapi.models.Product;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/products")
public class ProductController extends HttpServlet {

    private static final List<Product> PRODUCTS = List.of(
        new Product(1, "iPhone 15", 29990000),
        new Product(2, "MacBook Air M2", 32990000),
        new Product(3, "AirPods Pro 2", 6990000),
        new Product(4, "Apple Watch SE", 8990000),
        new Product(5, "iPad Air 5", 17990000)
    );

    public static Product getProductById(int id) {
        return PRODUCTS.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        addCorsHeaders(req, resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(new Gson().toJson(PRODUCTS));
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        addCorsHeaders(req, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}