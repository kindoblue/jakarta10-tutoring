package com.officemanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "floors")
public class Floor {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "floor_seq")
    @SequenceGenerator(name = "floor_seq", sequenceName = "floor_seq", allocationSize = 1)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "floor_number")
    private Integer floorNumber;

    private String name;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "floor", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("floor")
    private Set<OfficeRoom> rooms = new HashSet<>();

    @OneToOne(mappedBy = "floor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("floor")
    private FloorPlanimetry planimetryData;

    // Default constructor required by JPA/Hibernate
    public Floor() {}

    public Floor(Long id, String name, Integer level) {
        this.id = id;
        this.name = name;
        this.floorNumber = level;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get planimetry from the associated FloorPlanimetry entity This maintains backwards
     * compatibility with existing code
     */
    public String getPlanimetry() {
        return planimetryData != null ? planimetryData.getPlanimetry() : null;
    }

    /**
     * Set planimetry by creating or updating the associated FloorPlanimetry entity This maintains
     * backwards compatibility with existing code
     */
    public void setPlanimetry(String planimetry) {
        if (planimetry == null) {
            return;
        }

        if (planimetryData == null) {
            planimetryData = new FloorPlanimetry(this, planimetry);
        } else {
            planimetryData.setPlanimetry(planimetry);
        }
    }

    /** Get the FloorPlanimetry entity associated with this floor */
    public FloorPlanimetry getPlanimetryData() {
        return planimetryData;
    }

    /** Set the FloorPlanimetry entity associated with this floor */
    public void setPlanimetryData(FloorPlanimetry planimetryData) {
        this.planimetryData = planimetryData;
        if (planimetryData != null) {
            planimetryData.setFloor(this);
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<OfficeRoom> getRooms() {
        return rooms;
    }

    public void setRooms(Set<OfficeRoom> rooms) {
        this.rooms = rooms;
    }
}
