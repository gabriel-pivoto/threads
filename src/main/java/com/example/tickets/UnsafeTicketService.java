package com.example.tickets;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

public class UnsafeTicketService implements TicketService {

    private static final AtomicInteger PROOF_SEQUENCE = new AtomicInteger(1);

    private final TicketInventory inventory;
    private final long artificialDelayMillis;
    private final int firstApprovalWaveSize;
    private final AtomicInteger approvalReservations = new AtomicInteger();
    private final CountDownLatch approvalBurstLatch = new CountDownLatch(1);

    public UnsafeTicketService(TicketInventory inventory, long artificialDelayMillis, int totalBuyerThreads) {
        this.inventory = inventory;
        this.artificialDelayMillis = artificialDelayMillis;
        this.firstApprovalWaveSize = Math.min(
                totalBuyerThreads,
                inventory.getInitialTickets() + Math.max(2, inventory.getInitialTickets() / 4)
        );
    }

    @Override
    public PurchaseAttempt tryPurchase(String buyerId) {
        String workerThreadName = Thread.currentThread().getName();

        // This first read is the source of the race condition.
        // Multiple threads can observe the same positive stock at the same time.
        int observedStock = inventory.getRemainingTickets();

        if (observedStock <= 0) {
            return new PurchaseAttempt(
                    buyerId,
                    workerThreadName,
                    false,
                    observedStock,
                    inventory.getRemainingTickets(),
                    PurchaseAttempt.NO_PROOF_TOKEN,
                    "Thread saw no tickets left and failed immediately."
            );
        }

        // The system already tells the buyer that the purchase looks valid.
        // This token acts like a receipt or approval signature, even though
        // the stock update has not happened yet.
        int reservationNumber = approvalReservations.incrementAndGet();

        if (reservationNumber <= firstApprovalWaveSize) {
            String proofToken = createProofToken(observedStock);
            return commitInFirstWave(
                    buyerId,
                    workerThreadName,
                    observedStock,
                    proofToken,
                    reservationNumber
            );
        }

        return handleLateArrival(buyerId, workerThreadName);
    }

    private PurchaseAttempt commitInFirstWave(
            String buyerId,
            String workerThreadName,
            int observedStock,
            String proofToken,
            int reservationNumber
    ) {
        // This delay increases the chance that many threads overlap between
        // the stock check and the stock update. Because there is no mutual
        // exclusion here, several buyers can decide to sell the same ticket.
        //
        // Important: this latch does not protect the inventory. It only waits
        // until the first approval wave is full, then releases that wave
        // together. This still keeps the code unsafe, but avoids approving
        // literally every thread in the simulation.
        if (reservationNumber == firstApprovalWaveSize) {
            approvalBurstLatch.countDown();
        }

        waitForApprovalBurst();
        pauseForDemonstration();

        // This update happens later and without synchronization, so the
        // critical section is broken into unsafe pieces.
        int remainingAfterAttempt = inventory.decrementWithoutValidation();

        return new PurchaseAttempt(
                buyerId,
                workerThreadName,
                true,
                observedStock,
                remainingAfterAttempt,
                proofToken,
                "The buyer received proof " + proofToken
                        + " from an unsynchronized stock check. Buyer " + buyerId
                        + " joined the first approval wave of " + firstApprovalWaveSize
                        + " buyers before the inventory update, so several threads committed from the same stock snapshot."
        );
    }

    private PurchaseAttempt handleLateArrival(String buyerId, String workerThreadName) {
        pauseForLateArrival();

        int refreshedStock = inventory.getRemainingTickets();
        if (refreshedStock <= 0) {
            return new PurchaseAttempt(
                    buyerId,
                    workerThreadName,
                    false,
                    refreshedStock,
                    refreshedStock,
                    PurchaseAttempt.NO_PROOF_TOKEN,
                    "This buyer reached the unsafe flow after the first approval wave had already consumed the available stock."
            );
        }

        String proofToken = createProofToken(refreshedStock);
        pauseForDemonstration();
        int remainingAfterAttempt = inventory.decrementWithoutValidation();

        return new PurchaseAttempt(
                buyerId,
                workerThreadName,
                true,
                refreshedStock,
                remainingAfterAttempt,
                proofToken,
                "The buyer arrived after the first approval wave but still managed to receive proof "
                        + proofToken + " from another stale stock read."
        );
    }

    private String createProofToken(int observedStock) {
        return "U-" + observedStock + "-" + String.format("%04d", PROOF_SEQUENCE.getAndIncrement());
    }

    private void waitForApprovalBurst() {
        try {
            approvalBurstLatch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void pauseForDemonstration() {
        try {
            Thread.sleep(artificialDelayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void pauseForLateArrival() {
        try {
            Thread.sleep(artificialDelayMillis * 2);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
