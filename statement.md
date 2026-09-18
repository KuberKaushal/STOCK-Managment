# Project Problem Statement & Scope Specification

**Project Title:** Distributed Inventory & Billing Management System  
**Course:** Core & Advanced Java Programming  
**Repository:** https://github.com/sibu1411/Enterprise-Inventory-Billing-System  
**Author:** [Student Name] | [Registration Number]  
**Institution:** Vellore Institute of Technology (VIT)  

---

## 1. Problem Statement

Retail operations, localized distribution centers, and stock warehouses encounter persistent operational friction when coordinating inventory updates, point-of-sale customer billing, and stock depletion risks. Common limitations in conventional software systems include:
1. **Tight Coupling & Monolithic Architecture:** Inventory mutation methods are intertwined with checkout calculations and logging routines, violating the Interface Segregation Principle (ISP) and leading to rigid, brittle code.
2. **Data Volatility & Heavy DB Dependencies:** Simple prototypes lose data on JVM shutdown, while production databases (PostgreSQL, MySQL) impose excessive driver, networking, and schema maintenance overhead for edge warehouses.
3. **Concurrency & Overselling Defects:** Failure to isolate stock verification from deduction creates race conditions where multiple checkout cashiers sell out-of-stock items, resulting in negative inventory counts.
4. **Delayed Stockout Visibility:** Inventory managers often discover stock depletion only after orders fail, due to the lack of automated, real-time threshold alert hooks.

The **Distributed Inventory & Billing Management System** resolves these challenges by introducing a decoupled, thread-safe, and offline-first Java application. The system provides strict inventory invariant validation, transactional billing deductions, real-time low-stock threshold alerting, and offline binary data persistence via Java Object Serialization (`.ser`).

---

## 2. Project Scope

### 2.1 In-Scope Capabilities
- **Inventory Catalog Management (CRUD):** Full lifecycle management of stock items with SKU identification, product naming, category classification, dynamic pricing, and configurable safety reorder thresholds.
- **Transactional Billing Engine:** Real-time point-of-sale checkout processing where inventory is atomically decremented, transactions are logged, and ASCII sales receipts are generated.
- **Automated Threshold Monitoring:** Real-time evaluation of item safety thresholds upon every inventory mutation and application startup, automatically triggering visual low-stock alert warnings.
- **Offline Binary Persistence:** High-performance object serialization and deserialization (`inventory.ser` and `transactions.ser`) using atomic temporary file staging to protect against file corruption.
- **Domain Exception Hierarchy:** Explicit domain checked exceptions (`OutOfStockException`, `ItemNotFoundException`) to handle boundary violations predictably without crashing the runtime.
- **Automated Quality Assurance:** JUnit 5 test suite validating core business logic, stock reduction invariants, exception propagation, and persistence round-trips.

### 2.2 Out-of-Scope (Future Iterations)
- Distributed network socket RPC / REST API endpoints (the project implements an offline-first domain core ready for network binding).
- External SQL relational database drivers (RDBMS is intentionally replaced with native Java Object Serialization for zero-dependency portability).
- Graphical User Interface (JavaFX / Swing), opting for an 80-column clean ANSI terminal interface.

---

## 3. Target Audience & Stakeholders

| Stakeholder Role | Responsibilities & Touchpoints | Primary Business Value Delivered |
| :--- | :--- | :--- |
| **Warehouse / Inventory Manager** | Stock onboarding, item restocking, safety threshold configuration, catalog auditing. | Automated low-stock alerts eliminate stockout surprises and streamline reordering. |
| **Point-of-Sale (POS) Cashier** | Customer billing, real-time item lookup, checkout processing, receipt printing. | Instant stock validation prevents overselling; generates clean itemized receipts. |
| **Store Auditor / Business Analyst** | Reviewing historical sales logs, evaluating inventory valuation, revenue metrics. | Accurate cumulative revenue tracking and complete historical transaction trails. |

---

## 4. High-Level Feature List

### Module 1: Inventory Management & Low-Stock Alerts
- Onboard new items with SKU, Name, Category, Price, Quantity, and Reorder Threshold.
- Additive stock replenishment (`restock`) with positive quantity validation.
- Item search and lookup by SKU.
- Item deletion with automatic catalog rebalancing.
- Real-time low-stock warning banners upon application launch and on demand.

### Module 2: Billing & Transaction Engine
- Point-of-sale billing workflow verifying item availability.
- Strict rejection of overdraft purchases via `OutOfStockException`.
- Atomic stock quantity reduction upon bill completion.
- Automated generation of 80-column ASCII sales receipts.
- Complete transaction recording with unique transaction IDs and timestamps.

### Module 3: Analytics & Reporting
- Real-time catalog asset valuation ($\sum 	ext{Price} 	imes 	ext{Quantity}$).
- Cumulative gross sales revenue calculation.
- Tabular chronological log of all completed transactions.
- Active low-stock reorder alert dashboard.

### Module 4: Persistence & Storage
- Native Java Object Serialization (`SerializationUtil`) saving state to `data/inventory.ser` and `data/transactions.ser`.
- Safe atomic file replacement prevents corruption during unexpected termination.
- Automated data restoration on startup with fallback demo catalog seeding.
