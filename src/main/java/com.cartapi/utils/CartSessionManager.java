// src/main/java/com/cartapi/utils/CartSessionManager.java
package com.cartapi.utils;

import com.cartapi.models.Cart;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CartSessionManager {
    private static final Map<String, Long> sessionLastAccess = new ConcurrentHashMap<>();

    public static Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }
        sessionLastAccess.put(session.getId(), System.currentTimeMillis());
        return cart;
    }

    public static void invalidateIfExpired(HttpSession session) {
        Long last = sessionLastAccess.get(session.getId());
        if (last != null && (System.currentTimeMillis() - last) > 30 * 60 * 1000) {
            session.invalidate();
            sessionLastAccess.remove(session.getId());
        }
    }

    public static void updateLastAccess(HttpSession session) {
        sessionLastAccess.put(session.getId(), System.currentTimeMillis());
    }
}