package com.vityarthi.inventory;

import com.vityarthi.inventory.exception.ItemNotFoundException;
import com.vityarthi.inventory.exception.OutOfStockException;
import com.vityarthi.inventory.model.Item;
import com.vityarthi.inventory.model.Transaction;
import com.vityarthi.inventory.service.BillingService;
import com.vityarthi.inventory.service.InventoryService;
import com.vityarthi.inventory.service.impl.BillingServiceImpl;
import com.vityarthi.inventory.service.impl.InventoryServiceImpl;
import com.vityarthi.inventory.util.AlertLogger;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String INVENTORY_FILE = "data/inventory.ser";
    private static final String TRANSACTIONS_FILE = "data/transactions.ser";

    private static final InventoryService inventoryService = new InventoryServiceImpl();
    private static final BillingService billingService = new BillingServiceImpl(inventoryService);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        initializeApplication();

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = prompt("Select Option [1-5]: ");
            System.out.println();

            switch (choice) {
                case "1":
                    handleInventoryMenu();
                    break;
                case "2":
                    handleBillingMenu();
                    break;
                case "3":
                    handleAnalyticsMenu();
                    break;
                case "4":
                    handlePersistenceMenu();
                    break;
                case "5":
                    System.out.println("Auto-saving inventory and transaction state before exit...");
                    autoSave();
                    System.out.println("Exiting Distributed Inventory & Billing System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println(">> Invalid option. Please select 1 through 5.");
            }
        }
    }

    private static void initializeApplication() {
        System.out.println("================================================================================");
        System.out.println("     DISTRIBUTED INVENTORY & BILLING MANAGEMENT SYSTEM - VITYARTHI CORE       ");
        System.out.println("================================================================================");

        File invFile = new File(INVENTORY_FILE);
        File txFile = new File(TRANSACTIONS_FILE);

        if (invFile.exists()) {
            try {
                inventoryService.loadInventory(INVENTORY_FILE);
                System.out.println("[INIT] Successfully loaded inventory from " + INVENTORY_FILE);
            } catch (Exception e) {
                System.out.println("[WARN] Failed to load inventory (" + e.getMessage() + "). Seeding sample data...");
                seedSampleData();
            }
        } else {
            System.out.println("[INIT] No inventory.ser found. Seeding enterprise demo catalog...");
            seedSampleData();
            autoSave();
        }

        if (txFile.exists()) {
            try {
                billingService.loadTransactions(TRANSACTIONS_FILE);
                System.out.println("[INIT] Successfully loaded transaction history from " + TRANSACTIONS_FILE);
            } catch (Exception e) {
                System.out.println("[WARN] Failed to load transactions: " + e.getMessage());
            }
        }

        List<Item> lowStock = inventoryService.getLowStockItems();
        if (!lowStock.isEmpty()) {
            System.out.println("\n>>> WARNING: " + lowStock.size() + " item(s) are currently at or below reorder threshold!");
            for (Item item : lowStock) {
                System.out.println("    * SKU: " + item.getItemId() + " - " + item.getName() +
                        " [Current: " + item.getQuantity() + " | Threshold: " + item.getLowStockThreshold() + "]");
            }
        }
    }

    private static void seedSampleData() {
        inventoryService.addItem(new Item("SKU-101", "MacBook Pro 16 M3", "Laptops", 2499.00, 12, 4));
        inventoryService.addItem(new Item("SKU-102", "Dell 32 4K Monitor", "Monitors", 599.50, 3, 5));
        inventoryService.addItem(new Item("SKU-103", "Logitech MX Master 3S", "Accessories", 99.00, 25, 8));
        inventoryService.addItem(new Item("SKU-104", "Keychron Q1 Pro Wireless", "Keyboards", 199.00, 2, 5));
        inventoryService.addItem(new Item("SKU-105", "Sony WH-1000XM5 Headset", "Audio", 399.00, 15, 5));
        inventoryService.addItem(new Item("SKU-106", "Anker 100W USB-C Charger", "Power", 49.99, 4, 6));
    }

    private static void autoSave() {
        try {
            inventoryService.saveInventory(INVENTORY_FILE);
            billingService.saveTransactions(TRANSACTIONS_FILE);
            System.out.println("[SAVE] State safely committed to " + INVENTORY_FILE + " and " + TRANSACTIONS_FILE);
        } catch (Exception e) {
            System.err.println("[ERROR] Auto-save error: " + e.getMessage());
        }
    }

    private static void printMainMenu() {
        System.out.println("\n+------------------------------------------------------------------------------+");
        System.out.println("|                               MAIN DASHBOARD                                 |");
        System.out.println("+------------------------------------------------------------------------------+");
        System.out.println("|  [1] Inventory Management (Add Item, View All, Search, Restock, Delete)      |");
        System.out.println("|  [2] Billing & Sales Engine (Process Sale, Bill Receipt, Live Alert)         |");
        System.out.println("|  [3] Real-Time Alerts & Analytics (Low-Stock Alerts, Revenue Summary)       |");
        System.out.println("|  [4] Data Persistence (Manual Save, Reload, Seed Demo Data)                  |");
        System.out.println("|  [5] Exit Application                                                        |");
        System.out.println("+------------------------------------------------------------------------------+");
    }

    private static void handleInventoryMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [1] INVENTORY MANAGEMENT MODULE ---");
            System.out.println("1. List All Registered Inventory Items");
            System.out.println("2. Add New Product Item");
            System.out.println("3. Look Up Item by SKU");
            System.out.println("4. Restock Existing Item (Add Quantity)");
            System.out.println("5. Delete Item from Inventory");
            System.out.println("0. Return to Main Dashboard");

            String choice = prompt("Select Option [0-5]: ");
            switch (choice) {
                case "1":
                    System.out.println(AlertLogger.formatInventoryTable(inventoryService.getAllItems()));
                    break;
                case "2":
                    addNewItemFlow();
                    break;
                case "3":
                    lookupItemFlow();
                    break;
                case "4":
                    restockItemFlow();
                    break;
                case "5":
                    deleteItemFlow();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(">> Invalid option.");
            }
        }
    }

    private static void addNewItemFlow() {
        try {
            String sku = prompt("Enter SKU/ID (e.g. SKU-201): ");
            String name = prompt("Enter Item Name: ");
            String category = prompt("Enter Category: ");
            double price = Double.parseDouble(prompt("Enter Unit Price ($): "));
            int qty = Integer.parseInt(prompt("Enter Initial Stock Quantity: "));
            int threshold = Integer.parseInt(prompt("Enter Low-Stock Threshold: "));

            Item item = new Item(sku, name, category, price, qty, threshold);
            inventoryService.addItem(item);
            System.out.println(">> Item '" + sku + "' successfully onboarded to catalog!");
            autoSave();
        } catch (NumberFormatException e) {
            System.out.println(">> Input Error: Please enter valid numerical values.");
        } catch (IllegalArgumentException e) {
            System.out.println(">> Validation Error: " + e.getMessage());
        }
    }

    private static void lookupItemFlow() {
        String sku = prompt("Enter SKU to query: ");
        try {
            Item item = inventoryService.getItem(sku);
            System.out.println("\nItem Details: " + item);
        } catch (ItemNotFoundException e) {
            System.out.println(">> " + e.getMessage());
        }
    }

    private static void restockItemFlow() {
        try {
            String sku = prompt("Enter SKU to restock: ");
            int qty = Integer.parseInt(prompt("Enter quantity to add: "));
            inventoryService.restock(sku, qty);
            Item item = inventoryService.getItem(sku);
            System.out.println(">> Restock successful! Updated quantity for " + sku + ": " + item.getQuantity() + " units.");
            autoSave();
        } catch (ItemNotFoundException e) {
            System.out.println(">> " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println(">> Error: Invalid integer quantity.");
        } catch (IllegalArgumentException e) {
            System.out.println(">> Error: " + e.getMessage());
        }
    }

    private static void deleteItemFlow() {
        try {
            String sku = prompt("Enter SKU to delete: ");
            String confirm = prompt("Are you sure you want to delete '" + sku + "'? (Y/N): ");
            if (confirm.equalsIgnoreCase("Y")) {
                inventoryService.deleteItem(sku);
                System.out.println(">> Item '" + sku + "' purged from catalog.");
                autoSave();
            } else {
                System.out.println(">> Deletion canceled.");
            }
        } catch (ItemNotFoundException e) {
            System.out.println(">> " + e.getMessage());
        }
    }

    private static void handleBillingMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [2] BILLING & SALES ENGINE ---");
            System.out.println("1. Process New Sales Bill (Checkout Item)");
            System.out.println("2. View All Completed Transactions");
            System.out.println("0. Return to Main Dashboard");

            String choice = prompt("Select Option [0-2]: ");
            switch (choice) {
                case "1":
                    processSalesBillFlow();
                    break;
                case "2":
                    System.out.println(AlertLogger.formatTransactionTable(billingService.getTransactionHistory()));
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(">> Invalid option.");
            }
        }
    }

    private static void processSalesBillFlow() {
        System.out.println("\nCurrent Inventory Snapshot:");
        System.out.println(AlertLogger.formatInventoryTable(inventoryService.getAllItems()));

        String sku = prompt("Enter Item SKU to purchase: ");
        try {
            int qty = Integer.parseInt(prompt("Enter purchase quantity: "));
            Transaction tx = billingService.processBill(sku, qty);
            Item item = inventoryService.getItem(sku);

            System.out.println("\n>>> TRANSACTION SUCCESSFUL! <<<");
            System.out.println(AlertLogger.formatBillReceipt(tx, item));
            autoSave();
        } catch (ItemNotFoundException e) {
            System.out.println(">> Lookup Error: " + e.getMessage());
        } catch (OutOfStockException e) {
            System.out.println(">> [REJECTED] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println(">> Error: Please enter a valid quantity integer.");
        } catch (IllegalArgumentException e) {
            System.out.println(">> Error: " + e.getMessage());
        }
    }

    private static void handleAnalyticsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [3] REAL-TIME ALERTS & ANALYTICS ---");
            System.out.println("1. View Active Low-Stock Alerts");
            System.out.println("2. View Financial & Inventory Metrics Summary");
            System.out.println("3. View Historical Transaction Log");
            System.out.println("0. Return to Main Dashboard");

            String choice = prompt("Select Option [0-3]: ");
            switch (choice) {
                case "1":
                    List<Item> lowStock = inventoryService.getLowStockItems();
                    if (lowStock.isEmpty()) {
                        System.out.println(">> Great! All items are safely above their reorder thresholds.");
                    } else {
                        System.out.println("\n>>> [ACTION REQUIRED] LOW-STOCK REORDER ALERTS <<<");
                        System.out.println(AlertLogger.formatInventoryTable(lowStock));
                    }
                    break;
                case "2":
                    double valuation = inventoryService.calculateTotalInventoryValuation();
                    double revenue = billingService.calculateTotalRevenue();
                    int totalItems = inventoryService.getAllItems().size();
                    int totalTx = billingService.getTransactionHistory().size();

                    System.out.println("\n+------------------------------------------------------------------------------+");
                    System.out.println("|                   ENTERPRISE FINANCIAL & INVENTORY METRICS                   |");
                    System.out.println("+------------------------------------------------------------------------------+");
                    System.out.printf("|  Catalog Unique SKUs Registered           : %-32d |\n", totalItems);
                    System.out.printf("|  Total Asset Inventory Valuation          : $%-31.2f |\n", valuation);
                    System.out.printf("|  Total Sales Transactions Executed        : %-32d |\n", totalTx);
                    System.out.printf("|  Cumulative Gross Sales Revenue           : $%-31.2f |\n", revenue);
                    System.out.println("+------------------------------------------------------------------------------+");
                    break;
                case "3":
                    System.out.println(AlertLogger.formatTransactionTable(billingService.getTransactionHistory()));
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(">> Invalid option.");
            }
        }
    }

    private static void handlePersistenceMenu() {
        System.out.println("\n--- [4] DATA PERSISTENCE & SERIALIZATION ---");
        System.out.println("1. Manually Save State to Disk (.ser files)");
        System.out.println("2. Reload State from Disk (.ser files)");
        System.out.println("3. Reseed Enterprise Demo Catalog");
        System.out.println("0. Return to Main Dashboard");

        String choice = prompt("Select Option [0-3]: ");
        switch (choice) {
            case "1":
                autoSave();
                break;
            case "2":
                try {
                    inventoryService.loadInventory(INVENTORY_FILE);
                    billingService.loadTransactions(TRANSACTIONS_FILE);
                    System.out.println(">> System state successfully reloaded from .ser files!");
                } catch (Exception e) {
                    System.out.println(">> Deserialization error: " + e.getMessage());
                }
                break;
            case "3":
                seedSampleData();
                System.out.println(">> Enterprise sample catalog ensured in memory.");
                autoSave();
                break;
            case "0":
                break;
            default:
                System.out.println(">> Invalid option.");
        }
    }

    private static String prompt(String msg) {
        System.out.print(msg);
        return scanner.nextLine().trim();
    }
}
