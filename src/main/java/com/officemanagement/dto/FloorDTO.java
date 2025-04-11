package com.officemanagement.dto;

import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

// Import Hibernate

/** Data Transfer Object for Floor entities. */
public class FloorDTO {

    private Long id;
    private String name;
    private Integer floorNumber;
    private LocalDateTime createdAt;
    private Set<Long> roomIds; // IDs of rooms on this floor
    private boolean hasPlanimetry;

    // Default constructor
    public FloorDTO() {}

    // Constructor to map from Floor entity
    public FloorDTO(Floor floor) {
        this.id = floor.getId();
        this.name = floor.getName();
        this.floorNumber = floor.getFloorNumber();
        this.createdAt = floor.getCreatedAt();

        // Safely handle potentially lazy collections
        if (floor.getRooms() != null) {
            // Hibernate.initialize(floor.getRooms()); // May be needed
            this.roomIds =
                    floor.getRooms().stream().map(OfficeRoom::getId).collect(Collectors.toSet());
        } else {
            this.roomIds = Collections.emptySet();
        }

        // Check if planimetry exists without loading the potentially large data
        // This requires a specific query or a field on Floor indicating presence
        // For simplicity, let's assume Floor has a getter isPlanimetrySet() or similar
        // Or check if the association is non-null (might trigger lazy load)
        this.hasPlanimetry = floor.getPlanimetry() != null;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<Long> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(Set<Long> roomIds) {
        this.roomIds = roomIds;
    }

    public boolean isHasPlanimetry() {
        return hasPlanimetry;
    }

    public void setHasPlanimetry(boolean hasPlanimetry) {
        this.hasPlanimetry = hasPlanimetry;
    }
}
