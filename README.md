# Robotic Vacuum Cleaner Simulation - GurionRock

A **multithreaded SLAM simulation** built in Java, modeling a robotic vacuum cleaner that performs **environmental mapping and object detection** using virtual sensors (LiDAR, Camera, Pose).  
Each sensor is implemented as a **Microservice** running asynchronously, communicating via an **event-driven Message Bus** to a central **Fusion SLAM Service**.

---

## Project Structure

```
.
├── pom.xml
├── configuration_file.json
├── src/
│   ├── main/java/bgu/spl/mics/...      # Core services, messaging & simulation logic
│   └── test/java/bgu/spl/mics/...      # Unit tests (JUnit 5)
└── README.md
```

---

## Key Features

- Multithreaded Microservices - each sensor runs independently with configurable frequency.
- Event-driven Communication - implemented using a custom Message Bus (pub-sub pattern).
- Simulated Sensors - LiDAR, Camera and Pose modules load real-time data from JSON input.
- Fusion SLAM Engine - merges data from all sensors to build a dynamic environmental map (landmarks).
- Statistical Monitoring - runtime stats and detection metrics collected into a final output JSON.
- Testable & Modular - written with separation of concerns and tested using JUnit 5.

---

## Technologies Used

- Java 17
- Maven
- JUnit 5
- ConcurrentHashMap, CountDownLatch, Multithreading

---

## Runtime Flow Overview

1. **Input**: Load configuration and data from `configuration_file.json`, including:
   - Camera detections (`camera_data.json`)
   - LiDAR detections (`lidar_data.json`)
   - Robot poses over time (`pose_data.json`)

2. **Initialization**: Services are launched as threads:
   - CameraService, LiDarService, PoseService, FusionSlamService, and TimeService

3. **Messaging**:
   - Each service publishes/subscribes to Events and Broadcasts using a shared MessageBusImpl.
   - Data is transferred asynchronously to FusionSlamService.

4. **Fusion Logic**:
   - Detected objects are combined with position data to create or update Landmarks.
   - Duplicates are merged using spatial proximity and timestamp logic.

5. **Output**:
   - After a set simulation duration, output is saved to `output_file.json`:
     - System runtime
     - Number of objects detected/tracked
     - List of final landmarks and their coordinates

---

## Building and Running

### Build:
```bash
mvn clean install
```

### Run:
```bash
mvn exec:java -Dexec.mainClass="bgu.spl.mics.application.GurionRockRunner" -Dexec.args="configuration_file.json"
```

---

## Extensibility Ideas

- Offload local memory to cloud storage for shared access and scalable analysis.
- Add AI Agent for predictive tracking.
- REST API to expose real-time output.
- Add confidence scores to landmarks using classification models (e.g., CNN).
- Integrate with Grafana and Prometheus for real-time metrics visualization.
- Dynamic Tick scheduling based on system load (adaptive TimeService).
- Real-time decision agent for robotic path planning based on live data.

---
