package com.example.tickets;

public class UnsafeTicketService implements TicketService {

    private final TicketInventory inventory;
    private final long artificialDelayMillis;

    public UnsafeTicketService(TicketInventory inventory, long artificialDelayMillis) {
        this.inventory = inventory;
        this.artificialDelayMillis = artificialDelayMillis;
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
                    "Thread saw no tickets left and failed immediately."
            );
        }

        // This delay increases the chance that many threads overlap between
        // the stock check and the stock update. Because there is no mutual
        // exclusion here, several buyers can decide to sell the same ticket.
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
                "Purchase succeeded without synchronization; overselling may have happened."
        );
    }

    private void pauseForDemonstration() {
        try {
            Thread.sleep(artificialDelayMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
