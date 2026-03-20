package com.example.tickets;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class BuyerTask implements Callable<PurchaseAttempt> {

    private final String buyerId;
    private final TicketService ticketService;
    private final CountDownLatch readyLatch;
    private final CountDownLatch startLatch;

    public BuyerTask(
            String buyerId,
            TicketService ticketService,
            CountDownLatch readyLatch,
            CountDownLatch startLatch
    ) {
        this.buyerId = buyerId;
        this.ticketService = ticketService;
        this.readyLatch = readyLatch;
        this.startLatch = startLatch;
    }

    @Override
    public PurchaseAttempt call() {
        String workerThreadName = Thread.currentThread().getName();

        // Each buyer announces that it is ready, then waits for the common
        // start signal so the purchase attempts happen almost simultaneously.
        readyLatch.countDown();

        try {
            startLatch.await();
            return ticketService.tryPurchase(buyerId);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new PurchaseAttempt(
                    buyerId,
                    workerThreadName,
                    false,
                    PurchaseAttempt.NOT_AVAILABLE,
                    PurchaseAttempt.NOT_AVAILABLE,
                    "Thread was interrupted before it could attempt the purchase."
            );
        }
    }
}
