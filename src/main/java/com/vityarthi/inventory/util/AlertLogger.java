package com.vityarthi.inventory.util;

import com.vityarthi.inventory.model.Item;
import com.vityarthi.inventory.model.Transaction;

import java.time.format.DateTimeFormatter;
import java.util.List;

public final class AlertLogger {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AlertLogger() {}

    public static void checkAndAlert(Item item) {
        if (item != null && item.isLowStock()) {
            logLowStock(item.getItemId(), item.getName(), item.getQuantity(), item.getLowStockThreshold());
        }
    }

    public static void logLowStock(String itemId, String itemName, int currentStock, int threshold) {
        System.out.println(">>> [LOW-STOCK ALERT] Item '" + itemName + "' (SKU: " + itemId +
                ") has fallen to " + currentStock + " units! (Safety Threshold: " + threshold + ")");
    }

    public static void logTransaction(Transaction tx) {
        System.out.println("[TRANSACTION LOGGED] " + tx.getType() + " | ID: " + tx.getTransactionId() +
                " | Item: " + tx.getItemId() + " (" + tx.getItemName() + ") | Qty: " + tx.getQuantity() +
                " | Total: $" + String.format("%.2f", tx.getTotalAmount()));
    }

    public static String formatInventoryTable(List<Item> items) {
        StringBuilder sb = new StringBuilder();
        String line = "+----------+--------------------------+------------+-----------+-------+-------+------------+";
        sb.append(line).append(System.lineSeparator());
        sb.append(String.format("| %-8s | %-24s | %-10s | %-9s | %-5s | %-5s | %-10s |%n",
                "SKU", "ITEM NAME", "CATEGORY", "PRICE($)", "QTY", "MIN", "STATUS"));
        sb.append(line).append(System.lineSeparator());

        if (items == null || items.isEmpty()) {
            sb.append("|                            NO ITEMS REGISTERED IN INVENTORY                             |").append(System.lineSeparator());
        } else {
            for (Item item : items) {
                String status = item.isLowStock() ? "LOW ALERT" : "OK";
                sb.append(String.format("| %-8s | %-24s | %-10s | %9.2f | %5d | %5d | %-10s |%n",
                        item.getItemId(),
                        truncate(item.getName(), 24),
                        truncate(item.getCategory(), 10),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getLowStockThreshold(),
                        status));
            }
        }
        sb.append(line).append(System.lineSeparator());
        return sb.toString();
    }

    public static String formatTransactionTable(List<Transaction> transactions) {
        StringBuilder sb = new StringBuilder();
        String line = "+---------------------+----------+------------+----------+----------------------+-------+-----------+";
        sb.append(line).append(System.lineSeparator());
        sb.append(String.format("| %-19s | %-8s | %-10s | %-8s | %-20s | %-5s | %-9s |%n",
                "TIMESTAMP", "TX ID", "TYPE", "SKU", "ITEM NAME", "QTY", "TOTAL($)"));
        sb.append(line).append(System.lineSeparator());

        if (transactions == null || transactions.isEmpty()) {
            sb.append("|                               NO TRANSACTIONS RECORDED YET                              |").append(System.lineSeparator());
        } else {
            for (Transaction tx : transactions) {
                sb.append(String.format("| %-19s | %-8s | %-10s | %-8s | %-20s | %5d | %9.2f |%n",
                        tx.getTimestamp().format(DTF),
                        tx.getTransactionId(),
                        tx.getType(),
                        tx.getItemId(),
                        truncate(tx.getItemName(), 20),
                        tx.getQuantity(),
                        tx.getTotalAmount()));
            }
        }
        sb.append(line).append(System.lineSeparator());
        return sb.toString();
    }

    public static String formatBillReceipt(Transaction tx, Item item) {
        StringBuilder sb = new StringBuilder();
        String line = "================================================================================";
        String thin = "--------------------------------------------------------------------------------";

        sb.append(line).append(System.lineSeparator());
        sb.append("                      ENTERPRISE STORE POS RECEIPT                             ").append(System.lineSeparator());
        sb.append(line).append(System.lineSeparator());
        sb.append(String.format(" Transaction ID : %-30s Date: %s%n", tx.getTransactionId(), tx.getTimestamp().format(DTF)));
        sb.append(String.format(" Product SKU    : %-30s Category: %s%n", item.getItemId(), item.getCategory()));
        sb.append(String.format(" Product Name   : %s%n", item.getName()));
        sb.append(thin).append(System.lineSeparator());
        sb.append(String.format(" Units Purchased: %-30d Unit Price: $%.2f%n", tx.getQuantity(), tx.getUnitPrice()));
        sb.append(String.format(" Remaining Stock: %-30d Status: %s%n", item.getQuantity(), item.isLowStock() ? "REORDER REQUIRED" : "IN STOCK"));
        sb.append(line).append(System.lineSeparator());
        sb.append(String.format(" TOTAL BILL AMOUNT (PAID)                         :  $%10.2f%n", tx.getTotalAmount()));
        sb.append(line).append(System.lineSeparator());
        sb.append("           Thank you for your business! Please keep this receipt.              ").append(System.lineSeparator());
        sb.append(line).append(System.lineSeparator());

        return sb.toString();
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, max - 2) + "..";
    }
}
