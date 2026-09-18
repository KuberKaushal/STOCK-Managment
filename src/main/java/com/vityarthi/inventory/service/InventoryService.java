package com.vityarthi.inventory.service;

import com.vityarthi.inventory.exception.ItemNotFoundException;
import com.vityarthi.inventory.model.Item;

import java.io.IOException;
import java.util.List;

public interface InventoryService {
    void addItem(Item item);
    Item getItem(String itemId) throws ItemNotFoundException;
    void updateItem(Item item) throws ItemNotFoundException;
    void deleteItem(String itemId) throws ItemNotFoundException;
    List<Item> getAllItems();
    void restock(String itemId, int quantity) throws ItemNotFoundException;
    List<Item> getLowStockItems();
    double calculateTotalInventoryValuation();
    void saveInventory(String filePath) throws IOException;
    void loadInventory(String filePath) throws IOException, ClassNotFoundException;
}
