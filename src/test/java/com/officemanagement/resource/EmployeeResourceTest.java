package com.officemanagement.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.officemanagement.model.Employee;
import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import com.officemanagement.model.Seat;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

public class EmployeeResourceTest extends BaseResourceTest {

    @Inject ObjectMapper objectMapper;

    private static class Holder<T> {
        T value;
    }

    @Test
    public void testGetEmployeeNotFound() {
        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/999")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testCreateAndGetEmployee() throws Exception {
        Long createdEmployeeId;
        entityManager.getTransaction().begin();
        try {
            Employee newEmployee = new Employee();
            newEmployee.setFullName("John Doe CreateGet");
            newEmployee.setOccupation("Developer CreateGet");

            entityManager.persist(newEmployee);
            entityManager.flush();
            createdEmployeeId = newEmployee.getId();
            assertNotNull(createdEmployeeId, "Created Employee ID should not be null via EM");

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
        entityManager.clear();

        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/" + createdEmployeeId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(createdEmployeeId.intValue()))
                .body("fullName", equalTo("John Doe CreateGet"))
                .body("occupation", equalTo("Developer CreateGet"))
                .body("createdAt", notNullValue())
                .body("seatIds", hasSize(0));
        System.out.println("GET request finished for Employee ID: " + createdEmployeeId);
    }

    @Test
    public void testCreateEmployeeMissingName() {
        Employee employee = new Employee();
        employee.setOccupation("Tester");

        given().baseUri("http://localhost:8080/test")
                .contentType(ContentType.JSON)
                .body(employee)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCreateEmployeeMissingOccupation() {
        Employee employee = new Employee();
        employee.setFullName("Jane Smith");

        given().baseUri("http://localhost:8080/test")
                .contentType(ContentType.JSON)
                .body(employee)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testSearchEmployees() {
        entityManager.getTransaction().begin();
        try {
            Employee emp1 = new Employee();
            emp1.setFullName("Alice Search Wonderland");
            emp1.setOccupation("QA Search Engineer");
            entityManager.persist(emp1);

            Employee emp2 = new Employee();
            emp2.setFullName("Bob Search Builder");
            emp2.setOccupation("Search Architect");
            entityManager.persist(emp2);
            entityManager.flush();

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
        entityManager.clear();

        given().baseUri("http://localhost:8080/test")
                .queryParam("search", "lice Search")
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content", hasSize(1))
                .body("content[0].fullName", equalTo("Alice Search Wonderland"))
                .body("totalElements", equalTo(1));

        given().baseUri("http://localhost:8080/test")
                .queryParam("search", "Search Arch")
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content", hasSize(1))
                .body("content[0].fullName", equalTo("Bob Search Builder"))
                .body("totalElements", equalTo(1));

        given().baseUri("http://localhost:8080/test")
                .queryParam("search", "Search")
                .queryParam("page", 0)
                .queryParam("size", 1)
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content", hasSize(1))
                .body("totalElements", equalTo(2))
                .body("totalPages", equalTo(2));
    }

    @Test
    public void testCreateEmployee() {
        Employee employee = new Employee();
        employee.setFullName("John Doe Create");
        employee.setOccupation("Software Engineer Create");

        given().baseUri("http://localhost:8080/test")
                .contentType(ContentType.JSON)
                .body(employee)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body("id", notNullValue())
                .body("fullName", equalTo("John Doe Create"))
                .body("occupation", equalTo("Software Engineer Create"));
    }

    @Test
    public void testGetEmployee() {
        Long employeeId;
        entityManager.getTransaction().begin();
        try {
            Employee emp = new Employee();
            emp.setFullName("Jane Smith Get Test");
            emp.setOccupation("Product Manager Get Test");
            entityManager.persist(emp);
            entityManager.flush();
            employeeId = emp.getId();
            assertNotNull(employeeId, "Employee ID should be set after setup");

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
        entityManager.clear();

        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/" + employeeId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(employeeId.intValue()))
                .body("fullName", equalTo("Jane Smith Get Test"))
                .body("occupation", equalTo("Product Manager Get Test"))
                .body("seatIds", empty());
    }

    @Test
    public void testGetNonExistentEmployee() {
        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/999999")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testAssignAndUnassignSeat() {
        Long employeeId;
        Long seatId;
        entityManager.getTransaction().begin();
        try {
            Employee employee = new Employee();
            employee.setFullName("Test Assign Employee");
            employee.setOccupation("Tester");
            entityManager.persist(employee);

            Floor floor = new Floor();
            floor.setName("Test Floor AAUS");
            floor.setFloorNumber(10);
            entityManager.persist(floor);

            OfficeRoom room = new OfficeRoom();
            room.setName("Test Room AAUS");
            room.setRoomNumber("AAUS101");
            room.setFloor(floor);
            entityManager.persist(room);

            Seat seat = new Seat();
            seat.setSeatNumber("Test Seat AAUS");
            seat.setRoom(room);
            entityManager.persist(seat);

            entityManager.flush();
            employeeId = employee.getId();
            seatId = seat.getId();
            assertNotNull(employeeId);
            assertNotNull(seatId);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
        entityManager.clear();

        given().baseUri("http://localhost:8080/test")
                .when()
                .put("/employees/" + employeeId + "/assign-seat/" + seatId)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/" + employeeId)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("seatIds", hasItem(seatId.intValue()));

        given().baseUri("http://localhost:8080/test")
                .when()
                .put("/employees/" + employeeId + "/unassign-seat/" + seatId)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/" + employeeId)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("seatIds", not(hasItem(seatId.intValue())));
    }

    @Test
    public void testCreateEmployeeWithInvalidData() {
        Employee employeeNullName = new Employee();
        employeeNullName.setOccupation("Invalid Occupation");
        given().baseUri("http://localhost:8080/test")
                .contentType(ContentType.JSON)
                .body(employeeNullName)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        Employee employeeNullOccupation = new Employee();
        employeeNullOccupation.setFullName("Invalid Name");
        given().baseUri("http://localhost:8080/test")
                .contentType(ContentType.JSON)
                .body(employeeNullOccupation)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        Employee employeeEmptyName = new Employee();
        employeeEmptyName.setFullName("");
        employeeEmptyName.setOccupation("Valid Occupation");
        given().baseUri("http://localhost:8080/test")
                .contentType(ContentType.JSON)
                .body(employeeEmptyName)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        Employee employeeEmptyOccupation = new Employee();
        employeeEmptyOccupation.setFullName("Valid Name");
        employeeEmptyOccupation.setOccupation("");
        given().baseUri("http://localhost:8080/test")
                .contentType(ContentType.JSON)
                .body(employeeEmptyOccupation)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testAssignSeatWithInvalidIds() {
        long nonExistentEmployeeId = 9999L;
        long nonExistentSeatId = 8888L;
        Long validEmployeeId;
        Long validSeatId;

        entityManager.getTransaction().begin();
        try {
            Employee emp = new Employee();
            emp.setFullName("Valid Emp Invalid Assign");
            emp.setOccupation("Temp");
            entityManager.persist(emp);

            Floor floor = new Floor();
            floor.setName("Inv Floor");
            floor.setFloorNumber(99);
            entityManager.persist(floor);
            OfficeRoom room = new OfficeRoom();
            room.setName("Inv Room");
            room.setRoomNumber("INV1");
            room.setFloor(floor);
            entityManager.persist(room);
            Seat seat = new Seat();
            seat.setSeatNumber("Inv Seat");
            seat.setRoom(room);
            entityManager.persist(seat);

            entityManager.flush();
            validEmployeeId = emp.getId();
            validSeatId = seat.getId();
            assertNotNull(validEmployeeId);
            assertNotNull(validSeatId);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
        entityManager.clear();

        given().baseUri("http://localhost:8080/test")
                .when()
                .put("/employees/" + validEmployeeId + "/assign-seat/" + nonExistentSeatId)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .when()
                .put("/employees/" + nonExistentEmployeeId + "/assign-seat/" + validSeatId)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .when()
                .put("/employees/" + nonExistentEmployeeId + "/assign-seat/" + nonExistentSeatId)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testSearchEmployeesWithInvalidParameters() {
        given().baseUri("http://localhost:8080/test")
                .queryParam("search", (String) null)
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content", notNullValue());

        given().baseUri("http://localhost:8080/test")
                .queryParam("search", "")
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content", notNullValue());

        given().baseUri("http://localhost:8080/test")
                .queryParam("search", "test")
                .queryParam("page", -1)
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .queryParam("search", "test")
                .queryParam("size", 0)
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .queryParam("search", "test")
                .queryParam("size", -1)
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testAssignMultipleSeatsToOneEmployee() {
        Long employeeId;
        Long seatId1;
        Long seatId2;
        entityManager.getTransaction().begin();
        try {
            Employee employee = new Employee();
            employee.setFullName("Multi Assign Emp");
            employee.setOccupation("Manager");
            entityManager.persist(employee);

            Floor floor = new Floor();
            floor.setName("Multi Floor");
            floor.setFloorNumber(20);
            entityManager.persist(floor);
            OfficeRoom room = new OfficeRoom();
            room.setName("Multi Room");
            room.setRoomNumber("MULTI1");
            room.setFloor(floor);
            entityManager.persist(room);

            Seat seat1 = new Seat();
            seat1.setSeatNumber("MULTI-S1");
            seat1.setRoom(room);
            entityManager.persist(seat1);
            Seat seat2 = new Seat();
            seat2.setSeatNumber("MULTI-S2");
            seat2.setRoom(room);
            entityManager.persist(seat2);

            entityManager.flush();
            employeeId = employee.getId();
            seatId1 = seat1.getId();
            seatId2 = seat2.getId();
            assertNotNull(employeeId);
            assertNotNull(seatId1);
            assertNotNull(seatId2);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
        entityManager.clear();

        given().baseUri("http://localhost:8080/test")
                .when()
                .put("/employees/" + employeeId + "/assign-seat/" + seatId1)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .when()
                .put("/employees/" + employeeId + "/assign-seat/" + seatId2)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/" + employeeId)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("seatIds", contains(seatId2.intValue()))
                .body("seatIds", not(contains(seatId1.intValue())));
    }

    @Test
    public void testComplexSeatAssignmentScenarios() {
        Long empId1;
        Long empId2;
        Long seatId1;
        Long seatId2;
        Long seatId3;
        entityManager.getTransaction().begin();
        try {
            Floor floorA = new Floor();
            floorA.setName("Complex Floor A");
            floorA.setFloorNumber(30);
            entityManager.persist(floorA);
            Floor floorB = new Floor();
            floorB.setName("Complex Floor B");
            floorB.setFloorNumber(31);
            entityManager.persist(floorB);
            OfficeRoom roomA1 = new OfficeRoom();
            roomA1.setName("Complex Room A1");
            roomA1.setRoomNumber("CA1");
            roomA1.setFloor(floorA);
            entityManager.persist(roomA1);
            OfficeRoom roomB1 = new OfficeRoom();
            roomB1.setName("Complex Room B1");
            roomB1.setRoomNumber("CB1");
            roomB1.setFloor(floorB);
            entityManager.persist(roomB1);
            Seat seat1 = new Seat();
            seat1.setSeatNumber("CA1-S1");
            seat1.setRoom(roomA1);
            entityManager.persist(seat1);
            Seat seat2 = new Seat();
            seat2.setSeatNumber("CA1-S2");
            seat2.setRoom(roomA1);
            entityManager.persist(seat2);
            Seat seat3 = new Seat();
            seat3.setSeatNumber("CB1-S1");
            seat3.setRoom(roomB1);
            entityManager.persist(seat3);
            Employee emp1 = new Employee();
            emp1.setFullName("Complex Emp 1");
            emp1.setOccupation("Analyst");
            entityManager.persist(emp1);
            Employee emp2 = new Employee();
            emp2.setFullName("Complex Emp 2");
            emp2.setOccupation("Manager");
            entityManager.persist(emp2);
            entityManager.flush();
            empId1 = emp1.getId();
            empId2 = emp2.getId();
            seatId1 = seat1.getId();
            seatId2 = seat2.getId();
            seatId3 = seat3.getId();
            assertNotNull(empId1);
            assertNotNull(empId2);
            assertNotNull(seatId1);
            assertNotNull(seatId2);
            assertNotNull(seatId3);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
        entityManager.clear();

        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/assign-seat/{seatId}", empId1, seatId1)
                .then()
                .statusCode(200);
        given().baseUri("http://localhost:8080/test")
                .get("/employees/{empId}", empId1)
                .then()
                .body("seatIds", contains(seatId1.intValue()));
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/assign-seat/{seatId}", empId2, seatId2)
                .then()
                .statusCode(200);
        given().baseUri("http://localhost:8080/test")
                .get("/employees/{empId}", empId2)
                .then()
                .body("seatIds", contains(seatId2.intValue()));
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/assign-seat/{seatId}", empId2, seatId1)
                .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode());
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/assign-seat/{seatId}", empId1, seatId3)
                .then()
                .statusCode(200);
        given().baseUri("http://localhost:8080/test")
                .get("/employees/{empId}", empId1)
                .then()
                .body("seatIds", contains(seatId3.intValue()))
                .body("seatIds", not(contains(seatId1.intValue())));
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/unassign-seat/{seatId}", empId2, seatId2)
                .then()
                .statusCode(200);
        given().baseUri("http://localhost:8080/test")
                .get("/employees/{empId}", empId2)
                .then()
                .body("seatIds", empty());
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/unassign-seat/{seatId}", empId1, seatId2)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testSeatAssignmentEdgeCases() {
        Long empId;
        Long seatId;
        Long nonExistentSeatId = 9999L;
        Long nonExistentEmpId = 8888L;
        entityManager.getTransaction().begin();
        try {
            Employee employee = new Employee();
            employee.setFullName("Edge Case Emp");
            employee.setOccupation("Edge");
            entityManager.persist(employee);
            Floor floor = new Floor();
            floor.setName("Edge Floor");
            floor.setFloorNumber(40);
            entityManager.persist(floor);
            OfficeRoom room = new OfficeRoom();
            room.setName("Edge Room");
            room.setRoomNumber("EDGE1");
            room.setFloor(floor);
            entityManager.persist(room);
            Seat seat = new Seat();
            seat.setSeatNumber("EDGE-S1");
            seat.setRoom(room);
            entityManager.persist(seat);
            entityManager.flush();
            empId = employee.getId();
            seatId = seat.getId();
            assertNotNull(empId);
            assertNotNull(seatId);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
        entityManager.clear();

        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/assign-seat/{seatId}", empId, seatId)
                .then()
                .statusCode(200);
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/assign-seat/{seatId}", empId, seatId)
                .then()
                .statusCode(200);
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/assign-seat/{seatId}", empId, nonExistentSeatId)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/assign-seat/{seatId}", nonExistentEmpId, seatId)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/unassign-seat/{seatId}", empId, seatId)
                .then()
                .statusCode(200);
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/unassign-seat/{seatId}", empId, seatId)
                .then()
                .statusCode(200);
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/unassign-seat/{seatId}", empId, nonExistentSeatId)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/unassign-seat/{seatId}", nonExistentEmpId, seatId)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testDeleteEmployeeWithAssignedSeats() {
        Long employeeId;
        Long seatId;
        entityManager.getTransaction().begin();
        try {
            Employee employee = new Employee();
            employee.setFullName("Delete Me Emp");
            employee.setOccupation("Deleter");
            entityManager.persist(employee);
            Floor floor = new Floor();
            floor.setName("Delete Floor");
            floor.setFloorNumber(50);
            entityManager.persist(floor);
            OfficeRoom room = new OfficeRoom();
            room.setName("Delete Room");
            room.setRoomNumber("DEL1");
            room.setFloor(floor);
            entityManager.persist(room);
            Seat seat = new Seat();
            seat.setSeatNumber("DEL-S1");
            seat.setRoom(room);
            entityManager.persist(seat);
            entityManager.flush();
            employee.addSeat(seat);
            entityManager.merge(employee);
            entityManager.flush();
            employeeId = employee.getId();
            seatId = seat.getId();
            assertNotNull(employeeId);
            assertNotNull(seatId);

            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw e;
        }
        entityManager.clear();

        given().baseUri("http://localhost:8080/test")
                .when()
                .delete("/employees/{id}", employeeId)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/{id}", employeeId)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        Seat dbSeat = entityManager.find(Seat.class, seatId);
        assertNotNull(dbSeat, "Seat should still exist");
        assertNull(dbSeat.getEmployee(), "Seat should not be assigned to any employee");
    }

    @Test
    public void testDeleteNonExistentEmployee() {
        given().baseUri("http://localhost:8080/test")
                .when()
                .delete("/employees/777777")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
