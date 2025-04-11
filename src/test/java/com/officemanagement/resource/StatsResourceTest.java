package com.officemanagement.resource;

import com.officemanagement.model.Employee;
import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import com.officemanagement.model.Seat;
import org.junit.jupiter.api.Test;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import io.quarkus.narayana.jta.QuarkusTransaction;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import jakarta.ws.rs.core.Response;

/**
 * Integration tests for the StatsResource endpoint.
 */
@QuarkusTest
public class StatsResourceTest {

    @Inject
    EntityManager testEntityManager;

    // Add explicit cleanup before this specific test method
    @BeforeEach
    @Transactional
    public void cleanEmployeesBeforeTest() {
        testEntityManager.createQuery("DELETE FROM Employee").executeUpdate();
        // Optionally delete other entities if needed for a clean stats state
        testEntityManager.createQuery("DELETE FROM Seat").executeUpdate();
        testEntityManager.createQuery("DELETE FROM OfficeRoom").executeUpdate();
        testEntityManager.createQuery("DELETE FROM FloorPlanimetry").executeUpdate();
        testEntityManager.createQuery("DELETE FROM Floor").executeUpdate();
    }

    @Test
    // @Transactional // Keep Transactional or remove? Let's try removing for now as QuarkusTransaction might be better
    public void testGetStats() {
        // Clean slate should now be ensured by @BeforeEach above

        // Verify initial state (all zeros)
        given()
        .when()
            .get("/stats") // Use relative path
        .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("totalEmployees", equalTo(0))
            .body("totalFloors", equalTo(0))
            .body("totalOffices", equalTo(0))
            .body("totalSeats", equalTo(0));

        // Add some data within a transaction
        QuarkusTransaction.run(() -> {
            Floor floor = new Floor();
            floor.setName("Stats Floor");
            floor.setFloorNumber(30);
            testEntityManager.persist(floor);

            OfficeRoom room = new OfficeRoom();
            room.setName("Stats Room");
            room.setFloor(floor);
            room.setRoomNumber("StatR1");
            testEntityManager.persist(room);

            Seat seat = new Seat();
            seat.setSeatNumber("StatS1");
            seat.setRoom(room);
            testEntityManager.persist(seat);

            Employee employee = new Employee();
            employee.setFullName("Stats Employee");
            employee.setOccupation("Stat Worker");
            employee.addSeat(seat);
            testEntityManager.persist(employee);
            // No flush/clear needed inside QuarkusTransaction
        });

        // Verify updated stats
        given()
        .when()
            .get("/stats") // Use relative path
        .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .body("totalEmployees", equalTo(1))
            .body("totalFloors", equalTo(1))
            .body("totalOffices", equalTo(1))
            .body("totalSeats", equalTo(1));
    }
} 