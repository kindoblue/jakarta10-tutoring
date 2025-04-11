package com.officemanagement.resource;

import com.officemanagement.model.Floor;
import com.officemanagement.model.FloorPlanimetry;
import com.officemanagement.model.OfficeRoom;
import com.officemanagement.dto.FloorDTO;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import io.quarkus.narayana.jta.QuarkusTransaction;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the FloorResource endpoints.
 */
@QuarkusTest
public class FloorResourceTest extends BaseResourceTest {

    @Inject // Inject EntityManager for test setup
    EntityManager testEntityManager;

    // Helper class for IDs
    private static class Holder<T> { T value; }

    @Test
    public void testCreateAndGetFloor() {
        Floor floorPayload = new Floor();
        floorPayload.setName("Test Floor CG");
        floorPayload.setFloorNumber(101);

        // Create floor via POST and expect DTO
        FloorDTO createdFloorDto = given()
            .contentType(ContentType.JSON)
            .body(floorPayload)
        .when()
            .post("/floors")
        .then()
            .log().ifValidationFails()
            .statusCode(Response.Status.CREATED.getStatusCode())
            .body("id", notNullValue())
            .body("name", equalTo("Test Floor CG"))
            .body("floorNumber", equalTo(101))
            .body("roomIds", empty())
            .body("hasPlanimetry", equalTo(false))
        .extract().as(FloorDTO.class);
        
        Long createdFloorId = createdFloorDto.getId();
        assertNotNull(createdFloorId);

        // Get the created floor and expect DTO
        given()
        .when()
            .get("/floors/" + createdFloorId) 
        .then()
            .log().ifValidationFails()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(createdFloorId.intValue()))
            .body("name", equalTo("Test Floor CG"))
            .body("floorNumber", equalTo(101))
            .body("roomIds", empty())
            .body("hasPlanimetry", equalTo(false));
    }

    @Test
    public void testGetFloorNotFound() {
        given().when().get("/floors/999")
            .then().statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
    
    @Test
    public void testCreateFloorDuplicateNumber() {
        // Create first floor in transaction
        QuarkusTransaction.run(() -> {
             Floor floor1 = new Floor(); floor1.setName("Floor One Dup"); floor1.setFloorNumber(102); testEntityManager.persist(floor1);
        });

        // Try creating another with the same number via API
        Floor floor2Payload = new Floor();
        floor2Payload.setName("Floor Two Dup");
        floor2Payload.setFloorNumber(102); // Duplicate number
        given().contentType(ContentType.JSON).body(floor2Payload).when().post("/floors")
            .then().log().ifValidationFails().statusCode(Response.Status.CONFLICT.getStatusCode());
    }
    
    @Test
    public void testUpdateFloor() {
        // Create a floor in transaction
        final Holder<Long> floorIdHolder = new Holder<>();
        QuarkusTransaction.run(() -> {
            Floor floor = new Floor(); floor.setName("Original Name Upd"); floor.setFloorNumber(103); testEntityManager.persist(floor);
            testEntityManager.flush();
            floorIdHolder.value = floor.getId();
        });
        Long floorId = floorIdHolder.value;
        assertNotNull(floorId);

        // Update the floor via API, expect DTO
        Floor updatePayload = new Floor();
        updatePayload.setName("Updated Name Upd");
        updatePayload.setFloorNumber(104);
        given().contentType(ContentType.JSON).body(updatePayload).when().put("/floors/" + floorId)
            .then().log().ifValidationFails().statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(floorId.intValue()))
            .body("name", equalTo("Updated Name Upd"))
            .body("floorNumber", equalTo(104))
            .body("roomIds", empty()) // Assuming no rooms were added
            .body("hasPlanimetry", equalTo(false));
    }

    @Test
    public void testUpdateFloorNotFound() {
        Floor floorPayload = new Floor(); floorPayload.setName("Non Existent Upd"); floorPayload.setFloorNumber(999);
        given().contentType(ContentType.JSON).body(floorPayload).when().put("/floors/999")
            .then().statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
    
    @Test
    public void testUpdateFloorDuplicateNumber() {
        // Create floors in transaction
        final Holder<Long> floor1IdHolder = new Holder<>();
        final Holder<Long> floor2IdHolder = new Holder<>();
        QuarkusTransaction.run(() -> {
            Floor floor1 = new Floor(); floor1.setName("Floor One UpdDup"); floor1.setFloorNumber(105); testEntityManager.persist(floor1);
            Floor floor2 = new Floor(); floor2.setName("Floor Two UpdDup"); floor2.setFloorNumber(106); testEntityManager.persist(floor2);
            testEntityManager.flush();
            floor1IdHolder.value = floor1.getId();
            floor2IdHolder.value = floor2.getId();
        });
        Long floor1Id = floor1IdHolder.value; Long floor2Id = floor2IdHolder.value;
        assertNotNull(floor1Id); assertNotNull(floor2Id);

        // Try updating floor 2 to have floor number 105 (duplicate)
        Floor updatePayload = new Floor();
        updatePayload.setName("Floor Two UpdDup Updated"); // Name change is fine
        updatePayload.setFloorNumber(105); // Duplicate number
        given().contentType(ContentType.JSON).body(updatePayload).when().put("/floors/" + floor2Id)
            .then().log().ifValidationFails().statusCode(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testDeleteFloor() {
        // Create a floor in transaction
        final Holder<Long> floorIdHolder = new Holder<>();
        QuarkusTransaction.run(() -> {
            Floor floor = new Floor(); floor.setName("To Be Deleted"); floor.setFloorNumber(107); testEntityManager.persist(floor);
            testEntityManager.flush();
            floorIdHolder.value = floor.getId();
        });
        Long floorId = floorIdHolder.value;
        assertNotNull(floorId);

        // Delete the floor via API
        given().when().delete("/floors/" + floorId)
            .then().log().ifValidationFails().statusCode(Response.Status.NO_CONTENT.getStatusCode());

        // Verify it's gone via API
        given().when().get("/floors/" + floorId)
            .then().log().ifValidationFails().statusCode(Response.Status.NOT_FOUND.getStatusCode());
            
        // Verify it's gone via EntityManager (in new transaction)
        Floor deletedFloor = QuarkusTransaction.call(() -> testEntityManager.find(Floor.class, floorId));
        assertNull(deletedFloor);
    }
    
    @Test
    public void testDeleteFloorNotFound() {
        given().when().delete("/floors/999")
            .then().statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
    
    @Test
    public void testDeleteFloorWithRooms() {
        // Create floor and room in transaction
        final Holder<Long> floorIdHolder = new Holder<>();
        QuarkusTransaction.run(() -> {
            Floor floor = new Floor(); floor.setName("Floor With Room Del"); floor.setFloorNumber(108); testEntityManager.persist(floor);
            OfficeRoom room = new OfficeRoom(); room.setName("Test Room Del"); room.setRoomNumber("R1Del"); room.setFloor(floor); testEntityManager.persist(room);
            testEntityManager.flush();
            floorIdHolder.value = floor.getId();
        });
        Long floorId = floorIdHolder.value;
        assertNotNull(floorId);

        // Try deleting the floor via API (should fail)
        given().when().delete("/floors/" + floorId)
            .then().log().ifValidationFails().statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
    
    @Test
    public void testCreateAndGetFloorPlan() {
        // Create floor in transaction
        final Holder<Long> floorIdHolder = new Holder<>();
        QuarkusTransaction.run(() -> {
            Floor floor = new Floor(); floor.setName("Floor With Plan CG"); floor.setFloorNumber(109); testEntityManager.persist(floor);
            testEntityManager.flush();
            floorIdHolder.value = floor.getId();
        });
        Long floorId = floorIdHolder.value;
        assertNotNull(floorId);

        // Add SVG plan via API, expect updated FloorDTO
        String svgData = "<svg><rect x=\"0\" y=\"0\" width=\"100\" height=\"100\"/></svg>";
        given().contentType(MediaType.TEXT_PLAIN).body(svgData).when().put("/floors/" + floorId + "/svg")
            .then().log().ifValidationFails().statusCode(Response.Status.OK.getStatusCode())
            .body("id", equalTo(floorId.intValue()))
            .body("hasPlanimetry", equalTo(true)); // Verify planimetry flag

        // Get SVG plan via API
        given().accept("image/svg+xml").when().get("/floors/" + floorId + "/svg")
            .then().log().ifValidationFails().statusCode(Response.Status.OK.getStatusCode())
            .contentType("image/svg+xml")
            .body(equalTo(svgData));
            
        // Get Floor DTO again to confirm hasPlanimetry is still true
         given().when().get("/floors/" + floorId) 
            .then().log().ifValidationFails().statusCode(Response.Status.OK.getStatusCode())
            .body("hasPlanimetry", equalTo(true));
    }
    
    @Test
    public void testUpdateFloorPlan() {
        // Create floor and initial plan in transaction
        final Holder<Long> floorIdHolder = new Holder<>();
        String initialSvg = "<svg><circle cx=\"50\" cy=\"50\" r=\"40\"/></svg>";
        QuarkusTransaction.run(() -> {
            Floor floor = new Floor(); floor.setName("Floor Update Plan"); floor.setFloorNumber(110); testEntityManager.persist(floor);
            FloorPlanimetry planimetry = new FloorPlanimetry(); planimetry.setFloor(floor); planimetry.setPlanimetry(initialSvg);
            testEntityManager.persist(planimetry);
            testEntityManager.flush();
            floorIdHolder.value = floor.getId();
        });
        Long floorId = floorIdHolder.value;
        assertNotNull(floorId);

        // Update SVG plan via API
        String updatedSvgData = "<svg><rect x=\"10\" y=\"10\" width=\"80\" height=\"80\"/></svg>";
        given().contentType(MediaType.TEXT_PLAIN).body(updatedSvgData).when().put("/floors/" + floorId + "/svg")
            .then().log().ifValidationFails().statusCode(Response.Status.OK.getStatusCode());

        // Get SVG plan via API to verify update
        given().accept("image/svg+xml").when().get("/floors/" + floorId + "/svg")
            .then().log().ifValidationFails().statusCode(Response.Status.OK.getStatusCode())
            .contentType("image/svg+xml")
            .body(equalTo(updatedSvgData));
    }

    @Test
    public void testGetFloorPlanNotFound() {
         // Test non-existent floor ID
         given().accept("image/svg+xml").when().get("/floors/999/svg")
            .then().statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetFloorPlanNoPlanExists() {
         // Create floor without plan in transaction
        final Holder<Long> floorIdHolder = new Holder<>();
        QuarkusTransaction.run(() -> {
            Floor floor = new Floor(); floor.setName("Floor No Plan"); floor.setFloorNumber(111); testEntityManager.persist(floor);
            testEntityManager.flush();
            floorIdHolder.value = floor.getId();
        });
        Long floorId = floorIdHolder.value;
        assertNotNull(floorId);

        // Try getting SVG plan via API (should be 404)
        given().accept("image/svg+xml").when().get("/floors/" + floorId + "/svg")
            .then().log().ifValidationFails().statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
} 