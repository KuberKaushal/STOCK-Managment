package com.vityarthi.inventory.service;

import com.vityarthi.inventory.exception.ItemNotFoundException;
import com.vityarthi.inventory.exception.OutOfStockException;
import com.vityarthi.inventory.model.Transaction;

import java.io.IOException;
import java.util.List;

public interface BillingService {
    Transaction processBill(String itemId, int quantity) throws ItemNotFoundException, OutOfStockException;
    List<Transaction> getTransactionHistory();
    double calculateTotalRevenue();
    void saveTransactions(String filePath) throws IOException;
    void loadTransactions(String filePath) throws IOException, ClassNotFoundException;
}
