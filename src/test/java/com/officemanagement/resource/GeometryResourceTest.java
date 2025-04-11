package com.officemanagement.resource;

import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import com.officemanagement.model.Seat;
import com.officemanagement.dto.OfficeRoomDTO;
import com.officemanagement.dto.SeatDTO;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import io.quarkus.test.junit.QuarkusTest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests specifically for the geometry PATCH endpoints in RoomResource.
 */
@QuarkusTest
public class GeometryResourceTest extends BaseResourceTest {

    @Inject
    EntityManager testEntityManager;

    private static class SetupResult {
        Long floorId;
        Long roomId;
        Long seatId;
        Float initialRoomX;
        Float initialRoomY;
        Float initialSeatX;
        Float initialSeatY;
    }

    SetupResult createTestRoomWithSeat() {
        return QuarkusTransaction.call(() -> {
            Floor floor = new Floor();
            floor.setName("Geometry Test Floor");
            floor.setFloorNumber(100);
            testEntityManager.persist(floor);

            OfficeRoom room = new OfficeRoom();
            room.setName("Geometry Test Room");
            room.setRoomNumber("G101");
            room.setFloor(floor);
            room.setX(1.0f);
            room.setY(2.0f);
            testEntityManager.persist(room);

            Seat seat = new Seat();
            seat.setSeatNumber("GS1");
            seat.setRoom(room);
            seat.setX(0.5f);
            seat.setY(0.6f);
            testEntityManager.persist(seat);
            
            testEntityManager.flush();

            SetupResult result = new SetupResult();
            result.floorId = floor.getId();
            result.roomId = room.getId();
            result.seatId = seat.getId();
            result.initialRoomX = room.getX();
            result.initialRoomY = room.getY();
            result.initialSeatX = seat.getX();
            result.initialSeatY = seat.getY();
            
            assertNotNull(result.floorId, "Floor ID missing");
            assertNotNull(result.roomId, "Room ID missing");
            assertNotNull(result.seatId, "Seat ID missing");
            return result;
        });
    }

    @Test
    public void testUpdateRoomGeometry() {
        SetupResult setup = createTestRoomWithSeat();
        Long roomId = setup.roomId;

        Map<String, Object> geometryUpdate = new HashMap<>();
        geometryUpdate.put("x", 10.5f);
        geometryUpdate.put("y", 20.2f);
        geometryUpdate.put("width", 150.0f);
        geometryUpdate.put("height", 120.7f);

        given()
            .contentType(ContentType.JSON)
            .body(geometryUpdate)
        .when()
            .patch("/rooms/" + roomId + "/geometry")
        .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(roomId.intValue()))
            .body("x", equalTo(10.5f))
            .body("y", equalTo(20.2f))
            .body("width", equalTo(150.0f))
            .body("height", equalTo(120.7f));
    }
    
    @Test
    public void testUpdateRoomGeometryPartial() {
        SetupResult setup = createTestRoomWithSeat();
        Long roomId = setup.roomId;
        Float originalX = setup.initialRoomX;
        assertNotNull(originalX, "Original X should exist");

        Map<String, Object> geometryUpdate = new HashMap<>();
        geometryUpdate.put("width", 180.0f);

        given()
            .contentType(ContentType.JSON)
            .body(geometryUpdate)
        .when()
            .patch("/rooms/" + roomId + "/geometry")
        .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(roomId.intValue()))
            .body("x", equalTo(originalX))
            .body("width", equalTo(180.0f));
    }
    
    @Test
    public void testUpdateSeatGeometry() {
        SetupResult setup = createTestRoomWithSeat();
        Long roomId = setup.roomId;
        Long seatId = setup.seatId;

        Map<String, Object> geometryUpdate = new HashMap<>();
        geometryUpdate.put("x", 5.1f);
        geometryUpdate.put("y", 6.2f);
        geometryUpdate.put("width", 50.0f);
        geometryUpdate.put("height", 55.5f);
        geometryUpdate.put("rotation", 45.0f);

        given()
            .contentType(ContentType.JSON)
            .body(geometryUpdate)
        .when()
            .patch("/rooms/" + roomId + "/seats/" + seatId + "/geometry")
        .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(seatId.intValue()))
            .body("roomId", equalTo(roomId.intValue()))
            .body("x", equalTo(5.1f))
            .body("y", equalTo(6.2f))
            .body("width", equalTo(50.0f))
            .body("height", equalTo(55.5f))
            .body("rotation", equalTo(45.0f))
            .body("occupied", equalTo(false));
    }

    @Test
    public void testUpdateSeatGeometryPartial() {
        SetupResult setup = createTestRoomWithSeat();
        Long roomId = setup.roomId;
        Long seatId = setup.seatId;
        Float originalY = setup.initialSeatY;
        assertNotNull(originalY, "Original Y should exist");

        Map<String, Object> geometryUpdate = new HashMap<>();
        geometryUpdate.put("rotation", 90.0f);

        given()
            .contentType(ContentType.JSON)
            .body(geometryUpdate)
        .when()
            .patch("/rooms/" + roomId + "/seats/" + seatId + "/geometry")
        .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(seatId.intValue()))
            .body("roomId", equalTo(roomId.intValue()))
            .body("y", equalTo(originalY))
            .body("rotation", equalTo(90.0f));
    }

    @Test
    public void testUpdateRoomGeometryNotFound() {
        Map<String, Object> geometryUpdate = new HashMap<>();
        geometryUpdate.put("x", 10.0f);

        given()
            .contentType(ContentType.JSON)
            .body(geometryUpdate)
        .when()
            .patch("/rooms/999/geometry") // Use relative path
        .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
    
    @Test
    public void testUpdateSeatGeometryNotFoundSeat() {
        SetupResult result = createTestRoomWithSeat();
        Long roomId = result.roomId;

        Map<String, Object> geometryUpdate = new HashMap<>();
        geometryUpdate.put("x", 10.0f);

        given()
            .contentType(ContentType.JSON)
            .body(geometryUpdate)
        .when()
            .patch("/rooms/" + roomId + "/seats/999/geometry") // Use relative path
        .then()
            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
    
    @Test
    public void testUpdateSeatGeometryWrongRoom() {
        SetupResult setup1 = createTestRoomWithSeat(); // Creates room1, floor1, seat1
        SetupResult setup2 = createTestRoomWithSeat(); // Creates room2, floor2, seat2 (IDs will be different)
        Long room1Id = setup1.roomId;
        Long seat2Id = setup2.seatId; // Use ID from setup result
        assertNotNull(room1Id); assertNotNull(seat2Id);
        assertNotEquals(setup1.roomId, setup2.roomId, "Room IDs should be different");

        Map<String, Object> geometryUpdate = new HashMap<>();
        geometryUpdate.put("x", 10.0f);

        // Try to update seat2 using room1's ID in the path (should fail)
        given().contentType(ContentType.JSON).body(geometryUpdate)
            .when().patch("/rooms/" + room1Id + "/seats/" + seat2Id + "/geometry") 
            .then().log().ifValidationFails()
            // Expect NOT_FOUND because seat2 is not in room1
            .statusCode(Response.Status.NOT_FOUND.getStatusCode()); 
    }
} 