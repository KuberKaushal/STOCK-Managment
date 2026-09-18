package com.vityarthi.inventory.service;

import com.vityarthi.inventory.exception.InsufficientStockException;
import com.vityarthi.inventory.exception.InvalidTransactionException;
import com.vityarthi.inventory.exception.ItemNotFoundException;
import com.vityarthi.inventory.model.Invoice;
import java.util.List;

/**
 * Interface segregated specifically for order processing, billing calculation, and receipts.
 */
public interface BillingService {
    Invoice createInvoice(String customerName, String customerContact, double discountRate);
    void addItemToInvoice(Invoice invoice, String productId, int quantity)
            throws ItemNotFoundException, InsufficientStockException, InvalidTransactionException;
    Invoice finalizeInvoice(Invoice invoice) throws InvalidTransactionException;
    List<Invoice> getAllInvoices();
    Invoice getInvoice(String invoiceId) throws ItemNotFoundException;
}
