package com.officemanagement.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.officemanagement.dto.FloorDTO;
import com.officemanagement.dto.OfficeRoomDTO;
import com.officemanagement.dto.SeatDTO;
import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import com.officemanagement.model.Seat;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

/** Integration tests specifically for the geometry PATCH endpoints in RoomResource. */
public class GeometryResourceIT extends BaseResourceTest {

    private static final Logger log = Logger.getLogger(GeometryResourceIT.class.getName());

    private Long createFloorForTest(String name, int number) {
        Floor floorPayload = new Floor();
        floorPayload.setName(name);
        floorPayload.setFloorNumber(number);
        FloorDTO createdFloorDto =
                given().contentType(ContentType.JSON)
                        .baseUri("http://localhost:8080/test")
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

    private Long createRoomForTest(String name, String number, Long floorId, Float x, Float y) {
        OfficeRoom roomPayload = new OfficeRoom();
        roomPayload.setName(name);
        roomPayload.setRoomNumber(number);
        roomPayload.setX(x);
        roomPayload.setY(y);
        Floor floorRef = new Floor();
        floorRef.setId(floorId);
        roomPayload.setFloor(floorRef);
        OfficeRoomDTO createdRoomDto =
                given().contentType(ContentType.JSON)
                        .baseUri("http://localhost:8080/test")
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

    private Long createSeatForTest(String number, Long roomId, Float x, Float y) {
        Seat seatPayload = new Seat();
        seatPayload.setSeatNumber(number);
        seatPayload.setX(x);
        seatPayload.setY(y);
        OfficeRoom roomRef = new OfficeRoom();
        roomRef.setId(roomId);
        seatPayload.setRoom(roomRef);
        SeatDTO createdSeatDto =
                given().contentType(ContentType.JSON)
                        .baseUri("http://localhost:8080/test")
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

    private static class SetupResult {
        Long floorId;
        Long roomId;
        Long seatId;
        Float initialRoomX = 1.0f;
        Float initialRoomY = 2.0f;
        Float initialSeatX = 0.5f;
        Float initialSeatY = 0.6f;
    }

    SetupResult createTestRoomWithSeat() {
        SetupResult result = new SetupResult();
        result.floorId =
                createFloorForTest("Geometry Test Floor API", 1000 + (int) (Math.random() * 100));
        result.roomId =
                createRoomForTest(
                        "Geometry Test Room API",
                        "G101API",
                        result.floorId,
                        result.initialRoomX,
                        result.initialRoomY);
        result.seatId =
                createSeatForTest(
                        "GS1API", result.roomId, result.initialSeatX, result.initialSeatY);

        assertNotNull(result.floorId, "Floor ID missing after API setup");
        assertNotNull(result.roomId, "Room ID missing after API setup");
        assertNotNull(result.seatId, "Seat ID missing after API setup");
        return result;
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

        given().contentType(ContentType.JSON)
                .baseUri("http://localhost:8080/test")
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
        assertNotNull(originalX, "Original X should exist from setup");

        Map<String, Object> geometryUpdate = new HashMap<>();
        geometryUpdate.put("width", 180.0f);

        given().contentType(ContentType.JSON)
                .baseUri("http://localhost:8080/test")
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

        given().contentType(ContentType.JSON)
                .baseUri("http://localhost:8080/test")
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
        assertNotNull(originalY, "Original Y should exist from setup");

        Map<String, Object> geometryUpdate = new HashMap<>();
        geometryUpdate.put("rotation", 90.0f);

        given().contentType(ContentType.JSON)
                .baseUri("http://localhost:8080/test")
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

        given().contentType(ContentType.JSON)
                .baseUri("http://localhost:8080/test")
                .body(geometryUpdate)
                .when()
                .patch("/rooms/999/geometry")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testUpdateSeatGeometryNotFoundSeat() {
        Long floorId = createFloorForTest("Geo Floor NoSeat", 1011);
        Long roomId = createRoomForTest("Geo Room NoSeat", "G-NS", floorId, 1f, 1f);

        Map<String, Object> geometryUpdate = new HashMap<>();
        geometryUpdate.put("x", 10.0f);

        given().contentType(ContentType.JSON)
                .baseUri("http://localhost:8080/test")
                .body(geometryUpdate)
                .when()
                .patch("/rooms/" + roomId + "/seats/999/geometry")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testUpdateSeatGeometryWrongRoom() {
        SetupResult setup1 = createTestRoomWithSeat();
        SetupResult setup2 = createTestRoomWithSeat();
        Long room1Id = setup1.roomId;
        Long seat2Id = setup2.seatId;
        assertNotNull(room1Id);
        assertNotNull(seat2Id);
        assertNotEquals(setup1.roomId, setup2.roomId, "Room IDs should be different");
        assertNotEquals(setup1.seatId, setup2.seatId, "Seat IDs should be different");

        Map<String, Object> geometryUpdate = new HashMap<>();
        geometryUpdate.put("x", 10.0f);

        given().contentType(ContentType.JSON)
                .baseUri("http://localhost:8080/test")
                .body(geometryUpdate)
                .when()
                .patch("/rooms/" + room1Id + "/seats/" + seat2Id + "/geometry")
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
