package com.vityarthi.inventory.service.impl;

import com.vityarthi.inventory.exception.ItemNotFoundException;
import com.vityarthi.inventory.model.Item;
import com.vityarthi.inventory.service.InventoryService;
import com.vityarthi.inventory.util.AlertLogger;
import com.vityarthi.inventory.util.SerializationUtil;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InventoryServiceImpl implements InventoryService {

    private final Map<String, Item> inventoryMap;

    public InventoryServiceImpl() {
        this.inventoryMap = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized void addItem(Item item) {
        if (item == null) throw new IllegalArgumentException("Cannot add null item.");
        if (inventoryMap.containsKey(item.getItemId())) {
            throw new IllegalArgumentException("Item with SKU '" + item.getItemId() + "' already exists.");
        }
        inventoryMap.put(item.getItemId(), item);
        AlertLogger.checkAndAlert(item);
    }

    @Override
    public Item getItem(String itemId) throws ItemNotFoundException {
        if (itemId == null) throw new ItemNotFoundException("null");
        Item item = inventoryMap.get(itemId.trim().toUpperCase());
        if (item == null) throw new ItemNotFoundException(itemId);
        return item;
    }

    @Override
    public synchronized void updateItem(Item item) throws ItemNotFoundException {
        if (item == null || !inventoryMap.containsKey(item.getItemId())) {
            throw new ItemNotFoundException((item == null) ? "null" : item.getItemId());
        }
        inventoryMap.put(item.getItemId(), item);
        AlertLogger.checkAndAlert(item);
    }

    @Override
    public synchronized void deleteItem(String itemId) throws ItemNotFoundException {
        if (itemId == null || !inventoryMap.containsKey(itemId.trim().toUpperCase())) {
            throw new ItemNotFoundException(itemId);
        }
        inventoryMap.remove(itemId.trim().toUpperCase());
    }

    @Override
    public List<Item> getAllItems() {
        return inventoryMap.values().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public synchronized void restock(String itemId, int quantity) throws ItemNotFoundException {
        if (quantity <= 0) throw new IllegalArgumentException("Restock quantity must be positive. Received: " + quantity);
        Item item = getItem(itemId);
        item.addStock(quantity);
    }

    @Override
    public List<Item> getLowStockItems() {
        return inventoryMap.values().stream()
                .filter(Item::isLowStock)
                .sorted(Comparator.comparingInt(Item::getQuantity))
                .collect(Collectors.toList());
    }

    @Override
    public double calculateTotalInventoryValuation() {
        return inventoryMap.values().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void saveInventory(String filePath) throws IOException {
        HashMap<String, Item> serializableMap = new HashMap<>(this.inventoryMap);
        SerializationUtil.serialize(serializableMap, filePath);
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void loadInventory(String filePath) throws IOException, ClassNotFoundException {
        HashMap<String, Item> loaded = SerializationUtil.deserialize(filePath, HashMap.class);
        this.inventoryMap.clear();
        this.inventoryMap.putAll(loaded);
    }
}
