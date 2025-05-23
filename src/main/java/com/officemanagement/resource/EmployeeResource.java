package com.officemanagement.resource;

import com.officemanagement.dto.EmployeeDTO;
import com.officemanagement.dto.SeatDTO;
import com.officemanagement.model.Employee;
import com.officemanagement.model.Seat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Hibernate;

// Add static inner class for pagination response
class PageResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int size;

    public PageResponse(List<T> content, long totalElements, int currentPage, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.size = size;
        this.totalPages = (int) Math.ceil(totalElements / (double) size);
    }

    // Getters and setters
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped // Make it a CDI bean
@Tag(name = "Employee", description = "Operations related to employees")
public class EmployeeResource {
    @Inject // Inject EntityManager
    EntityManager entityManager;

    @GET
    @Path("/{id}")
    @Transactional // Added Transactional annotation
    @Operation(summary = "Get employee by ID", description = "Returns an employee by their ID.")
    public Response getEmployee(@PathParam("id") Long id) {
        Employee employee =
                entityManager
                        .createQuery(
                                "select e from Employee e " + "where e.id = :id", Employee.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

        if (employee == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Ensure seats are initialized before mapping to DTO
        Hibernate.initialize(employee.getSeats());

        // Map to DTO and return
        EmployeeDTO dto = new EmployeeDTO(employee);
        return Response.ok(dto).build();
    }

    @GET
    @Path("/{id}/seats")
    @Transactional
    @Operation(
            summary = "Get seats assigned to employee",
            description = "Returns all seats assigned to a specific employee.")
    public Response getEmployeeSeats(@PathParam("id") Long id) {
        Employee employee =
                entityManager
                        .createQuery("select e from Employee e where e.id = :id", Employee.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

        if (employee == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Explicitly initialize lazy collection before returning
        Hibernate.initialize(employee.getSeats());

        // Map to DTOs to avoid lazy loading issues
        Set<SeatDTO> seatDTOs =
                employee.getSeats().stream()
                        .map(SeatDTO::new)
                        .collect(java.util.stream.Collectors.toSet());

        return Response.ok(seatDTOs).build();
    }

    @POST
    @Transactional // Add transaction management
    @Operation(
            summary = "Create a new employee",
            description = "Creates a new employee with the provided details.")
    public Response createEmployee(Employee employee) {
        // Validate input
        if (employee == null
                || employee.getFullName() == null
                || employee.getFullName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Employee full name is required")
                    .build();
        }

        if (employee.getOccupation() == null || employee.getOccupation().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Employee occupation is required")
                    .build();
        }

        // Set creation timestamp
        employee.setCreatedAt(LocalDateTime.now());

        // Use EntityManager, no need for manual transaction
        entityManager.persist(employee);
        entityManager.flush(); // Ensure ID is generated before returning

        // Ensure lazy collections are initialized before creating DTO
        Hibernate.initialize(employee.getSeats());

        // Return DTO instead of entity
        EmployeeDTO dto = new EmployeeDTO(employee);
        return Response.status(Response.Status.CREATED).entity(dto).build();
    }

    @PUT
    @Path("/{id}/seats/{seatId}")
    @Transactional
    @Operation(summary = "Assign seat to employee", description = "Assigns a seat to an employee.")
    public Response assignSeat(@PathParam("id") Long employeeId, @PathParam("seatId") Long seatId) {
        // No need for manual transaction
        Employee employee =
                entityManager.find(Employee.class, employeeId); // Use find instead of get
        if (employee == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Employee not found").build();
        }

        Seat seat = entityManager.find(Seat.class, seatId); // Use find instead of get
        if (seat == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Seat not found").build();
        }

        // Add seat to employee's seats
        employee.addSeat(seat);

        // Managed entity, update is often implicit, but merge ensures it
        entityManager.merge(employee); // Use merge instead of update
        entityManager.flush(); // Flush to apply changes before re-query

        // Fix: Remove aliases from fetch joins to comply with strict JPQL
        Employee refreshedEmployee =
                entityManager
                        .createQuery("SELECT e FROM Employee e WHERE e.id = :id", Employee.class)
                        .setParameter("id", employeeId)
                        .getSingleResult();

        // Manually initialize collections to avoid LazyInitializationException
        if (refreshedEmployee != null) {
            // Load seats collection
            entityManager
                    .createQuery(
                            "SELECT s FROM Seat s WHERE s.id IN (SELECT s2.id FROM Employee e JOIN e.seats s2 WHERE e.id = :employeeId)")
                    .setParameter("employeeId", employeeId)
                    .getResultList();
        }

        // Create a response DTO with assignment info
        Map<String, Object> result = new HashMap<>();
        result.put("employeeId", employeeId);
        result.put("seatId", seatId);
        result.put("assigned", true);

        return Response.ok(result).build();
    }

    @DELETE
    @Path("/{employeeId}/seats/{seatId}")
    @Transactional
    @Operation(
            summary = "Unassign seat from employee",
            description = "Unassigns a seat from an employee.")
    public Response unassignSeat(
            @PathParam("employeeId") Long employeeId, @PathParam("seatId") Long seatId) {
        // No need for manual transaction
        Employee employee = entityManager.find(Employee.class, employeeId); // Use find
        if (employee == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Employee not found").build();
        }

        Seat seat = entityManager.find(Seat.class, seatId); // Use find
        if (seat == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Seat not found").build();
        }

        // Check if this seat is assigned to the employee
        if (!employee.getSeats().contains(seat)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("This seat is not assigned to the employee")
                    .build();
        }

        // Remove the seat from employee
        employee.removeSeat(seat);
        entityManager.merge(employee); // Use merge
        entityManager.flush(); // Flush to apply changes before re-query

        // Fix: Remove aliases from fetch joins to comply with strict JPQL
        Employee refreshedEmployee =
                entityManager
                        .createQuery("SELECT e FROM Employee e WHERE e.id = :id", Employee.class)
                        .setParameter("id", employeeId)
                        .getSingleResult();

        // Manually initialize collections to avoid LazyInitializationException
        if (refreshedEmployee != null) {
            // Load seats collection
            entityManager
                    .createQuery(
                            "SELECT s FROM Seat s WHERE s.id IN (SELECT s2.id FROM Employee e JOIN e.seats s2 WHERE e.id = :employeeId)")
                    .setParameter("employeeId", employeeId)
                    .getResultList();
        }

        // Create a response DTO with assignment info
        Map<String, Object> result = new HashMap<>();
        result.put("employeeId", employeeId);
        result.put("seatId", seatId);
        result.put("assigned", false);

        return Response.ok(result).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional // Add transaction management
    @Operation(summary = "Delete employee", description = "Deletes an employee by their ID.")
    public Response deleteEmployee(@PathParam("id") Long id) {
        // No need for manual transaction
        Employee employee = entityManager.find(Employee.class, id); // Use find
        if (employee == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Employee not found").build();
        }

        // Consider cascading deletes or manual unassignment if needed
        // For simplicity, just removing the employee here
        entityManager.remove(employee); // Use remove
        return Response.noContent().build();
    }

    @GET
    @Path("/search")
    @Operation(
            summary = "Search employees",
            description = "Searches for employees by name or occupation with pagination.")
    public Response searchEmployees(
            @QueryParam("search") @DefaultValue("") String searchTerm,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {

        // Add validation for pagination parameters
        if (page < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Page index must not be less than zero!")
                    .build();
        }
        if (size <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Page size must not be less than one!")
                    .build();
        }
        // Add a reasonable upper limit for size to prevent excessive results
        if (size > 100) { // Example limit
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Page size must not exceed 100!")
                    .build();
        }

        String countQuery =
                "SELECT COUNT(e) FROM Employee e WHERE LOWER(e.fullName) LIKE LOWER(:searchTerm) OR LOWER(e.occupation) LIKE LOWER(:searchTerm)";
        long totalElements =
                entityManager
                        .createQuery(countQuery, Long.class)
                        .setParameter("searchTerm", "%" + searchTerm + "%")
                        .getSingleResult();

        String query =
                "SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.seats WHERE LOWER(e.fullName) LIKE LOWER(:searchTerm) OR LOWER(e.occupation) LIKE LOWER(:searchTerm) ORDER BY e.fullName";
        List<Employee> employees =
                entityManager
                        .createQuery(query, Employee.class)
                        .setParameter("searchTerm", "%" + searchTerm + "%")
                        .setFirstResult(page * size)
                        .setMaxResults(size)
                        .getResultList();

        // Map to DTOs
        List<EmployeeDTO> employeeDTOs = employees.stream().map(EmployeeDTO::new).toList();
        PageResponse<EmployeeDTO> response =
                new PageResponse<>(employeeDTOs, totalElements, page, size);
        return Response.ok(response).build();
    }
}
