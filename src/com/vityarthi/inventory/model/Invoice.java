package com.vityarthi.inventory.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates an immutable checkout transaction receipt with full line-item details.
 */
public class Invoice implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String invoiceId;
    private final String customerName;
    private final String customerContact;
    private final LocalDateTime timestamp;
    private final List<InvoiceItem> items;
    private double discountRate;

    public Invoice(String invoiceId, String customerName, String customerContact, double discountRate) {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Invoice ID cannot be null or empty.");
        }
        if (discountRate < 0.0 || discountRate > 1.0) {
            throw new IllegalArgumentException("Discount rate must be between 0.0 and 1.0.");
        }
        this.invoiceId = invoiceId.trim();
        this.customerName = (customerName == null || customerName.trim().isEmpty()) ? "Walk-in Customer" : customerName.trim();
        this.customerContact = (customerContact == null || customerContact.trim().isEmpty()) ? "N/A" : customerContact.trim();
        this.timestamp = LocalDateTime.now();
        this.items = new ArrayList<>();
        this.discountRate = discountRate;
    }

    public void addItem(InvoiceItem item) {
        if (item == null) throw new IllegalArgumentException("Invoice item cannot be null.");
        this.items.add(item);
    }

    public double getSubtotal() {
        return items.stream().mapToDouble(InvoiceItem::getSubtotal).sum();
    }

    public double getTotalTax() {
        return items.stream().mapToDouble(InvoiceItem::getTaxAmount).sum();
    }

    public double getDiscountAmount() {
        return getSubtotal() * discountRate;
    }

    public double getGrandTotal() {
        double subtotalAfterDiscount = getSubtotal() - getDiscountAmount();
        return Math.max(0, subtotalAfterDiscount + getTotalTax());
    }

    public String getInvoiceId() { return invoiceId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerContact() { return customerContact; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public List<InvoiceItem> getItems() { return Collections.unmodifiableList(items); }
    public double getDiscountRate() { return discountRate; }
    public void setDiscountRate(double discountRate) { this.discountRate = discountRate; }
    public int getTotalUnits() { return items.stream().mapToInt(InvoiceItem::getQuantity).sum(); }
}
