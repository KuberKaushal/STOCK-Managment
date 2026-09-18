package com.vityarthi.inventory.test;

import com.vityarthi.inventory.exception.InsufficientStockException;
import com.vityarthi.inventory.exception.InvalidTransactionException;
import com.vityarthi.inventory.exception.ItemNotFoundException;
import com.vityarthi.inventory.model.DigitalProduct;
import com.vityarthi.inventory.model.Invoice;
import com.vityarthi.inventory.model.PhysicalProduct;
import com.vityarthi.inventory.model.Product;
import com.vityarthi.inventory.service.StoreManager;
import com.vityarthi.inventory.util.CircularAuditBuffer;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreManagerTest {

    private StoreManager store;

    @BeforeEach
    public void setUp() {
        store = new StoreManager();
    }

    @Test
    @Order(1)
    @DisplayName("Test polymorphic product addition and tax calculations")
    public void testPolymorphicProductsAndTaxes() throws InvalidTransactionException, ItemNotFoundException {
        PhysicalProduct book = new PhysicalProduct("BOOK-01", "Clean Architecture", "Books", 35.00, 20, 5, 0.6, 5.0);
        DigitalProduct course = new DigitalProduct("CRS-01", "Java Design Patterns", "Education", 75.00, 100, 10,
                "https://vityarthi.com/dl", "CRS-KEY-001");

        store.addProduct(book);
        store.addProduct(course);

        Product retrievedBook = store.getProduct("BOOK-01");
        Product retrievedCourse = store.getProduct("CRS-01");

        assertNotNull(retrievedBook);
        assertEquals(0.12, retrievedBook.calculateTaxRate(), 0.001);
        assertEquals("PHYSICAL", retrievedBook.getProductType());

        assertNotNull(retrievedCourse);
        assertEquals(0.18, retrievedCourse.calculateTaxRate(), 0.001);
        assertEquals("DIGITAL ", retrievedCourse.getProductType());
    }

    @Test
    @Order(2)
    @DisplayName("Test restock and stock adjustment")
    public void testRestockAndAdjustment() throws InvalidTransactionException, ItemNotFoundException {
        PhysicalProduct mouse = new PhysicalProduct("MOU-01", "Ergo Mouse", "Accessories", 45.0, 10, 5, 0.2, 5.0);
        store.addProduct(mouse);

        store.restock("MOU-01", 15);
        assertEquals(25, store.getProduct("MOU-01").getStockQuantity());

        store.adjustStock("MOU-01", 30, "Quarterly audit count");
        assertEquals(30, store.getProduct("MOU-01").getStockQuantity());
    }

    @Test
    @Order(3)
    @DisplayName("Test ItemNotFoundException thrown for non-existent product")
    public void testItemNotFoundException() {
        assertThrows(ItemNotFoundException.class, () -> {
            store.getProduct("NON-EXISTENT-SKU");
        });
    }

    @Test
    @Order(4)
    @DisplayName("Test InsufficientStockException thrown when requesting excess stock")
    public void testInsufficientStockException() throws InvalidTransactionException {
        PhysicalProduct pen = new PhysicalProduct("PEN-01", "Gel Pen Box", "Stationery", 10.0, 5, 2, 0.1, 2.0);
        store.addProduct(pen);

        Invoice invoice = store.createInvoice("Alice Smith", "9876543210", 0.0);

        assertThrows(InsufficientStockException.class, () -> {
            store.addItemToInvoice(invoice, "PEN-01", 10);
        });
    }

    @Test
    @Order(5)
    @DisplayName("Test full checkout billing workflow, discounts, and inventory deduction")
    public void testBillingWorkflowAndStockDeduction()
            throws InvalidTransactionException, ItemNotFoundException, InsufficientStockException {
        PhysicalProduct laptop = new PhysicalProduct("LAP-01", "UltraBook Pro", "Computers", 1000.00, 10, 2, 1.2, 20.0);
        store.addProduct(laptop);

        Invoice invoice = store.createInvoice("Bob Tech", "9870001111", 0.10);
        store.addItemToInvoice(invoice, "LAP-01", 2);

        assertEquals(2000.00, invoice.getSubtotal(), 0.01);
        assertEquals(200.00, invoice.getDiscountAmount(), 0.01);
        assertEquals(240.00, invoice.getTotalTax(), 0.01);
        assertEquals(2040.00, invoice.getGrandTotal(), 0.01);

        Invoice finalized = store.finalizeInvoice(invoice);
        assertNotNull(finalized);

        Product updatedLaptop = store.getProduct("LAP-01");
        assertEquals(8, updatedLaptop.getStockQuantity());
    }

    @Test
    @Order(6)
    @DisplayName("Test low-stock alert detection")
    public void testLowStockAlerts() throws InvalidTransactionException {
        PhysicalProduct p1 = new PhysicalProduct("ALT-01", "Item Low", "Misc", 10.0, 2, 5, 0.5, 5.0);
        PhysicalProduct p2 = new PhysicalProduct("ALT-02", "Item OK", "Misc", 15.0, 20, 5, 0.5, 5.0);

        store.addProduct(p1);
        store.addProduct(p2);

        List<Product> alerts = store.getLowStockAlerts();
        assertEquals(1, alerts.size());
        assertEquals("ALT-01", alerts.get(0).getId());
    }

    @Test
    @Order(7)
    @DisplayName("Test CircularAuditBuffer capacity, FIFO eviction, and ring wrapping")
    public void testCircularAuditBuffer() {
        CircularAuditBuffer<String> buffer = new CircularAuditBuffer<>(3);
        assertEquals(0, buffer.size());

        buffer.add("Event 1");
        buffer.add("Event 2");
        buffer.add("Event 3");
        assertEquals(3, buffer.size());
        assertTrue(buffer.isFull());

        buffer.add("Event 4");
        assertEquals(3, buffer.size());

        List<String> list = buffer.toList();
        assertEquals("Event 2", list.get(0));
        assertEquals("Event 3", list.get(1));
        assertEquals("Event 4", list.get(2));
    }

    @Test
    @Order(8)
    @DisplayName("Test Object Serialization and state restoration round-trip")
    public void testSerializationPersistence() throws Exception {
        String testFilePath = "data/test_state.dat";
        PhysicalProduct cable = new PhysicalProduct("CAB-01", "USB-C Braided Cable", "Cables", 15.0, 50, 10, 0.1, 2.0);
        store.addProduct(cable);

        store.saveToFile(testFilePath);

        StoreManager restoredStore = new StoreManager();
        restoredStore.loadFromFile(testFilePath);

        Product restoredCable = restoredStore.getProduct("CAB-01");
        assertNotNull(restoredCable);
        assertEquals("USB-C Braided Cable", restoredCable.getName());
        assertEquals(50, restoredCable.getStockQuantity());

        new File(testFilePath).delete();
    }
}
