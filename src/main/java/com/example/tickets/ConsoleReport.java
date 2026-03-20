package com.example.tickets;

import java.util.Comparator;
import java.util.List;

public class ConsoleReport {

    private static final int WIDTH = 118;

    public void printPresentationHeader(int ticketCount, int buyerCount, long delayMillis) {
        printSeparator('=');
        System.out.println(center("UNSAFE TICKET CONCURRENCY DEMO"));
        printSeparator('=');
        System.out.printf("Scenario                : %d tickets, %d buyer threads%n", ticketCount, buyerCount);
        System.out.printf("Artificial delay        : %d ms between check and update%n", delayMillis);
        System.out.println("Goal                    : Show overselling caused by an unsynchronized approval flow");
        printSeparator('=');
        System.out.println();
    }

    public void printSimulationReport(SimulationResult result) {
        printSectionTitle(result.getSimulationType());
        printOverview(result);
        printIncidentSummary(result);
        printExplanation(result);
        printApprovalTable(result);
        printRejectedTable(result);
        printClosing(result);
    }

    private void printOverview(SimulationResult result) {
        printSeparator('-');
        System.out.println("OVERVIEW");
        printSeparator('-');
        System.out.printf("Initial tickets         : %d%n", result.getInitialTicketCount());
        System.out.printf("Buyer threads           : %d%n", result.getTotalBuyerThreads());
        System.out.printf("Execution time          : %d ms%n", result.getDurationMillis());
        System.out.println();
    }

    private void printIncidentSummary(SimulationResult result) {
        printSeparator('-');
        System.out.println("INCIDENT SUMMARY");
        printSeparator('-');
        System.out.printf("Signed approvals issued : %d%n", result.getProofTokensIssued());
        System.out.printf("Real ticket capacity    : %d%n", result.getInitialTicketCount());
        System.out.printf("Purchases above limit   : %d%n", result.getExtraTicketsSold());
        System.out.printf("Final remaining stock   : %d%n", result.getFinalRemainingStock());
        System.out.printf("Business rule violated  : %s%n", yesOrNo(result.isOversold()));
        System.out.println();
    }

    private void printExplanation(SimulationResult result) {
        printSeparator('-');
        System.out.println("WHAT HAPPENED");
        printSeparator('-');
        System.out.println("1. Several threads checked stock before any synchronized update existed.");
        System.out.println("2. The system issued proof tokens before decrementing the shared inventory.");
        System.out.printf("3. %d successful buyers were approved while still seeing the initial stock value of %d.%n",
                result.getApprovalsFromInitialSnapshot(),
                result.getInitialTicketCount());
        System.out.printf("4. The application finished with %d signed purchases even though only %d tickets existed.%n",
                result.getSuccessfulPurchases(),
                result.getInitialTicketCount());
        System.out.println();
    }

    private void printApprovalTable(SimulationResult result) {
        List<PurchaseAttempt> approvedAttempts = result.getAttempts().stream()
                .filter(PurchaseAttempt::isSuccess)
                .sorted(Comparator.comparingInt(PurchaseAttempt::getProofSequence))
                .toList();

        printSeparator('-');
        System.out.println("SIGNED PURCHASES");
        printSeparator('-');
        System.out.printf("%-4s %-10s %-16s %-10s %-12s %-11s%n",
                "No.",
                "Buyer",
                "Thread",
                "Saw stock",
                "Proof",
                "Stock after");
        printSeparator('-');

        int rowNumber = 1;
        for (PurchaseAttempt attempt : approvedAttempts) {
            System.out.printf("%-4d %-10s %-16s %-10s %-12s %-11s%n",
                    rowNumber++,
                    attempt.getBuyerId(),
                    attempt.getWorkerThreadName(),
                    formatStockValue(attempt.getStockObservedBeforeAttempt()),
                    attempt.getProofToken(),
                    formatStockValue(attempt.getRemainingStockAfterAttempt()));
        }

        System.out.println();
    }

    private void printRejectedTable(SimulationResult result) {
        List<PurchaseAttempt> rejectedAttempts = result.getAttempts().stream()
                .filter(attempt -> !attempt.isSuccess())
                .sorted(Comparator.comparing(PurchaseAttempt::getBuyerId))
                .toList();

        if (rejectedAttempts.isEmpty()) {
            printSeparator('-');
            System.out.println("REJECTED PURCHASES");
            printSeparator('-');
            System.out.println("None. Every buyer received approval in this run.");
            System.out.println();
            return;
        }

        printSeparator('-');
        System.out.println("REJECTED PURCHASES");
        printSeparator('-');
        System.out.printf("%-4s %-10s %-16s %-10s %-12s%n",
                "No.",
                "Buyer",
                "Thread",
                "Saw stock",
                "Proof");
        printSeparator('-');

        int rowNumber = 1;
        for (PurchaseAttempt attempt : rejectedAttempts) {
            System.out.printf("%-4d %-10s %-16s %-10s %-12s%n",
                    rowNumber++,
                    attempt.getBuyerId(),
                    attempt.getWorkerThreadName(),
                    formatStockValue(attempt.getStockObservedBeforeAttempt()),
                    attempt.getProofToken());
        }

        System.out.println();
    }

    private void printClosing(SimulationResult result) {
        printSeparator('=');
        System.out.println(center("FINAL READING"));
        printSeparator('=');
        System.out.printf("The unsafe system issued %d signed purchases for only %d real tickets.%n",
                result.getSuccessfulPurchases(),
                result.getInitialTicketCount());
        System.out.printf("Because of that, %d signed purchases exceeded the allowed stock.%n",
                result.getExtraTicketsSold());
        System.out.printf("Terminal verdict        : %s%n",
                result.isOversold() ? "OVERSOLD WITH VALID-LOOKING PROOF TOKENS" : "NO OVERSALE OBSERVED");
    }

    private void printSectionTitle(String title) {
        printSeparator('=');
        System.out.println(center(title));
        printSeparator('=');
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
