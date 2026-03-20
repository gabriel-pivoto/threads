# Ticket Concurrency Demo

## 1. Project title

**Ticket Concurrency Demo**

This project was designed for an Operating Systems / concurrency assignment. It demonstrates how concurrent threads can corrupt shared state in a ticket-selling system and how synchronization fixes the problem.

## 2. Objective

The objective is to simulate many buyers trying to purchase a small number of tickets at the same time. The application runs two versions of the same scenario:

- an unsafe version, which contains a race condition and can oversell tickets
- a safe version, which protects the critical section and prevents overselling

## 3. Problem being demonstrated

The system starts with a limited ticket stock and more buyer threads than available tickets. Because many threads run concurrently, the shared inventory becomes a classic concurrency problem.

With the default scenario:

- available tickets: `10`
- buyer threads: `50`

The unsafe simulation can report more successful purchases than the initial stock, which means the system sold tickets that did not exist.

## 4. What is a race condition

A race condition happens when multiple threads access and modify shared data at the same time without proper coordination. The final result depends on the timing of execution instead of correct program logic.

In this project, the shared data is the ticket inventory.

## 5. Why overselling happens in the unsafe version

The unsafe service intentionally makes a mistake:

1. it checks whether stock is greater than zero
2. it pauses briefly with `Thread.sleep(...)`
3. it decrements the stock later, without synchronization

Because the check and the update are separated, many threads can see the same ticket as still available and all decide to sell it. The artificial delay increases the chance that several threads overlap exactly in this vulnerable window.

## 6. How synchronization fixes the problem

The safe service places the stock check and the stock decrement inside one synchronized critical section. That means only one thread at a time can execute the purchase logic.

This guarantees:

- mutual exclusion
- consistent updates to shared data
- no overselling

## 7. Project structure

```text
.
|-- .vscode
|   |-- launch.json
|   `-- settings.json
|-- pom.xml
|-- README.md
`-- src
    `-- main
        `-- java
            `-- com
                `-- example
                    `-- tickets
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

## 8. Technologies used

- Java 17
- Maven
- Java standard concurrency utilities:
  - `ExecutorService`
  - `CountDownLatch`
  - `Future`
- Console output only

## 9. How to run in VSCode

1. Open the project folder in VSCode.
2. Make sure Java 17 and Maven are installed.
3. Open [Main.java](./src/main/java/com/example/tickets/Main.java).
4. Use the Run button above `main`, or start the `Run Ticket Concurrency Demo` launch configuration.

You can also change the arguments in `.vscode/launch.json` if you want a different scenario.

## 10. How to run with Maven in terminal

Run the default scenario:

```bash
mvn compile exec:java
```

Run with custom values:

```bash
mvn compile exec:java -Dexec.args="10 50"
```

Argument format:

- first argument: ticket count
- second argument: buyer thread count

Important: the buyer thread count should be greater than the ticket count.

## 11. Example output explanation

The console output is divided into three parts:

1. an introduction showing the scenario configuration
2. a full report for the unsafe simulation
3. a full report for the safe simulation, followed by a final comparison

For each simulation, the report shows:

- simulation type
- initial stock
- number of buyer threads
- per-thread purchase result
- final remaining stock
- successful and failed purchases
- whether overselling happened
- how many extra tickets were sold

In the unsafe version, you should see more successful purchases than the original number of tickets. In the safe version, successful purchases must never exceed the initial stock.

## 12. Key Operating Systems concepts demonstrated

This project demonstrates the following Operating Systems and concurrency concepts:

- threads
- concurrent execution
- shared resources
- race condition
- critical section
- thread synchronization
- mutual exclusion
- data inconsistency
- overselling caused by unsynchronized access
