package com.example.tickets;

public interface TicketService {

    PurchaseAttempt tryPurchase(String buyerId);
}
