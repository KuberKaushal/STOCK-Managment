package com.vityarthi.inventory.util;

import com.vityarthi.inventory.model.Invoice;
import com.vityarthi.inventory.model.InvoiceItem;
import com.vityarthi.inventory.model.Product;
import com.vityarthi.inventory.model.TransactionRecord;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Enterprise ASCII tabular layout and receipt formatting utility.
 */
public final class ReceiptFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    private ReceiptFormatter() {}

    public static String formatReceipt(Invoice invoice) {
        StringBuilder sb = new StringBuilder();
        String line = "================================================================================";
        String thinLine = "--------------------------------------------------------------------------------";

        sb.append(line).append(System.lineSeparator());
        sb.append("                      VITYARTHI SMART STORE ENTERPRISE                        ").append(System.lineSeparator());
        sb.append("                       TAX INVOICE & CASH RECEIPT                             ").append(System.lineSeparator());
        sb.append(line).append(System.lineSeparator());
        sb.append(String.format(" Invoice No  : %-30s Date: %s%n", invoice.getInvoiceId(), invoice.getTimestamp().format(DATE_FORMATTER)));
        sb.append(String.format(" Customer    : %-30s Contact: %s%n", invoice.getCustomerName(), invoice.getCustomerContact()));
        sb.append(thinLine).append(System.lineSeparator());
        sb.append(String.format(" %-8s | %-24s | %-8s | %-4s | %-9s | %-8s %n",
                "SKU", "ITEM DESCRIPTION", "TYPE", "QTY", "PRICE($)", "TOTAL($)"));
        sb.append(thinLine).append(System.lineSeparator());

        for (InvoiceItem item : invoice.getItems()) {
            sb.append(String.format(" %-8s | %-24s | %-8s | %4d | %9.2f | %8.2f %n",
                    item.getProductId(),
                    truncate(item.getProductName(), 24),
                    truncate(item.getProductType(), 8),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotal()));
        }

        sb.append(thinLine).append(System.lineSeparator());
        sb.append(String.format(" Subtotal (Gross Items)                         :  $%10.2f%n", invoice.getSubtotal()));
        if (invoice.getDiscountRate() > 0) {
            sb.append(String.format(" Applied Discount (%.1f%%)                         : -$%10.2f%n",
                    invoice.getDiscountRate() * 100, invoice.getDiscountAmount()));
        }
        sb.append(String.format(" Applicable Taxes (GST / Digital Levy)          :  $%10.2f%n", invoice.getTotalTax()));
        sb.append(line).append(System.lineSeparator());
        sb.append(String.format(" NET PAYABLE AMOUNT (GRAND TOTAL)              :  $%10.2f%n", invoice.getGrandTotal()));
        sb.append(line).append(System.lineSeparator());
        sb.append("           Thank you for shopping with us! Please retain this receipt.         ").append(System.lineSeparator());
        sb.append("               Powered by Distributed Inventory & Billing Core                ").append(System.lineSeparator());
        sb.append(line).append(System.lineSeparator());

        return sb.toString();
    }

    public static String formatProductTable(List<Product> products) {
        StringBuilder sb = new StringBuilder();
        String line = "+----------+--------------------------+------------+-----------+-------+-------+----------+";
        sb.append(line).append(System.lineSeparator());
        sb.append(String.format("| %-8s | %-24s | %-10s | %-9s | %-5s | %-5s | %-8s |%n",
                "SKU", "PRODUCT NAME", "TYPE", "PRICE($)", "STOCK", "MIN", "STATUS"));
        sb.append(line).append(System.lineSeparator());

        if (products.isEmpty()) {
            sb.append("|                       NO PRODUCTS REGISTERED IN CATALOG                      |").append(System.lineSeparator());
        } else {
            for (Product p : products) {
                String status = p.isLowStock() ? "LOW ALERT" : "OK";
                sb.append(String.format("| %-8s | %-24s | %-10s | %9.2f | %5d | %5d | %-8s |%n",
                        p.getId(),
                        truncate(p.getName(), 24),
                        truncate(p.getProductType(), 10),
                        p.getUnitPrice(),
                        p.getStockQuantity(),
                        p.getMinThreshold(),
                        status));
            }
        }
        sb.append(line).append(System.lineSeparator());
        return sb.toString();
    }

    public static String formatAuditLogTable(List<TransactionRecord> logs) {
        StringBuilder sb = new StringBuilder();
        String line = "+---------------------+------------------+----------+----------+-------+----------+----------------------+";
        sb.append(line).append(System.lineSeparator());
        sb.append(String.format("| %-19s | %-16s | %-8s | %-8s | %-5s | %-8s | %-20s |%n",
                "TIMESTAMP", "TX TYPE", "TX ID", "SKU", "DELTA", "UNIT($)", "NOTE"));
        sb.append(line).append(System.lineSeparator());

        if (logs.isEmpty()) {
            sb.append("|                                NO AUDIT RECORDS RECORDED YET                             |").append(System.lineSeparator());
        } else {
            for (TransactionRecord r : logs) {
                sb.append(String.format("| %-19s | %-16s | %-8s | %-8s | %+5d | %8.2f | %-20s |%n",
                        r.getTimestamp().format(DATE_FORMATTER),
                        truncate(r.getType().name(), 16),
                        r.getTransactionId(),
                        r.getProductId(),
                        r.getQuantityDelta(),
                        r.getUnitPrice(),
                        truncate(r.getNote(), 20)));
            }
        }
        sb.append(line).append(System.lineSeparator());
        return sb.toString();
    }

    private static String truncate(String val, int maxLen) {
        if (val == null) return "";
        if (val.length() <= maxLen) return val;
        return val.substring(0, maxLen - 2) + "..";
    }
}
