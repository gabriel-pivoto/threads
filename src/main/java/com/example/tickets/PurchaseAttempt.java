package com.example.tickets;

public class PurchaseAttempt {

    public static final int NOT_AVAILABLE = Integer.MIN_VALUE;

    private final String buyerId;
    private final String workerThreadName;
    private final boolean success;
    private final int stockObservedBeforeAttempt;
    private final int remainingStockAfterAttempt;
    private final String detail;

    public PurchaseAttempt(
            String buyerId,
            String workerThreadName,
            boolean success,
            int stockObservedBeforeAttempt,
            int remainingStockAfterAttempt,
            String detail
    ) {
        this.buyerId = buyerId;
        this.workerThreadName = workerThreadName;
        this.success = success;
        this.stockObservedBeforeAttempt = stockObservedBeforeAttempt;
        this.remainingStockAfterAttempt = remainingStockAfterAttempt;
        this.detail = detail;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getWorkerThreadName() {
        return workerThreadName;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStockObservedBeforeAttempt() {
        return stockObservedBeforeAttempt;
    }

    public int getRemainingStockAfterAttempt() {
        return remainingStockAfterAttempt;
    }

    public String getDetail() {
        return detail;
    }
}
