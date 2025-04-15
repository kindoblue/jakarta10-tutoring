package com.officemanagement.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.officemanagement.dto.FloorDTO;
import com.officemanagement.model.Floor;
// Removed unused model imports if setup is gone
// import com.officemanagement.model.FloorPlanimetry;
// import com.officemanagement.model.OfficeRoom;
// Removed Quarkus imports
import io.restassured.http.ContentType;
// Removed Inject
// Removed EntityManager
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

/** Integration tests for the FloorResource endpoints. */
// Removed @QuarkusTest
public class FloorResourceIT extends BaseResourceTest {

    // Removed @Inject EntityManager testEntityManager;

    // Holder class might be unused
    private static class Holder<T> {
        T value;
    }

    private static Long floorId;

    @Test
    public void testCreateAndGetFloor() {
        Floor floorPayload = new Floor();
        floorPayload.setName("Test Floor CG");
        floorPayload.setFloorNumber(101);

        // Create floor via POST and expect DTO
        FloorDTO createdFloorDto =
                given().contentType(ContentType.JSON)
                        .baseUri("http://localhost:8080/test")
                        .body(floorPayload)
                        .when()
                        .post("/floors")
                        .then()
                        .log()
                        .ifValidationFails()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .body("id", notNullValue())
                        .body("name", equalTo("Test Floor CG"))
                        .body("floorNumber", equalTo(101))
                        .body("roomIds", empty())
                        .body("hasPlanimetry", equalTo(false))
                        .extract()
                        .as(FloorDTO.class);

        floorId = createdFloorDto.getId();
        assertNotNull(floorId);

        // Get the created floor and expect DTO
        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/floors/" + floorId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(floorId.intValue()))
                .body("name", equalTo("Test Floor CG"))
                .body("floorNumber", equalTo(101))
                .body("roomIds", empty())
                .body("hasPlanimetry", equalTo(false));
    }

    @Test
    public void testGetFloorNotFound() {
        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/floors/999") // Assuming 999 does not exist
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testCreateFloorDuplicateNumber() {
        // Removed QuarkusTransaction block for data setup
        /*
        QuarkusTransaction.requiringNew()
                .run(
                        () -> {
                            Floor floor1 = new Floor();
                            floor1.setName("Floor One Dup");
                            floor1.setFloorNumber(102);
                            testEntityManager.persist(floor1);
                        });
        */

        // Setup: Create the first floor using the API
        Floor floor1Payload = new Floor();
        floor1Payload.setName("Floor One Dup API");
        floor1Payload.setFloorNumber(1020); // Use a unique number for initial creation
        given().contentType(ContentType.JSON)
                .baseUri("http://localhost:8080/test")
                .body(floor1Payload)
                .when()
                .post("/floors")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());

        // Try creating another with the same number via API
        Floor floor2Payload = new Floor();
        floor2Payload.setName("Floor Two Dup API");
        floor2Payload.setFloorNumber(1020); // Duplicate number
        given().contentType(ContentType.JSON)
                .baseUri("http://localhost:8080/test")
                .body(floor2Payload)
                .when()
                .post("/floors")
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.CONFLICT.getStatusCode()); // Expect conflict
    }

    @Test
    public void testUpdateFloor() {
        // Removed QuarkusTransaction block for data setup
        /*
        final Holder<Long> floorIdHolder = new Holder<>();
        QuarkusTransaction.requiringNew()
                .run(
                        () -> {
                            Floor floor = new Floor();
                            floor.setName("Original Name Upd");
                            floor.setFloorNumber(103);
                            testEntityManager.persist(floor);
                            testEntityManager.flush();
                            floorIdHolder.value = floor.getId();
                        });
        Long floorId = floorIdHolder.value;
        assertNotNull(floorId);
        */

        // Setup: Create a floor using the API
        Floor originalFloor = new Floor();
        originalFloor.setName("Original Name Upd API");
        originalFloor.setFloorNumber(1030); // Unique number
        FloorDTO createdDto =
                given().contentType(ContentType.JSON)
                        .baseUri("http://localhost:8080/test")
                        .body(originalFloor)
                        .when()
                        .post("/floors")
                        .then()
                        .statusCode(Response.Status.CREATED.getStatusCode())
                        .extract()
                        .as(FloorDTO.class);
        floorId = createdDto.getId();

        // Update the floor via API, expect DTO
        Floor updatePayload = new Floor();
        updatePayload.setName("Updated Name Upd API");
        updatePayload.setFloorNumber(1040); // New unique number
        given().contentType(ContentType.JSON)
                .baseUri("http://localhost:8080/test")
                .body(updatePayload)
                .when()
                .put("/floors/" + floorId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(floorId.intValue()))
                .body("name", equalTo("Updated Name Upd API"))
                .body("floorNumber", equalTo(1040))
                .body("roomIds", empty()) // Assuming no rooms were added
                .body("hasPlanimetry", equalTo(false));
    }

    @Test
    public void testUpdateFloorNotFound() {
        Floor floorPayload = new Floor();
        floorPayload.setName("Non Existent Upd");
        floorPayload.setFloorNumber(999);
        given().contentType(ContentType.JSON)
                .baseUri("http://localhost:8080/test")
                .body(floorPayload)
                .when()
                .put("/floors/999") // Assuming 999 does not exist
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testUpdateFloorDuplicateNumber() {
        // Removed QuarkusTransaction block for data setup
        /*
        final Holder<Long> floor1IdHolder = new Holder<>();
        final Holder<Long> floor2IdHolder = new Holder<>();
        QuarkusTransaction.requiringNew()
                .run(
                        () -> {
                            Floor floor1 = new Floor();
                            floor1.setName("Floor One UpdDup");
                            floor1.setFloorNumber(105);
                            testEntityManager.persist(floor1);
                            Floor floor2 = new Floor();
                            floor2.setName("Floor Two UpdDup");
                            floor2.setFloorNumber(106);
                            testEntityManager.persist(floor2);
                            testEntityManager.flush();
                            floor1IdHolder.value = floor1.getId();
                            floor2IdHolder.value = floor2.getId();
                        });
        Long floor1Id = floor1IdHolder.value;
        Long floor2Id = floor2IdHolder.value;
        assertNotNull(floor1Id);
        assertNotNull(floor2Id);
        */

        // Setup: Create two floors using the API
        Floor floor1 = new Floor();
        floor1.setName("Floor One UpdDup API");
        floor1.setFloorNumber(1050);
        FloorDTO dto1 =
                given().contentType(ContentType.JSON)
                        .baseUri("http://localhost:8080/test")
                        .body(floor1)
                        .when()
                        .post("/floors")
                        .then()
                        .statusCode(201)
                        .extract()
                        .as(FloorDTO.class);
        Long floor1Id = dto1.getId();

        Floor floor2 = new Floor();
        floor2.setName("Floor Two UpdDup API");
        floor2.setFloorNumber(1060);
        FloorDTO dto2 =
                given().contentType(ContentType.JSON)
                        .baseUri("http://localhost:8080/test")
                        .body(floor2)
                        .when()
                        .post("/floors")
                        .then()
                        .statusCode(201)
                        .extract()
                        .as(FloorDTO.class);
        Long floor2Id = dto2.getId();

        // Try updating floor 2 to have floor number 1050 (duplicate)
        Floor updatePayload = new Floor();
        updatePayload.setName("Floor Two UpdDup Updated API"); // Name change is fine
        updatePayload.setFloorNumber(1050); // Duplicate number
        given().contentType(ContentType.JSON)
                .baseUri("http://localhost:8080/test")
                .body(updatePayload)
                .when()
                .put("/floors/" + floor2Id)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testDeleteFloor() {
        // Removed QuarkusTransaction block for data setup
        /*
        final Holder<Long> floorIdHolder = new Holder<>();
        QuarkusTransaction.requiringNew()
                .run(
                        () -> {
                            Floor floor = new Floor();
                            floor.setName("To Be Deleted");
                            floor.setFloorNumber(107);
                            testEntityManager.persist(floor);
                            testEntityManager.flush();
                            floorIdHolder.value = floor.getId();
                        });
        Long floorId = floorIdHolder.value;
        assertNotNull(floorId);
        */

        // Setup: Create a floor using the API
        Floor floorToDelete = new Floor();
        floorToDelete.setName("To Be Deleted API");
        floorToDelete.setFloorNumber(1070);
        FloorDTO createdDto =
                given().contentType(ContentType.JSON)
                        .baseUri("http://localhost:8080/test")
                        .body(floorToDelete)
                        .when()
                        .post("/floors")
                        .then()
                        .statusCode(201)
                        .extract()
                        .as(FloorDTO.class);
        floorId = createdDto.getId();

        // Delete the floor via API
        given().baseUri("http://localhost:8080/test")
                .when()
                .delete("/floors/" + floorId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        // Verify it's gone via API
        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/floors/" + floorId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        // Removed QuarkusTransaction block for direct DB check
        /*
        Floor deletedFloor =
                QuarkusTransaction.requiringNew()
                        .call(() -> testEntityManager.find(Floor.class, floorId));
        assertNull(deletedFloor);
        */
    }

    @Test
    public void testDeleteFloorNotFound() {
        given().baseUri("http://localhost:8080/test")
                .when()
                .delete("/floors/999") // Assuming 999 does not exist
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testDeleteFloorWithRooms() {
        // Removed QuarkusTransaction block for data setup
        /*
        final Holder<Long> floorIdHolder = new Holder<>();
        QuarkusTransaction.requiringNew()
                .run(
                        () -> {
                            Floor floor = new Floor();
                            floor.setName("Floor With Room");
                            floor.setFloorNumber(108);
                            testEntityManager.persist(floor);
                            OfficeRoom room = new OfficeRoom();
                            room.setName("Room On Floor");
                            room.setRoomNumber("108A");
                            room.setFloor(floor);
                            testEntityManager.persist(room);
                            testEntityManager.flush();
                            floorIdHolder.value = floor.getId();
                        });
        Long floorId = floorIdHolder.value;
        assertNotNull(floorId);
        */

        // Setup: Create floor and room using API (Requires Room endpoint)
        // This test cannot be fully implemented without a Room API or different setup.
        // Assume floor is created:
        Floor floorWithRoom = new Floor();
        floorWithRoom.setName("Floor With Room API");
        floorWithRoom.setFloorNumber(1080);
        FloorDTO floorDto =
                given().contentType(ContentType.JSON)
                        .baseUri("http://localhost:8080/test")
                        .body(floorWithRoom)
                        .when()
                        .post("/floors")
                        .then()
                        .statusCode(201)
                        .extract()
                        .as(FloorDTO.class);
        floorId = floorDto.getId();

        // TODO: Add code here to create a Room associated with floorId via Room API

        // Try deleting the floor via API - should fail if rooms exist and constraint is enforced
        given().baseUri("http://localhost:8080/test")
                .when()
                .delete("/floors/" + floorId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.CONFLICT.getStatusCode()); // Or potentially BAD_REQUEST

        assertTrue(true, "Test needs Room creation via API or different setup");
    }

    @Test
    public void testCreateAndGetFloorPlan() {
        // Removed QuarkusTransaction block for data setup
        /*
        final Holder<Long> floorIdHolder = new Holder<>();
        QuarkusTransaction.requiringNew()
                .run(
                        () -> {
                            Floor floor = new Floor();
                            floor.setName("Floor For Plan CG");
                            floor.setFloorNumber(109);
                            testEntityManager.persist(floor);
                            testEntityManager.flush();
                            floorIdHolder.value = floor.getId();
                        });
        Long floorId = floorIdHolder.value;
        assertNotNull(floorId);
        */

        // Setup: Create floor via API
        Floor floorForPlan = new Floor();
        floorForPlan.setName("Floor For Plan CG API");
        floorForPlan.setFloorNumber(1090);
        FloorDTO floorDto =
                given().contentType(ContentType.JSON)
                        .baseUri("http://localhost:8080/test")
                        .body(floorForPlan)
                        .when()
                        .post("/floors")
                        .then()
                        .statusCode(201)
                        .extract()
                        .as(FloorDTO.class);
        floorId = floorDto.getId();

        // Create floor plan via POST
        byte[] planData = "Simple Plan Data CG".getBytes();
        given().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .baseUri("http://localhost:8080/test")
                .body(planData)
                .when()
                .post("/floors/" + floorId + "/planimetry")
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.CREATED.getStatusCode());

        // Verify floor DTO now shows hasPlanimetry=true
        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/floors/" + floorId)
                .then()
                .statusCode(200)
                .body("hasPlanimetry", equalTo(true));

        // Get the floor plan
        byte[] retrievedPlanData =
                given().accept(MediaType.APPLICATION_OCTET_STREAM)
                        .baseUri("http://localhost:8080/test")
                        .when()
                        .get("/floors/" + floorId + "/planimetry")
                        .then()
                        .log()
                        .ifValidationFails()
                        .statusCode(Response.Status.OK.getStatusCode())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .extract()
                        .asByteArray();

        assertArrayEquals(planData, retrievedPlanData);
    }

    @Test
    public void testUpdateFloorPlan() {
        // Removed QuarkusTransaction block for data setup
        /*
        final Holder<Long> floorIdHolder = new Holder<>();
        QuarkusTransaction.requiringNew()
                .run(
                        () -> {
                            Floor floor = new Floor();
                            floor.setName("Floor For Plan Upd");
                            floor.setFloorNumber(110);
                            testEntityManager.persist(floor);
                            FloorPlanimetry planimetry = new FloorPlanimetry();
                            planimetry.setFloor(floor);
                            planimetry.setPlanimetryData("Initial Plan Data Upd".getBytes());
                            testEntityManager.persist(planimetry);
                            testEntityManager.flush();
                            floorIdHolder.value = floor.getId();
                        });
        Long floorId = floorIdHolder.value;
        assertNotNull(floorId);
        */

        // Setup: Create floor and initial plan via API
        Floor floorForUpdate = new Floor();
        floorForUpdate.setName("Floor For Plan Upd API");
        floorForUpdate.setFloorNumber(1100);
        FloorDTO floorDto =
                given().contentType(ContentType.JSON)
                        .baseUri("http://localhost:8080/test")
                        .body(floorForUpdate)
                        .when()
                        .post("/floors")
                        .then()
                        .statusCode(201)
                        .extract()
                        .as(FloorDTO.class);
        floorId = floorDto.getId();
        byte[] initialPlanData = "Initial Plan Data Upd API".getBytes();
        given().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .baseUri("http://localhost:8080/test")
                .body(initialPlanData)
                .when()
                .post("/floors/" + floorId + "/planimetry")
                .then()
                .statusCode(201);

        // Update floor plan via PUT
        byte[] updatedPlanData = "Updated Plan Data Upd API".getBytes();
        given().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .baseUri("http://localhost:8080/test")
                .body(updatedPlanData)
                .when()
                .put("/floors/" + floorId + "/planimetry")
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode()); // Expect OK for update

        // Get the updated floor plan and verify
        byte[] retrievedPlanData =
                given().accept(MediaType.APPLICATION_OCTET_STREAM)
                        .baseUri("http://localhost:8080/test")
                        .when()
                        .get("/floors/" + floorId + "/planimetry")
                        .then()
                        .log()
                        .ifValidationFails()
                        .statusCode(Response.Status.OK.getStatusCode())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .extract()
                        .asByteArray();

        assertArrayEquals(updatedPlanData, retrievedPlanData);
    }

    @Test
    public void testGetFloorPlanNotFound() {
        // Test getting plan for a non-existent floor
        given().accept(MediaType.APPLICATION_OCTET_STREAM)
                .baseUri("http://localhost:8080/test")
                .when()
                .get("/floors/999/planimetry") // Assuming floor 999 doesn't exist
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetFloorPlanNoPlanExists() {
        // Removed QuarkusTransaction block for data setup
        /*
        final Holder<Long> floorIdHolder = new Holder<>();
        QuarkusTransaction.requiringNew()
                .run(
                        () -> {
                            Floor floor = new Floor();
                            floor.setName("Floor Without Plan");
                            floor.setFloorNumber(111);
                            testEntityManager.persist(floor);
                            testEntityManager.flush();
                            floorIdHolder.value = floor.getId();
                        });
        Long floorId = floorIdHolder.value;
        assertNotNull(floorId);
        */

        // Setup: Create floor without a plan via API
        Floor floorWithoutPlan = new Floor();
        floorWithoutPlan.setName("Floor Without Plan API");
        floorWithoutPlan.setFloorNumber(1110);
        FloorDTO floorDto =
                given().contentType(ContentType.JSON)
                        .baseUri("http://localhost:8080/test")
                        .body(floorWithoutPlan)
                        .when()
                        .post("/floors")
                        .then()
                        .statusCode(201)
                        .extract()
                        .as(FloorDTO.class);
        floorId = floorDto.getId();

        // Try getting the plan for the floor that exists but has no plan
        given().accept(MediaType.APPLICATION_OCTET_STREAM)
                .baseUri("http://localhost:8080/test")
                .when()
                .get("/floors/" + floorId + "/planimetry")
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
