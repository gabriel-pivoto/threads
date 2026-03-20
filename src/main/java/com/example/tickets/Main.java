package com.example.tickets;

import java.util.Scanner;

public class Main {

    private static final long ARTIFICIAL_DELAY_MILLIS = 50L;

    // Application State
    private static TicketInventory inventory = new TicketInventory(10);
    private static boolean isSafeMode = false;
    private static int nextBuyerId = 1;

    private static final Scanner scanner = new Scanner(System.in);
    private static final ConsoleReport report = new ConsoleReport();
    private static final SimulationRunner runner = new SimulationRunner();

    public static void main(String[] args) {
        System.out.println("Starting Interactive Ticket Concurrency Demo...");

        while (true) {
            printHeader();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> triggerBuyers(1);
                    case "2" -> {
                        System.out.print("How many simultaneous buyers? ");
                        int count = Integer.parseInt(scanner.nextLine().trim());
                        triggerBuyers(count);
                    }
                    case "3" -> {
                        isSafeMode = !isSafeMode;
                        System.out.println("\n> Mode switched to: " + (isSafeMode ? "SAFE (Synchronized)" : "UNSAFE (Race Condition)"));
                        pause();
                    }
                    case "4" -> {
                        System.out.print("Enter new inventory capacity: ");
                        int capacity = Integer.parseInt(scanner.nextLine().trim());
                        if (capacity <= 0) {
                            System.out.println("Capacity must be greater than zero.");
                        } else {
                            inventory = new TicketInventory(capacity);
                            nextBuyerId = 1; // reset buyer count
                            System.out.println("\n> Inventory reset to " + capacity + " tickets.");
                        }
                        pause();
                    }
                    case "5" -> {
                        System.out.println("\nExiting. Goodbye!");
                        return;
                    }
                    default -> {
                        System.out.println("\nInvalid option. Please try again.");
                        pause();
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("\nError: Please enter a valid number.");
                pause();
            } catch (Exception e) {
                System.out.println("\nError: " + e.getMessage());
                pause();
            }
        }
    }

    private static void triggerBuyers(int count) throws InterruptedException {
        if (count <= 0) {
            System.out.println("\nBuyer count must be greater than 0.");
            pause();
            return;
        }

        System.out.println("\n> Processing " + count + " buyers simultaneously. Please wait...");

        TicketService service;
        if (isSafeMode) {
            service = new SafeTicketService(inventory, ARTIFICIAL_DELAY_MILLIS);
        } else {
            // Unsafe service is recreated per batch to handle its internal burst latches correctly
            service = new UnsafeTicketService(inventory, ARTIFICIAL_DELAY_MILLIS, count);
        }

        String type = isSafeMode ? "SAFE BATCH" : "UNSAFE BATCH";

        // Process the batch and increment the global buyer ID counter
        SimulationResult result = runner.runSimulation(type, inventory, count, nextBuyerId, service);
        nextBuyerId += count;

        report.printBatchResult(result);
        pause();
    }

    private static void printHeader() {
        System.out.println("\n==================================================");
        System.out.println("      INTERACTIVE TICKET CONCURRENCY DEMO");
        System.out.println("==================================================");
        System.out.printf(" Tickets Remaining : %d / %d%n", inventory.getRemainingTickets(), inventory.getInitialTickets());
        System.out.printf(" Active Mode       : %s%n", isSafeMode ? "SAFE (Synchronized)" : "UNSAFE (Race Condition)");
        System.out.println("==================================================");
        System.out.println(" [1] Trigger 1 buyer");
        System.out.println(" [2] Trigger N simultaneous buyers");
        System.out.println(" [3] Switch Mode (Safe / Unsafe)");
        System.out.println(" [4] Reset Inventory");
        System.out.println(" [5] Exit");
        System.out.println("--------------------------------------------------");
        System.out.print(" Choose an option: ");
    }

    private static void pause() {
        System.out.print("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}