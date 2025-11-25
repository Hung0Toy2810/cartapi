// src/main/java/com/controllers/ApiServlet.java
package com.controllers;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        PrintWriter out = resp.getWriter();

        try {
            switch (path == null ? "" : path) {
                case "/register" -> handleRegister(req, out, resp);
                case "/login" -> handleLogin(req, out, resp);
                case "/cart/add" -> handleAddToCart(req, out, resp);
                case "/cart/checkout" -> handleCheckout(req, out, resp);
                default -> sendError(out, resp, "Endpoint not found", 404);
            }
        } catch (Exception e) {
            sendError(out, resp, e.getMessage() != null ? e.getMessage() : "Server error", 500);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        PrintWriter out = resp.getWriter();

        String token = extractToken(req);
        String username = JwtUtil.validateTokenAndGetUsername(token);

        // Chỉ yêu cầu đăng nhập khi không phải lấy danh sách album
        if (username == null && !"/albums".equals(path)) {
            sendError(out, resp, "Unauthorized", 401);
            return;
        }

        switch (path == null ? "" : path) {
            case "/albums" -> handleListAlbumsWithPhotos(out);     // Public – ai cũng xem được
            case "/cart" -> handleGetCart(username, out);
            case "/owned" -> handleGetOwned(username, out);
            default -> sendError(out, resp, "Endpoint not found", 404);
        }
    }

    // TRẢ VỀ DANH SÁCH ALBUM + 10 URL ẢNH THẬT (frontend tự tải trực tiếp)
    private void handleListAlbumsWithPhotos(PrintWriter out) {
        List<Map<String, Object>> albums = new ArrayList<>();
        for (String id : InMemoryStore.availableAlbums) {
            Map<String, Object> album = new HashMap<>();
            album.put("id", id);
            album.put("name", "Album " + id.replace("album", ""));
            album.put("cover", "https://picsum.photos/seed/" + id + "_cover/600/400");

            // 10 ảnh thật – frontend sẽ dùng trực tiếp URL này để hiển thị + tải về
            List<String> photos = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                photos.add("https://picsum.photos/seed/" + id + "_" + i + "/1600/1200");
            }
            album.put("photos", photos);
            albums.add(album);
        }
        sendSuccess(out, albums, "Danh sách album + ảnh");
    }

    // ================== AUTH & CART ==================
    private void handleRegister(HttpServletRequest req, PrintWriter out, HttpServletResponse resp) throws IOException {
        Map<String, String> body = parseJsonBody(req);
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            sendError(out, resp, "Thiếu thông tin đăng ký", 400);
            return;
        }
        if (InMemoryStore.users.containsKey(username)) {
            sendError(out, resp, "Tên đăng nhập đã tồn tại", 400);
            return;
        }

        InMemoryStore.users.put(username, new User(username, password));
        sendSuccess(out, Map.of("status", "ok"), "Đăng ký thành công!");
    }

    private void handleLogin(HttpServletRequest req, PrintWriter out, HttpServletResponse resp) throws IOException {
        Map<String, String> body = parseJsonBody(req);
        String username = body.get("username");
        String password = body.get("password");

        User user = InMemoryStore.users.get(username);
        if (user != null && user.password.equals(password)) {
            String token = JwtUtil.generateToken(username);
            sendSuccess(out, Map.of("token", token, "username", username), "Đăng nhập thành công!");
        } else {
            sendError(out, resp, "Sai tài khoản hoặc mật khẩu", 401);
        }
    }

    private void handleAddToCart(HttpServletRequest req, PrintWriter out, HttpServletResponse resp) throws IOException {
        String token = extractToken(req);
        String username = JwtUtil.validateTokenAndGetUsername(token);
        if (username == null) {
            sendError(out, resp, "Unauthorized", 401);
            return;
        }

        Map<String, String> body = parseJsonBody(req);
        String albumId = body.get("albumId");
        if (!InMemoryStore.availableAlbums.contains(albumId)) {
            sendError(out, resp, "Album không tồn tại", 404);
            return;
        }

        User user = InMemoryStore.users.get(username);
        if (user.ownedAlbums.contains(albumId)) {
            sendError(out, resp, "Bạn đã sở hữu album này", 400);
            return;
        }
        user.cart.add(albumId);
        sendSuccess(out, null, "Đã thêm vào giỏ hàng");
    }

    private void handleCheckout(HttpServletRequest req, PrintWriter out, HttpServletResponse resp) throws IOException {
        String token = extractToken(req);
        String username = JwtUtil.validateTokenAndGetUsername(token);
        if (username == null) {
            sendError(out, resp, "Unauthorized", 401);
            return;
        }

        User user = InMemoryStore.users.get(username);
        if (user.cart.isEmpty()) {
            sendError(out, resp, "Giỏ hàng trống", 400);
            return;
        }

        user.ownedAlbums.addAll(user.cart);
        user.cart.clear();
        sendSuccess(out, new ArrayList<>(user.ownedAlbums), "Thanh toán thành công! Bạn có thể tải ảnh ngay.");
    }

    private void handleGetCart(String username, PrintWriter out) {
        User user = InMemoryStore.users.get(username);
        sendSuccess(out, new ArrayList<>(user.cart), "Giỏ hàng hiện tại");
    }

    private void handleGetOwned(String username, PrintWriter out) {
        User user = InMemoryStore.users.get(username);
        sendSuccess(out, new ArrayList<>(user.ownedAlbums), "Album đã sở hữu");
    }

    // ================== UTILS ==================
    private String extractToken(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseJsonBody(HttpServletRequest req) throws IOException {
        String body = req.getReader().lines().collect(Collectors.joining());
        if (body.isBlank()) return Collections.emptyMap();
        return gson.fromJson(body, Map.class);
    }

    private void sendSuccess(PrintWriter out, Object data, String message) {
        out.print(gson.toJson(new ApiResponse(data, message)));
    }

    private void sendError(PrintWriter out, HttpServletResponse resp, String message, int status) {
        resp.setStatus(status);
        out.print(gson.toJson(new ApiResponse(null, message)));
    }
}