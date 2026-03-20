package com.example.tickets;

public class ConsoleReport {

    private static final int WIDTH = 108;

    public void printPresentationHeader(int ticketCount, int buyerCount, long delayMillis) {
        printSeparator('=');
        System.out.println(center("THREAD CONCURRENCY IN A TICKET-SELLING SYSTEM"));
        printSeparator('=');
        System.out.printf("Scenario                : %d tickets, %d buyer threads%n", ticketCount, buyerCount);
        System.out.printf("Artificial delay        : %d ms between check and update%n", delayMillis);
        System.out.println("Goal                    : Compare an unsafe race condition with a synchronized solution");
        printSeparator('=');
        System.out.println();
    }

    public void printSimulationReport(SimulationResult result) {
        printSeparator('-');
        System.out.println(center(result.getSimulationType()));
        printSeparator('-');
        System.out.printf("Initial tickets         : %d%n", result.getInitialTicketCount());
        System.out.printf("Buyer threads           : %d%n", result.getTotalBuyerThreads());
        System.out.printf("Execution time          : %d ms%n", result.getDurationMillis());
        printSeparator('-');
        System.out.println("Per-thread results");
        printSeparator('-');
        System.out.printf("%-10s | %-16s | %-7s | %-14s | %-16s | %s%n",
                "Buyer",
                "Worker thread",
                "Result",
                "Observed stock",
                "Remaining stock",
                "Explanation");
        printSeparator('-');

        for (PurchaseAttempt attempt : result.getAttempts()) {
            System.out.printf("%-10s | %-16s | %-7s | %-14s | %-16s | %s%n",
                    attempt.getBuyerId(),
                    attempt.getWorkerThreadName(),
                    attempt.isSuccess() ? "SUCCESS" : "FAILURE",
                    formatStockValue(attempt.getStockObservedBeforeAttempt()),
                    formatStockValue(attempt.getRemainingStockAfterAttempt()),
                    attempt.getDetail());
        }

        printSeparator('-');
        System.out.println("Summary");
        printSeparator('-');
        System.out.printf("Final remaining stock   : %d%n", result.getFinalRemainingStock());
        System.out.printf("Successful purchases    : %d%n", result.getSuccessfulPurchases());
        System.out.printf("Failed purchases        : %d%n", result.getFailedPurchases());
        System.out.printf("Overselling happened    : %s%n", yesOrNo(result.isOversold()));
        System.out.printf("Extra tickets sold      : %d%n", result.getExtraTicketsSold());
        System.out.println();
    }

    public void printComparison(SimulationResult unsafeResult, SimulationResult safeResult) {
        printSeparator('=');
        System.out.println(center("FINAL COMPARISON"));
        printSeparator('=');
        System.out.printf("Unsafe successful purchases : %d%n", unsafeResult.getSuccessfulPurchases());
        System.out.printf("Safe successful purchases   : %d%n", safeResult.getSuccessfulPurchases());
        System.out.printf("Unsafe remaining stock      : %d%n", unsafeResult.getFinalRemainingStock());
        System.out.printf("Safe remaining stock        : %d%n", safeResult.getFinalRemainingStock());
        System.out.printf("Overselling in unsafe?      : %s%n", yesOrNo(unsafeResult.isOversold()));
        System.out.printf("Overselling in safe?        : %s%n", yesOrNo(safeResult.isOversold()));
        printSeparator('=');
        System.out.println("Conclusion");
        printSeparator('=');
        System.out.println(buildConclusion(unsafeResult, safeResult));
    }

    private String buildConclusion(SimulationResult unsafeResult, SimulationResult safeResult) {
        if (unsafeResult.isOversold() && !safeResult.isOversold()) {
            return "The unsafe version oversold tickets because the stock check and stock update were not atomic. "
                    + "The safe version kept both operations inside one synchronized critical section, so mutual "
                    + "exclusion prevented data inconsistency.";
        }

        return "The comparison completed, but the expected contrast was not fully observed. "
                + "Try rerunning the default scenario to increase contention.";
    }

    private String center(String text) {
        if (text.length() >= WIDTH) {
            return text;
        }

        int totalPadding = WIDTH - text.length();
        int leftPadding = totalPadding / 2;
        return " ".repeat(leftPadding) + text;
    }

    private void printSeparator(char character) {
        System.out.println(String.valueOf(character).repeat(WIDTH));
    }

    private String formatStockValue(int value) {
        return value == PurchaseAttempt.NOT_AVAILABLE ? "n/a" : Integer.toString(value);
    }

    private String yesOrNo(boolean value) {
        return value ? "YES" : "NO";
    }
}
