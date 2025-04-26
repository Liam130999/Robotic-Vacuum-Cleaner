name: Robotic Vacuum Cleaner Simulation

description: >
  A multithreaded robotic vacuum cleaner simulation using Java Microservices and event-driven architecture.

features:
  - Microservices-based multithreading model
  - Asynchronous event-driven programming
  - Message-passing between services
  - Unit testing with JUnit

technologies:
  - Java 17+
  - Maven
  - JUnit 5

structure: |
  .
  ├── pom.xml
  ├── src/
  │   ├── main/java/bgu/spl/mics/...
  │   └── test/java/bgu/spl/mics/...
  └── README.md

build: 
  mvn clean install

run: 
  mvn exec:java -Dexec.mainClass="bgu.spl.mics.application.GurionRockRunner"

notes:
  - Built around event-driven, message-passing concurrency design.
  - Modular Microservices architecture.
