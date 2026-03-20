package com.example.tickets;

import java.util.Comparator;
import java.util.List;

public class ConsoleReport {

    public void printBatchResult(SimulationResult result) {
        System.out.println("\n" + "-".repeat(65));
        System.out.println(" RESULT OF THE OPERATION: " + result.getSimulationType());
        System.out.println("-".repeat(65));
        System.out.printf(" Buyers triggered         : %d%n", result.getTotalBuyerThreads());
        System.out.printf(" Successful purchases     : %d%n", result.getSuccessfulPurchases());
        System.out.printf(" Failed/Rejected attempts : %d%n", result.getFailedPurchases());

        if (result.isOversold()) {
            System.out.printf(" OVERSOLD IN THIS BATCH   : %d tickets!%n", result.getExtraTicketsSold());
        }

        System.out.printf(" Current remaining stock  : %d%n", result.getFinalRemainingStock());

        System.out.println("-".repeat(65));
        System.out.println(" DETAILED BUYER ACTIVITY");
        System.out.println("-".repeat(65));
        System.out.printf("%-10s %-16s %-10s %-12s %-11s%n", "Buyer", "Thread", "Saw stock", "Proof Token", "Stock after");
        System.out.println("-".repeat(65));

        List<PurchaseAttempt> sortedAttempts = result.getAttempts().stream()
                .sorted(Comparator.comparing(PurchaseAttempt::getBuyerId))
                .toList();

        for (PurchaseAttempt attempt : sortedAttempts) {
            System.out.printf("%-10s %-16s %-10s %-12s %-11s%n",
                    attempt.getBuyerId(),
                    attempt.getWorkerThreadName(),
                    formatStockValue(attempt.getStockObservedBeforeAttempt()),
                    attempt.getProofToken(),
                    formatStockValue(attempt.getRemainingStockAfterAttempt()));
        }
        System.out.println("-".repeat(65));

        // Didactic explanation based on what just happened
        if (result.isOversold()) {
            System.out.println(" >> DIDACTIC NOTE: Notice how multiple buyers 'Saw stock' at");
            System.out.println("    the exact same positive value. Because the check and the");
            System.out.println("    update are not synchronized, they all generated valid");
            System.out.println("    proof tokens, pushing the stock below zero.");
        } else if (!result.getSimulationType().contains("SAFE") && result.getTotalBuyerThreads() > 1 && result.getFinalRemainingStock() > 0) {
            System.out.println(" >> DIDACTIC NOTE: This was UNSAFE mode, but there was enough");
            System.out.println("    stock to fulfill all requests without dipping below zero.");
            System.out.println("    Try triggering more buyers than the available stock!");
        } else if (result.getSimulationType().contains("SAFE")) {
            System.out.println(" >> DIDACTIC NOTE: Notice how 'Stock after' perfectly decrements.");
            System.out.println("    The synchronized block guarantees only one thread validates");
            System.out.println("    and updates the stock at a time, preventing overselling.");
        }
        System.out.println("-".repeat(65));
    }

    private String formatStockValue(int value) {
        return value == PurchaseAttempt.NOT_AVAILABLE ? "n/a" : Integer.toString(value);
    }
}