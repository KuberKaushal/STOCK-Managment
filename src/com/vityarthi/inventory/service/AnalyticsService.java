package com.vityarthi.inventory.service;

import com.vityarthi.inventory.model.Product;
import com.vityarthi.inventory.model.TransactionRecord;
import java.util.List;

/**
 * Interface segregated specifically for reporting, threshold monitoring, and audit log analysis.
 */
public interface AnalyticsService {
    List<Product> getLowStockAlerts();
    List<Product> searchProducts(String keyword);
    List<Product> getProductsByCategory(String category);
    double calculateTotalInventoryValuation();
    double calculateTotalRevenue();
    int getTotalStockUnits();
    List<TransactionRecord> getRecentAuditLog();
    List<TransactionRecord> getAllAuditLog();
}
