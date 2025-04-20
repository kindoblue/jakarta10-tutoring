package com.officemanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "floors")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Floor {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "floor_seq")
    @SequenceGenerator(name = "floor_seq", sequenceName = "floor_seq", allocationSize = 1)
    @Column(name = "id", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(name = "floor_number")
    @ToString.Include
    private Integer floorNumber;

    @ToString.Include
    @Column(name = "name")
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @ToString.Include
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "floor", fetch = FetchType.EAGER)
    @JsonIgnoreProperties("floor")
    private Set<OfficeRoom> rooms = new HashSet<>();

    @OneToOne(mappedBy = "floor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("floor")
    private FloorPlanimetry planimetryData;

    // Default constructor provided by @NoArgsConstructor

    // Keep custom constructor
    public Floor(Long id, String name, Integer level) {
        this.id = id;
        this.name = name;
        this.floorNumber = level;
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
            planimetryData.setFloor(this); // Assumes FloorPlanimetry has @Setter
        }
    }
}
