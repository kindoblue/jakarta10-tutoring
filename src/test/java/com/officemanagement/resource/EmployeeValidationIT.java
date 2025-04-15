package com.officemanagement.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.officemanagement.model.Employee;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmployeeValidationIT extends BaseResourceTest {

    @Test
    public void testCreateEmployeeMissingName() {
        Employee employee = new Employee();
        employee.setOccupation("Tester");

        given().baseUri("http://localhost:8080/test")
                .contentType(ContentType.JSON)
                .body(employee)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCreateEmployeeMissingOccupation() {
        Employee employee = new Employee();
        employee.setFullName("Jane Smith");

        given().baseUri("http://localhost:8080/test")
                .contentType(ContentType.JSON)
                .body(employee)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testCreateEmployeeWithInvalidData() {
        Employee employeeNullName = new Employee();
        employeeNullName.setOccupation("Invalid Occupation");
        given().baseUri("http://localhost:8080/test")
                .contentType(ContentType.JSON)
                .body(employeeNullName)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        Employee employeeNullOccupation = new Employee();
        employeeNullOccupation.setFullName("Invalid Name");
        given().baseUri("http://localhost:8080/test")
                .contentType(ContentType.JSON)
                .body(employeeNullOccupation)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        Employee employeeEmptyName = new Employee();
        employeeEmptyName.setFullName("");
        employeeEmptyName.setOccupation("Valid Occupation");
        given().baseUri("http://localhost:8080/test")
                .contentType(ContentType.JSON)
                .body(employeeEmptyName)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        Employee employeeEmptyOccupation = new Employee();
        employeeEmptyOccupation.setFullName("Valid Name");
        employeeEmptyOccupation.setOccupation("");
        given().baseUri("http://localhost:8080/test")
                .contentType(ContentType.JSON)
                .body(employeeEmptyOccupation)
                .when()
                .post("/employees")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testSearchEmployeesWithInvalidParameters() {
        // Test with null search parameter (should default to empty string and return all employees)
        given().baseUri("http://localhost:8080/test")
                .queryParam("search", (String) null)
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content", notNullValue());

        // Test with empty search parameter (should return all employees)
        given().baseUri("http://localhost:8080/test")
                .queryParam("search", "")
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content", notNullValue());

        // Test with negative page (should return 400 Bad Request)
        given().baseUri("http://localhost:8080/test")
                .queryParam("search", "test")
                .queryParam("page", -1)
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        // Test with zero size (should return 400 Bad Request)
        given().baseUri("http://localhost:8080/test")
                .queryParam("search", "test")
                .queryParam("size", 0)
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        // Test with negative size (should return 400 Bad Request)
        given().baseUri("http://localhost:8080/test")
                .queryParam("search", "test")
                .queryParam("size", -1)
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }
} 