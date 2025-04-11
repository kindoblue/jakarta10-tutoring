package com.officemanagement.dto;

import com.officemanagement.model.OfficeRoom;
import com.officemanagement.model.Seat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for OfficeRoom entities.
 */
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

    // Default constructor
    public OfficeRoomDTO() {
    }

    // Constructor to map from OfficeRoom entity
    public OfficeRoomDTO(OfficeRoom room) {
        this.id = room.getId();
        this.name = room.getName();
        this.roomNumber = room.getRoomNumber();
        this.createdAt = room.getCreatedAt();
        this.x = room.getX();
        this.y = room.getY();
        this.width = room.getWidth();
        this.height = room.getHeight();

        if (room.getFloor() != null) {
            this.floorId = room.getFloor().getId();
            this.floorName = room.getFloor().getName(); // Assuming Floor has getName()
        }

        if (room.getSeats() != null) {
            // Ensure seats collection is initialized if lazy
            // Hibernate.initialize(room.getSeats()); // Potentially needed if called outside transaction
            this.seatIds = room.getSeats().stream()
                                .map(seat -> seat.getId()) // Assuming Seat has getId()
                                .collect(Collectors.toSet());
        } else {
            this.seatIds = Collections.emptySet();
        }
    }

    // Getters and Setters

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

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Long getFloorId() {
        return floorId;
    }

    public void setFloorId(Long floorId) {
        this.floorId = floorId;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public Float getWidth() {
        return width;
    }

    public void setWidth(Float width) {
        this.width = width;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public Set<Long> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(Set<Long> seatIds) {
        this.seatIds = seatIds;
    }
} 