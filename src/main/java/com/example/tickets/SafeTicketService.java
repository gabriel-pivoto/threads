package com.example.tickets;

public class SafeTicketService implements TicketService {

    private final TicketInventory inventory;
    private final long artificialDelayMillis;

    public SafeTicketService(TicketInventory inventory, long artificialDelayMillis) {
        this.inventory = inventory;
        this.artificialDelayMillis = artificialDelayMillis;
    }

    @Override
    public PurchaseAttempt tryPurchase(String buyerId) {
        String workerThreadName = Thread.currentThread().getName();

        // The critical section starts here. The stock check and the stock
        // decrement now happen together under one lock, which enforces
        // mutual exclusion and prevents two buyers from selling the same ticket.
        synchronized (inventory) {
            int observedStock = inventory.getRemainingTickets();

            if (observedStock <= 0) {
                return new PurchaseAttempt(
                        buyerId,
                        workerThreadName,
                        false,
                        observedStock,
                        observedStock,
                        "Thread entered the critical section after stock reached zero."
                );
            }

            // The same artificial delay is kept here to make the comparison fair.
            // The difference is that the delay now happens inside the synchronized
            // critical section, so other threads cannot corrupt the shared stock.
            pauseForDemonstration();
            int remainingAfterAttempt = inventory.decrementWithoutValidation();

            return new PurchaseAttempt(
                    buyerId,
                    workerThreadName,
                    true,
                    observedStock,
                    remainingAfterAttempt,
                    "Purchase succeeded inside one synchronized critical section."
            );
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
