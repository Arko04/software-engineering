# Software Engineering

Coursework for **Software Engineering I** at the University of Tehran, Faculty of Electrical and Computer Engineering (Spring 2025, Dr. Fatemeh Ghassemi). The course covered domain modelling, system sequence diagrams and operation contracts, GRASP and SOLID principles, and design patterns, using Java and Spring Boot.

| # | Project | Description |
|---|---------|-------------|
| CA0 | [Java warm-up](ca0-java-warmup/) | A small domain model (actors, movies, starring periods) loaded from CSV with OpenCSV. It has an immutable **Solar Hijri `Date`** value type with validation, next-day arithmetic across month and leap-year boundaries, and working-day calculations |
| Course project | [**WealthWise**](wealthwise/) *(team)* | A **Spring Boot** portfolio-management back end for the Tehran Stock Exchange. Portfolio actions (buy, sale, deposit, withdrawal, capital raise, stock-right usage) arrive as **ActiveMQ Artemis** messages, are modelled with the **Visitor** pattern, and are folded into balance and holding services |

## CA0 – run

```bash
cd ca0-java-warmup
mvn compile exec:java -Dexec.mainClass=org.example.Main     # JDK 23
```

## WealthWise – my contributions

WealthWise was built by a team of four (original repository: [babakhm83/SE1_WealthWise](https://github.com/babakhm83/SE1_WealthWise)). My part:

- the **capital-raise** action and the **Artemis message listener** that turns incoming messages into portfolio actions
- fixes to the action-description visitor
- unit tests for the **stock-right-usage** action
