package com.vityarthi.inventory;

import com.vityarthi.inventory.exception.InsufficientStockException;
import com.vityarthi.inventory.exception.InvalidTransactionException;
import com.vityarthi.inventory.exception.ItemNotFoundException;
import com.vityarthi.inventory.model.DigitalProduct;
import com.vityarthi.inventory.model.Invoice;
import com.vityarthi.inventory.model.PhysicalProduct;
import com.vityarthi.inventory.model.Product;
import com.vityarthi.inventory.service.StoreManager;
import com.vityarthi.inventory.util.ReceiptFormatter;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String DATA_FILE = "data/store_state.dat";
    private static final StoreManager store = new StoreManager();
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
                    System.out.println("Saving final state to " + DATA_FILE + " before exit...");
                    autoSave();
                    System.out.println("Thank you for using Distributed Inventory & Billing Management System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println(">> Invalid selection. Please enter a number from 1 to 5.");
            }
        }
    }

    private static void initializeApplication() {
        System.out.println("================================================================================");
        System.out.println("     DISTRIBUTED INVENTORY & BILLING MANAGEMENT SYSTEM - VITYARTHI CORE       ");
        System.out.println("================================================================================");
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try {
                store.loadFromFile(DATA_FILE);
                System.out.println("[INIT] Successfully loaded existing store state from " + DATA_FILE);
            } catch (Exception e) {
                System.out.println("[WARN] Failed to load data file (" + e.getMessage() + "). Seeding sample catalog...");
                store.seedSampleData();
            }
        } else {
            System.out.println("[INIT] No existing data store found. Seeding enterprise demo catalog...");
            store.seedSampleData();
            autoSave();
        }

        List<Product> lowStock = store.getLowStockAlerts();
        if (!lowStock.isEmpty()) {
            System.out.println("\n>>> WARNING: " + lowStock.size() + " product(s) are currently BELOW the safety stock threshold!");
            for (Product p : lowStock) {
                System.out.println("    * SKU: " + p.getId() + " - " + p.getName() + " [Stock: " + p.getStockQuantity() + " / Min: " + p.getMinThreshold() + "]");
            }
        }
    }

    private static void autoSave() {
        try {
            store.saveToFile(DATA_FILE);
            System.out.println("[SAVE] State safely committed to " + DATA_FILE);
        } catch (Exception e) {
            System.err.println("[ERROR] Auto-save failed: " + e.getMessage());
        }
    }

    private static void printMainMenu() {
        System.out.println("\n+------------------------------------------------------------------------------+");
        System.out.println("|                               MAIN DASHBOARD                                 |");
        System.out.println("+------------------------------------------------------------------------------+");
        System.out.println("|  [1] Inventory Management (CRUD, Restock, Stock Adjustments)                 |");
        System.out.println("|  [2] Billing & Receipt Generation Engine (POS Checkout, Discounts)           |");
        System.out.println("|  [3] Analytics & Audit Logs (Valuation, Low-Stock Alerts, Recent Logs)       |");
        System.out.println("|  [4] Data Persistence & Catalog Reseeding                                    |");
        System.out.println("|  [5] Exit Application                                                        |");
        System.out.println("+------------------------------------------------------------------------------+");
    }

    private static void handleInventoryMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [1] INVENTORY MANAGEMENT MODULE ---");
            System.out.println("1. List All Products in Catalog");
            System.out.println("2. Add New Product (Physical or Digital)");
            System.out.println("3. Search Product by SKU or Name");
            System.out.println("4. Restock Product (Add Quantity)");
            System.out.println("5. Adjust Product Stock Level (Audit Correction)");
            System.out.println("6. Delete Product from Catalog");
            System.out.println("0. Return to Main Dashboard");

            String choice = prompt("Select Option [0-6]: ");
            switch (choice) {
                case "1":
                    System.out.println(ReceiptFormatter.formatProductTable(store.getAllProducts()));
                    break;
                case "2":
                    addNewProductFlow();
                    break;
                case "3":
                    String query = prompt("Enter search keyword (SKU, Name, Category): ");
                    List<Product> matches = store.searchProducts(query);
                    System.out.println(ReceiptFormatter.formatProductTable(matches));
                    break;
                case "4":
                    restockProductFlow();
                    break;
                case "5":
                    adjustStockFlow();
                    break;
                case "6":
                    deleteProductFlow();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(">> Invalid option.");
            }
        }
    }

    private static void addNewProductFlow() {
        System.out.println("\n>> Select Product Type:");
        System.out.println("1. Physical Tangible Product (Weight, Shipping, 12% Goods Tax)");
        System.out.println("2. Digital Download / License Product (Zero Shipping, 18% Digital Tax)");
        String typeChoice = prompt("Choice (1 or 2): ");

        try {
            String sku = prompt("Enter SKU/ID (e.g. SKU-999): ");
            String name = prompt("Enter Product Name: ");
            String category = prompt("Enter Category: ");
            double price = Double.parseDouble(prompt("Enter Unit Price ($): "));
            int qty = Integer.parseInt(prompt("Enter Initial Stock Quantity: "));
            int threshold = Integer.parseInt(prompt("Enter Low-Stock Threshold: "));

            if (typeChoice.equals("1")) {
                double weight = Double.parseDouble(prompt("Enter Weight (kg): "));
                double shipping = Double.parseDouble(prompt("Enter Shipping Surcharge ($): "));
                PhysicalProduct p = new PhysicalProduct(sku, name, category, price, qty, threshold, weight, shipping);
                store.addProduct(p);
                System.out.println(">> Physical product '" + sku + "' successfully onboarded!");
            } else if (typeChoice.equals("2")) {
                String dlUrl = prompt("Enter Download/Delivery URL: ");
                String keyFmt = prompt("Enter License Key Format: ");
                DigitalProduct d = new DigitalProduct(sku, name, category, price, qty, threshold, dlUrl, keyFmt);
                store.addProduct(d);
                System.out.println(">> Digital product '" + sku + "' successfully onboarded!");
            } else {
                System.out.println(">> Aborted: Invalid product type selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println(">> Error: Invalid numerical input.");
        } catch (InvalidTransactionException e) {
            System.out.println(">> Business Rule Violation: " + e.getMessage());
        }
    }

    private static void restockProductFlow() {
        try {
            String sku = prompt("Enter SKU to restock: ");
            int qty = Integer.parseInt(prompt("Enter quantity to add: "));
            store.restock(sku, qty);
            Product p = store.getProduct(sku);
            System.out.println(">> Restocked successfully! Updated stock: " + p.getStockQuantity() + " units.");
        } catch (ItemNotFoundException | InvalidTransactionException e) {
            System.out.println(">> Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println(">> Error: Invalid integer value.");
        }
    }

    private static void adjustStockFlow() {
        try {
            String sku = prompt("Enter SKU to adjust: ");
            int newQty = Integer.parseInt(prompt("Enter absolute new stock quantity: "));
            String reason = prompt("Enter adjustment rationale: ");
            store.adjustStock(sku, newQty, reason);
            System.out.println(">> Stock successfully adjusted for SKU '" + sku + "'.");
        } catch (ItemNotFoundException | InvalidTransactionException e) {
            System.out.println(">> Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println(">> Error: Invalid number.");
        }
    }

    private static void deleteProductFlow() {
        try {
            String sku = prompt("Enter SKU to delete: ");
            String confirm = prompt("Are you sure you want to purge '" + sku + "'? (Y/N): ");
            if (confirm.equalsIgnoreCase("Y")) {
                store.deleteProduct(sku);
                System.out.println(">> Product SKU '" + sku + "' deleted from catalog.");
            } else {
                System.out.println(">> Deletion canceled.");
            }
        } catch (ItemNotFoundException e) {
            System.out.println(">> Error: " + e.getMessage());
        }
    }

    private static void handleBillingMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [2] BILLING ENGINE & RECEIPT GENERATION ---");
            System.out.println("1. Create New Customer Bill / Checkout");
            System.out.println("2. View Registered Past Invoices");
            System.out.println("3. Look Up Specific Invoice by Number");
            System.out.println("0. Return to Main Dashboard");

            String choice = prompt("Select Option [0-3]: ");
            switch (choice) {
                case "1":
                    processNewBillFlow();
                    break;
                case "2":
                    viewAllInvoicesFlow();
                    break;
                case "3":
                    lookupInvoiceFlow();
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(">> Invalid option.");
            }
        }
    }

    private static void processNewBillFlow() {
        System.out.println("\n>>> STARTING NEW CHECKOUT TRANSACTION <<<");
        String custName = prompt("Enter Customer Name (default: Walk-in): ");
        String custPhone = prompt("Enter Contact / Mobile: ");
        double discount = 0.0;
        try {
            String discInput = prompt("Enter Discount % (0 for none, e.g. 10 for 10%): ");
            discount = discInput.isEmpty() ? 0.0 : Double.parseDouble(discInput) / 100.0;
        } catch (NumberFormatException e) {
            System.out.println(">> Invalid discount percentage. Defaulting to 0%.");
            discount = 0.0;
        }

        Invoice invoice = store.createInvoice(custName, custPhone, discount);

        boolean addingItems = true;
        while (addingItems) {
            System.out.println("\nAvailable Catalog Snapshot:");
            System.out.println(ReceiptFormatter.formatProductTable(store.getAllProducts()));

            String sku = prompt("Enter SKU to add to cart (or 'DONE' to finish): ");
            if (sku.equalsIgnoreCase("DONE")) {
                if (invoice.getItems().isEmpty()) {
                    System.out.println(">> Cart is empty. Checkout canceled.");
                    return;
                }
                addingItems = false;
                break;
            }

            try {
                int qty = Integer.parseInt(prompt("Enter purchase quantity: "));
                store.addItemToInvoice(invoice, sku, qty);
                System.out.println(">> Added " + qty + " unit(s) of " + sku + " to current invoice.");
            } catch (ItemNotFoundException e) {
                System.out.println(">> Error: " + e.getMessage());
            } catch (InsufficientStockException e) {
                System.out.println(">> [STOCK INSUFFICIENT] " + e.getMessage());
            } catch (InvalidTransactionException e) {
                System.out.println(">> Invalid Transaction: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println(">> Error: Invalid quantity number.");
            }

            String continueChoice = prompt("Add another item? (Y/N): ");
            if (!continueChoice.equalsIgnoreCase("Y")) {
                addingItems = false;
            }
        }

        if (invoice.getItems().isEmpty()) {
            System.out.println(">> Checkout discarded.");
            return;
        }

        try {
            store.finalizeInvoice(invoice);
            System.out.println("\n>>> ORDER COMPLETED SUCCESSFULLY! <<<");
            System.out.println(ReceiptFormatter.formatReceipt(invoice));
            autoSave();
        } catch (InvalidTransactionException e) {
            System.out.println(">> Checkout failed during finalization: " + e.getMessage());
        }
    }

    private static void viewAllInvoicesFlow() {
        List<Invoice> invoices = store.getAllInvoices();
        if (invoices.isEmpty()) {
            System.out.println(">> No invoices generated in the system yet.");
            return;
        }
        System.out.println("\n+-------------+----------------------+-------------+----------+---------------+");
        System.out.println("| INVOICE ID  | CUSTOMER NAME        | TOTAL UNITS | SUB($)   | GRAND TOTAL($)|");
        System.out.println("+-------------+----------------------+-------------+----------+---------------+");
        for (Invoice inv : invoices) {
            System.out.printf("| %-11s | %-20s | %11d | %8.2f | %13.2f |\n",
                    inv.getInvoiceId(),
                    inv.getCustomerName(),
                    inv.getTotalUnits(),
                    inv.getSubtotal(),
                    inv.getGrandTotal());
        }
        System.out.println("+-------------+----------------------+-------------+----------+---------------+");
    }

    private static void lookupInvoiceFlow() {
        String invId = prompt("Enter Invoice ID (e.g. INV-1001): ");
        try {
            Invoice invoice = store.getInvoice(invId);
            System.out.println(ReceiptFormatter.formatReceipt(invoice));
        } catch (ItemNotFoundException e) {
            System.out.println(">> " + e.getMessage());
        }
    }

    private static void handleAnalyticsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [3] ANALYTICS, ALERTS & AUDIT TRAIL ---");
            System.out.println("1. View Active Low-Stock Alerts");
            System.out.println("2. View Financial Metrics & Inventory Valuation");
            System.out.println("3. View Recent Audit Trail (From Ring Buffer)");
            System.out.println("4. View All Historical Transaction Audit Records");
            System.out.println("0. Return to Main Dashboard");

            String choice = prompt("Select Option [0-4]: ");
            switch (choice) {
                case "1":
                    List<Product> lowStock = store.getLowStockAlerts();
                    if (lowStock.isEmpty()) {
                        System.out.println(">> Excellent! All products are safely above their reorder thresholds.");
                    } else {
                        System.out.println("\n>>> [ACTION REQUIRED] LOW-STOCK REORDER ALERTS <<<");
                        System.out.println(ReceiptFormatter.formatProductTable(lowStock));
                    }
                    break;
                case "2":
                    printFinancialSummary();
                    break;
                case "3":
                    System.out.println("\n>>> RECENT AUDIT LOGS (CIRCULAR BUFFER SNAPSHOT) <<<");
                    System.out.println(ReceiptFormatter.formatAuditLogTable(store.getRecentAuditLog()));
                    break;
                case "4":
                    System.out.println("\n>>> FULL ARCHIVAL TRANSACTION TRAIL <<<");
                    System.out.println(ReceiptFormatter.formatAuditLogTable(store.getAllAuditLog()));
                    break;
                case "0":
                    back = true;
                    break;
                default:
                    System.out.println(">> Invalid option.");
            }
        }
    }

    private static void printFinancialSummary() {
        double valuation = store.calculateTotalInventoryValuation();
        double revenue = store.calculateTotalRevenue();
        int totalUnits = store.getTotalStockUnits();
        int totalProducts = store.getAllProducts().size();
        int totalInvoices = store.getAllInvoices().size();

        System.out.println("\n+------------------------------------------------------------------------------+");
        System.out.println("|                   ENTERPRISE FINANCIAL & INVENTORY METRICS                   |");
        System.out.println("+------------------------------------------------------------------------------+");
        System.out.printf("|  Catalog Unique SKUs Registered           : %-32d |\n", totalProducts);
        System.out.printf("|  Total Physical & Digital Units in Stock  : %-32d |\n", totalUnits);
        System.out.printf("|  Total Asset Inventory Valuation          : $%-31.2f |\n", valuation);
        System.out.printf("|  Total Invoices Executed                  : %-32d |\n", totalInvoices);
        System.out.printf("|  Cumulative Gross Sales Revenue           : $%-31.2f |\n", revenue);
        System.out.println("+------------------------------------------------------------------------------+");
    }

    private static void handlePersistenceMenu() {
        System.out.println("\n--- [4] DATA PERSISTENCE & SYSTEM STATE ---");
        System.out.println("1. Manually Save State to Disk (Serialization)");
        System.out.println("2. Reload State from Disk (Deserialization)");
        System.out.println("3. Reseed Sample Demo Data (Resets or adds defaults)");
        System.out.println("0. Return to Main Dashboard");

        String choice = prompt("Select Option [0-3]: ");
        switch (choice) {
            case "1":
                try {
                    store.saveToFile(DATA_FILE);
                    System.out.println(">> State successfully persisted to " + DATA_FILE);
                } catch (Exception e) {
                    System.out.println(">> Serialization error: " + e.getMessage());
                }
                break;
            case "2":
                try {
                    store.loadFromFile(DATA_FILE);
                    System.out.println(">> State restored successfully from " + DATA_FILE);
                } catch (Exception e) {
                    System.out.println(">> Deserialization error: " + e.getMessage());
                }
                break;
            case "3":
                store.seedSampleData();
                System.out.println(">> Sample catalog items ensured in memory.");
                break;
            case "0":
                break;
            default:
                System.out.println(">> Invalid option.");
        }
    }

    private static String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}
