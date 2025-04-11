package com.officemanagement.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/stats") // Base path for all stats-related endpoints
@ApplicationScoped // Make it a CDI bean
public class StatsResource {

    @Inject // Inject EntityManager
    EntityManager entityManager;

    // DTO for stats response
    public static class StatsDTO {
        @JsonProperty("totalEmployees")
        private final long totalEmployees;

        @JsonProperty("totalFloors")
        private final long totalFloors;

        @JsonProperty("totalOffices")
        private final long totalOffices;

        @JsonProperty("totalSeats")
        private final long totalSeats;

        public StatsDTO(long totalEmployees, long totalFloors, long totalOffices, long totalSeats) {
            this.totalEmployees = totalEmployees;
            this.totalFloors = totalFloors;
            this.totalOffices = totalOffices;
            this.totalSeats = totalSeats;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON) // Return JSON response
    public Response getStats() {
        try {
            // Use EntityManager
            Long totalEmployees =
                    entityManager
                            .createQuery("SELECT COUNT(e) FROM Employee e", Long.class)
                            .getSingleResult();

            Long totalFloors =
                    entityManager
                            .createQuery("SELECT COUNT(f) FROM Floor f", Long.class)
                            .getSingleResult();

            Long totalOffices =
                    entityManager
                            .createQuery("SELECT COUNT(o) FROM OfficeRoom o", Long.class)
                            .getSingleResult();

            Long totalSeats =
                    entityManager
                            .createQuery("SELECT COUNT(s) FROM Seat s", Long.class)
                            .getSingleResult();

            StatsDTO stats = new StatsDTO(totalEmployees, totalFloors, totalOffices, totalSeats);
            return Response.ok(stats).build();
        } catch (Exception e) {
            // Basic error handling (consider more specific logging/exceptions)
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Failed to retrieve stats: " + e.getMessage()))
                    .build();
        }
    }

    // Error response class
    private static class ErrorResponse {
        @JsonProperty("message")
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
