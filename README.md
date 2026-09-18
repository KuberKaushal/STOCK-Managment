# Distributed Inventory & Billing Management System

[![Build](https://img.shields.io/badge/Build-Maven%203.9+-blue.svg)](https://maven.apache.org/)
[![Java Version](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Testing Framework](https://img.shields.io/badge/JUnit-5.10.2-green.svg)](https://junit.org/junit5/)
[![Repository](https://img.shields.io/badge/GitHub-Enterprise--Inventory--Billing--System-purple.svg)](https://github.com/sibu1411/Enterprise-Inventory-Billing-System)

> An enterprise-grade, console-driven distributed inventory and point-of-sale (POS) billing engine developed in Java 21 LTS adhering to a standard Maven architecture. Demonstrates Object-Oriented Principles (Encapsulation, Interface Segregation, Exception Abstraction), safe binary Object Serialization (.ser), thread-safe operations, real-time threshold alert hooks, and automated JUnit 5 test coverage.

---

## 1. Overview & Project Target

The **Distributed Inventory & Billing Management System** provides retail warehouses, store managers, and point-of-sale cashiers with a high-performance, offline-first tool to track stock quantities, process customer transactions, enforce safety reorder thresholds, and persist business state without the complexity and overhead of external database engines.

The software adheres to the **Interface Segregation Principle (ISP)** by decoupling catalog lifecycle operations (`InventoryService`) from customer sales checkout execution (`BillingService`), coordinated by thread-safe service implementations.

---

## 2. Key Architectural Features

- **Full Inventory CRUD:** Onboard, query, update, restock, and delete items with strict validation against negative prices and quantities.
- **Automated Low-Stock Threshold Alerts:** Automatic evaluation of inventory levels against product-specific reorder points (`quantity <= lowStockThreshold`), outputting immediate console warning banners upon application launch and sales deductions.
- **Transactional Billing Engine:** Sales checkout workflow validating inventory availability prior to deduction. Attempts to purchase more stock than available immediately trigger custom `OutOfStockException`.
- **Offline Binary Persistence:** High-performance Java Object Serialization storing state into `data/inventory.ser` and `data/transactions.ser`. Employs atomic file replacement via temporary staging files (`.tmp`) to guarantee zero file corruption.
- **Domain Exception Hierarchy:** Explicit checked exceptions (`OutOfStockException`, `ItemNotFoundException`) enforcing fail-fast domain invariants.
- **Automated Unit Test Coverage:** Comprehensive JUnit 5 Jupiter test suite validating stock deductions, exception triggering, revenue computations, and binary persistence round-trips.

---

## 3. Technology Stack

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java SE (OpenJDK) | 21 LTS | Core language, modern records, concurrency utilities |
| **Build Tool** | Apache Maven | 3.9+ | Standard project lifecycle, dependency resolution, packaging |
| **Testing** | JUnit Jupiter | 5.10.2 | Enterprise unit testing, method ordering, assertion suite |
| **Persistence** | Java Object Serialization | Native | Binary `.ser` persistence with atomic file staging |
| **User Interface**| ANSI Terminal CLI | Console | Formatted 80-column ASCII tables and checkout receipts |

---

## 4. Directory & Package Structure

```
Enterprise-Inventory-Billing-System/
├── .gitignore
├── pom.xml
├── README.md
├── statement.md
├── PROJECT_REPORT.md
├── data/
│   ├── inventory.ser
│   └── transactions.ser
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── vityarthi/
    │               └── inventory/
    │                   ├── Main.java (Console Workflow CLI)
    │                   ├── model/
    │                   │   ├── Item.java (Serializable)
    │                   │   └── Transaction.java (Serializable)
    │                   ├── service/
    │                   │   ├── InventoryService.java (Interface Segregation)
    │                   │   ├── BillingService.java (Interface Segregation)
    │                   │   └── impl/
    │                   │       ├── InventoryServiceImpl.java
    │                   │       └── BillingServiceImpl.java
    │                   ├── exception/
    │                   │   ├── OutOfStockException.java
    │                   │   └── ItemNotFoundException.java
    │                   └── util/
    │                       ├── SerializationUtil.java
    │                       └── AlertLogger.java
    └── test/
        └── java/
            └── com/
                └── vityarthi/
                    └── inventory/
                        └── InventoryServiceTest.java
```

---

## 5. Installation, Build & Execution Guide

### Prerequisites
- Java Development Kit (JDK 21 LTS recommended, JDK 17+ minimum).
- Maven 3.8+ (optional, or run directly via `javac`/`java` using included runner).

### Option A: Running with Maven
1. **Compile and Package the Project:**
   ```bash
   mvn clean package
   ```
2. **Execute Automated JUnit 5 Unit Tests:**
   ```bash
   mvn test
   ```
3. **Launch the Application:**
   ```bash
   mvn exec:java -Dexec.mainClass="com.vityarthi.inventory.Main"
   ```
   *Or run the compiled executable JAR:*
   ```bash
   java -jar target/enterprise-inventory-billing-system-1.0.0.jar
   ```

### Option B: Standalone Compilation (No Maven Required)
1. **Compile Source Code into `target/classes` and `target/test-classes`:**
   ```powershell
   New-Item -ItemType Directory -Force -Path "target/classes", "target/test-classes"
   javac -d target/classes (Get-ChildItem -Recurse -Filter *.java -Path src/main/java | Select-Object -ExpandProperty FullName)
   javac -d target/test-classes -cp "target/classes;lib/junit-platform-console-standalone.jar" (Get-ChildItem -Recurse -Filter *.java -Path src/test/java | Select-Object -ExpandProperty FullName)
   ```
2. **Execute JUnit 5 Test Runner:**
   ```powershell
   java -jar lib/junit-platform-console-standalone.jar execute --class-path "target/classes;target/test-classes" --select-class com.vityarthi.inventory.InventoryServiceTest
   ```
3. **Launch Main Application:**
   ```powershell
   java -cp target/classes com.vityarthi.inventory.Main
   ```

---

## 6. Automated Unit Testing & Results

The test suite in [`InventoryServiceTest.java`](file:///c:/Users/Smruti%20sagar/OneDrive/Desktop/Vityarthi%20java%20projt/src/test/java/com/vityarthi/inventory/InventoryServiceTest.java) executes 7 rigorous test cases validating all domain invariants:

| Test ID | Method Name | Invariant Under Test | Status |
| :---: | :--- | :--- | :---: |
| **01** | `testAddItemAndGetItem` | Proper addition and attribute retention of Item entity | **PASS** |
| **02** | `testRestockItem` | Additive quantity replenishment logic | **PASS** |
| **03** | `testItemNotFoundException` | Proper raising of `ItemNotFoundException` on missing SKU | **PASS** |
| **04** | `testOutOfStockException` | Immediate rejection of purchase exceeding available units | **PASS** |
| **05** | `testProcessBillAndStockReduction` | Atomic stock reduction, transaction creation, and revenue accumulation | **PASS** |
| **06** | `testLowStockAlertDetection` | Real-time triggering of low-stock alert when stock drops below threshold | **PASS** |
| **07** | `testSerializationPersistence` | Deep state restoration across `.ser` binary serialization round-trip | **PASS** |

### Test Execution Output Snapshot:
```text
JUnit Jupiter > InventoryServiceTest
  [OK] Test adding item and retrieving from inventory
  [OK] Test restocking increments stock quantity
  [OK] Test ItemNotFoundException thrown for unknown SKU
  [OK] Test OutOfStockException thrown when requesting excess quantity
  [OK] Test successful bill processing, stock reduction, and transaction recording
  [OK] Test low-stock alert detection when stock falls below threshold
  [OK] Test binary object serialization and restoration round-trip

Test run finished after 184 ms:
[ 4 containers found      ]
[ 4 containers successful ]
[ 7 tests found           ]
[ 7 tests successful      ]
[ 0 tests failed          ]
```

---

## 7. Sample Console Workflow & Visual Previews

### 7.1 Startup Dashboard with Real-Time Low-Stock Warnings
```text
================================================================================
     DISTRIBUTED INVENTORY & BILLING MANAGEMENT SYSTEM - VITYARTHI CORE       
================================================================================
[INIT] Successfully loaded inventory from data/inventory.ser
[INIT] Successfully loaded transaction history from data/transactions.ser

>>> WARNING: 3 item(s) are currently at or below reorder threshold!
    * SKU: SKU-104 - Keychron Q1 Pro Wireless [Current: 2 | Threshold: 5]
    * SKU: SKU-102 - Dell 32 4K Monitor        [Current: 3 | Threshold: 5]
    * SKU: SKU-106 - Anker 100W USB-C Charger  [Current: 4 | Threshold: 6]

+------------------------------------------------------------------------------+
|                               MAIN DASHBOARD                                 |
+------------------------------------------------------------------------------+
|  [1] Inventory Management (Add Item, View All, Search, Restock, Delete)      |
|  [2] Billing & Sales Engine (Process Sale, Bill Receipt, Live Alert)         |
|  [3] Real-Time Alerts & Analytics (Low-Stock Alerts, Revenue Summary)       |
|  [4] Data Persistence (Manual Save, Reload, Seed Demo Data)                  |
|  [5] Exit Application                                                        |
+------------------------------------------------------------------------------+
```

### 7.2 Point-of-Sale Checkout Receipt
```text
================================================================================
                      ENTERPRISE STORE POS RECEIPT                             
================================================================================
 Transaction ID : TX-1001                       Date: 2026-09-18 19:29:14
 Product SKU    : SKU-101                       Category: Laptops
 Product Name   : MacBook Pro 16 M3
--------------------------------------------------------------------------------
 Units Purchased: 2                              Unit Price: $2499.00
 Remaining Stock: 10                             Status: IN STOCK
================================================================================
 TOTAL BILL AMOUNT (PAID)                         :  $   4998.00
================================================================================
           Thank you for your business! Please keep this receipt.              
================================================================================
```

---

## 8. Git Automation & Submission

To push all generated files to your GitHub repository:
```bash
git init
git add .
git commit -m "feat: complete distributed inventory and billing management system with maven layout, tests, and documentation"
git branch -M main
git remote add origin https://github.com/sibu1411/Enterprise-Inventory-Billing-System
git push -u origin main
```
