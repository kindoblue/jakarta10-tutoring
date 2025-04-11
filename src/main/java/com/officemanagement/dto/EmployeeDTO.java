package com.officemanagement.dto;

import com.officemanagement.model.Employee;
import com.officemanagement.model.Seat; // Ensure Seat import if needed
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/** Data Transfer Object for Employee entities. */
public class EmployeeDTO {

    private Long id;
    private String fullName;
    private String occupation;
    private LocalDateTime createdAt;
    private Set<Long> seatIds; // Represent seats by their IDs

    // Default constructor (required by Jackson/JAX-RS)
    public EmployeeDTO() {}

    // Constructor to map from Employee entity
    public EmployeeDTO(Employee employee) {
        this.id = employee.getId();
        this.fullName = employee.getFullName();
        this.occupation = employee.getOccupation();
        this.createdAt = employee.getCreatedAt();
        // Ensure seats are initialized before mapping
        if (employee.getSeats() != null) {
            this.seatIds =
                    employee.getSeats().stream().map(Seat::getId).collect(Collectors.toSet());
        } else {
            this.seatIds = Set.of(); // Or null, depending on desired representation
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(Set<Long> seatIds) {
        this.seatIds = seatIds;
    }
}
