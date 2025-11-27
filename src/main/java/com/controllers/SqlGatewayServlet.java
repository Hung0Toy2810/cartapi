package com.controllers;

import com.google.gson.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;

@WebServlet("/api/query")
public class SqlGatewayServlet extends HttpServlet {

    private static HikariDataSource dataSource;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_ROWS = 1000;

    @Override
    public void init() throws ServletException {
        HikariConfig config = new HikariConfig();

        // TỰ ĐỘNG PHÁT HIỆN MÔI TRƯỜNG: LOCAL hay RAILWAY
        String host = System.getenv("MYSQLHOST");           // Railway: mysql.railway.internal
        String port = System.getenv("MYSQLPORT");           // 3306
        String database = System.getenv("MYSQLDATABASE");   // railway
        String username = System.getenv("MYSQLUSER");       // root
        String password = System.getenv("MYSQLPASSWORD");

        if (host != null && !host.isEmpty()) {
            // CHẠY TRÊN RAILWAY → DÙNG INTERNAL (nhanh + an toàn)
            String jdbcUrl = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&allowMultiQueries=true&serverTimezone=UTC",
                host, port, database
            );
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            System.out.println("RAILWAY MODE → Kết nối MySQL internal");
        } else {
            // CHẠY LOCAL → DÙNG PUBLIC URL CỦA RAILWAY (từ ảnh bạn gửi)
            config.setJdbcUrl("jdbc:mysql://interchange.proxy.rlwy.net:46786/railway" +
                    "?useSSL=false&allowPublicKeyRetrieval=true&allowMultiQueries=true&serverTimezone=UTC");
            config.setUsername("root");
            config.setPassword("YcdpkpqFwCORRAMKBKxWMiATtdIdnVlg");
            System.out.println("LOCAL MODE → Kết nối MySQL Railway qua Public URL");
        }

        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10);
        config.addDataSourceProperty("cachePrepStmts", "true");

        try {
            dataSource = new HikariDataSource(config);
            System.out.println("KẾT NỐI DATABASE THÀNH CÔNG!");
        } catch (Exception e) {
            throw new ServletException("Không thể khởi tạo kết nối database: " + e.getMessage(), e);
        }
    }

    // CORS CHO TẤT CẢ MÁY TÍNH TRÊN THẾ GIỚI + LOCAL + PRODUCTION
    private void setCorsHeaders(HttpServletResponse resp) {
        String origin = resp.getHeader("Origin");
        
        // CHO PHÉP TẤT CẢ DOMAIN (khi deploy live + khi dev local)
        if (origin != null) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            resp.setHeader("Access-Control-Allow-Origin", "*");
        }

        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json; charset=UTF-8");

        JsonObject result = new JsonObject();
        long start = System.currentTimeMillis();

        try {
            String body = req.getReader().lines().reduce("", String::concat);
            JsonObject input = gson.fromJson(body, JsonObject.class);

            if (input == null || !input.has("query") || input.get("query").isJsonNull()) {
                throw new IllegalArgumentException("Thiếu trường 'query'");
            }

            String query = input.get("query").getAsString().trim();

            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {

                boolean hasResultSet = stmt.execute(query);
                JsonArray dataArray = new JsonArray();
                int affectedRows = 0;

                do {
                    if (stmt.getUpdateCount() != -1) {
                        affectedRows += stmt.getUpdateCount();
                    }
                    if (stmt.getResultSet() != null) {
                        try (ResultSet rs = stmt.getResultSet()) {
                            ResultSetMetaData meta = rs.getMetaData();
                            int cols = meta.getColumnCount();

                            while (rs.next() && dataArray.size() < MAX_ROWS) {
                                JsonObject row = new JsonObject();
                                for (int i = 1; i <= cols; i++) {
                                    Object val = rs.getObject(i);
                                    String key = meta.getColumnLabel(i);
                                    if (val == null) {
                                        row.add(key, JsonNull.INSTANCE);
                                    } else {
                                        row.addProperty(key, val.toString());
                                    }
                                }
                                dataArray.add(row);
                            }
                        }
                    }
                } while (stmt.getMoreResults() || stmt.getUpdateCount() != -1);

                result.addProperty("success", true);
                result.addProperty("message", "Thực thi thành công");

                if (!dataArray.isEmpty()) {
                    result.add("data", dataArray);
                    result.addProperty("rows", dataArray.size());
                } else {
                    result.addProperty("affectedRows", affectedRows);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("success", false);
            result.addProperty("message", e.getMessage() != null ? e.getMessage() : "Lỗi server");
            resp.setStatus(400);
        }

        result.addProperty("executionTimeMs", System.currentTimeMillis() - start);
        resp.getWriter().write(gson.toJson(result));
    }

    @Override
    public void destroy() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}