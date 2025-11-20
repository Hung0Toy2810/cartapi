// src/main/java/com/cartapi/models/LineItem.java
package com.cartapi.models;

public class LineItem {
    private final Product product;
    private int quantity;

    public LineItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotal() {
        return product.getPrice() * quantity;
    }
}