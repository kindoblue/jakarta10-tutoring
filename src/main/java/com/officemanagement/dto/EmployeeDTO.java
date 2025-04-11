package com.officemanagement.dto;

import com.officemanagement.model.Employee;
import com.officemanagement.model.Seat; // Ensure Seat import if needed
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.*;

/** Data Transfer Object for Employee entities. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Optional: provides a constructor for all fields
@EqualsAndHashCode
@ToString
public class EmployeeDTO {

    private Long id;
    private String fullName;
    private String occupation;
    private LocalDateTime createdAt;
    private Set<Long> seatIds; // Represent seats by their IDs

    // Default constructor provided by @NoArgsConstructor

    // Constructor to map from Employee entity (Keep this custom one)
    public EmployeeDTO(Employee employee) {
        this.id = employee.getId(); // Assumes Employee has @Getter
        this.fullName = employee.getFullName(); // Assumes Employee has @Getter
        this.occupation = employee.getOccupation(); // Assumes Employee has @Getter
        this.createdAt = employee.getCreatedAt(); // Assumes Employee has @Getter
        // Ensure seats are initialized before mapping
        if (employee.getSeats() != null) { // Assumes Employee has @Getter
            this.seatIds =
                    employee.getSeats().stream()
                            .map(Seat::getId) // Assumes Seat has @Getter
                            .collect(Collectors.toSet());
        } else {
            this.seatIds = Set.of(); // Or null, depending on desired representation
        }
    }

    // Getters and Setters removed (using @Getter, @Setter)
}
