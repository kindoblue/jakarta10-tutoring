package com.officemanagement.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity representing floor planimetry (SVG floor plans) Separated into a dedicated table for
 * better performance and clean design
 */
@Entity
@Table(name = "floor_planimetry")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "floor")
public class FloorPlanimetry {
    @Id
    @Column(name = "floor_id")
    @EqualsAndHashCode.Include
    private Long floorId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "floor_id")
    private Floor floor;

    @Column(columnDefinition = "TEXT")
    private String planimetry;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Constructors
    public FloorPlanimetry(Floor floor, String planimetry) {
        this.floor = floor;
        this.planimetry = planimetry;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and setters
    public Long getFloorId() {
        return floorId;
    }

    public void setFloorId(Long floorId) {
        this.floorId = floorId;
    }

    public Floor getFloor() {
        return floor;
    }

    public void setFloor(Floor floor) {
        this.floor = floor;
    }

    public String getPlanimetry() {
        return planimetry;
    }

    public void setPlanimetry(String planimetry) {
        this.planimetry = planimetry;
        this.lastUpdated = LocalDateTime.now();
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
