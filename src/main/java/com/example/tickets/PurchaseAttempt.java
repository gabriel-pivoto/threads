package com.example.tickets;

public class PurchaseAttempt {

    public static final int NOT_AVAILABLE = Integer.MIN_VALUE;
    public static final String NO_PROOF_TOKEN = "NO-PROOF";

    private final String buyerId;
    private final String workerThreadName;
    private final boolean success;
    private final int stockObservedBeforeAttempt;
    private final int remainingStockAfterAttempt;
    private final String proofToken;
    private final String detail;

    public PurchaseAttempt(
            String buyerId,
            String workerThreadName,
            boolean success,
            int stockObservedBeforeAttempt,
            int remainingStockAfterAttempt,
            String proofToken,
            String detail
    ) {
        this.buyerId = buyerId;
        this.workerThreadName = workerThreadName;
        this.success = success;
        this.stockObservedBeforeAttempt = stockObservedBeforeAttempt;
        this.remainingStockAfterAttempt = remainingStockAfterAttempt;
        this.proofToken = proofToken;
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

    public String getProofToken() {
        return proofToken;
    }

    public boolean hasProofToken() {
        return !NO_PROOF_TOKEN.equals(proofToken);
    }

    public int getProofSequence() {
        if (!hasProofToken()) {
            return Integer.MAX_VALUE;
        }

        int lastDash = proofToken.lastIndexOf('-');
        if (lastDash < 0 || lastDash == proofToken.length() - 1) {
            return Integer.MAX_VALUE;
        }

        try {
            return Integer.parseInt(proofToken.substring(lastDash + 1));
        } catch (NumberFormatException exception) {
            return Integer.MAX_VALUE;
        }
    }

    public String getDetail() {
        return detail;
    }
}
