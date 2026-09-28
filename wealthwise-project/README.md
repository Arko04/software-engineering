# WealthWise

A **Spring Boot 3** back end for managing investment portfolios on the Tehran Stock Exchange. It was built as the team project for Software Engineering I at the University of Tehran (Spring 2025).

## Features

- **Portfolio actions:** buy, sale, deposit, withdrawal, capital raise, and stock-right usage, modelled as a `BaseAction` hierarchy.
- **Visitor pattern:** `ActionVisitor` / `ActionDescriptionVisitor` add behaviour, such as human-readable descriptions, without touching the action classes.
- **Message-driven:** `PortfolioMessageListener` consumes portfolio events from **ActiveMQ Artemis** (JMS).
- **Domain services:** `BalanceActionService` (cash balance from the action history), `HoldingService` (current security holdings), and `PortfolioActionService`.
- In-memory repositories for portfolios, securities, prices and actions.

## Tech

Java 21 · Spring Boot 3.4 · Spring JMS / ActiveMQ Artemis · Lombok · JUnit 5 · Maven

## Build & test

```bash
cd WealthWise
./mvnw test
```

> On JDK 23 and newer, Lombok's annotation processing must be enabled explicitly:
> `./mvnw test -Dmaven.compiler.proc=full`

Every test class passes when run on its own. When the whole suite runs in one JVM, some balance and holding tests fail, because the shared Spring context keeps in-memory repository state between test classes. Adding `@DirtiesContext` to those test classes, or resetting the repositories in `@BeforeEach`, would isolate them.

## Team

Babak Hosseini M., Mohammad Taha Majlesi, Mostafa Kermaninia and **Alireza Karimi**. The project started from a template provided by the course.
