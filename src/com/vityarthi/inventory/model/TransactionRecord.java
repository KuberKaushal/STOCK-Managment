package com.vityarthi.inventory.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable audit record tracking inventory mutations and financial transactions.
 */
public class TransactionRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TransactionType {
        PURCHASE_SALE,
        RESTOCK,
        STOCK_ADJUSTMENT,
        PRODUCT_CREATION,
        PRODUCT_DELETION
    }

    private final String transactionId;
    private final LocalDateTime timestamp;
    private final TransactionType type;
    private final String productId;
    private final String productName;
    private final int quantityDelta;
    private final double unitPrice;
    private final String note;

    public TransactionRecord(String transactionId, TransactionType type, String productId,
                             String productName, int quantityDelta, double unitPrice, String note) {
        this.transactionId = transactionId;
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.productId = productId;
        this.productName = productName;
        this.quantityDelta = quantityDelta;
        this.unitPrice = unitPrice;
        this.note = (note == null) ? "" : note;
    }

    public String getTransactionId() { return transactionId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public TransactionType getType() { return type; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantityDelta() { return quantityDelta; }
    public double getUnitPrice() { return unitPrice; }
    public String getNote() { return note; }

    @Override
    public String toString() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("[%s] %-16s | ID: %-8s | SKU: %-8s (%-15s) | Delta: %+4d | Price: $%7.2f | Note: %s",
                timestamp.format(dtf), type, transactionId, productId, productName, quantityDelta, unitPrice, note);
    }
}
