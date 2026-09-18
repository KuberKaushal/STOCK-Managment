package com.vityarthi.inventory.exception;

public class InvalidTransactionException extends InventoryException {
    private static final long serialVersionUID = 1L;

    public InvalidTransactionException(String message) {
        super(message);
    }

    public InvalidTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
