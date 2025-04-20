package com.officemanagement.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.officemanagement.model.Employee;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmployeeSearchIT extends BaseResourceTest {

    @Test
    public void testSearchEmployees() {
        runInTransaction(
                () -> {
                    Employee emp1 = new Employee();
                    emp1.setFullName("Alice Search Wonderland");
                    emp1.setOccupation("QA Search Engineer");
                    entityManager.persist(emp1);

                    Employee emp2 = new Employee();
                    emp2.setFullName("Bob Search Builder");
                    emp2.setOccupation("Search Architect");
                    entityManager.persist(emp2);
                    entityManager.flush();
                });

        // Test valid search with partial name - use space instead of URL encoded space
        given().baseUri("http://localhost:8080/test")
                .queryParam("search", "lice Search")
                .queryParam("size", 1)
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content", hasSize(1))
                .body("content[0].fullName", equalTo("Alice Search Wonderland"))
                .body("totalElements", equalTo(1));

        // Test valid search with partial occupation - use space instead of URL encoded space
        given().baseUri("http://localhost:8080/test")
                .queryParam("search", "Search Arch")
                .queryParam("size", 1)
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content", hasSize(1))
                .body("content[0].fullName", equalTo("Bob Search Builder"))
                .body("totalElements", equalTo(1));

        // Test valid search with valid pagination parameters
        given().baseUri("http://localhost:8080/test")
                .queryParam("search", "Search")
                .queryParam("page", 0)
                .queryParam("size", 1)
                .when()
                .get("/employees/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content", hasSize(1))
                .body("totalElements", equalTo(2))
                .body("totalPages", equalTo(2));
    }
}
