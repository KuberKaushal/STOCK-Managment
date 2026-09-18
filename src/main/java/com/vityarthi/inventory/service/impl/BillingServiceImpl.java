package com.vityarthi.inventory.service.impl;

import com.vityarthi.inventory.exception.ItemNotFoundException;
import com.vityarthi.inventory.exception.OutOfStockException;
import com.vityarthi.inventory.model.Item;
import com.vityarthi.inventory.model.Transaction;
import com.vityarthi.inventory.service.BillingService;
import com.vityarthi.inventory.service.InventoryService;
import com.vityarthi.inventory.util.AlertLogger;
import com.vityarthi.inventory.util.SerializationUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class BillingServiceImpl implements BillingService {

    private final InventoryService inventoryService;
    private final List<Transaction> transactionHistory;
    private final AtomicInteger txCounter;

    public BillingServiceImpl(InventoryService inventoryService) {
        if (inventoryService == null) throw new IllegalArgumentException("InventoryService dependency cannot be null.");
        this.inventoryService = inventoryService;
        this.transactionHistory = new CopyOnWriteArrayList<>();
        this.txCounter = new AtomicInteger(1001);
    }

    @Override
    public synchronized Transaction processBill(String itemId, int quantity)
            throws ItemNotFoundException, OutOfStockException {
        if (quantity <= 0) throw new IllegalArgumentException("Purchase quantity must be positive. Received: " + quantity);

        Item item = inventoryService.getItem(itemId);
        if (item.getQuantity() < quantity) {
            throw new OutOfStockException(item.getItemId(), quantity, item.getQuantity());
        }

        item.reduceStock(quantity);
        AlertLogger.checkAndAlert(item);

        String txId = "TX-" + txCounter.getAndIncrement();
        Transaction transaction = new Transaction(
                txId,
                Transaction.TransactionType.SALE,
                item.getItemId(),
                item.getName(),
                quantity,
                item.getPrice()
        );

        transactionHistory.add(transaction);
        AlertLogger.logTransaction(transaction);

        return transaction;
    }

    @Override
    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }

    @Override
    public double calculateTotalRevenue() {
        return transactionHistory.stream().mapToDouble(Transaction::getTotalAmount).sum();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void saveTransactions(String filePath) throws IOException {
        ArrayList<Transaction> serializableList = new ArrayList<>(this.transactionHistory);
        SerializationUtil.serialize(serializableList, filePath);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void loadTransactions(String filePath) throws IOException, ClassNotFoundException {
        ArrayList<Transaction> loaded = SerializationUtil.deserialize(filePath, ArrayList.class);
        this.transactionHistory.clear();
        this.transactionHistory.addAll(loaded);
        this.txCounter.set(1001 + transactionHistory.size());
    }
}
