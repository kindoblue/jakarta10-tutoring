package com.officemanagement.dto;

import com.officemanagement.model.Employee; // Import Employee
import com.officemanagement.model.Seat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/** Data Transfer Object for Seat entities. */
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

    // Default constructor
    public SeatDTO() {}

    // Constructor to map from Seat entity
    public SeatDTO(Seat seat) {
        this.id = seat.getId();
        this.seatNumber = seat.getSeatNumber();
        this.createdAt = seat.getCreatedAt();
        this.x = seat.getX();
        this.y = seat.getY();
        this.width = seat.getWidth();
        this.height = seat.getHeight();
        this.rotation = seat.getRotation();

        if (seat.getRoom() != null) {
            this.roomId = seat.getRoom().getId();
            this.roomName = seat.getRoom().getName(); // Assuming OfficeRoom has getName()
            if (seat.getRoom().getFloor() != null) {
                this.floorId = seat.getRoom().getFloor().getId();
                this.floorName =
                        seat.getRoom().getFloor().getName(); // Assuming Floor has getName()
            }
        }

        if (seat.getEmployees() != null) {
            // Ensure employees collection is initialized if lazy
            // Hibernate.initialize(seat.getEmployees()); // Potentially needed if called outside
            // transaction
            this.employeeIds =
                    seat.getEmployees().stream()
                            .map(Employee::getId) // Assuming Employee has getId()
                            .collect(Collectors.toSet());
            this.occupied = !this.employeeIds.isEmpty();
        } else {
            this.employeeIds = Collections.emptySet();
            this.occupied = false;
        }
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
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

    public Float getRotation() {
        return rotation;
    }

    public void setRotation(Float rotation) {
        this.rotation = rotation;
    }

    public Set<Long> getEmployeeIds() {
        return employeeIds;
    }

    public void setEmployeeIds(Set<Long> employeeIds) {
        this.employeeIds = employeeIds;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
}
