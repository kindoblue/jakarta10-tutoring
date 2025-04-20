# Office Management System

![Build Status](https://github.com/kindoblue/java-tutoring/actions/workflows/build.yml/badge.svg)
![Java Version](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Jakarta EE Version](https://img.shields.io/badge/Jakarta%20EE-10-blue?logo=jakartaee)
![Application Server](https://img.shields.io/badge/JBoss%20EAP-8-red)
![Last Commit](https://img.shields.io/github/last-commit/kindoblue/java-tutoring)

A Java-based office management system built with Jakarta EE 10 and running on JBoss EAP 8. It provides a RESTful API for managing office spaces (floors, rooms, seats), employees, seat assignments (many-to-many), and viewing SVG floor plans.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Quick Start (Dev Container)](#quick-start-dev-container)
- [Development Environment](#development-environment)
- [Database Setup](#database-setup)
- [Building and Running](#building-and-running)
- [API Documentation](#api-documentation)
- [Key Code Packages](#key-code-packages)
- [Testing](#testing)
- [Contributing](#contributing)
- [Educational Purpose](#educational-purpose)

## Overview

This system demonstrates a modern Java web application using Jakarta EE 10 standards deployed on JBoss EAP 8. It manages a hierarchical office structure, tracks employees, and handles flexible many-to-many seat assignments. The application includes features like searching, pagination, statistics, and SVG floor plan visualization.

## Features

*   **Employee Management**: CRUD operations, search by name/occupation, paginated results.
*   **Office Space Management**: Manage Floors, Rooms, and Seats with a clear hierarchy.
*   **Seat Assignment**: Many-to-many relationship between Employees and Seats. Assign/unassign seats.
*   **Floor Planimetry**: Upload, store, and retrieve SVG floor plans for visualization.
*   **Geometry Updates**: Partially update room and seat positions/dimensions via PATCH requests.
*   **Statistics**: API endpoint to get basic office statistics (counts of entities).
*   **RESTful API**: Well-defined endpoints using JAX-RS.
*   **Data Persistence**: Uses JPA/Hibernate with PostgreSQL.
*   **Containerized Development**: Consistent environment using VS Code Dev Containers and Docker.

## Technology Stack

*   **Java**: Version 21
*   **Jakarta EE**: Version 10 (JAX-RS, JPA, CDI, etc.)
*   **Application Server**: JBoss EAP 8
*   **ORM**: Hibernate (via Jakarta Persistence API)
*   **Database**: PostgreSQL 15
*   **Build Tool**: Maven
*   **API Framework**: JAX-RS (provided by JBoss EAP)
*   **JSON Processing**: Jackson (provided by JBoss EAP)
*   **Development Environment**: Docker, VS Code Dev Containers
*   **Testing**: JUnit 5, Arquillian, REST Assured
*   **Code Quality**: Lombok (for reducing boilerplate), Spotless (for formatting)

## Prerequisites

*   [Visual Studio Code](https://code.visualstudio.com/download)
*   [Docker Desktop](https://www.docker.com/products/docker-desktop/) (or Docker Engine + Docker Compose)
*   VSCode Extension: [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) (ID: `ms-vscode-remote.remote-containers`)

## Quick Start (Dev Container)

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd java-tutoring
    ```
2.  **Open in VS Code:**
    ```bash
    code .
    ```
3.  **Reopen in Container:** VS Code will detect the `.devcontainer` configuration. When prompted ("Folder contains a Dev Container configuration file..."), click **"Reopen in Container"**.

VS Code will build the Docker image (using the JBoss EAP 8 base image and installing necessary tools), start the application (`app`) and database (`db`) containers defined in `docker-compose.yml`, initialize the database schema, load example floor plans, and connect your VS Code instance to the running development container.

## Development Environment

The Dev Container setup provides:
*   **`app` Service:** Runs JBoss EAP 8 in a container based on the provided `Dockerfile`. Includes Java 21, Maven, PostgreSQL client, and necessary JBoss configuration. Your project workspace is mounted into `/workspaces/tutoring`.
*   **`db` Service:** Runs a PostgreSQL 15 container. Data is persisted in a Docker volume (`tutoring-postgres-data`).
*   **Automatic Setup:** The `postCreateCommand` in `devcontainer.json` executes `.devcontainer/load_example_data.sh`, which waits for the database, runs `schema.sql`, and loads example SVG floor plans using `load_floor_plan.sh`.

## Database Setup

*   **Type:** PostgreSQL 15
*   **Container Name:** `db` (within the Docker Compose network)
*   **Database Name:** `office_management`
*   **User:** `postgres`
*   **Password:** `postgres`
*   **Host (from `app` container):** `db`
*   **Port:** `5432`

The schema is automatically created and sample data is inserted when the Dev Container starts, as defined in `.devcontainer/schema.sql`.

## Building and Running

All commands should be run *inside the Dev Container terminal* (VS Code: Terminal > New Terminal).

1.  **Build the Project:**
    ```bash
    mvn package
    ```
    This compiles the code, runs tests (against an in-memory H2 database defined in `src/test/resources/META-INF/persistence.xml`), and packages the application into `target/office-management-system.war`.

2.  **Run the Application Server:**
    ```bash
    ./run_server.sh
    ```
    This script builds the WAR, copies it to the JBoss deployment directory (as `ROOT.war`), and starts the server.

The application will be available at `http://localhost:8080`. The JBoss Management 

## API Documentation

This project includes interactive API documentation using Swagger UI. Once you start the server, Swagger UI is automatically deployed and can be accessed in your browser at:

[http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)

Use this interface to explore and test the available REST API endpoints.

The RESTful API is defined using JAX-RS under the base path `/api`. Data Transfer Objects (DTOs) are generally used for request/response bodies.

For detailed endpoint descriptions, request/response examples, and to try out the API, refer to the `docs/api-tests.http` file. You can use the [REST Client](https://marketplace.visualstudio.com/items?itemName=humao.rest-client) VS Code extension with this file.

**Key API Resources:**
*   `/floors`: Manage floors and their SVG planimetry.
*   `/rooms`: Manage office rooms within floors.
*   `/seats`: Manage individual seats within rooms.
*   `/employees`: Manage employees and their seat assignments.
*   `/stats`: Get basic statistics about the office space.

## Key Code Packages

*   `com.officemanagement.model`: Contains JPA entity classes (e.g., `Employee`, `Floor`, `OfficeRoom`, `Seat`, `FloorPlanimetry`).
*   `com.officemanagement.dto`: Contains Data Transfer Objects used in API responses.
*   `com.officemanagement.resource`: Contains JAX-RS resource classes defining API endpoints.
*   `com.officemanagement.util`: Utility classes (e.g., `EntityManagerProducer`).
*   `com.officemanagement`: Contains the main JAX-RS `Application` class.

## Testing

*   **Integration Tests:** Located in `src/test/java/com/officemanagement/resource`. Use Arquillian to deploy the application to a managed JBoss EAP instance (within the test environment) and test API endpoints using REST Assured. These tests run against an in-memory H2 database configured in `src/test/resources/META-INF/persistence.xml`.
*   **Unit Tests:** Basic JUnit 5 tests for model classes can be found in `src/test/java/com/officemanagement/model`.

Run all tests with:
```bash
mvn test