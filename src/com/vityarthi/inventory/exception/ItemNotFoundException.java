package com.vityarthi.inventory.exception;

public class ItemNotFoundException extends InventoryException {
    private static final long serialVersionUID = 1L;
    private final String productId;

    public ItemNotFoundException(String productId) {
        super("Product with ID/SKU '" + productId + "' was not found in the inventory registry.");
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}
