package com.officemanagement.resource;

import com.officemanagement.model.OfficeRoom;
import com.officemanagement.model.Floor;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import io.quarkus.test.junit.QuarkusTest;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import jakarta.ws.rs.core.Response;
import com.officemanagement.dto.FloorDTO;
import com.officemanagement.model.Employee;
import com.officemanagement.dto.OfficeRoomDTO;

/**
 * Integration tests for the RoomResource endpoints.
 */
@QuarkusTest
public class RoomResourceTest extends BaseResourceTest {

    @Inject
    EntityManager testEntityManager;

    @Test
    @Transactional
    public void testCreateAndGetRoom() {
        // 1. Create Floor via API
        Floor floorPayload = new Floor();
        floorPayload.setName("API Test Floor - Room");
        floorPayload.setFloorNumber(202);
        FloorDTO createdFloorDto = given()
            .contentType(ContentType.JSON)
            .body(floorPayload)
            .when().post("/floors")
            .then().statusCode(Response.Status.CREATED.getStatusCode())
            .extract().as(FloorDTO.class);
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
        OfficeRoomDTO createdRoomDto = given()
            .contentType(ContentType.JSON)
            .body(roomPayload)
        .when()
            .post("/rooms") // Use relative path
        .then()
            .statusCode(Response.Status.CREATED.getStatusCode()) // This should now pass
            .body("id", notNullValue())
            .body("name", equalTo("API Test Room"))
            .body("roomNumber", equalTo("RRoom1"))
            .body("floorId", equalTo(createdFloorDto.getId().intValue())) // Check floorId from DTO
        .extract().as(OfficeRoomDTO.class); // Extract as OfficeRoomDTO

        // 3. Get the created room to verify
        given()
        .when()
            .get("/rooms/" + createdRoomDto.getId()) // Use DTO getter for ID
        .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(createdRoomDto.getId().intValue())) // Use DTO getter for ID
            .body("name", equalTo("API Test Room"))
            .body("floor.name", equalTo("API Test Floor - Room"));
    }
    
    @Test
    public void testGetRoomNotFound() {
        given()
        .when()
            .get("/rooms/999") // Use relative path
        .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @Transactional
    public void testCreateRoomNoFloor() {
        OfficeRoom room = new OfficeRoom();
        room.setName("No Floor Room");
        room.setRoomNumber("NF1");
        // room.setFloor(null); // Floor is missing

        given()
            .contentType(ContentType.JSON)
            .body(room)
        .when()
            .post("/rooms") // Use relative path
        .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
    
    @Test
    @Transactional
    public void testCreateRoomInvalidFloor() {
        OfficeRoom room = new OfficeRoom();
        room.setName("Invalid Floor Room");
        room.setRoomNumber("IF1");
        Floor invalidFloorRef = new Floor();
        invalidFloorRef.setId(999L); // Non-existent floor ID
        room.setFloor(invalidFloorRef);

        given()
            .contentType(ContentType.JSON)
            .body(room)
        .when()
            .post("/rooms") // Use relative path
        .then()
            .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
    
    @Test
    @Transactional
    public void testCreateRoomDuplicateNumberOnFloor() {
        // API Setup: Floor
        Floor floorPayload = new Floor(); floorPayload.setName("API Floor - DupRoom"); floorPayload.setFloorNumber(208);
        FloorDTO createdFloorDto = given().contentType(ContentType.JSON).body(floorPayload).when().post("/floors").then().statusCode(Response.Status.CREATED.getStatusCode()).extract().as(FloorDTO.class);

        // Create Room 1
        OfficeRoom room1Payload = new OfficeRoom();
        room1Payload.setName("Room One");
        room1Payload.setRoomNumber("Dup1");
        Floor floorRef1 = new Floor(); floorRef1.setId(createdFloorDto.getId());
        room1Payload.setFloor(floorRef1);
        given().contentType(ContentType.JSON).body(room1Payload).when().post("/rooms").then().statusCode(Response.Status.CREATED.getStatusCode());

        // Create Room 2 (duplicate number on same floor)
        OfficeRoom room2Payload = new OfficeRoom();
        room2Payload.setName("Room Two");
        room2Payload.setRoomNumber("Dup1"); // Same number, same floor
        Floor floorRef2 = new Floor(); floorRef2.setId(createdFloorDto.getId());
        room2Payload.setFloor(floorRef2);
        given()
            .contentType(ContentType.JSON)
            .body(room2Payload)
        .when()
            .post("/rooms") // Use relative path
        .then()
            .statusCode(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    @Transactional
    public void testUpdateRoom() {
        // API Setup: Floor -> Room
        Floor floorPayload = new Floor(); floorPayload.setName("API Floor - UpdRoom"); floorPayload.setFloorNumber(209);
        FloorDTO createdFloorDto = given().contentType(ContentType.JSON).body(floorPayload).when().post("/floors").then().statusCode(Response.Status.CREATED.getStatusCode()).extract().as(FloorDTO.class);
        OfficeRoom roomPayload = new OfficeRoom(); roomPayload.setName("Original Room Name"); roomPayload.setRoomNumber("Upd1");
        Floor floorRef = new Floor(); floorRef.setId(createdFloorDto.getId()); roomPayload.setFloor(floorRef);
        // Expect OfficeRoomDTO from POST /rooms
        OfficeRoomDTO createdRoomDto = given().contentType(ContentType.JSON).body(roomPayload).when().post("/rooms").then().statusCode(Response.Status.CREATED.getStatusCode()).extract().as(OfficeRoomDTO.class);

        // Update room data
        OfficeRoom updatePayload = new OfficeRoom();
        updatePayload.setName("Updated Room Name");
        updatePayload.setRoomNumber("Upd2");
        updatePayload.setFloor(floorRef);

        given()
            .contentType(ContentType.JSON)
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
    @Transactional
    public void testDeleteRoom() {
        // API Setup: Floor -> Room
        Floor floorPayload = new Floor(); floorPayload.setName("API Floor - DelRoom"); floorPayload.setFloorNumber(210);
        FloorDTO createdFloorDto = given().contentType(ContentType.JSON).body(floorPayload).when().post("/floors").then().statusCode(Response.Status.CREATED.getStatusCode()).extract().as(FloorDTO.class);
        OfficeRoom roomPayload = new OfficeRoom(); roomPayload.setName("Delete Me"); roomPayload.setRoomNumber("Del1");
        Floor floorRef = new Floor(); floorRef.setId(createdFloorDto.getId()); roomPayload.setFloor(floorRef);
        
        // Create the initial room and extract its ID from the DTO response
        Integer createdRoomId = given()
            .contentType(ContentType.JSON)
            .body(roomPayload)
            .when().post("/rooms")
            .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body("id", notNullValue()) // Verify DTO structure briefly
            .extract().path("id");
        assertNotNull(createdRoomId, "Initial Room ID should not be null");

        // Delete the room
        given()
        .when()
            .delete("/rooms/" + createdRoomId) // Use extracted ID
        .then()
            .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        // Verify it's gone
        given()
        .when()
            .get("/rooms/" + createdRoomId) // Use extracted ID
        .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
    
    // Add tests for deleting room with seats, getting seats, etc.

} 