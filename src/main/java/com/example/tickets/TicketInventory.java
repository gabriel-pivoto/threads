package com.example.tickets;

public class TicketInventory {

    private final int initialTickets;
    private int remainingTickets;

    public TicketInventory(int initialTickets) {
        if (initialTickets <= 0) {
            throw new IllegalArgumentException("Initial ticket count must be greater than zero.");
        }

        this.initialTickets = initialTickets;
        this.remainingTickets = initialTickets;
    }

    public int getInitialTickets() {
        return initialTickets;
    }

    public int getRemainingTickets() {
        return remainingTickets;
    }

    public int decrementWithoutValidation() {
        remainingTickets--;
        return remainingTickets;
    }
}
