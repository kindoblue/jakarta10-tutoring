package com.officemanagement.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.officemanagement.model.Employee;
import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import com.officemanagement.model.Seat;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmployeeCrudIT extends BaseResourceTest {

    private static class Holder<T> {
        T value;
    }

    @Test
    public void testGetEmployeeNotFound() {
        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/999")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testCreateAndGetEmployee() throws Exception {
        final Holder<Long> createdEmployeeId = new Holder<>();

        runInTransaction(
                () -> {
                    Employee newEmployee = new Employee();
                    newEmployee.setFullName("John Doe CreateGet");
                    newEmployee.setOccupation("Developer CreateGet");

                    entityManager.persist(newEmployee);
                    entityManager.flush();
                    createdEmployeeId.value = newEmployee.getId();
                    assertNotNull(
                            createdEmployeeId.value,
                            "Created Employee ID should not be null via EM");
                });

        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/" + createdEmployeeId.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(createdEmployeeId.value.intValue()))
                .body("fullName", equalTo("John Doe CreateGet"))
                .body("occupation", equalTo("Developer CreateGet"))
                .body("createdAt", notNullValue())
                .body("seatIds", hasSize(0));
        System.out.println("GET request finished for Employee ID: " + createdEmployeeId.value);
    }

    @Test
    public void testCreateEmployee() {
        Employee employee = new Employee();
        employee.setFullName("John Doe Create");
        employee.setOccupation("Software Engineer Create");

        given().baseUri("http://localhost:8080/test")
                .contentType(ContentType.JSON)
                .body(employee)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .body("id", notNullValue())
                .body("fullName", equalTo("John Doe Create"))
                .body("occupation", equalTo("Software Engineer Create"));
    }

    @Test
    public void testGetEmployee() {
        final Holder<Long> employeeId = new Holder<>();

        runInTransaction(
                () -> {
                    Employee emp = new Employee();
                    emp.setFullName("Jane Smith Get Test");
                    emp.setOccupation("Product Manager Get Test");
                    entityManager.persist(emp);
                    entityManager.flush();
                    employeeId.value = emp.getId();
                    assertNotNull(employeeId.value, "Employee ID should be set after setup");
                });

        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/" + employeeId.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("id", equalTo(employeeId.value.intValue()))
                .body("fullName", equalTo("Jane Smith Get Test"))
                .body("occupation", equalTo("Product Manager Get Test"))
                .body("seatIds", empty());
    }

    @Test
    public void testGetNonExistentEmployee() {
        given().baseUri("http://localhost:8080/test")
                .when()
                .get("/employees/999999")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testDeleteEmployeeWithAssignedSeats() {
        final Holder<Long> floorId = new Holder<>();
        final Holder<Long> roomId = new Holder<>();
        final Holder<Long> seatId = new Holder<>();
        final Holder<Long> employeeId = new Holder<>();

        runInTransaction(
                () -> {
                    Floor floor = new Floor();
                    floor.setFloorNumber(600);
                    floor.setName("Delete Test Floor");
                    entityManager.persist(floor);
                    entityManager.flush();
                    floorId.value = floor.getId();

                    OfficeRoom room = new OfficeRoom();
                    room.setRoomNumber("R600A");
                    room.setName("Delete Test Room");
                    room.setFloor(floor);
                    entityManager.persist(room);
                    entityManager.flush();
                    roomId.value = room.getId();

                    Seat seat = new Seat();
                    seat.setSeatNumber("S600A1");
                    seat.setRoom(room);
                    entityManager.persist(seat);
                    entityManager.flush();
                    seatId.value = seat.getId();

                    Employee employee = new Employee();
                    employee.setFullName("To Be Deleted User");
                    employee.setOccupation("Temporary");
                    entityManager.persist(employee);
                    entityManager.flush();
                    employeeId.value = employee.getId();

                    employee.addSeat(seat);
                    entityManager.merge(employee);
                    entityManager.flush();
                });

        runInTransaction(
                () -> {
                    Employee empCheck = entityManager.find(Employee.class, employeeId.value);
                    assertNotNull(empCheck, "Employee should exist after setup");
                    assertFalse(
                            empCheck.getSeats().isEmpty(), "Employee should have assigned seat");

                    Seat seatCheck = entityManager.find(Seat.class, seatId.value);
                    assertNotNull(seatCheck, "Seat should exist after setup");
                    assertTrue(seatCheck.isOccupied(), "Seat should be occupied");
                });

        given().baseUri("http://localhost:8080/test")
                .when()
                .delete("/employees/" + employeeId.value)
                .then()
                .log()
                .ifValidationFails()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        runInTransaction(
                () -> {
                    Employee deletedEmp = entityManager.find(Employee.class, employeeId.value);
                    assertNull(deletedEmp, "Employee should be deleted");

                    Seat seatAfterDelete = entityManager.find(Seat.class, seatId.value);
                    assertNotNull(seatAfterDelete, "Seat should still exist");
                    assertFalse(
                            seatAfterDelete.isOccupied(),
                            "Seat should be unassigned after employee deletion");
                });
    }

    @Test
    public void testDeleteNonExistentEmployee() {
        given().baseUri("http://localhost:8080/test")
                .when()
                .delete("/employees/777777")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}
