package com.officemanagement.dto;

import com.officemanagement.model.OfficeRoom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.*;

/** Data Transfer Object for OfficeRoom entities. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class OfficeRoomDTO {

    private Long id;
    private String name;
    private String roomNumber;
    private Long floorId; // Reference floor by ID
    private String floorName; // Include floor name for convenience
    private LocalDateTime createdAt;
    private Float x;
    private Float y;
    private Float width;
    private Float height;
    private Set<Long> seatIds; // Include IDs of seats in this room

    // Default constructor provided by @NoArgsConstructor

    // Constructor to map from OfficeRoom entity (Keep this custom one)
    public OfficeRoomDTO(OfficeRoom room) {
        this.id = room.getId(); // Assumes OfficeRoom has @Getter
        this.name = room.getName(); // Assumes OfficeRoom has @Getter
        this.roomNumber = room.getRoomNumber(); // Assumes OfficeRoom has @Getter
        this.createdAt = room.getCreatedAt(); // Assumes OfficeRoom has @Getter
        this.x = room.getX(); // Assumes OfficeRoom has @Getter
        this.y = room.getY(); // Assumes OfficeRoom has @Getter
        this.width = room.getWidth(); // Assumes OfficeRoom has @Getter
        this.height = room.getHeight(); // Assumes OfficeRoom has @Getter

        if (room.getFloor() != null) { // Assumes OfficeRoom has @Getter
            this.floorId = room.getFloor().getId(); // Assumes Floor has @Getter
            this.floorName = room.getFloor().getName(); // Assumes Floor has @Getter
        }

        if (room.getSeats() != null) { // Assumes OfficeRoom has @Getter
            // Ensure seats collection is initialized if lazy
            // Hibernate.initialize(room.getSeats()); // Potentially needed if called outside
            // transaction
            this.seatIds =
                    room.getSeats().stream()
                            .map(seat -> seat.getId()) // Assumes Seat has @Getter
                            .collect(Collectors.toSet());
        } else {
            this.seatIds = Collections.emptySet();
        }
    }

    // Getters and Setters removed (using @Getter, @Setter)
}
