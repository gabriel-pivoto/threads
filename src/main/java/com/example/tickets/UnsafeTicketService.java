package com.example.tickets;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

public class UnsafeTicketService implements TicketService {

    private static final AtomicInteger PROOF_SEQUENCE = new AtomicInteger(1);

    private final TicketInventory inventory;
    private final long artificialDelayMillis;
    private final int minimumApprovedBuyersBeforeCommit;
    private final AtomicInteger approvedBuyers = new AtomicInteger();
    private final CountDownLatch approvalBurstLatch = new CountDownLatch(1);

    public UnsafeTicketService(TicketInventory inventory, long artificialDelayMillis, int totalBuyerThreads) {
        this.inventory = inventory;
        this.artificialDelayMillis = artificialDelayMillis;
        this.minimumApprovedBuyersBeforeCommit = Math.min(
                totalBuyerThreads,
                inventory.getInitialTickets() + 1
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
        String proofToken = createProofToken(observedStock);

        // This delay increases the chance that many threads overlap between
        // the stock check and the stock update. Because there is no mutual
        // exclusion here, several buyers can decide to sell the same ticket.
        //
        // Important: this latch does not protect the inventory. It only waits
        // until several buyers have already been approved from the same stale
        // stock snapshot, then releases them together so the oversell becomes
        // easy to demonstrate in class.
        int approvalsSoFar = approvedBuyers.incrementAndGet();
        if (approvalsSoFar >= minimumApprovedBuyersBeforeCommit) {
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
                        + " was approved in a burst of at least " + minimumApprovedBuyersBeforeCommit
                        + " buyers before the inventory update, so several threads can commit using the same stock snapshot."
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
}
