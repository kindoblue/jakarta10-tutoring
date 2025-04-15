package com.officemanagement.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.officemanagement.dto.FloorDTO;
import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

/** Integration tests for the FloorResource endpoints. */
public class FloorResourceIT extends BaseResourceTest {

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

        // Setup: Create two floors using the API
        Floor floor1 = new Floor();
        floor1.setName("Floor One UpdDup API");
        floor1.setFloorNumber(1050);

        given().contentType(ContentType.JSON)
                .baseUri("http://localhost:8080/test")
                .body(floor1)
                .when()
                .post("/floors")
                .then()
                .statusCode(201)
                .extract()
                .as(FloorDTO.class);

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

        // Create a Room associated with floorId via Room API
        OfficeRoom roomPayload = new OfficeRoom();
        roomPayload.setName("Test Room For Delete");
        roomPayload.setRoomNumber("1080-A");
        Floor floorRef = new Floor(); // Create a Floor object just for the reference
        floorRef.setId(floorId);
        roomPayload.setFloor(floorRef); // Set the floor reference

        given().contentType(ContentType.JSON)
                .baseUri("http://localhost:8080/test")
                .body(roomPayload)
                .when()
                .post("/rooms") // Use the Room creation endpoint
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.CREATED.getStatusCode());

        // Try deleting the floor via API - should fail because the room exists
        given().baseUri("http://localhost:8080/test")
                .when()
                .delete("/floors/" + floorId)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.CONFLICT.getStatusCode()); // Expect CONFLICT
    }

    @Test
    public void testCreateAndGetFloorPlan() {

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

        // Create/Update floor plan via PUT
        String planData = "<svg>Simple Plan Data CG</svg>"; // Use String for text/plain
        given().contentType(MediaType.TEXT_PLAIN) // Change to TEXT_PLAIN
                .baseUri("http://localhost:8080/test")
                .body(planData)
                .when()
                .put("/floors/" + floorId + "/svg") // Use PUT and /svg path
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode()); // Expect OK (200) for PUT

        // Verify floor DTO now shows hasPlanimetry=true
        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/floors/" + floorId)
                .then()
                .statusCode(200)
                .body("hasPlanimetry", equalTo(true));

        // Get the floor plan
        String retrievedPlanData =
                given().accept(MediaType.TEXT_PLAIN) // Change accept type
                        .baseUri("http://localhost:8080/test")
                        .when()
                        .get("/floors/" + floorId + "/svg") // Change path to /svg
                        .then()
                        .log()
                        .ifValidationFails()
                        .statusCode(Response.Status.OK.getStatusCode())
                        .contentType(MediaType.TEXT_PLAIN) // Change expected content type
                        .extract()
                        .asString(); // Extract as String

        assertEquals(planData, retrievedPlanData);
    }

    @Test
    public void testUpdateFloorPlan() {

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
        String initialPlanData = "<svg>Initial Plan Data Upd API</svg>"; // Use String
        given().contentType(MediaType.TEXT_PLAIN) // Use TEXT_PLAIN
                .baseUri("http://localhost:8080/test")
                .body(initialPlanData)
                .when()
                .put("/floors/" + floorId + "/svg") // Use PUT and /svg path
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode()); // Expect OK (200) for PUT

        // Update floor plan via PUT
        String updatedPlanData = "<svg>Updated Plan Data Upd API</svg>"; // Use String
        given().contentType(MediaType.TEXT_PLAIN) // Use TEXT_PLAIN
                .baseUri("http://localhost:8080/test")
                .body(updatedPlanData)
                .when()
                .put("/floors/" + floorId + "/svg") // Use PUT and /svg path
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode()); // Expect OK for update

        // Get the updated floor plan and verify
        String retrievedPlanData =
                given().accept(MediaType.TEXT_PLAIN) // Use TEXT_PLAIN
                        .baseUri("http://localhost:8080/test")
                        .when()
                        .get("/floors/" + floorId + "/svg") // Use PUT and /svg path
                        .then()
                        .log()
                        .ifValidationFails()
                        .statusCode(Response.Status.OK.getStatusCode())
                        .contentType(MediaType.TEXT_PLAIN) // Use TEXT_PLAIN
                        .extract()
                        .asString(); // Extract as String

        assertEquals(updatedPlanData, retrievedPlanData);
    }

    @Test
    public void testGetFloorPlanNotFound() {
        // Test getting plan for a non-existent floor
        given().accept(MediaType.TEXT_PLAIN) // Change accept type
                .baseUri("http://localhost:8080/test")
                .when()
                .get("/floors/999/svg") // Change path to /svg
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetFloorPlanNoPlanExists() {

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
        given().accept(MediaType.TEXT_PLAIN) // Change accept type
                .baseUri("http://localhost:8080/test")
                .when()
                .get("/floors/" + floorId + "/svg") // Change path to /svg
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
