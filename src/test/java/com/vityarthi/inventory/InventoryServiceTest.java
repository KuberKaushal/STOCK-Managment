package com.vityarthi.inventory;

import com.vityarthi.inventory.exception.ItemNotFoundException;
import com.vityarthi.inventory.exception.OutOfStockException;
import com.vityarthi.inventory.model.Item;
import com.vityarthi.inventory.model.Transaction;
import com.vityarthi.inventory.service.BillingService;
import com.vityarthi.inventory.service.InventoryService;
import com.vityarthi.inventory.service.impl.BillingServiceImpl;
import com.vityarthi.inventory.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InventoryServiceTest {

    private InventoryService inventoryService;
    private BillingService billingService;

    @BeforeEach
    public void setUp() {
        inventoryService = new InventoryServiceImpl();
        billingService = new BillingServiceImpl(inventoryService);
    }

    @Test
    @Order(1)
    @DisplayName("Test adding item and retrieving from inventory")
    public void testAddItemAndGetItem() throws ItemNotFoundException {
        Item item = new Item("SKU-001", "Mechanical Keyboard", "Hardware", 89.99, 10, 3);
        inventoryService.addItem(item);

        Item retrieved = inventoryService.getItem("SKU-001");
        assertNotNull(retrieved);
        assertEquals("Mechanical Keyboard", retrieved.getName());
        assertEquals(89.99, retrieved.getPrice(), 0.001);
        assertEquals(10, retrieved.getQuantity());
        assertEquals(3, retrieved.getLowStockThreshold());
    }

    @Test
    @Order(2)
    @DisplayName("Test restocking increments stock quantity")
    public void testRestockItem() throws ItemNotFoundException {
        Item item = new Item("SKU-002", "USB-C Hub", "Accessories", 29.99, 5, 2);
        inventoryService.addItem(item);

        inventoryService.restock("SKU-002", 15);
        Item updated = inventoryService.getItem("SKU-002");
        assertEquals(20, updated.getQuantity());
    }

    @Test
    @Order(3)
    @DisplayName("Test ItemNotFoundException thrown for unknown SKU")
    public void testItemNotFoundException() {
        assertThrows(ItemNotFoundException.class, () -> inventoryService.getItem("UNKNOWN-SKU"));
    }

    @Test
    @Order(4)
    @DisplayName("Test OutOfStockException thrown when requesting excess quantity")
    public void testOutOfStockException() {
        Item item = new Item("SKU-003", "Wireless Mouse", "Accessories", 49.99, 2, 1);
        inventoryService.addItem(item);

        assertThrows(OutOfStockException.class, () -> billingService.processBill("SKU-003", 5));
    }

    @Test
    @Order(5)
    @DisplayName("Test successful bill processing, stock reduction, and transaction recording")
    public void testProcessBillAndStockReduction() throws ItemNotFoundException, OutOfStockException {
        Item item = new Item("SKU-004", "Dell XPS Laptop", "Computers", 1200.00, 8, 2);
        inventoryService.addItem(item);

        Transaction tx = billingService.processBill("SKU-004", 3);
        assertNotNull(tx);
        assertEquals("SKU-004", tx.getItemId());
        assertEquals(3, tx.getQuantity());
        assertEquals(3600.00, tx.getTotalAmount(), 0.001);

        Item updated = inventoryService.getItem("SKU-004");
        assertEquals(5, updated.getQuantity());

        List<Transaction> history = billingService.getTransactionHistory();
        assertEquals(1, history.size());
        assertEquals(3600.00, billingService.calculateTotalRevenue(), 0.001);
    }

    @Test
    @Order(6)
    @DisplayName("Test low-stock alert detection when stock falls below threshold")
    public void testLowStockAlertDetection() throws ItemNotFoundException, OutOfStockException {
        Item item = new Item("SKU-005", "4K Webcam", "Video", 129.99, 5, 3);
        inventoryService.addItem(item);
        assertFalse(item.isLowStock());

        billingService.processBill("SKU-005", 3);

        Item updated = inventoryService.getItem("SKU-005");
        assertEquals(2, updated.getQuantity());
        assertTrue(updated.isLowStock());

        List<Item> lowStockList = inventoryService.getLowStockItems();
        assertEquals(1, lowStockList.size());
        assertEquals("SKU-005", lowStockList.get(0).getItemId());
    }

    @Test
    @Order(7)
    @DisplayName("Test binary object serialization and restoration round-trip")
    public void testSerializationPersistence() throws Exception {
        String testInvPath = "data/test_inventory.ser";
        String testTxPath = "data/test_transactions.ser";

        Item item = new Item("SKU-006", "Curved Gaming Monitor", "Displays", 450.00, 10, 2);
        inventoryService.addItem(item);
        billingService.processBill("SKU-006", 2);

        inventoryService.saveInventory(testInvPath);
        billingService.saveTransactions(testTxPath);

        InventoryService restoredInv = new InventoryServiceImpl();
        BillingService restoredBilling = new BillingServiceImpl(restoredInv);

        restoredInv.loadInventory(testInvPath);
        restoredBilling.loadTransactions(testTxPath);

        Item restoredItem = restoredInv.getItem("SKU-006");
        assertNotNull(restoredItem);
        assertEquals(8, restoredItem.getQuantity());
        assertEquals(1, restoredBilling.getTransactionHistory().size());
        assertEquals(900.00, restoredBilling.calculateTotalRevenue(), 0.001);

        new File(testInvPath).delete();
        new File(testTxPath).delete();
    }
}
