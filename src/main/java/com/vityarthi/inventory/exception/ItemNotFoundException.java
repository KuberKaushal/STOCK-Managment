package com.vityarthi.inventory.exception;

public class ItemNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String itemId;

    public ItemNotFoundException(String itemId) {
        super("Item with SKU/ID '" + itemId + "' was not found in the inventory registry.");
        this.itemId = itemId;
    }

    public String getItemId() { return itemId; }
}
