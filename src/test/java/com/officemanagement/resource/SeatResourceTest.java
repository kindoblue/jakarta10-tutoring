package com.officemanagement.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.officemanagement.dto.SeatDTO;
import com.officemanagement.model.Employee;
import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import com.officemanagement.model.Seat;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

/** Integration tests for the SeatResource endpoints. */
@QuarkusTest
public class SeatResourceTest extends BaseResourceTest {

    @Inject EntityManager testEntityManager;

    // Helper class for IDs
    private static class Holder<T> {
        T value;
    }

    @Test
    public void testCreateAndGetSeat() {
        // 1. Setup Floor and Room using QuarkusTransaction
        final Holder<Long> roomIdHolder = new Holder<>();
        QuarkusTransaction.requiringNew()
                .run(
                        () -> {
                            Floor floor = new Floor();
                            floor.setName("API Test Floor - Seat");
                            floor.setFloorNumber(201);
                            testEntityManager.persist(floor);
                            OfficeRoom room = new OfficeRoom();
                            room.setName("API Test Room - Seat");
                            room.setRoomNumber("SRoom1");
                            room.setFloor(floor);
                            testEntityManager.persist(room);
                            testEntityManager.flush();
                            roomIdHolder.value = room.getId();
                        });
        Long roomId = roomIdHolder.value;
        assertNotNull(roomId);

        // 2. Create Seat via API, referencing the created Room ID
        Seat seatPayload = new Seat();
        seatPayload.setSeatNumber("A1");
        OfficeRoom roomRef = new OfficeRoom(); // Create a detached reference for the payload
        roomRef.setId(roomId);
        seatPayload.setRoom(roomRef);

        // 3. Assert POST response (expecting SeatDTO)
        SeatDTO createdSeatDto =
                given().contentType(ContentType.JSON)
                        .body(seatPayload)
                        .when()
                        .post("/seats")
                        .then()
                        .log()
                        .ifValidationFails()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .body("id", notNullValue())
                        .body("seatNumber", equalTo("A1"))
                        .body("roomId", equalTo(roomId.intValue()))
                        .body("occupied", equalTo(false))
                        .body("employeeIds", empty())
                        .extract()
                        .as(SeatDTO.class); // Extract DTO

        assertNotNull(createdSeatDto.getId());
        Long createdSeatId = createdSeatDto.getId();

        // 4. Get the created seat to verify (expecting SeatDTO)
        given().when()
                .get("/seats/" + createdSeatId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(createdSeatId.intValue()))
                .body("seatNumber", equalTo("A1"))
                .body("roomId", equalTo(roomId.intValue()))
                .body("occupied", equalTo(false))
                .body("employeeIds", empty());
    }

    @Test // No DB interaction
    public void testGetSeatNotFound() {
        given().when()
                .get("/seats/999")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test // No DB interaction
    public void testCreateSeatNoRoom() {
        Seat seat = new Seat();
        seat.setSeatNumber("NR1");
        given().contentType(ContentType.JSON)
                .body(seat)
                .when()
                .post("/seats")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test // No DB interaction
    public void testCreateSeatInvalidRoom() {
        Seat seat = new Seat();
        seat.setSeatNumber("IR1");
        OfficeRoom invalidRoomRef = new OfficeRoom();
        invalidRoomRef.setId(999L);
        seat.setRoom(invalidRoomRef);
        given().contentType(ContentType.JSON)
                .body(seat)
                .when()
                .post("/seats")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test // Needs setup transaction
    public void testCreateSeatDuplicateNumberInRoom() {
        // Setup Floor and Room
        final Holder<Long> roomIdHolder = new Holder<>();
        QuarkusTransaction.requiringNew()
                .run(
                        () -> {
                            Floor floor = new Floor();
                            floor.setName("API Test Floor - DupSeat");
                            floor.setFloorNumber(203);
                            testEntityManager.persist(floor);
                            OfficeRoom room = new OfficeRoom();
                            room.setName("API Test Room - DupSeat");
                            room.setRoomNumber("R-DupS");
                            room.setFloor(floor);
                            testEntityManager.persist(room);
                            testEntityManager.flush();
                            roomIdHolder.value = room.getId();
                        });
        Long roomId = roomIdHolder.value;
        assertNotNull(roomId);

        // Create Seat 1
        Seat seat1Payload = new Seat();
        seat1Payload.setSeatNumber("DupS1");
        OfficeRoom roomRef1 = new OfficeRoom();
        roomRef1.setId(roomId);
        seat1Payload.setRoom(roomRef1);
        given().contentType(ContentType.JSON)
                .body(seat1Payload)
                .when()
                .post("/seats")
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.CREATED.getStatusCode());

        // Create Seat 2 (duplicate number in same room)
        Seat seat2Payload = new Seat();
        seat2Payload.setSeatNumber("DupS1"); // Same number
        OfficeRoom roomRef2 = new OfficeRoom();
        roomRef2.setId(roomId);
        seat2Payload.setRoom(roomRef2);
        given().contentType(ContentType.JSON)
                .body(seat2Payload)
                .when()
                .post("/seats")
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.CONFLICT.getStatusCode());
    }

    @Test // Needs setup transaction
    public void testUpdateSeat() {
        // Setup Floor, Room, Seat
        final Holder<Long> roomIdHolder = new Holder<>();
        final Holder<Long> seatIdHolder = new Holder<>();
        QuarkusTransaction.run(
                () -> {
                    Floor floor = new Floor();
                    floor.setName("API Floor - UpdSeat");
                    floor.setFloorNumber(204);
                    testEntityManager.persist(floor);
                    OfficeRoom room = new OfficeRoom();
                    room.setName("API Room - UpdSeat");
                    room.setRoomNumber("R-UpdS");
                    room.setFloor(floor);
                    testEntityManager.persist(room);
                    Seat seat = new Seat();
                    seat.setSeatNumber("OrigS1");
                    seat.setRoom(room);
                    testEntityManager.persist(seat);
                    testEntityManager.flush();
                    roomIdHolder.value = room.getId();
                    seatIdHolder.value = seat.getId();
                });
        Long roomId = roomIdHolder.value;
        Long seatId = seatIdHolder.value;
        assertNotNull(roomId);
        assertNotNull(seatId);

        // Update seat data
        Seat updatePayload = new Seat();
        updatePayload.setSeatNumber("UpdatedS1");
        updatePayload.setX(10f);
        updatePayload.setY(20f);
        updatePayload.setRotation(90f);
        OfficeRoom roomRef = new OfficeRoom();
        roomRef.setId(roomId); // Reference existing room
        updatePayload.setRoom(roomRef);

        // Assert PUT response (expecting SeatDTO)
        given().contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/seats/" + seatId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(seatId.intValue()))
                .body("seatNumber", equalTo("UpdatedS1"))
                .body("roomId", equalTo(roomId.intValue()))
                .body("x", equalTo(10f))
                .body("rotation", equalTo(90f))
                .body("occupied", equalTo(false));
    }

    @Test // Needs setup transaction
    public void testDeleteSeat() {
        // Setup Floor, Room, Seat
        final Holder<Long> seatIdHolder = new Holder<>();
        QuarkusTransaction.requiringNew()
                .run(
                        () -> {
                            Floor floor = new Floor();
                            floor.setName("API Floor - DelSeat");
                            floor.setFloorNumber(205);
                            testEntityManager.persist(floor);
                            OfficeRoom room = new OfficeRoom();
                            room.setName("API Room - DelSeat");
                            room.setRoomNumber("R-DelS");
                            room.setFloor(floor);
                            testEntityManager.persist(room);
                            Seat seat = new Seat();
                            seat.setSeatNumber("DeleteS1");
                            seat.setRoom(room);
                            testEntityManager.persist(seat);
                            testEntityManager.flush();
                            seatIdHolder.value = seat.getId();
                        });
        Long seatId = seatIdHolder.value;
        assertNotNull(seatId);

        // Delete the seat
        given().when()
                .delete("/seats/" + seatId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        // Verify it's gone
        given().when()
                .get("/seats/" + seatId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test // No DB interaction
    public void testCreateSeatWithInvalidData() {
        Seat seat = new Seat(); // Missing seat number
        OfficeRoom roomRef = new OfficeRoom();
        roomRef.setId(1L); // Assume room 1 exists or use valid ID
        seat.setRoom(roomRef);
        given().contentType(ContentType.JSON)
                .body(seat)
                .when()
                .post("/seats")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        Seat seat2 = new Seat();
        seat2.setSeatNumber("ValidNum"); // Missing room
        given().contentType(ContentType.JSON)
                .body(seat2)
                .when()
                .post("/seats")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test // Needs setup transaction
    public void testDeleteSeatWithEmployeesAssigned() {
        // Setup Floor, Room, Seat, Employee, Assignment
        final Holder<Long> seatIdHolder = new Holder<>();
        QuarkusTransaction.requiringNew()
                .run(
                        () -> {
                            Floor floor = new Floor();
                            floor.setName("API Floor - DelAssignSeat");
                            floor.setFloorNumber(206);
                            testEntityManager.persist(floor);
                            OfficeRoom room = new OfficeRoom();
                            room.setName("API Room - DelAssignSeat");
                            room.setRoomNumber("R-DelAS");
                            room.setFloor(floor);
                            testEntityManager.persist(room);
                            Seat seat = new Seat();
                            seat.setSeatNumber("DelS-Assign");
                            seat.setRoom(room);
                            testEntityManager.persist(seat);
                            Employee employee = new Employee();
                            employee.setFullName("Assigned Emp");
                            employee.setOccupation("Worker");
                            testEntityManager.persist(employee);
                            testEntityManager.flush(); // Flush to get IDs
                            // Fetch the managed employee to associate the seat
                            Employee managedEmployee =
                                    testEntityManager.find(Employee.class, employee.getId());
                            Seat managedSeat = testEntityManager.find(Seat.class, seat.getId());
                            if (managedEmployee != null && managedSeat != null) {
                                managedEmployee.addSeat(
                                        managedSeat); // Associate employee with seat
                                testEntityManager.merge(managedEmployee); // Persist association
                            }
                            testEntityManager.flush();
                            seatIdHolder.value = seat.getId();
                        });
        Long seatId = seatIdHolder.value;
        assertNotNull(seatId);

        // Attempt to delete the assigned seat (should fail)
        given().when()
                .delete("/seats/" + seatId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.CONFLICT.getStatusCode());
    }
}
