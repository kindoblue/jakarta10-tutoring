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

/** Integration tests for the StatsResource endpoint. */
public class StatsResourceIT extends BaseResourceTest {

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
        assertNotNull(createdFloorDto.getId(), "Floor creation failed in helper");
        return createdFloorDto.getId();
    }

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
        assertNotNull(createdRoomDto.getId(), "Room creation failed in helper");
        return createdRoomDto.getId();
    }

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
        assertNotNull(createdEmpDto.getId(), "Employee creation failed in helper");
        return createdEmpDto.getId();
    }

    private Long createSeatForTest(String number, Long roomId) {
        Seat seatPayload = new Seat();
        seatPayload.setSeatNumber(number);
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
        assertNotNull(createdSeatDto.getId(), "Seat creation failed in helper");
        return createdSeatDto.getId();
    }

    @Test
    public void testGetStats() {
        // NOTE: Assumes a clean state before this test. Proper cleanup (@AfterEach or similar)
        // might be needed if tests interfere.

        // Verify initial state (might not be zero if other tests ran)
        // It's better to check relative changes after setup.
        /*
        given().when()
                .get("/stats") // Use relative path
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("totalEmployees", equalTo(0))
                .body("totalFloors", equalTo(0))
                .body("totalOffices", equalTo(0))
                .body("totalSeats", equalTo(0));
        */

        // Get initial stats using jsonPath()
        int initialFloors =
                given().contentType(ContentType.JSON)
                        .when()
                        .get("/stats")
                        .then()
                        .extract()
                        .jsonPath()
                        .getInt("totalFloors");
        int initialRooms =
                given().contentType(ContentType.JSON)
                        .when()
                        .get("/stats")
                        .then()
                        .extract()
                        .jsonPath()
                        .getInt("totalOffices");
        int initialSeats =
                given().contentType(ContentType.JSON)
                        .when()
                        .get("/stats")
                        .then()
                        .extract()
                        .jsonPath()
                        .getInt("totalSeats");
        int initialEmployees =
                given().contentType(ContentType.JSON)
                        .when()
                        .get("/stats")
                        .then()
                        .extract()
                        .jsonPath()
                        .getInt("totalEmployees");

        // Add data using API helpers
        Long floorId = createFloorForTest("Stats Floor API", 3010);
        Long roomId = createRoomForTest("Stats Room API", "StatR1API", floorId);
        Long seatId = createSeatForTest("StatS1API", roomId);
        Long employeeId = createEmployeeForTest("Stats Employee API", "Stat Worker API");

        // Assign employee to seat (optional for basic count check, but good practice)
        given().when()
                .put("/employees/{empId}/assign-seat/{seatId}", employeeId, seatId)
                .then()
                .statusCode(200);

        // Verify updated stats (check increments from initial state)
        given().when()
                .get("/stats") // Use relative path
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("totalFloors", equalTo(initialFloors + 1))
                .body("totalOffices", equalTo(initialRooms + 1))
                .body("totalSeats", equalTo(initialSeats + 1))
                .body("totalEmployees", equalTo(initialEmployees + 1));
    }
}
