package com.vityarthi.inventory.service;

import com.vityarthi.inventory.exception.ItemNotFoundException;
import com.vityarthi.inventory.exception.InvalidTransactionException;
import com.vityarthi.inventory.model.Product;
import java.util.List;

/**
 * Interface segregated specifically for inventory lifecycle and catalog mutations.
 */
public interface InventoryService {
    void addProduct(Product product) throws InvalidTransactionException;
    Product getProduct(String id) throws ItemNotFoundException;
    void updateProduct(Product product) throws ItemNotFoundException;
    void deleteProduct(String id) throws ItemNotFoundException;
    List<Product> getAllProducts();
    void restock(String id, int quantity) throws ItemNotFoundException, InvalidTransactionException;
    void adjustStock(String id, int newQuantity, String reason) throws ItemNotFoundException, InvalidTransactionException;
}
