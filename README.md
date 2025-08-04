# Sensor Mapping System

A multi-threaded microservice framework simulating a real-time sensor fusion platform.  
Integrates Camera, LiDAR, GPS, and IMU data to perform SLAM (Simultaneous Localization and Mapping), object recognition, and environment reconstruction.  
Developed in Java as part of the Systems Programming Lab (SPL) course at Ben-Gurion University.

---

## 🧠 Overview

The system models a distributed sensor environment where autonomous clients equipped with multiple sensors transmit data to backend services. These services fuse information asynchronously to reconstruct a shared spatial model of the world and identify obstacles in real time.

---

## 🚀 Key Features

- 🛰️ **Sensor Integration:** Simulates clients with Camera, LiDAR, GPS & IMU devices producing structured data.
- 🧠 **SLAM Pipeline:** Combines localization and mapping services to build a consistent model of the environment.
- 🧵 **Multithreading & Concurrency:** Thread-safe message bus and services coordination via pub-sub architecture.
- 🧩 **Microservices Architecture:** Modular components including SensorService, LocalizationService, MappingService, ObjectRecognitionService, and more.
- ⏱️ **Global Tick-Based Simulation:** TimeService controls system-wide tick progression and synchronization.
- 📦 **Structured JSON Input:** Defines clients, sensors, services, and initial configurations.
- 📊 **Output Generation:** Logs and JSON files describing environment perception and recognized objects.

---

## 📁 Project Structure

