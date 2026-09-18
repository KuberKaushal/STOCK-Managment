package com.vityarthi.inventory.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base class representing a generic inventory product.
 * Encapsulates core item attributes, stock mutation logic, and polymorphic behavior.
 */
public abstract class Product implements Serializable, Comparable<Product> {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String name;
    private String category;
    private double unitPrice;
    private int stockQuantity;
    private int minThreshold;

    public Product(String id, String name, String category, double unitPrice, int stockQuantity, int minThreshold) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank.");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        if (minThreshold < 0) {
            throw new IllegalArgumentException("Minimum threshold cannot be negative.");
        }
        this.id = id.trim().toUpperCase();
        this.name = name.trim();
        this.category = category.trim();
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.minThreshold = minThreshold;
    }

    public abstract double calculateTaxRate();

    public abstract String getProductType();

    public synchronized void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Stock quantity to add must be positive.");
        }
        this.stockQuantity += quantity;
    }

    public synchronized void deductStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Stock quantity to deduct must be positive.");
        }
        if (quantity > this.stockQuantity) {
            throw new IllegalStateException("Insufficient inventory: requested " + quantity + ", available " + this.stockQuantity);
        }
        this.stockQuantity -= quantity;
    }

    public boolean isLowStock() {
        return this.stockQuantity <= this.minThreshold;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        if (unitPrice < 0) throw new IllegalArgumentException("Unit price cannot be negative.");
        this.unitPrice = unitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) throw new IllegalArgumentException("Stock cannot be negative.");
        this.stockQuantity = stockQuantity;
    }

    public int getMinThreshold() {
        return minThreshold;
    }

    public void setMinThreshold(int minThreshold) {
        if (minThreshold < 0) throw new IllegalArgumentException("Threshold cannot be negative.");
        this.minThreshold = minThreshold;
    }

    @Override
    public int compareTo(Product other) {
        return this.id.compareToIgnoreCase(other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] SKU: %-8s | Name: %-22s | Cat: %-12s | Price: $%7.2f | Stock: %-4d | LowStock: %s",
                getProductType(), id, name, category, unitPrice, stockQuantity, isLowStock() ? "YES [ALERT]" : "NO");
    }
}
