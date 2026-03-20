package com.example.tickets;

import java.util.List;

public class SimulationResult {

    private final String simulationType;
    private final int initialTicketCount;
    private final int totalBuyerThreads;
    private final List<PurchaseAttempt> attempts;
    private final int finalRemainingStock;
    private final long durationMillis;
    private final int successfulPurchases;
    private final int failedPurchases;
    private final boolean oversold;
    private final int extraTicketsSold;
    private final int proofTokensIssued;
    private final int approvalsFromInitialSnapshot;

    public SimulationResult(
            String simulationType,
            int initialTicketCount,
            int totalBuyerThreads,
            List<PurchaseAttempt> attempts,
            int finalRemainingStock,
            long durationMillis
    ) {
        this.simulationType = simulationType;
        this.initialTicketCount = initialTicketCount;
        this.totalBuyerThreads = totalBuyerThreads;
        this.attempts = List.copyOf(attempts);
        this.finalRemainingStock = finalRemainingStock;
        this.durationMillis = durationMillis;
        this.successfulPurchases = (int) attempts.stream().filter(PurchaseAttempt::isSuccess).count();
        this.failedPurchases = attempts.size() - successfulPurchases;
        this.extraTicketsSold = Math.max(0, successfulPurchases - initialTicketCount);
        this.oversold = extraTicketsSold > 0;
        this.proofTokensIssued = (int) attempts.stream().filter(PurchaseAttempt::hasProofToken).count();
        this.approvalsFromInitialSnapshot = (int) attempts.stream()
                .filter(PurchaseAttempt::isSuccess)
                .filter(attempt -> attempt.getStockObservedBeforeAttempt() == initialTicketCount)
                .count();
    }

    public String getSimulationType() {
        return simulationType;
    }

    public int getInitialTicketCount() {
        return initialTicketCount;
    }

    public int getTotalBuyerThreads() {
        return totalBuyerThreads;
    }

    public List<PurchaseAttempt> getAttempts() {
        return attempts;
    }

    public int getFinalRemainingStock() {
        return finalRemainingStock;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public int getSuccessfulPurchases() {
        return successfulPurchases;
    }

    public int getFailedPurchases() {
        return failedPurchases;
    }

    public boolean isOversold() {
        return oversold;
    }

    public int getExtraTicketsSold() {
        return extraTicketsSold;
    }

    public int getProofTokensIssued() {
        return proofTokensIssued;
    }

    public int getApprovalsFromInitialSnapshot() {
        return approvalsFromInitialSnapshot;
    }
}
