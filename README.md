# Emergency Response Management System

A Java-based emergency response simulation that records emergency details, analyzes priority, assigns the appropriate response unit, and tracks handled incidents.

## Overview

This project models a multi-stage emergency workflow:

1. Record emergency details from user input
2. Analyze the incident severity and priority
3. Assign the required response unit
4. Simulate a response lifecycle and release the unit after completion

It demonstrates Java concurrency, object modeling, and a simple pipeline architecture.

## Features

- Collect emergency information such as:
  - affected person name
  - description
  - location
  - emergency type
- Validate required fields before continuing
- Determine priority based on incident type and keywords
- Assign a suitable response unit based on emergency category
- Simulate real-time response handling using threads
- Review handled incidents through the service API

## Project Structure

```text
src/
├── main/
│   └── java/
│       └── org/example/
│           ├── App.java
│           ├── Model/
│           │   ├── EmergencyDetails.java
│           │   ├── EmergencyPriority.java
│           │   ├── EmergencyStatus.java
│           │   ├── EmergencyType.java
│           │   ├── ResponseUnit.java
│           │   └── UnitType.java
│           ├── Service/
│           │   └── EmergencyService.java
│           └── Service/impl/
│               └── EmergencyServiceImpl.java
└── test/
    └── java/
        └── org/example/EmergencyServiceTest/
            └── EmergencyServiceImplTest.java
```

## Emergency Types

- MEDICAL -> Ambulance
- FIRE -> Fire Truck
- SECURITY -> Police Unit
- NATURAL_DISASTER -> Disaster Response Team
- ROAD_ACCIDENT -> Ambulance

## Priority Logic

Priority is assigned based on emergency type and description keywords.

Example keywords that escalate priority:
- unconscious
- trapped
- bleeding
- explosion

Priority levels include:
- CRITICAL
- HIGH
- MEDIUM
- LOW

## How to Run

### Prerequisites

- Java JDK 17+
- Maven

### Build and run

```bash
mvn compile
mvn exec:java -Dexec.mainClass=org.example.App
```

Or run the application class directly from your IDE.

## Running Tests

```bash
mvn test
```

## Example Flow

The app accepts input such as:

```text
Name of affected person (or 'done' to stop): Ravi
Description: Person unconscious
Location: Shahdara
Type: 1-MEDICAL 2-FIRE 3-SECURITY 4-NATURAL_DISASTER 5-ROAD_ACCIDENT
```

The system then:
- validates all fields
- determines the priority
- assigns the appropriate unit
- simulates unit release when the response completes

## Notes

- The service uses a synchronized pipeline to coordinate recording, analysis, and assignment.
- Response units are simulated asynchronously to model real-world emergency dispatch timing.

## License

This project is provided as an educational Java project for emergency workflow simulation.
