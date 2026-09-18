package com.vityarthi.inventory.service;

import com.vityarthi.inventory.exception.InsufficientStockException;
import com.vityarthi.inventory.exception.InvalidTransactionException;
import com.vityarthi.inventory.exception.ItemNotFoundException;
import com.vityarthi.inventory.model.*;
import com.vityarthi.inventory.util.CircularAuditBuffer;
import com.vityarthi.inventory.util.DataSerializer;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class StoreManager implements InventoryService, BillingService, AnalyticsService {

    private final Map<String, Product> inventoryMap;
    private final List<Invoice> invoiceRegistry;
    private final CircularAuditBuffer<TransactionRecord> recentAuditBuffer;
    private final List<TransactionRecord> fullAuditTrail;
    private final AtomicInteger invoiceSequence;
    private final AtomicInteger txSequence;

    public StoreManager() {
        this.inventoryMap = new ConcurrentHashMap<>();
        this.invoiceRegistry = new CopyOnWriteArrayList<>();
        this.recentAuditBuffer = new CircularAuditBuffer<>(100);
        this.fullAuditTrail = new CopyOnWriteArrayList<>();
        this.invoiceSequence = new AtomicInteger(1001);
        this.txSequence = new AtomicInteger(5001);
    }

    @Override
    public synchronized void addProduct(Product product) throws InvalidTransactionException {
        if (product == null) {
            throw new InvalidTransactionException("Cannot register null product.");
        }
        if (inventoryMap.containsKey(product.getId())) {
            throw new InvalidTransactionException("Product with SKU '" + product.getId() + "' already exists.");
        }
        inventoryMap.put(product.getId(), product);
        logTransaction(TransactionRecord.TransactionType.PRODUCT_CREATION,
                product.getId(), product.getName(), product.getStockQuantity(), product.getUnitPrice(),
                "New item onboarded: " + product.getProductType());
    }

    @Override
    public Product getProduct(String id) throws ItemNotFoundException {
        if (id == null) throw new ItemNotFoundException("null");
        Product product = inventoryMap.get(id.trim().toUpperCase());
        if (product == null) throw new ItemNotFoundException(id);
        return product;
    }

    @Override
    public synchronized void updateProduct(Product product) throws ItemNotFoundException {
        if (product == null || !inventoryMap.containsKey(product.getId())) {
            throw new ItemNotFoundException((product == null) ? "null" : product.getId());
        }
        inventoryMap.put(product.getId(), product);
        logTransaction(TransactionRecord.TransactionType.STOCK_ADJUSTMENT,
                product.getId(), product.getName(), 0, product.getUnitPrice(), "Product metadata updated");
    }

    @Override
    public synchronized void deleteProduct(String id) throws ItemNotFoundException {
        Product removed = inventoryMap.remove(id.trim().toUpperCase());
        if (removed == null) throw new ItemNotFoundException(id);
        logTransaction(TransactionRecord.TransactionType.PRODUCT_DELETION,
                removed.getId(), removed.getName(), -removed.getStockQuantity(), removed.getUnitPrice(),
                "Product purged from catalog");
    }

    @Override
    public List<Product> getAllProducts() {
        return inventoryMap.values().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public synchronized void restock(String id, int quantity) throws ItemNotFoundException, InvalidTransactionException {
        if (quantity <= 0) {
            throw new InvalidTransactionException("Restock quantity must be positive. Provided: " + quantity);
        }
        Product product = getProduct(id);
        product.addStock(quantity);
        logTransaction(TransactionRecord.TransactionType.RESTOCK,
                product.getId(), product.getName(), quantity, product.getUnitPrice(),
                "Inventory replenished: +" + quantity + " units");
    }

    @Override
    public synchronized void adjustStock(String id, int newQuantity, String reason)
            throws ItemNotFoundException, InvalidTransactionException {
        if (newQuantity < 0) {
            throw new InvalidTransactionException("New stock level cannot be negative: " + newQuantity);
        }
        Product product = getProduct(id);
        int delta = newQuantity - product.getStockQuantity();
        product.setStockQuantity(newQuantity);
        logTransaction(TransactionRecord.TransactionType.STOCK_ADJUSTMENT,
                product.getId(), product.getName(), delta, product.getUnitPrice(),
                "Manual adjustment: " + (reason == null ? "Audit correction" : reason));
    }

    @Override
    public Invoice createInvoice(String customerName, String customerContact, double discountRate) {
        String invoiceId = "INV-" + invoiceSequence.getAndIncrement();
        return new Invoice(invoiceId, customerName, customerContact, discountRate);
    }

    @Override
    public synchronized void addItemToInvoice(Invoice invoice, String productId, int quantity)
            throws ItemNotFoundException, InsufficientStockException, InvalidTransactionException {
        if (invoice == null) throw new InvalidTransactionException("Target invoice cannot be null.");
        if (quantity <= 0) throw new InvalidTransactionException("Cart quantity must be positive: " + quantity);
        Product product = getProduct(productId);
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(productId, quantity, product.getStockQuantity());
        }
        InvoiceItem item = new InvoiceItem(product, quantity);
        invoice.addItem(item);
    }

    @Override
    public synchronized Invoice finalizeInvoice(Invoice invoice) throws InvalidTransactionException {
        if (invoice == null || invoice.getItems().isEmpty()) {
            throw new InvalidTransactionException("Cannot finalize an empty invoice.");
        }

        for (InvoiceItem item : invoice.getItems()) {
            Product product = inventoryMap.get(item.getProductId());
            if (product == null) {
                throw new InvalidTransactionException("Product unavailable during checkout: " + item.getProductId());
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new InvalidTransactionException("Insufficient stock for item: " + product.getId());
            }
        }

        for (InvoiceItem item : invoice.getItems()) {
            Product product = inventoryMap.get(item.getProductId());
            product.deductStock(item.getQuantity());
            logTransaction(TransactionRecord.TransactionType.PURCHASE_SALE,
                    product.getId(), product.getName(), -item.getQuantity(), item.getUnitPrice(),
                    "Sale checkout on invoice: " + invoice.getInvoiceId());
        }

        invoiceRegistry.add(invoice);
        return invoice;
    }

    @Override
    public List<Invoice> getAllInvoices() {
        return Collections.unmodifiableList(invoiceRegistry);
    }

    @Override
    public Invoice getInvoice(String invoiceId) throws ItemNotFoundException {
        return invoiceRegistry.stream()
                .filter(inv -> inv.getInvoiceId().equalsIgnoreCase(invoiceId.trim()))
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException("Invoice: " + invoiceId));
    }

    @Override
    public List<Product> getLowStockAlerts() {
        return inventoryMap.values().stream()
                .filter(Product::isLowStock)
                .sorted(Comparator.comparingInt(Product::getStockQuantity))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAllProducts();
        String term = keyword.trim().toLowerCase();
        return inventoryMap.values().stream()
                .filter(p -> p.getId().toLowerCase().contains(term) ||
                             p.getName().toLowerCase().contains(term) ||
                             p.getCategory().toLowerCase().contains(term))
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        if (category == null) return Collections.emptyList();
        return inventoryMap.values().stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category.trim()))
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public double calculateTotalInventoryValuation() {
        return inventoryMap.values().stream()
                .mapToDouble(p -> p.getUnitPrice() * p.getStockQuantity())
                .sum();
    }

    @Override
    public double calculateTotalRevenue() {
        return invoiceRegistry.stream().mapToDouble(Invoice::getGrandTotal).sum();
    }

    @Override
    public int getTotalStockUnits() {
        return inventoryMap.values().stream().mapToInt(Product::getStockQuantity).sum();
    }

    @Override
    public List<TransactionRecord> getRecentAuditLog() {
        return recentAuditBuffer.toList();
    }

    @Override
    public List<TransactionRecord> getAllAuditLog() {
        return Collections.unmodifiableList(fullAuditTrail);
    }

    public static class StoreSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;

        private final Map<String, Product> inventory;
        private final List<Invoice> invoices;
        private final List<TransactionRecord> auditTrail;
        private final int invoiceSeq;
        private final int txSeq;

        public StoreSnapshot(Map<String, Product> inventory, List<Invoice> invoices,
                             List<TransactionRecord> auditTrail, int invoiceSeq, int txSeq) {
            this.inventory = new HashMap<>(inventory);
            this.invoices = new ArrayList<>(invoices);
            this.auditTrail = new ArrayList<>(auditTrail);
            this.invoiceSeq = invoiceSeq;
            this.txSeq = txSeq;
        }

        public Map<String, Product> getInventory() { return inventory; }
        public List<Invoice> getInvoices() { return invoices; }
        public List<TransactionRecord> getAuditTrail() { return auditTrail; }
        public int getInvoiceSeq() { return invoiceSeq; }
        public int getTxSeq() { return txSeq; }
    }

    public synchronized void saveToFile(String filePath) throws IOException {
        StoreSnapshot snapshot = new StoreSnapshot(
                this.inventoryMap,
                this.invoiceRegistry,
                this.fullAuditTrail,
                this.invoiceSequence.get(),
                this.txSequence.get()
        );
        DataSerializer.serialize(snapshot, filePath);
    }

    public synchronized void loadFromFile(String filePath) throws IOException, ClassNotFoundException {
        StoreSnapshot snapshot = DataSerializer.deserialize(filePath);
        this.inventoryMap.clear();
        this.inventoryMap.putAll(snapshot.getInventory());

        this.invoiceRegistry.clear();
        this.invoiceRegistry.addAll(snapshot.getInvoices());

        this.fullAuditTrail.clear();
        this.fullAuditTrail.addAll(snapshot.getAuditTrail());

        this.recentAuditBuffer.clear();
        for (TransactionRecord r : snapshot.getAuditTrail()) {
            this.recentAuditBuffer.add(r);
        }

        this.invoiceSequence.set(snapshot.getInvoiceSeq());
        this.txSequence.set(snapshot.getTxSeq());
    }

    public void seedSampleData() {
        try {
            addProduct(new PhysicalProduct("SKU-101", "ThinkPad T14 Gen 4", "Electronics", 1199.99, 15, 5, 1.45, 25.0));
            addProduct(new PhysicalProduct("SKU-102", "Dell UltraSharp 27 Monitor", "Electronics", 449.50, 4, 5, 6.20, 35.0));
            addProduct(new PhysicalProduct("SKU-103", "Logitech MX Master 3S", "Accessories", 99.00, 28, 8, 0.35, 10.0));
            addProduct(new PhysicalProduct("SKU-104", "Keychron K2 Mech Keyboard", "Accessories", 89.90, 3, 5, 0.95, 12.0));
            addProduct(new PhysicalProduct("SKU-105", "Sony WH-1000XM5 ANC Headset", "Audio", 398.00, 12, 4, 0.25, 15.0));

            addProduct(new DigitalProduct("DIG-201", "IntelliJ IDEA Ultimate 1-Yr", "Software", 169.00, 100, 10,
                    "https://download.jetbrains.com/idea", "IDEA-XXXX-YYYY-ZZZZ"));
            addProduct(new DigitalProduct("DIG-202", "Oracle Java SE Dev License", "Software", 240.00, 50, 10,
                    "https://oracle.com/java/download", "ORCL-JAVA-PRO-99"));
            addProduct(new DigitalProduct("DIG-203", "AWS Cloud Practitioner Prep", "Courses", 49.99, 2, 5,
                    "https://vityarthi.courses/aws", "CERT-AWS-KEY-101"));
        } catch (InvalidTransactionException e) {
            // Seeded
        }
    }

    private void logTransaction(TransactionRecord.TransactionType type, String productId,
                                String productName, int delta, double price, String note) {
        String txId = "TX-" + txSequence.getAndIncrement();
        TransactionRecord record = new TransactionRecord(txId, type, productId, productName, delta, price, note);
        recentAuditBuffer.add(record);
        fullAuditTrail.add(record);
    }
}
