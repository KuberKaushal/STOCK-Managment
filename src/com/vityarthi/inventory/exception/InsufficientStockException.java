package com.vityarthi.inventory.exception;

public class InsufficientStockException extends InventoryException {
    private static final long serialVersionUID = 1L;
    private final String productId;
    private final int requested;
    private final int available;

    public InsufficientStockException(String productId, int requested, int available) {
        super(String.format("Stock deficit for product '%s': requested %d units, but only %d units available.",
                productId, requested, available));
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }

    public String getProductId() {
        return productId;
    }

    public int getRequested() {
        return requested;
    }

    public int getAvailable() {
        return available;
    }
}
