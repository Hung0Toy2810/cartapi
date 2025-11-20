// src/main/java/com/cartapi/models/Cart.java
package com.cartapi.models;

import com.cartapi.controllers.ProductController;   // THÊM DÒNG NÀY
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private final List<LineItem> items = new ArrayList<>();

    public List<LineItem> getItems() {
        return new ArrayList<>(items);
    }

    public void addItem(Product product, int quantity) {
        for (LineItem item : items) {
            if (item.getProduct().getId() == product.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        items.add(new LineItem(product, quantity));
    }

    public void updateQuantity(int productId, int quantity) {
        items.removeIf(item -> item.getProduct().getId() == productId);
        if (quantity > 0) {
            Product p = ProductController.getProductById(productId);  // giờ đã thấy
            if (p != null) items.add(new LineItem(p, quantity));
        }
    }

    public void removeItem(int productId) {
        items.removeIf(item -> item.getProduct().getId() == productId);
    }

    public void clear() {
        items.clear();
    }

    public double getTotalPrice() {
        return items.stream().mapToDouble(LineItem::getTotal).sum();
    }
}