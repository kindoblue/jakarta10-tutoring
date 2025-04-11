# Office Management System

![Build Status](https://github.com/kindoblue/java-tutoring/actions/workflows/build.yml/badge.svg)
![Dependabot](https://img.shields.io/badge/dependabot-enabled-025E8C?logo=dependabot)
![Java Version](https://img.shields.io/badge/Java-11-orange?logo=java)
![Last Commit](https://img.shields.io/github/last-commit/kindoblue/java-tutoring)

A Java-based office management system that provides a RESTful API for managing office spaces, employees, and seat assignments. The system allows you to:
- Create and manage multiple floors with rooms
- Track and assign seats to employees
- Search employees with pagination and filtering
- Monitor office space utilization

## Table of Contents
- [Technologies Used](#technologies-used)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Development Container Details](#development-container-details)
- [Database Setup](#database-setup)
- [Building and Running](#building-and-running)
- [API Documentation](#api-documentation)
- [Example API Requests](#example-api-requests)
- [Contributing](#contributing)
- [Error Handling](#error-handling)

## Technologies Used

- Java 11
- Hibernate 5.6
- Jersey (JAX-RS) 2.34
- PostgreSQL
- HikariCP
- Maven

## Features

- **Employee Management**
  - Create and update employee profiles
  - Search employees by name or occupation
  - Paginated results for efficient data retrieval
  
- **Office Space Management**
  - Hierarchical structure: Floors → Rooms → Seats
  - Prevent duplicate floor/room numbers
  - Track seat availability and assignments
  
- **Seat Assignment System**
  - Assign/unassign seats to employees
  - Prevent double booking of seats
  - View seat occupancy status
  
- **API Features**
  - RESTful endpoints with proper HTTP methods
  - Comprehensive error handling
  - Input validation
  - Pagination support
  
- **Technical Features**
  - Connection pooling with HikariCP
  - Hibernate for ORM
  - Containerized development environment
  - Automated tests

## Prerequisites

- [Visual Studio Code](https://code.visualstudio.com/download)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- VSCode Extension: [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers)

## Quick Start

1. Clone this repository
2. Open VSCode
3. Install the "Dev Containers" extension if you haven't already
4. Open the project folder in VSCode
5. When prompted "Folder contains a Dev Container configuration file. Reopen folder to develop in a container?" click "Reopen in Container"

VSCode will then:
1. Build and start two containers:
   - A development container with all necessary tools (Java, Maven, etc.)
   - A PostgreSQL database container (accessible on port 5432)
2. Mount your project files into the development container
3. Connect VSCode to the development container

## Development Container Details

The project uses VSCode's Dev Containers feature to provide a consistent development environment. The setup includes:

1. **Development Container**:
   - Based on devbox image
   - Contains all development tools (Java 11, Maven, etc.)
   - Mounts your project directory
   - Connected to the database container
   - Configured with necessary extensions for Java development

2. **Database Container**:
   - PostgreSQL 15
   - Persists data in a Docker volume
   - Automatically initialized with schema
   - Health checks ensure database is ready before app starts

## Database Setup

The PostgreSQL database is automatically:
- Created with name: `office_management`
- Initialized with tables: `floors`, `office_rooms`, and `seats`
- Populated with sample data
- Accessible with:
  - Host: `localhost`
  - Port: `5432`
  - Username: `postgres`
  - Password: `postgres`

## Building and Running

### Building
Build the project, run tests and create WAR file:

```bash
mvn package
```

This will:
1. Compile the Java code
2. Run all tests
3. Package the application into a WAR file

### Running
Start the application using the embedded Tomcat server:

```bash
mvn cargo:run
```

This will:
1. Start an embedded Tomcat server
2. Deploy the WAR file to Tomcat
3. Make the application available at `http://localhost:8080`

## API Documentation

### Floors
- `GET /api/floors` - List all floors
  - Response: Array of floors with basic info (id, name, floorNumber)
- `GET /api/floors/{id}` - Get floor details with rooms and seats
  - Response: Floor object with nested rooms and seats
- `POST /api/floors` - Create a new floor
  - Request Body: `{"name": "First Floor", "floorNumber": 1}`
  - Response: Created floor object with id
- `PUT /api/floors/{id}` - Update a floor
  - Request Body: `{"name": "Updated Floor", "floorNumber": 1}`
  - Response: Updated floor object
- `DELETE /api/floors/{id}` - Delete a floor
  - Response: 204 No Content
  - Error: 400 Bad Request if floor has rooms

### Rooms
- `GET /api/rooms/{id}` - Get room details with seats
  - Response: Room object with nested seats
- `GET /api/rooms/{id}/seats` - Get all seats in a room
  - Response: Array of seats
- `POST /api/rooms` - Create a new room
  - Request Body: `{"name": "Conference Room", "roomNumber": "101", "floor": {"id": 1}}`
  - Response: Created room object with id
- `PUT /api/rooms/{id}` - Update a room
  - Request Body: `{"name": "Updated Room", "roomNumber": "102", "floor": {"id": 1}}`
  - Response: Updated room object
- `DELETE /api/rooms/{id}` - Delete a room
  - Response: 204 No Content
  - Error: 400 Bad Request if room has seats
- `PATCH /api/rooms/{id}/geometry` - Update room and seat positions/dimensions
  - Request Body: 
    ```
    {
      "x": 100, "y": 200, "width": 500, "height": 300,
      "seats": {
        "1": {"x": 120, "y": 220, "width": 50, "height": 50, "rotation": 0},
        "2": {"x": 180, "y": 220, "width": 50, "height": 50, "rotation": 0}
      }
    }
    ```
  - Response: Updated room with seat count
  - Note: Only specified properties will be updated; missing properties remain unchanged

### Seats
- `GET /api/seats/{id}` - Get seat details
  - Response: Seat object with room info
- `POST /api/seats` - Create a new seat
  - Request Body: `{"seatNumber": "101-A", "room": {"id": 1}}`
  - Response: Created seat object with id
- `PUT /api/seats/{id}` - Update a seat
  - Request Body: `{"seatNumber": "101-B", "room": {"id": 1}}`
  - Response: Updated seat object
- `DELETE /api/seats/{id}` - Delete a seat
  - Response: 204 No Content
  - Error: 400 Bad Request if seat is assigned to an employee

### Employees
- `GET /api/employees/{id}` - Get employee details
  - Response: Employee object with assigned seats
- `GET /api/employees/{id}/seats` - Get employee's assigned seats
  - Response: Array of seats
- `GET /api/employees/search` - Search employees with pagination
  - Query Parameters:
    - `search`: Search term for name or occupation
    - `page`: Page number (default: 0)
    - `size`: Page size (default: 10)
  - Response: Paginated employee results
- `POST /api/employees` - Create new employee
  - Request Body: `{"fullName": "John Doe", "occupation": "Software Engineer"}`
  - Response: Created employee object with id
- `PUT /api/employees/{id}/assign-seat/{seatId}` - Assign seat to employee
  - Response: Updated employee object with seats
  - Error: 400 Bad Request if seat is already occupied
- `DELETE /api/employees/{id}/unassign-seat/{seatId}` - Unassign seat from employee
  - Response: Updated employee object with seats
  - Error: 400 Bad Request if seat is not assigned to employee

### Statistics
- `GET /api/stats` - Get office statistics
  - Response: Object containing:
    - Total number of floors
    - Total number of rooms
    - Total number of seats
    - Total number of employees
    - Seat occupancy rate

## Example API Requests

### Basic CRUD Operations

```bash
# Get all floors
curl http://localhost:8080/api/floors

# Get specific floor with rooms and seats
curl http://localhost:8080/api/floors/1

# Create a new floor
curl -X POST http://localhost:8080/api/floors \
-H "Content-Type: application/json" \
-d '{
  "name": "First Floor",
  "floorNumber": 1
}'

# Update a floor
curl -X PUT http://localhost:8080/api/floors/1 \
-H "Content-Type: application/json" \
-d '{
  "name": "Ground Floor",
  "floorNumber": 0
}'

# Delete a floor
curl -X DELETE http://localhost:8080/api/floors/1
```

### Employee and Seat Management

```bash
# Create a new employee
curl -X POST http://localhost:8080/api/employees \
-H "Content-Type: application/json" \
-d '{
  "fullName": "John Doe",
  "occupation": "Software Engineer"
}'

# Search employees by occupation
curl "http://localhost:8080/api/employees/search?search=engineer&page=0&size=10"

# Get employee's assigned seats
curl http://localhost:8080/api/employees/1/seats

# Assign a seat to an employee
curl -X PUT http://localhost:8080/api/employees/1/assign-seat/1

# Unassign a seat
curl -X DELETE http://localhost:8080/api/employees/1/unassign-seat/1
```

### Room and Seat Geometry

```bash
# Update room and seat geometry
curl -X PATCH http://localhost:8080/api/rooms/1/geometry \
-H "Content-Type: application/json" \
-d '{
  "x": 100, 
  "y": 200, 
  "width": 500, 
  "height": 300,
  "seats": {
    "1": {"x": 120, "y": 220, "width": 50, "height": 50, "rotation": 0},
    "2": {"x": 180, "y": 220, "width": 50, "height": 50, "rotation": 90}
  }
}'
```

### Office Statistics

```bash
# Get office statistics
curl http://localhost:8080/api/stats
```

Example Response:
```json
{
  "totalFloors": 3,
  "totalRooms": 15,
  "totalSeats": 100,
  "occupiedSeats": 75,
  "totalEmployees": 80,
  "occupancyRate": 75.0
}
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Java code style guidelines
- Write unit tests for new features
- Update documentation for API changes
- Use meaningful commit messages
- Keep pull requests focused and atomic

## Error Handling

The API uses standard HTTP status codes and provides detailed error messages:

### Common Status Codes
- `200 OK` - Request successful
- `201 Created` - Resource successfully created
- `204 No Content` - Request successful (no response body)
- `400 Bad Request` - Invalid input/parameters
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource already exists
- `500 Internal Server Error` - Server error

### Common Error Scenarios
- Creating duplicate floor/room numbers
- Deleting floors with existing rooms
- Deleting rooms with existing seats
- Deleting seats assigned to employees
- Invalid pagination parameters
- Missing required fields
- Invalid data formats

### Error Response Format
```json
{
  "status": 400,
  "message": "Detailed error message",
  "timestamp": "2024-03-21T10:15:30Z"
}
```
