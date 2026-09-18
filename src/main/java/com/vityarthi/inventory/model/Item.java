package com.vityarthi.inventory.model;

import java.io.Serializable;
import java.util.Objects;

public class Item implements Serializable, Comparable<Item> {
    private static final long serialVersionUID = 1L;

    private final String itemId;
    private String name;
    private String category;
    private double price;
    private int quantity;
    private int lowStockThreshold;

    public Item(String itemId, String name, String category, double price, int quantity, int lowStockThreshold) {
        if (itemId == null || itemId.trim().isEmpty()) throw new IllegalArgumentException("Item ID cannot be null or blank.");
        if (price < 0.0) throw new IllegalArgumentException("Price cannot be negative.");
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative.");
        if (lowStockThreshold < 0) throw new IllegalArgumentException("Low stock threshold cannot be negative.");
        this.itemId = itemId.trim().toUpperCase();
        this.name = name.trim();
        this.category = category.trim();
        this.price = price;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
    }

    public synchronized void addStock(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Stock to add must be positive. Received: " + amount);
        this.quantity += amount;
    }

    public synchronized void reduceStock(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Stock to reduce must be positive. Received: " + amount);
        if (amount > this.quantity) {
            throw new IllegalStateException(String.format("Insufficient stock: requested %d, available %d", amount, this.quantity));
        }
        this.quantity -= amount;
    }

    public boolean isLowStock() {
        return this.quantity <= this.lowStockThreshold;
    }

    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) {
        if (price < 0.0) throw new IllegalArgumentException("Price cannot be negative.");
        this.price = price;
    }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative.");
        this.quantity = quantity;
    }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) {
        if (lowStockThreshold < 0) throw new IllegalArgumentException("Threshold cannot be negative.");
        this.lowStockThreshold = lowStockThreshold;
    }

    @Override
    public int compareTo(Item other) {
        return this.itemId.compareToIgnoreCase(other.itemId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return Objects.equals(itemId, item.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %-20s | Cat: %-12s | Price: $%7.2f | Qty: %-4d | Min: %-3d | Status: %s",
                itemId, name, category, price, quantity, lowStockThreshold, isLowStock() ? "LOW ALERT" : "OK");
    }
}
