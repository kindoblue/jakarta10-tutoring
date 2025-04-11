package com.officemanagement.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.officemanagement.dto.EmployeeDTO;
import com.officemanagement.model.Employee;
import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import com.officemanagement.model.Seat;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class EmployeeResourceTest extends BaseResourceTest {

    @Inject EntityManager testEntityManager;

    @Inject ObjectMapper objectMapper;

    private static class Holder<T> {
        T value;
    }

    @Test
    public void testGetEmployeeNotFound() {
        given().when()
                .get("/employees/999")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testCreateAndGetEmployee() throws Exception {
        Employee newEmployee = new Employee();
        newEmployee.setFullName("John Doe CreateGet");
        newEmployee.setOccupation("Developer CreateGet");

        ExtractableResponse<io.restassured.response.Response> createdResponse =
                given().contentType(ContentType.JSON)
                        .body(newEmployee)
                        .when()
                        .post("/employees")
                        .then()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .extract();

        String responseBody = createdResponse.body().asString();
        System.out.println("Create Employee Response Body (For ObjectMapper): " + responseBody);

        EmployeeDTO createdDto = objectMapper.readValue(responseBody, EmployeeDTO.class);
        Long createdEmployeeId = createdDto.getId();
        assertNotNull(createdEmployeeId, "Created Employee ID should not be null");
        System.out.println("Created Employee ID: " + createdEmployeeId);

        Employee foundEmployee =
                QuarkusTransaction.call(
                        () -> testEntityManager.find(Employee.class, createdEmployeeId));
        assertNotNull(
                foundEmployee,
                "Employee should be found directly in DB after POST transaction commits");
        assertEquals("John Doe CreateGet", foundEmployee.getFullName());

        given().when()
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

        given().contentType(ContentType.JSON)
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

        given().contentType(ContentType.JSON)
                .body(employee)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testSearchEmployees() {
        QuarkusTransaction.run(
                () -> {
                    Employee emp1 = new Employee();
                    emp1.setFullName("Alice Search Wonderland");
                    emp1.setOccupation("QA Search Engineer");
                    testEntityManager.persist(emp1);

                    Employee emp2 = new Employee();
                    emp2.setFullName("Bob Search Builder");
                    emp2.setOccupation("Search Architect");
                    testEntityManager.persist(emp2);
                });

        given().queryParam("search", "lice Search")
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content", hasSize(1))
                .body("content[0].fullName", equalTo("Alice Search Wonderland"))
                .body("totalElements", equalTo(1));

        given().queryParam("search", "Search Arch")
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content", hasSize(1))
                .body("content[0].fullName", equalTo("Bob Search Builder"))
                .body("totalElements", equalTo(1));

        given().queryParam("search", "Search")
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

        given().contentType(ContentType.JSON)
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
        final Holder<Long> employeeIdHolder = new Holder<>();
        QuarkusTransaction.run(
                () -> {
                    Employee emp = new Employee();
                    emp.setFullName("Jane Smith Get Test");
                    emp.setOccupation("Product Manager Get Test");
                    testEntityManager.persist(emp);
                    testEntityManager.flush();
                    employeeIdHolder.value = emp.getId();
                });
        Long employeeId = employeeIdHolder.value;
        assertNotNull(employeeId, "Employee ID should be set after setup transaction");
        System.out.println("testGetEmployee - ID created: " + employeeId);

        given().when()
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
        given().when()
                .get("/employees/999999")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testAssignAndUnassignSeat() {
        final Holder<Long> employeeId = new Holder<>();
        final Holder<Long> seatId = new Holder<>();

        QuarkusTransaction.run(
                () -> {
                    Employee employee = new Employee();
                    employee.setFullName("Test Assign Employee");
                    employee.setOccupation("Tester");
                    testEntityManager.persist(employee);

                    Floor floor = new Floor();
                    floor.setName("Test Floor AAUS");
                    floor.setFloorNumber(10);
                    testEntityManager.persist(floor);

                    OfficeRoom room = new OfficeRoom();
                    room.setName("Test Room AAUS");
                    room.setRoomNumber("AAUS101");
                    room.setFloor(floor);
                    testEntityManager.persist(room);

                    Seat seat = new Seat();
                    seat.setSeatNumber("Test Seat AAUS");
                    seat.setRoom(room);
                    testEntityManager.persist(seat);

                    testEntityManager.flush();
                    employeeId.value = employee.getId();
                    seatId.value = seat.getId();
                });

        assertNotNull(employeeId.value);
        assertNotNull(seatId.value);

        given().when()
                .put("/employees/" + employeeId.value + "/assign-seat/" + seatId.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(employeeId.value.intValue()))
                .body("seatIds", contains(seatId.value.intValue()));

        given().when()
                .delete("/employees/" + employeeId.value + "/unassign-seat/" + seatId.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(employeeId.value.intValue()))
                .body("seatIds", empty());
    }

    @Test
    public void testCreateEmployeeWithInvalidData() {
        Employee emptyEmployee = new Employee();
        given().contentType(ContentType.JSON)
                .body(emptyEmployee)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        Employee nullEmployee = new Employee();
        nullEmployee.setFullName(null);
        nullEmployee.setOccupation(null);
        given().contentType(ContentType.JSON)
                .body(nullEmployee)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        given().contentType(ContentType.TEXT)
                .body("Invalid data")
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    }

    @Test
    public void testAssignSeatWithInvalidIds() {
        given().when()
                .put("/employees/99999/assign-seat/1")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        final Holder<Long> validEmployeeIdHolder = new Holder<>();
        QuarkusTransaction.run(
                () -> {
                    Employee emp = new Employee();
                    emp.setFullName("Test Invalid Seat Assign");
                    emp.setOccupation("Tester");
                    testEntityManager.persist(emp);
                    testEntityManager.flush();
                    validEmployeeIdHolder.value = emp.getId();
                });
        Long validEmployeeId = validEmployeeIdHolder.value;
        assertNotNull(validEmployeeId);

        given().when()
                .put("/employees/" + validEmployeeId + "/assign-seat/99999")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testSearchEmployeesWithInvalidParameters() {
        given().queryParam("search", "John")
                .queryParam("page", "-1")
                .queryParam("size", "10")
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        given().queryParam("search", "John")
                .queryParam("page", "0")
                .queryParam("size", "-1")
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        given().queryParam("search", "John")
                .queryParam("page", "0")
                .queryParam("size", "101")
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testAssignMultipleSeatsToOneEmployee() {
        final Holder<Long> employeeId = new Holder<>();
        final Holder<Long> seat1Id = new Holder<>();
        final Holder<Long> seat2Id = new Holder<>();
        final Holder<Long> seat3Id = new Holder<>();

        QuarkusTransaction.run(
                () -> {
                    Employee employee = new Employee();
                    employee.setFullName("Multi-Seat Employee");
                    employee.setOccupation("Flexible Worker");
                    testEntityManager.persist(employee);

                    Floor floor = new Floor();
                    floor.setName("Test Floor Multi");
                    floor.setFloorNumber(20);
                    testEntityManager.persist(floor);

                    OfficeRoom room1 = new OfficeRoom();
                    room1.setName("Room 1 Multi");
                    room1.setRoomNumber("M101");
                    room1.setFloor(floor);
                    testEntityManager.persist(room1);

                    OfficeRoom room2 = new OfficeRoom();
                    room2.setName("Room 2 Multi");
                    room2.setRoomNumber("M102");
                    room2.setFloor(floor);
                    testEntityManager.persist(room2);

                    Seat seat1 = new Seat();
                    seat1.setSeatNumber("Seat 1 Multi");
                    seat1.setRoom(room1);
                    testEntityManager.persist(seat1);

                    Seat seat2 = new Seat();
                    seat2.setSeatNumber("Seat 2 Multi");
                    seat2.setRoom(room1);
                    testEntityManager.persist(seat2);

                    Seat seat3 = new Seat();
                    seat3.setSeatNumber("Seat 3 Multi");
                    seat3.setRoom(room2);
                    testEntityManager.persist(seat3);

                    testEntityManager.flush();
                    employeeId.value = employee.getId();
                    seat1Id.value = seat1.getId();
                    seat2Id.value = seat2.getId();
                    seat3Id.value = seat3.getId();
                });

        assertNotNull(employeeId.value);
        assertNotNull(seat1Id.value);
        assertNotNull(seat2Id.value);
        assertNotNull(seat3Id.value);

        given().when()
                .put("/employees/" + employeeId.value + "/assign-seat/" + seat1Id.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("seatIds", contains(seat1Id.value.intValue()));

        given().when()
                .put("/employees/" + employeeId.value + "/assign-seat/" + seat2Id.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(
                        "seatIds",
                        containsInAnyOrder(seat1Id.value.intValue(), seat2Id.value.intValue()));

        given().when()
                .put("/employees/" + employeeId.value + "/assign-seat/" + seat3Id.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(
                        "seatIds",
                        containsInAnyOrder(
                                seat1Id.value.intValue(),
                                seat2Id.value.intValue(),
                                seat3Id.value.intValue()));

        given().when()
                .delete("/employees/" + employeeId.value + "/unassign-seat/" + seat2Id.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(
                        "seatIds",
                        containsInAnyOrder(seat1Id.value.intValue(), seat3Id.value.intValue()));

        QuarkusTransaction.run(
                () -> {
                    Employee verifiedEmployee =
                            testEntityManager.find(Employee.class, employeeId.value);
                    assertNotNull(verifiedEmployee);
                    testEntityManager.refresh(verifiedEmployee);
                    assertEquals(2, verifiedEmployee.getSeats().size());
                    assertTrue(
                            verifiedEmployee.getSeats().stream()
                                    .anyMatch(s -> s.getId().equals(seat1Id.value)));
                    assertTrue(
                            verifiedEmployee.getSeats().stream()
                                    .anyMatch(s -> s.getId().equals(seat3Id.value)));
                    assertFalse(
                            verifiedEmployee.getSeats().stream()
                                    .anyMatch(s -> s.getId().equals(seat2Id.value)));
                });
    }

    @Test
    public void testComplexSeatAssignmentScenarios() {
        final Holder<Long> emp1Id = new Holder<>();
        final Holder<Long> emp2Id = new Holder<>();
        final Holder<Long> emp3Id = new Holder<>();
        final Holder<Long> seat1Id = new Holder<>();
        final Holder<Long> seat2Id = new Holder<>();
        final Holder<Long> seat3Id = new Holder<>();

        QuarkusTransaction.run(
                () -> {
                    Employee employee1 = new Employee();
                    employee1.setFullName("Employee 1 Complex");
                    employee1.setOccupation("Developer");
                    testEntityManager.persist(employee1);
                    Employee employee2 = new Employee();
                    employee2.setFullName("Employee 2 Complex");
                    employee2.setOccupation("Designer");
                    testEntityManager.persist(employee2);
                    Employee employee3 = new Employee();
                    employee3.setFullName("Employee 3 Complex");
                    employee3.setOccupation("Manager");
                    testEntityManager.persist(employee3);
                    Floor floor = new Floor();
                    floor.setName("Complex Test Floor");
                    floor.setFloorNumber(30);
                    testEntityManager.persist(floor);
                    OfficeRoom room = new OfficeRoom();
                    room.setName("Complex Test Room");
                    room.setRoomNumber("C201");
                    room.setFloor(floor);
                    testEntityManager.persist(room);
                    Seat seat1 = new Seat();
                    seat1.setSeatNumber("Complex Seat 1");
                    seat1.setRoom(room);
                    testEntityManager.persist(seat1);
                    Seat seat2 = new Seat();
                    seat2.setSeatNumber("Complex Seat 2");
                    seat2.setRoom(room);
                    testEntityManager.persist(seat2);
                    Seat seat3 = new Seat();
                    seat3.setSeatNumber("Complex Seat 3");
                    seat3.setRoom(room);
                    testEntityManager.persist(seat3);
                    testEntityManager.flush();
                    emp1Id.value = employee1.getId();
                    emp2Id.value = employee2.getId();
                    emp3Id.value = employee3.getId();
                    seat1Id.value = seat1.getId();
                    seat2Id.value = seat2.getId();
                    seat3Id.value = seat3.getId();
                });

        assertNotNull(emp1Id.value);
        assertNotNull(emp2Id.value);
        assertNotNull(emp3Id.value);
        assertNotNull(seat1Id.value);
        assertNotNull(seat2Id.value);
        assertNotNull(seat3Id.value);

        given().when()
                .put("/employees/" + emp1Id.value + "/assign-seat/" + seat1Id.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("seatIds", contains(seat1Id.value.intValue()));
        given().when()
                .put("/employees/" + emp2Id.value + "/assign-seat/" + seat1Id.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("seatIds", contains(seat1Id.value.intValue()));

        given().when()
                .put("/employees/" + emp3Id.value + "/assign-seat/" + seat2Id.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("seatIds", contains(seat2Id.value.intValue()));
        given().when()
                .put("/employees/" + emp3Id.value + "/assign-seat/" + seat3Id.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(
                        "seatIds",
                        containsInAnyOrder(seat2Id.value.intValue(), seat3Id.value.intValue()));

        given().when()
                .put("/employees/" + emp1Id.value + "/assign-seat/" + seat2Id.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(
                        "seatIds",
                        containsInAnyOrder(seat1Id.value.intValue(), seat2Id.value.intValue()));

        given().when()
                .delete("/employees/" + emp1Id.value + "/unassign-seat/" + seat1Id.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("seatIds", contains(seat2Id.value.intValue()));

        given().when()
                .put("/employees/" + emp3Id.value + "/assign-seat/" + seat3Id.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(
                        "seatIds",
                        containsInAnyOrder(seat2Id.value.intValue(), seat3Id.value.intValue()));

        QuarkusTransaction.run(
                () -> {
                    Seat verifiedSeat1 = testEntityManager.find(Seat.class, seat1Id.value);
                    testEntityManager.refresh(verifiedSeat1);
                    assertEquals(1, verifiedSeat1.getEmployees().size());
                    assertEquals(
                            emp2Id.value, verifiedSeat1.getEmployees().iterator().next().getId());

                    Seat verifiedSeat2 = testEntityManager.find(Seat.class, seat2Id.value);
                    testEntityManager.refresh(verifiedSeat2);
                    assertEquals(2, verifiedSeat2.getEmployees().size());
                    assertTrue(
                            verifiedSeat2.getEmployees().stream()
                                    .anyMatch(e -> e.getId().equals(emp1Id.value)));
                    assertTrue(
                            verifiedSeat2.getEmployees().stream()
                                    .anyMatch(e -> e.getId().equals(emp3Id.value)));

                    Employee verifiedEmployee3 =
                            testEntityManager.find(Employee.class, emp3Id.value);
                    testEntityManager.refresh(verifiedEmployee3);
                    assertEquals(2, verifiedEmployee3.getSeats().size());
                });
    }

    @Test
    public void testSeatAssignmentEdgeCases() {
        final Holder<Long> employeeId = new Holder<>();
        final Holder<Long> seatId = new Holder<>();

        QuarkusTransaction.run(
                () -> {
                    Employee employee = new Employee();
                    employee.setFullName("Edge Case Employee");
                    employee.setOccupation("Tester");
                    testEntityManager.persist(employee);
                    Floor floor = new Floor();
                    floor.setName("Edge Case Floor");
                    floor.setFloorNumber(40);
                    testEntityManager.persist(floor);
                    OfficeRoom room = new OfficeRoom();
                    room.setName("Edge Case Room");
                    room.setRoomNumber("E401");
                    room.setFloor(floor);
                    testEntityManager.persist(room);
                    Seat seat = new Seat();
                    seat.setSeatNumber("Edge Case Seat");
                    seat.setRoom(room);
                    testEntityManager.persist(seat);
                    testEntityManager.flush();
                    employeeId.value = employee.getId();
                    seatId.value = seat.getId();
                });

        assertNotNull(employeeId.value);
        assertNotNull(seatId.value);

        given().when()
                .delete("/employees/" + employeeId.value + "/unassign-seat/" + seatId.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        given().when()
                .put("/employees/" + employeeId.value + "/assign-seat/" + seatId.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("seatIds", contains(seatId.value.intValue()));

        given().when()
                .put("/employees/" + employeeId.value + "/assign-seat/" + seatId.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("seatIds", contains(seatId.value.intValue()));

        given().when()
                .delete("/employees/" + employeeId.value + "/unassign-seat/" + seatId.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("seatIds", empty());

        for (int i = 0; i < 5; i++) {
            given().when()
                    .put("/employees/" + employeeId.value + "/assign-seat/" + seatId.value)
                    .then()
                    .log()
                    .ifValidationFails()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .body("seatIds", hasSize(1));
            given().when()
                    .delete("/employees/" + employeeId.value + "/unassign-seat/" + seatId.value)
                    .then()
                    .log()
                    .ifValidationFails()
                    .statusCode(Response.Status.OK.getStatusCode())
                    .body("seatIds", hasSize(0));
        }

        QuarkusTransaction.run(
                () -> {
                    Employee verifiedEmployee =
                            testEntityManager.find(Employee.class, employeeId.value);
                    assertNotNull(verifiedEmployee);
                    assertTrue(verifiedEmployee.getSeats().isEmpty());
                    Seat verifiedSeat = testEntityManager.find(Seat.class, seatId.value);
                    assertNotNull(verifiedSeat);
                    assertTrue(verifiedSeat.getEmployees().isEmpty());
                });
    }

    @Test
    public void testDeleteEmployeeWithAssignedSeats() {
        final Holder<Long> employeeId = new Holder<>();
        final Holder<Long> seat1Id = new Holder<>();
        final Holder<Long> seat2Id = new Holder<>();

        QuarkusTransaction.run(
                () -> {
                    Employee employee = new Employee();
                    employee.setFullName("Test Delete Employee");
                    employee.setOccupation("Tester");
                    testEntityManager.persist(employee);
                    Floor floor = new Floor();
                    floor.setName("Test Delete Floor");
                    floor.setFloorNumber(50);
                    testEntityManager.persist(floor);
                    OfficeRoom room = new OfficeRoom();
                    room.setName("Test Delete Room");
                    room.setRoomNumber("D101");
                    room.setFloor(floor);
                    testEntityManager.persist(room);
                    Seat seat1 = new Seat();
                    seat1.setSeatNumber("Test Delete Seat 1");
                    seat1.setRoom(room);
                    testEntityManager.persist(seat1);
                    Seat seat2 = new Seat();
                    seat2.setSeatNumber("Test Delete Seat 2");
                    seat2.setRoom(room);
                    testEntityManager.persist(seat2);
                    testEntityManager.flush();
                    employee.addSeat(seat1);
                    employee.addSeat(seat2);
                    testEntityManager.merge(employee);
                    testEntityManager.flush();
                    employeeId.value = employee.getId();
                    seat1Id.value = seat1.getId();
                    seat2Id.value = seat2.getId();
                });

        assertNotNull(employeeId.value);
        assertNotNull(seat1Id.value);
        assertNotNull(seat2Id.value);

        given().when()
                .delete("/employees/" + employeeId.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        QuarkusTransaction.run(
                () -> {
                    Employee deletedEmployee =
                            testEntityManager.find(Employee.class, employeeId.value);
                    assertNull(deletedEmployee, "Employee should be deleted");

                    Seat updatedSeat1 = testEntityManager.find(Seat.class, seat1Id.value);
                    Seat updatedSeat2 = testEntityManager.find(Seat.class, seat2Id.value);
                    assertNotNull(updatedSeat1, "Seat 1 should still exist");
                    assertNotNull(updatedSeat2, "Seat 2 should still exist");

                    testEntityManager.refresh(updatedSeat1);
                    testEntityManager.refresh(updatedSeat2);
                    assertTrue(
                            updatedSeat1.getEmployees().isEmpty(),
                            "Seat 1 should not have any employees assigned");
                    assertTrue(
                            updatedSeat2.getEmployees().isEmpty(),
                            "Seat 2 should not have any employees assigned");
                });
    }

    @Test
    public void testDeleteNonExistentEmployee() {
        given().when()
                .delete("/employees/999999")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
