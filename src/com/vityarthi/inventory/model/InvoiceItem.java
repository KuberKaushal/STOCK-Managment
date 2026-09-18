package com.vityarthi.inventory.model;

import java.io.Serializable;

/**
 * Line item within a customer invoice snapshotting product details at purchase time.
 */
public class InvoiceItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String productId;
    private final String productName;
    private final String productType;
    private final int quantity;
    private final double unitPrice;
    private final double taxRate;

    public InvoiceItem(Product product, int quantity) {
        if (product == null) throw new IllegalArgumentException("Product cannot be null.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        this.productId = product.getId();
        this.productName = product.getName();
        this.productType = product.getProductType().trim();
        this.quantity = quantity;
        this.unitPrice = product.getUnitPrice();
        this.taxRate = product.calculateTaxRate();
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductType() { return productType; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTaxRate() { return taxRate; }
    public double getSubtotal() { return unitPrice * quantity; }
    public double getTaxAmount() { return getSubtotal() * taxRate; }
    public double getTotal() { return getSubtotal() + getTaxAmount(); }

    @Override
    public String toString() {
        return String.format("%-8s %-20s %4d x $%7.2f = $%8.2f (Tax: $%6.2f)",
                productId, productName, quantity, unitPrice, getSubtotal(), getTaxAmount());
    }
}
