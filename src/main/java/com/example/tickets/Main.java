package com.example.tickets;

public class Main {

    private static final int DEFAULT_TICKET_COUNT = 10;
    private static final int DEFAULT_BUYER_COUNT = 50;
    private static final long ARTIFICIAL_DELAY_MILLIS = 50L;

    public static void main(String[] args) {
        try {
            AppConfig config = parseArguments(args);

            ConsoleReport report = new ConsoleReport();
            SimulationRunner runner = new SimulationRunner();

            report.printPresentationHeader(
                    config.ticketCount(),
                    config.buyerCount(),
                    ARTIFICIAL_DELAY_MILLIS
            );

            TicketInventory unsafeInventory = new TicketInventory(config.ticketCount());
            TicketService unsafeService = new UnsafeTicketService(
                    unsafeInventory,
                    ARTIFICIAL_DELAY_MILLIS,
                    config.buyerCount()
            );
            SimulationResult unsafeResult = runner.runSimulation(
                    "UNSAFE VERSION",
                    unsafeInventory,
                    config.buyerCount(),
                    unsafeService
            );

            TicketInventory safeInventory = new TicketInventory(config.ticketCount());
            TicketService safeService = new SafeTicketService(safeInventory, ARTIFICIAL_DELAY_MILLIS);
            SimulationResult safeResult = runner.runSimulation(
                    "SAFE VERSION",
                    safeInventory,
                    config.buyerCount(),
                    safeService
            );

            report.printSimulationReport(unsafeResult);
            report.printSimulationReport(safeResult);
            report.printComparison(unsafeResult, safeResult);
        } catch (IllegalArgumentException exception) {
            System.err.println("Invalid configuration: " + exception.getMessage());
            printUsage();
            System.exit(1);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.err.println("Simulation interrupted before completion.");
            System.exit(1);
        }
    }

    private static AppConfig parseArguments(String[] args) {
        if (args.length == 0) {
            return new AppConfig(DEFAULT_TICKET_COUNT, DEFAULT_BUYER_COUNT);
        }

        if (args.length != 2) {
            throw new IllegalArgumentException("Use zero arguments for defaults or exactly two arguments: <ticketCount> <buyerCount>.");
        }

        int ticketCount = parsePositiveInteger(args[0], "ticket count");
        int buyerCount = parsePositiveInteger(args[1], "buyer thread count");

        if (buyerCount <= ticketCount) {
            throw new IllegalArgumentException("buyer thread count must be greater than ticket count to demonstrate overselling pressure.");
        }

        return new AppConfig(ticketCount, buyerCount);
    }

    private static int parsePositiveInteger(String value, String label) {
        try {
            int parsedValue = Integer.parseInt(value);
            if (parsedValue <= 0) {
                throw new IllegalArgumentException(label + " must be greater than zero.");
            }
            return parsedValue;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a valid integer.");
        }
    }

    private static void printUsage() {
        System.err.println("Usage:");
        System.err.println("  mvn compile exec:java");
        System.err.println("  mvn compile exec:java -Dexec.args=\"<ticketCount> <buyerCount>\"");
        System.err.println("Example:");
        System.err.println("  mvn compile exec:java -Dexec.args=\"10 50\"");
    }

    private record AppConfig(int ticketCount, int buyerCount) {
    }
}
