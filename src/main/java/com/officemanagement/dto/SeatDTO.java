package com.officemanagement.dto;

import com.officemanagement.model.Employee; // Import Employee
import com.officemanagement.model.Seat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.*;

/** Data Transfer Object for Seat entities. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SeatDTO {

    private Long id;
    private String seatNumber;
    private Long roomId; // Reference room by ID
    private String roomName; // Include room name for convenience
    private Long floorId; // Include floor ID
    private String floorName; // Include floor name
    private LocalDateTime createdAt;
    private Float x;
    private Float y;
    private Float width;
    private Float height;
    private Float rotation;
    private Set<Long> employeeIds; // IDs of employees assigned to this seat
    private boolean occupied; // Calculated field

    // Default constructor provided by @NoArgsConstructor

    // Constructor to map from Seat entity (Keep this custom one)
    public SeatDTO(Seat seat) {
        this.id = seat.getId(); // Assumes Seat has @Getter
        this.seatNumber = seat.getSeatNumber(); // Assumes Seat has @Getter
        this.createdAt = seat.getCreatedAt(); // Assumes Seat has @Getter
        this.x = seat.getX(); // Assumes Seat has @Getter
        this.y = seat.getY(); // Assumes Seat has @Getter
        this.width = seat.getWidth(); // Assumes Seat has @Getter
        this.height = seat.getHeight(); // Assumes Seat has @Getter
        this.rotation = seat.getRotation(); // Assumes Seat has @Getter

        if (seat.getRoom() != null) { // Assumes Seat has @Getter
            this.roomId = seat.getRoom().getId(); // Assumes OfficeRoom has @Getter
            this.roomName = seat.getRoom().getName(); // Assumes OfficeRoom has @Getter
            if (seat.getRoom().getFloor() != null) { // Assumes OfficeRoom has @Getter
                this.floorId = seat.getRoom().getFloor().getId(); // Assumes Floor has @Getter
                this.floorName = seat.getRoom().getFloor().getName(); // Assumes Floor has @Getter
            }
        }

        if (seat.getEmployees() != null) { // Assumes Seat has @Getter
            // Ensure employees collection is initialized if lazy
            // Hibernate.initialize(seat.getEmployees()); // Potentially needed if called outside
            // transaction
            this.employeeIds =
                    seat.getEmployees().stream()
                            .map(Employee::getId) // Assumes Employee has @Getter
                            .collect(Collectors.toSet());
            this.occupied = !this.employeeIds.isEmpty();
        } else {
            this.employeeIds = Collections.emptySet();
            this.occupied = false;
        }
    }

    // --- Getters and Setters removed (using @Getter, @Setter) ---
}
