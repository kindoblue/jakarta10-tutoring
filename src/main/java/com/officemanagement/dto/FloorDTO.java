package com.officemanagement.dto;

import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.*;

// Import Hibernate

/** Data Transfer Object for Floor entities. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class FloorDTO {

    private Long id;
    private String name;
    private Integer floorNumber;
    private LocalDateTime createdAt;
    private Set<Long> roomIds; // IDs of rooms on this floor
    private boolean hasPlanimetry;

    // Default constructor provided by @NoArgsConstructor

    // Constructor to map from Floor entity (Keep this custom one)
    public FloorDTO(Floor floor) {
        this.id = floor.getId(); // Assumes Floor has @Getter
        this.name = floor.getName(); // Assumes Floor has @Getter
        this.floorNumber = floor.getFloorNumber(); // Assumes Floor has @Getter
        this.createdAt = floor.getCreatedAt(); // Assumes Floor has @Getter

        // Safely handle potentially lazy collections
        if (floor.getRooms() != null) { // Assumes Floor has @Getter
            // Hibernate.initialize(floor.getRooms()); // May be needed
            this.roomIds =
                    floor.getRooms().stream()
                            .map(OfficeRoom::getId) // Assumes OfficeRoom has @Getter
                            .collect(Collectors.toSet());
        } else {
            this.roomIds = Collections.emptySet();
        }

        // Check if planimetry exists without loading the potentially large data
        // Use the custom getPlanimetry() method we kept in Floor
        this.hasPlanimetry = floor.getPlanimetry() != null;
    }

    // --- Getters and Setters removed (using @Getter, @Setter) ---
}
