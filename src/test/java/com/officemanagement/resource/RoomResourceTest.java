package com.officemanagement.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.officemanagement.dto.FloorDTO;
import com.officemanagement.dto.OfficeRoomDTO;
import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

/** Integration tests for the RoomResource endpoints. */
public class RoomResourceTest extends BaseResourceTest {

    @Test
    public void testCreateAndGetRoom() {
        // 1. Create Floor via API
        Floor floorPayload = new Floor();
        floorPayload.setName("API Test Floor - Room");
        floorPayload.setFloorNumber(2020); // Using unique number for test isolation
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

        // 2. Create Room via API, referencing the created Floor
        OfficeRoom roomPayload = new OfficeRoom();
        roomPayload.setName("API Test Room");
        roomPayload.setRoomNumber("RRoom1");
        // Reference floor only by ID for the request
        Floor floorRef = new Floor();
        floorRef.setId(createdFloorDto.getId());
        roomPayload.setFloor(floorRef);

        // Expect OfficeRoomDTO from POST /rooms
        OfficeRoomDTO createdRoomDto =
                given().contentType(ContentType.JSON)
                        .body(roomPayload)
                        .when()
                        .post("/rooms") // Use relative path
                        .then()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .body("id", notNullValue())
                        .body("name", equalTo("API Test Room"))
                        .body("roomNumber", equalTo("RRoom1"))
                        .body("floorId", equalTo(createdFloorDto.getId().intValue()))
                        .extract()
                        .as(OfficeRoomDTO.class); // Extract as OfficeRoomDTO

        // 3. Get the created room to verify
        given().when()
                .get("/rooms/" + createdRoomDto.getId()) // Use DTO getter for ID
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(createdRoomDto.getId().intValue())) // Use DTO getter for ID
                .body("name", equalTo("API Test Room"))
                // Assuming GET /rooms/{id} might not populate the full Floor object by default
                // Adjust assertion if it does include floor details
                // .body("floor.name", equalTo("API Test Floor - Room"))
                .body("floorId", equalTo(createdFloorDto.getId().intValue()));
    }

    @Test
    public void testGetRoomNotFound() {
        given().when()
                .get("/rooms/999") // Use relative path, assuming 999 not found
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testCreateRoomNoFloor() {
        OfficeRoom room = new OfficeRoom();
        room.setName("No Floor Room");
        room.setRoomNumber("NF1");
        // room.setFloor(null); // Floor is missing

        given().contentType(ContentType.JSON)
                .body(room)
                .when()
                .post("/rooms") // Use relative path
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCreateRoomInvalidFloor() {
        OfficeRoom room = new OfficeRoom();
        room.setName("Invalid Floor Room");
        room.setRoomNumber("IF1");
        Floor invalidFloorRef = new Floor();
        invalidFloorRef.setId(999L); // Non-existent floor ID
        room.setFloor(invalidFloorRef);

        given().contentType(ContentType.JSON)
                .body(room)
                .when()
                .post("/rooms") // Use relative path
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCreateRoomDuplicateNumberOnFloor() {
        // API Setup: Floor
        Floor floorPayload = new Floor();
        floorPayload.setName("API Floor - DupRoom");
        floorPayload.setFloorNumber(2080); // Unique floor number
        FloorDTO createdFloorDto =
                given().contentType(ContentType.JSON)
                        .body(floorPayload)
                        .when()
                        .post("/floors")
                        .then()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .extract()
                        .as(FloorDTO.class);

        // Create Room 1
        OfficeRoom room1Payload = new OfficeRoom();
        room1Payload.setName("Room One");
        room1Payload.setRoomNumber("Dup1");
        Floor floorRef1 = new Floor();
        floorRef1.setId(createdFloorDto.getId());
        room1Payload.setFloor(floorRef1);
        given().contentType(ContentType.JSON)
                .body(room1Payload)
                .when()
                .post("/rooms")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());

        // Create Room 2 (duplicate number on same floor)
        OfficeRoom room2Payload = new OfficeRoom();
        room2Payload.setName("Room Two");
        room2Payload.setRoomNumber("Dup1"); // Same number, same floor
        Floor floorRef2 = new Floor();
        floorRef2.setId(createdFloorDto.getId());
        room2Payload.setFloor(floorRef2);
        given().contentType(ContentType.JSON)
                .body(room2Payload)
                .when()
                .post("/rooms") // Use relative path
                .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testUpdateRoom() {
        // API Setup: Floor -> Room
        Floor floorPayload = new Floor();
        floorPayload.setName("API Floor - UpdRoom");
        floorPayload.setFloorNumber(2090); // Unique floor number
        FloorDTO createdFloorDto =
                given().contentType(ContentType.JSON)
                        .body(floorPayload)
                        .when()
                        .post("/floors")
                        .then()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .extract()
                        .as(FloorDTO.class);
        OfficeRoom roomPayload = new OfficeRoom();
        roomPayload.setName("Original Room Name");
        roomPayload.setRoomNumber("Upd1");
        Floor floorRef = new Floor();
        floorRef.setId(createdFloorDto.getId());
        roomPayload.setFloor(floorRef);
        // Expect OfficeRoomDTO from POST /rooms
        OfficeRoomDTO createdRoomDto =
                given().contentType(ContentType.JSON)
                        .body(roomPayload)
                        .when()
                        .post("/rooms")
                        .then()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .extract()
                        .as(OfficeRoomDTO.class);

        // Update room data
        OfficeRoom updatePayload = new OfficeRoom();
        updatePayload.setName("Updated Room Name");
        updatePayload.setRoomNumber("Upd2");
        updatePayload.setFloor(floorRef); // Keep the same floor

        given().contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/rooms/" + createdRoomDto.getId()) // Use DTO getter for ID
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(createdRoomDto.getId().intValue()))
                .body("name", equalTo("Updated Room Name"))
                .body("roomNumber", equalTo("Upd2"));
    }

    @Test
    public void testDeleteRoom() {
        // API Setup: Floor -> Room
        Floor floorPayload = new Floor();
        floorPayload.setName("API Floor - DelRoom");
        floorPayload.setFloorNumber(2100); // Unique floor number
        FloorDTO createdFloorDto =
                given().contentType(ContentType.JSON)
                        .body(floorPayload)
                        .when()
                        .post("/floors")
                        .then()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .extract()
                        .as(FloorDTO.class);
        OfficeRoom roomPayload = new OfficeRoom();
        roomPayload.setName("Delete Me");
        roomPayload.setRoomNumber("Del1");
        Floor floorRef = new Floor();
        floorRef.setId(createdFloorDto.getId());
        roomPayload.setFloor(floorRef);

        // Create the initial room and extract its ID from the DTO response
        OfficeRoomDTO createdRoomDto =
                given().contentType(ContentType.JSON)
                        .body(roomPayload)
                        .when()
                        .post("/rooms")
                        .then()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .extract()
                        .as(OfficeRoomDTO.class);
        Long createdRoomId = createdRoomDto.getId();
        assertNotNull(createdRoomId);

        // Delete the room
        given().when()
                .delete("/rooms/" + createdRoomId)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        // Verify it's gone
        given().when()
                .get("/rooms/" + createdRoomId)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testUpdateRoomNotFound() {
        OfficeRoom updatePayload = new OfficeRoom();
        updatePayload.setName("Updated Room Name");
        updatePayload.setRoomNumber("UpdNotFound");
        // Floor is needed for valid payload, but the room ID is the target
        Floor dummyFloor = new Floor();
        dummyFloor.setId(
                1L); // Assume some floor ID might exist, or use one created in another test if
        // state persists
        updatePayload.setFloor(dummyFloor);

        given().contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/rooms/9999") // Non-existent ID
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testDeleteRoomNotFound() {
        given().when()
                .delete("/rooms/9999") // Non-existent ID
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
