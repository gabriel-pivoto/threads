package com.example.tickets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SimulationRunner {

    public SimulationResult runSimulation(
            String simulationType,
            TicketInventory inventory,
            int buyerCount,
            TicketService ticketService
    ) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(buyerCount);
        CountDownLatch readyLatch = new CountDownLatch(buyerCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<PurchaseAttempt>> futures = new ArrayList<>();

        int idWidth = String.valueOf(buyerCount).length();

        try {
            for (int buyerNumber = 1; buyerNumber <= buyerCount; buyerNumber++) {
                String buyerId = String.format("Buyer-%0" + idWidth + "d", buyerNumber);
                BuyerTask task = new BuyerTask(buyerId, ticketService, readyLatch, startLatch);
                futures.add(executorService.submit(task));
            }

            // Wait until every buyer thread is prepared, then release all of them
            // with one signal to maximize contention on the shared inventory.
            readyLatch.await();
            long startTimeNanos = System.nanoTime();
            startLatch.countDown();

            List<PurchaseAttempt> attempts = collectResults(futures);
            long durationMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTimeNanos);

            attempts.sort(Comparator.comparing(PurchaseAttempt::getBuyerId));

            return new SimulationResult(
                    simulationType,
                    inventory.getInitialTickets(),
                    buyerCount,
                    attempts,
                    inventory.getRemainingTickets(),
                    durationMillis
            );
        } finally {
            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private List<PurchaseAttempt> collectResults(List<Future<PurchaseAttempt>> futures) throws InterruptedException {
        List<PurchaseAttempt> attempts = new ArrayList<>();

        for (Future<PurchaseAttempt> future : futures) {
            try {
                attempts.add(future.get());
            } catch (ExecutionException exception) {
                Throwable cause = exception.getCause();
                attempts.add(new PurchaseAttempt(
                        "Unknown-buyer",
                        "unknown-thread",
                        false,
                        PurchaseAttempt.NOT_AVAILABLE,
                        PurchaseAttempt.NOT_AVAILABLE,
                        "Task failed with exception: " + cause.getClass().getSimpleName() + " - " + cause.getMessage()
                ));
            }
        }

        return attempts;
    }
}
