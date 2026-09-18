package com.vityarthi.inventory.exception;

public class OutOfStockException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String itemId;
    private final int requested;
    private final int available;

    public OutOfStockException(String itemId, int requested, int available) {
        super(String.format("Stock deficit for item '%s': requested %d units, but only %d units are in stock.",
                itemId, requested, available));
        this.itemId = itemId;
        this.requested = requested;
        this.available = available;
    }

    public String getItemId() { return itemId; }
    public int getRequested() { return requested; }
    public int getAvailable() { return available; }
}
