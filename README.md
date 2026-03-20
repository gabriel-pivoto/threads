# Unsafe Ticket Concurrency Demo

This project is a didactic Java console application for an Operating Systems / concurrency assignment.

It demonstrates one specific failure:

- a ticket-selling system with limited stock
- many buyers trying to buy at the same time
- no synchronization between stock validation and stock update
- signed approvals being issued even when the system sells beyond the real limit

## Objective

The goal is to show a race condition clearly in the terminal.

With the default scenario:

- `10` tickets
- `20` buyer threads

the application intentionally oversells tickets and prints proof tokens for purchases that should never have been approved.

## What the demo proves

The unsafe flow is:

1. a thread checks whether stock is available
2. it receives a proof token that looks valid
3. it waits before updating the shared stock
4. other threads do the same thing in parallel
5. multiple approved purchases commit from the same stale stock snapshot

That creates:

- race condition
- critical section violation
- lack of mutual exclusion
- overselling
- inconsistent shared state

## Project structure

```text
src/main/java/com/example/tickets
|-- BuyerTask.java
|-- ConsoleReport.java
|-- Main.java
|-- PurchaseAttempt.java
|-- SafeTicketService.java
|-- SimulationResult.java
|-- SimulationRunner.java
|-- TicketInventory.java
|-- TicketService.java
`-- UnsafeTicketService.java
```

Note: `SafeTicketService.java` remains in the project as reference code, but the current presentation flow runs only the unsafe simulation.

## Run in VSCode

Use the `Run Unsafe Ticket Demo` launch configuration or run:

```powershell
.\mvnw.cmd -q compile exec:java
```

## Run with custom values

```powershell
.\mvnw.cmd -q compile exec:java '-Dexec.args=10 20'
```

Format:

- first argument: ticket count
- second argument: buyer thread count

The buyer thread count must be greater than the ticket count.

## How to read the output

The terminal output is split into:

- `OVERVIEW`: scenario and execution time
- `INCIDENT SUMMARY`: how many approvals were issued and how many exceeded the stock
- `WHAT HAPPENED`: plain-language explanation of the race condition
- `SIGNED PURCHASES`: each approved buyer with thread name, observed stock, token, and stock after commit
- `REJECTED PURCHASES`: buyers that did not receive proof tokens
- `FINAL READING`: one short conclusion for presentation

## Default expected result

For `10` tickets and `20` buyers, the expected behavior is:

- more than `10` buyers receive signed approval
- final stock becomes negative
- the system finishes without crashing
- the business rule fails even though the program continues running
