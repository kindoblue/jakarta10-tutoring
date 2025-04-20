package com.officemanagement.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FloorPlanimetryTest {
    private FloorPlanimetry planimetry;
    private final LocalDateTime testDateTime = LocalDateTime.of(2024, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        planimetry = new FloorPlanimetry();
    }

    @Test
    void testInitialization() {
        assertNotNull(planimetry);
    }

    @Test
    void testSetAndGetFloorId() {
        Long id = 1L;
        planimetry.setFloorId(id);
        assertEquals(id, planimetry.getFloorId());
    }

    @Test
    void testSetAndGetFloor() {
        Floor floor = new Floor();
        floor.setId(1L);
        planimetry.setFloor(floor);
        assertEquals(floor, planimetry.getFloor());
    }

    @Test
    void testSetAndGetPlanimetry() {
        String svg = "<svg>...</svg>";
        planimetry.setPlanimetry(svg);
        assertEquals(svg, planimetry.getPlanimetry());
    }

    @Test
    void testSetAndGetLastUpdated() {
        planimetry.setLastUpdated(testDateTime);
        assertEquals(testDateTime, planimetry.getLastUpdated());
    }

    @Test
    void testConstructorWithParams() {
        Floor floor = new Floor();
        String svg = "<svg>...</svg>";
        FloorPlanimetry fp = new FloorPlanimetry(floor, svg);
        assertEquals(floor, fp.getFloor());
        assertEquals(svg, fp.getPlanimetry());
        assertNotNull(fp.getLastUpdated());
    }
}
