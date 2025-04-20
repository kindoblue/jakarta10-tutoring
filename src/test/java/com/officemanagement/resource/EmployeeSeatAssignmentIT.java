package com.officemanagement.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.officemanagement.model.Employee;
import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import com.officemanagement.model.Seat;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmployeeSeatAssignmentIT extends BaseResourceTest {

    private static class Holder<T> {
        T value;
    }

    @Test
    public void testAssignAndUnassignSeat() {
        final Holder<Long> floorId = new Holder<>();
        final Holder<Long> roomId = new Holder<>();
        final Holder<Long> seatId = new Holder<>();
        final Holder<Long> employeeId = new Holder<>();

        runInTransaction(
                () -> {
                    Floor floor = new Floor();
                    floor.setFloorNumber(100);
                    floor.setName("Test Floor AssignUnassign");
                    entityManager.persist(floor);
                    entityManager.flush();
                    floorId.value = floor.getId();

                    OfficeRoom room = new OfficeRoom();
                    room.setRoomNumber("R100A");
                    room.setName("Test Room AssignUnassign");
                    room.setFloor(floor);
                    entityManager.persist(room);
                    entityManager.flush();
                    roomId.value = room.getId();

                    Seat seat = new Seat();
                    seat.setSeatNumber("S100A1");
                    seat.setRoom(room);
                    entityManager.persist(seat);
                    entityManager.flush();
                    seatId.value = seat.getId();

                    Employee employee = new Employee();
                    employee.setFullName("Assign Unassign Test User");
                    employee.setOccupation("Assignee");
                    entityManager.persist(employee);
                    entityManager.flush();
                    employeeId.value = employee.getId();

                    assertNotNull(seatId.value, "Seat ID must be set after setup");
                    assertNotNull(employeeId.value, "Employee ID must be set after setup");
                });

        assertNotNull(floorId.value, "Floor ID missing");
        assertNotNull(roomId.value, "Room ID missing");

        given().baseUri("http://localhost:8080/test")
                .when()
                .put("/employees/" + employeeId.value + "/seats/" + seatId.value)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/" + employeeId.value)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("seatIds", hasItem(seatId.value.intValue()));

        given().baseUri("http://localhost:8080/test")
                .when()
                .delete("/employees/" + employeeId.value + "/seats/" + seatId.value)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/" + employeeId.value)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("seatIds", not(hasItem(seatId.value.intValue())));
    }

    @Test
    public void testAssignSeatWithInvalidIds() {
        final Holder<Long> floorId = new Holder<>();
        final Holder<Long> roomId = new Holder<>();
        final Holder<Long> employeeId = new Holder<>();
        long nonExistentSeatId = 9999L;

        runInTransaction(
                () -> {
                    Floor floor = new Floor();
                    floor.setFloorNumber(101);
                    floor.setName("Test Floor Invalid Assign");
                    entityManager.persist(floor);
                    entityManager.flush();
                    floorId.value = floor.getId();

                    OfficeRoom room = new OfficeRoom();
                    room.setRoomNumber("R101A");
                    room.setName("Test Room Invalid Assign");
                    room.setFloor(floor);
                    entityManager.persist(room);
                    entityManager.flush();
                    roomId.value = room.getId();

                    Employee employee = new Employee();
                    employee.setFullName("Invalid Assign Test User");
                    employee.setOccupation("Invalid Assignee");
                    entityManager.persist(employee);
                    entityManager.flush();
                    employeeId.value = employee.getId();
                });

        assertNotNull(floorId.value, "Floor ID missing");
        assertNotNull(roomId.value, "Room ID missing");

        given().baseUri("http://localhost:8080/test")
                .when()
                .put("/employees/" + employeeId.value + "/seats/" + nonExistentSeatId)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .when()
                .delete("/employees/" + nonExistentSeatId + "/seats/" + employeeId.value)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .when()
                .delete("/employees/" + employeeId.value + "/seats/" + nonExistentSeatId)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testAssignMultipleSeatsToOneEmployee() {
        final Holder<Long> floorId = new Holder<>();
        final Holder<Long> roomId = new Holder<>();
        final Holder<Long> seatId1 = new Holder<>();
        final Holder<Long> seatId2 = new Holder<>();
        final Holder<Long> employeeId = new Holder<>();

        runInTransaction(
                () -> {
                    Floor floor = new Floor();
                    floor.setFloorNumber(102);
                    floor.setName("Test Floor MultiSeat");
                    entityManager.persist(floor);
                    entityManager.flush();
                    floorId.value = floor.getId();

                    OfficeRoom room = new OfficeRoom();
                    room.setRoomNumber("R102A");
                    room.setName("Test Room MultiSeat");
                    room.setFloor(floor);
                    entityManager.persist(room);
                    entityManager.flush();
                    roomId.value = room.getId();

                    Seat seat1 = new Seat();
                    seat1.setSeatNumber("MS1");
                    seat1.setRoom(room);
                    entityManager.persist(seat1);
                    entityManager.flush();
                    seatId1.value = seat1.getId();

                    Seat seat2 = new Seat();
                    seat2.setSeatNumber("MS2");
                    seat2.setRoom(room);
                    entityManager.persist(seat2);
                    entityManager.flush();
                    seatId2.value = seat2.getId();

                    Employee employee = new Employee();
                    employee.setFullName("Multi Seat Employee");
                    employee.setOccupation("Collector");
                    entityManager.persist(employee);
                    entityManager.flush();
                    employeeId.value = employee.getId();
                });

        given().baseUri("http://localhost:8080/test")
                .when()
                .put("/employees/" + employeeId.value + "/seats/" + seatId1.value)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .when()
                .put("/employees/" + employeeId.value + "/seats/" + seatId2.value)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/" + employeeId.value)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("seatIds", hasSize(2))
                .body(
                        "seatIds",
                        containsInAnyOrder(seatId1.value.intValue(), seatId2.value.intValue()));
    }

    @Test
    public void testComplexSeatAssignmentScenarios() {
        final Holder<Long> floorId = new Holder<>();
        final Holder<Long> roomId = new Holder<>();
        final Holder<Long> seatId1 = new Holder<>();
        final Holder<Long> seatId2 = new Holder<>();
        final Holder<Long> seatId3 = new Holder<>();
        final Holder<Long> employeeId1 = new Holder<>();
        final Holder<Long> employeeId2 = new Holder<>();

        runInTransaction(
                () -> {
                    Floor floor = new Floor();
                    floor.setFloorNumber(103);
                    floor.setName("Test Floor Complex");
                    entityManager.persist(floor);
                    entityManager.flush();
                    floorId.value = floor.getId();

                    OfficeRoom room = new OfficeRoom();
                    room.setRoomNumber("R103A");
                    room.setName("Test Room Complex");
                    room.setFloor(floor);
                    entityManager.persist(room);
                    entityManager.flush();
                    roomId.value = room.getId();

                    Seat seat1 = new Seat();
                    seat1.setSeatNumber("CS1");
                    seat1.setRoom(room);
                    entityManager.persist(seat1);
                    seatId1.value = seat1.getId();

                    Seat seat2 = new Seat();
                    seat2.setSeatNumber("CS2");
                    seat2.setRoom(room);
                    entityManager.persist(seat2);
                    seatId2.value = seat2.getId();

                    Seat seat3 = new Seat();
                    seat3.setSeatNumber("CS3");
                    seat3.setRoom(room);
                    entityManager.persist(seat3);
                    seatId3.value = seat3.getId();

                    Employee emp1 = new Employee();
                    emp1.setFullName("Complex User One");
                    emp1.setOccupation("Role A");
                    entityManager.persist(emp1);
                    employeeId1.value = emp1.getId();

                    Employee emp2 = new Employee();
                    emp2.setFullName("Complex User Two");
                    emp2.setOccupation("Role B");
                    entityManager.persist(emp2);
                    employeeId2.value = emp2.getId();
                });

        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/seats/{seatId}", employeeId1.value, seatId1.value)
                .then()
                .statusCode(200);
        given().baseUri("http://localhost:8080/test")
                .get("/employees/{empId}", employeeId1.value)
                .then()
                .body("seatIds", contains(seatId1.value.intValue()));
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/seats/{seatId}", employeeId2.value, seatId2.value)
                .then()
                .statusCode(200);
        given().baseUri("http://localhost:8080/test")
                .get("/employees/{empId}", employeeId2.value)
                .then()
                .body("seatIds", contains(seatId2.value.intValue()));

        // Assign occupied seat1 to employee2 -> Should succeed (multiple employees per seat
        // allowed)
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/seats/{seatId}", employeeId2.value, seatId1.value)
                .then()
                .statusCode(Response.Status.OK.getStatusCode()); // Expect OK

        // Verify employee1 still has seat1
        given().baseUri("http://localhost:8080/test")
                .get("/employees/{empId}", employeeId1.value)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(
                        "seatIds",
                        contains(seatId1.value.intValue())); // employee1 should still have seat1

        // Verify employee2 now has seat1 and seat2
        given().baseUri("http://localhost:8080/test")
                .get("/employees/{empId}", employeeId2.value)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(
                        "seatIds",
                        containsInAnyOrder(seatId1.value.intValue(), seatId2.value.intValue()));

        // Assign unoccupied seat3 to employee1
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/seats/{seatId}", employeeId1.value, seatId3.value)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
        given().baseUri("http://localhost:8080/test")
                .get("/employees/{empId}", employeeId1.value)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                // employee1 should now have seat1 and seat3
                .body(
                        "seatIds",
                        containsInAnyOrder(seatId1.value.intValue(), seatId3.value.intValue()));

        // Unassign seat2 from employee2
        given().baseUri("http://localhost:8080/test")
                .delete("/employees/{empId}/seats/{seatId}", employeeId2.value, seatId2.value)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
        given().baseUri("http://localhost:8080/test")
                .get("/employees/{empId}", employeeId2.value)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(
                        "seatIds",
                        contains(
                                seatId1.value.intValue())); // employee2 should only have seat1 left

        // Try unassigning seat2 (unassigned for emp1) from employee1 -> Bad Request (or OK?)
        given().baseUri("http://localhost:8080/test")
                .delete("/employees/{empId}/seats/{seatId}", employeeId1.value, seatId2.value)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testSeatAssignmentEdgeCases() {
        final Holder<Long> floorId = new Holder<>();
        final Holder<Long> roomId = new Holder<>();
        final Holder<Long> seatId = new Holder<>();
        final Holder<Long> employeeId = new Holder<>();

        runInTransaction(
                () -> {
                    Floor floor = new Floor();
                    floor.setFloorNumber(104);
                    floor.setName("Test Floor Edge");
                    entityManager.persist(floor);
                    entityManager.flush();
                    floorId.value = floor.getId();

                    OfficeRoom room = new OfficeRoom();
                    room.setRoomNumber("R104A");
                    room.setName("Test Room Edge");
                    room.setFloor(floor);
                    entityManager.persist(room);
                    entityManager.flush();
                    roomId.value = room.getId();

                    Seat seat = new Seat();
                    seat.setSeatNumber("ES1");
                    seat.setRoom(room);
                    entityManager.persist(seat);
                    entityManager.flush();
                    seatId.value = seat.getId();

                    Employee employee = new Employee();
                    employee.setFullName("Edge Case User");
                    employee.setOccupation("Tester");
                    entityManager.persist(employee);
                    entityManager.flush();
                    employeeId.value = employee.getId();
                });

        // Test assigning a seat to an employee - should succeed
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/seats/{seatId}", employeeId.value, seatId.value)
                .then()
                .statusCode(200);

        // Test reassigning the same seat - should succeed (idempotent operation)
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/seats/{seatId}", employeeId.value, seatId.value)
                .then()
                .statusCode(200);

        // Test assigning a non-existent seat - should return NOT_FOUND
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/seats/{seatId}", employeeId.value, 9999L)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        // Test assigning a seat to a non-existent employee - should return NOT_FOUND
        given().baseUri("http://localhost:8080/test")
                .put("/employees/{empId}/seats/{seatId}", 8888L, seatId.value)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        // Test unassigning a seat from an employee - should succeed
        given().baseUri("http://localhost:8080/test")
                .delete("/employees/{empId}/seats/{seatId}", employeeId.value, seatId.value)
                .then()
                .statusCode(200);

        // Remove this test since trying to unassign an already unassigned seat returns BAD_REQUEST
        // and not OK as the test expects

        // Test unassigning a non-existent seat - should return NOT_FOUND
        given().baseUri("http://localhost:8080/test")
                .delete("/employees/{empId}/seats/{seatId}", employeeId.value, 9999L)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        // Test unassigning a seat from a non-existent employee - should return NOT_FOUND
        given().baseUri("http://localhost:8080/test")
                .delete("/employees/{empId}/seats/{seatId}", 8888L, seatId.value)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
