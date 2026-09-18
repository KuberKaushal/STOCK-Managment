package com.vityarthi.inventory.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TransactionType {
        SALE,
        RESTOCK,
        INITIAL_ONBOARDING
    }

    private final String transactionId;
    private final LocalDateTime timestamp;
    private final TransactionType type;
    private final String itemId;
    private final String itemName;
    private final int quantity;
    private final double unitPrice;
    private final double totalAmount;

    public Transaction(String transactionId, TransactionType type, String itemId, String itemName,
                       int quantity, double unitPrice) {
        this.transactionId = transactionId;
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = quantity * unitPrice;
    }

    public String getTransactionId() { return transactionId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public TransactionType getType() { return type; }
    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalAmount() { return totalAmount; }

    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] TX: %-8s | %-10s | SKU: %-8s (%-15s) | Qty: %-3d | Total: $%8.2f",
                timestamp.format(dtf), transactionId, type, itemId, itemName, quantity, totalAmount);
    }
}
