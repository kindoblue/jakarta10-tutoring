package com.officemanagement.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.officemanagement.dto.EmployeeDTO;
import com.officemanagement.dto.FloorDTO;
import com.officemanagement.dto.OfficeRoomDTO;
import com.officemanagement.dto.SeatDTO;
import com.officemanagement.model.Employee;
import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import com.officemanagement.model.Seat;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/** Integration tests for the SeatResource endpoints. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SeatResourceIT extends BaseResourceTest {

    // Helper class for IDs - might still be useful for API setup
    private static class Holder<T> {
        T value;
    }

    // Helper method to create a Floor via API for setup
    private Long createFloorForTest(String name, int number) {
        Floor floorPayload = new Floor();
        floorPayload.setName(name);
        floorPayload.setFloorNumber(number);
        FloorDTO createdFloorDto =
                given().contentType(ContentType.JSON)
                        .body(floorPayload)
                        .when()
                        .post("/floors")
                        .then()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .extract()
                        .as(FloorDTO.class);
        assertNotNull(createdFloorDto.getId());
        return createdFloorDto.getId();
    }

    // Helper method to create a Room via API for setup
    private Long createRoomForTest(String name, String number, Long floorId) {
        OfficeRoom roomPayload = new OfficeRoom();
        roomPayload.setName(name);
        roomPayload.setRoomNumber(number);
        Floor floorRef = new Floor();
        floorRef.setId(floorId);
        roomPayload.setFloor(floorRef);
        OfficeRoomDTO createdRoomDto =
                given().contentType(ContentType.JSON)
                        .body(roomPayload)
                        .when()
                        .post("/rooms")
                        .then()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .extract()
                        .as(OfficeRoomDTO.class);
        assertNotNull(createdRoomDto.getId());
        return createdRoomDto.getId();
    }

    // Helper method to create an Employee via API for setup
    private Long createEmployeeForTest(String name, String occupation) {
        Employee empPayload = new Employee();
        empPayload.setFullName(name);
        empPayload.setOccupation(occupation);
        EmployeeDTO createdEmpDto =
                given().contentType(ContentType.JSON)
                        .body(empPayload)
                        .when()
                        .post("/employees")
                        .then()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .extract()
                        .as(EmployeeDTO.class);
        assertNotNull(createdEmpDto.getId());
        return createdEmpDto.getId();
    }

    @Test
    public void testCreateAndGetSeat() {
        // Removed QuarkusTransaction block
        // 1. Setup Floor and Room using API helpers
        Long floorId = createFloorForTest("API Floor - SeatCG", 2010);
        Long roomId = createRoomForTest("API Room - SeatCG", "SRoomCG1", floorId);

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

    @Test // No DB interaction needed
    public void testGetSeatNotFound() {
        given().when()
                .get("/seats/999")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test // No DB interaction needed
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

    @Test // No DB interaction needed
    public void testCreateSeatInvalidRoom() {
        Seat seat = new Seat();
        seat.setSeatNumber("IR1");
        OfficeRoom invalidRoomRef = new OfficeRoom();
        invalidRoomRef.setId(999L); // Non-existent room ID
        seat.setRoom(invalidRoomRef);
        given().contentType(ContentType.JSON)
                .body(seat)
                .when()
                .post("/seats")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCreateSeatDuplicateNumberInRoom() {
        // Removed QuarkusTransaction block
        // Setup Floor and Room using API helpers
        Long floorId = createFloorForTest("API Floor - DupSeat", 2030);
        Long roomId = createRoomForTest("API Room - DupSeat", "R-DupS", floorId);

        // Create Seat 1 via API
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

        // Create Seat 2 (duplicate number in same room) via API
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
                .statusCode(Response.Status.CONFLICT.getStatusCode()); // Expect conflict
    }

    @Test
    public void testUpdateSeat() {
        // Removed QuarkusTransaction block
        // Setup Floor, Room, Seat using API helpers
        Long floorId = createFloorForTest("API Floor - UpdSeat", 2040);
        Long roomId = createRoomForTest("API Room - UpdSeat", "R-UpdS", floorId);

        Seat initialSeatPayload = new Seat();
        initialSeatPayload.setSeatNumber("OrigS1");
        OfficeRoom initialRoomRef = new OfficeRoom();
        initialRoomRef.setId(roomId);
        initialSeatPayload.setRoom(initialRoomRef);

        SeatDTO createdSeatDto =
                given().contentType(ContentType.JSON)
                        .body(initialSeatPayload)
                        .when()
                        .post("/seats")
                        .then()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .extract()
                        .as(SeatDTO.class);
        Long seatId = createdSeatDto.getId();

        // Update seat data via API
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
                .body("y", equalTo(20f)) // Added y check
                .body("rotation", equalTo(90f))
                .body("occupied", equalTo(false));
    }

    @Test
    public void testDeleteSeat() {
        // Removed QuarkusTransaction block
        // Setup Floor, Room, Seat using API helpers
        Long floorId = createFloorForTest("API Floor - DelSeat", 2050);
        Long roomId = createRoomForTest("API Room - DelSeat", "R-DelS", floorId);

        Seat seatPayload = new Seat();
        seatPayload.setSeatNumber("DelS1");
        OfficeRoom roomRef = new OfficeRoom();
        roomRef.setId(roomId);
        seatPayload.setRoom(roomRef);

        SeatDTO createdSeatDto =
                given().contentType(ContentType.JSON)
                        .body(seatPayload)
                        .when()
                        .post("/seats")
                        .then()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .extract()
                        .as(SeatDTO.class);
        Long seatId = createdSeatDto.getId();

        // Delete seat via API
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

    @Test // No DB interaction needed
    public void testCreateSeatWithInvalidData() {
        // Test missing seat number
        Seat seatNoNumber = new Seat();
        OfficeRoom dummyRoomRef = new OfficeRoom();
        dummyRoomRef.setId(1L); // Needs a room reference for validation
        seatNoNumber.setRoom(dummyRoomRef);
        given().contentType(ContentType.JSON)
                .body(seatNoNumber)
                .when()
                .post("/seats")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        // Test empty seat number (assuming validation catches this)
        Seat seatEmptyNumber = new Seat();
        seatEmptyNumber.setSeatNumber("");
        seatEmptyNumber.setRoom(dummyRoomRef);
        given().contentType(ContentType.JSON)
                .body(seatEmptyNumber)
                .when()
                .post("/seats")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        // Test missing room is handled by testCreateSeatNoRoom
    }

    @Test
    public void testDeleteSeatWithEmployeesAssigned() {
        // Removed QuarkusTransaction block
        // Setup: Floor -> Room -> Seat -> Employee -> Assign Employee to Seat
        Long floorId = createFloorForTest("API Floor - DelSeatEmp", 2060);
        Long roomId = createRoomForTest("API Room - DelSeatEmp", "R-DelSE", floorId);

        Seat seatPayload = new Seat();
        seatPayload.setSeatNumber("DelSE1");
        OfficeRoom roomRef = new OfficeRoom();
        roomRef.setId(roomId);
        seatPayload.setRoom(roomRef);
        SeatDTO createdSeatDto =
                given().contentType(ContentType.JSON)
                        .body(seatPayload)
                        .when()
                        .post("/seats")
                        .then()
                        .statusCode(201)
                        .extract()
                        .as(SeatDTO.class);
        Long seatId = createdSeatDto.getId();

        Long employeeId = createEmployeeForTest("Emp On Seat", "Tester");

        // Assign employee to seat using Employee endpoint
        given().when()
                .put("/employees/{empId}/assign-seat/{seatId}", employeeId, seatId)
                .then()
                .statusCode(200);

        // Verify seat is occupied
        given().when().get("/seats/{id}", seatId).then().body("occupied", equalTo(true));

        // Try deleting the seat - should fail (CONFLICT)
        given().when()
                .delete("/seats/" + seatId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.CONFLICT.getStatusCode());

        // Cleanup: Unassign employee first
        given().when()
                .put("/employees/{empId}/unassign-seat/{seatId}", employeeId, seatId)
                .then()
                .statusCode(200);

        // Now delete the seat - should succeed
        given().when()
                .delete("/seats/" + seatId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }
}
