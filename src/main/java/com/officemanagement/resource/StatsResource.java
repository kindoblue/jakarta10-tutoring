package com.officemanagement.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Statistics", description = "Provides overall statistics about the office setup.")
public class StatsResource {

    @Inject // Inject EntityManager
    EntityManager entityManager;

    // DTO for stats response
    public static class StatsDTO {
        @JsonProperty("totalEmployees")
        @Schema(description = "Total number of employees.", example = "42")
        private final long totalEmployees;

        @JsonProperty("totalFloors")
        @Schema(description = "Total number of floors.", example = "5")
        private final long totalFloors;

        @JsonProperty("totalOffices")
        @Schema(description = "Total number of offices.", example = "12")
        private final long totalOffices;

        @JsonProperty("totalSeats")
        @Schema(description = "Total number of seats available.", example = "100")
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
    @Operation(
            summary = "Get office statistics",
            description =
                    "Returns total counts of employees, floors, offices, and seats in the system.")
    @ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully.",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = StatsDTO.class)))
    @ApiResponse(
            responseCode = "500",
            description = "Failed to retrieve statistics.",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
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

    @Schema(description = "Standard error response.")
    private static class ErrorResponse {
        @JsonProperty("message")
        @Schema(
                description = "Error message detailing the issue.",
                example = "Failed to retrieve stats")
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
