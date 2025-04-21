package com.officemanagement.dto;

import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.*;

/**
 * Data Transfer Object for Floor entities. Contains only basic floor information and a set of room
 * IDs.
 */
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

    /**
     * Constructs a FloorDTO from a Floor entity. Only room IDs are extracted, so no need to
     * initialize the full collection.
     *
     * @param floor the Floor entity
     */
    public FloorDTO(Floor floor) {
        this.id = floor.getId();
        this.name = floor.getName();
        this.floorNumber = floor.getFloorNumber();
        this.createdAt = floor.getCreatedAt();
        if (floor.getRooms() != null) {
            this.roomIds =
                    floor.getRooms().stream().map(OfficeRoom::getId).collect(Collectors.toSet());
        } else {
            this.roomIds = Collections.emptySet();
        }
        // Only check for existence of planimetry, do not load the data
        this.hasPlanimetry = floor.getPlanimetry() != null;
    }
}
