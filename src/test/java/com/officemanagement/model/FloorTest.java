package com.officemanagement.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FloorTest {
    private Floor floor;
    private final LocalDateTime testDateTime = LocalDateTime.of(2024, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        floor = new Floor();
    }

    @Test
    void testFloorInitialization() {
        assertNotNull(floor);
        assertNotNull(floor.getRooms(), "Rooms set should be initialized");
        assertTrue(floor.getRooms().isEmpty(), "Initial rooms set should be empty");
    }

    @Test
    void testSetAndGetId() {
        Long id = 1L;
        floor.setId(id);
        assertEquals(id, floor.getId());
    }

    @Test
    void testSetAndGetName() {
        String name = "First Floor";
        floor.setName(name);
        assertEquals(name, floor.getName());
    }

    @Test
    void testSetAndGetFloorNumber() {
        Integer number = 2;
        floor.setFloorNumber(number);
        assertEquals(number, floor.getFloorNumber());
    }

    @Test
    void testSetAndGetCreatedAt() {
        floor.setCreatedAt(testDateTime);
        assertEquals(testDateTime, floor.getCreatedAt());
    }

    @Test
    void testSetAndGetRooms() {
        HashSet<OfficeRoom> rooms = new HashSet<>();
        OfficeRoom room1 = new OfficeRoom();
        room1.setId(1L);
        OfficeRoom room2 = new OfficeRoom();
        room2.setId(2L);
        rooms.add(room1);
        rooms.add(room2);
        floor.setRooms(rooms);
        assertNotNull(floor.getRooms());
        assertEquals(2, floor.getRooms().size());
        assertTrue(floor.getRooms().contains(room1));
        assertTrue(floor.getRooms().contains(room2));
    }

    @Test
    void testSetAndGetPlanimetryData() {
        FloorPlanimetry planimetry = new FloorPlanimetry();
        planimetry.setPlanimetry("SVG DATA");
        floor.setPlanimetryData(planimetry);
        assertEquals(planimetry, floor.getPlanimetryData());
        assertEquals("SVG DATA", floor.getPlanimetry());
    }

    @Test
    void testSetPlanimetryCreatesPlanimetryData() {
        floor.setPlanimetry("SVG DATA");
        assertNotNull(floor.getPlanimetryData());
        assertEquals("SVG DATA", floor.getPlanimetry());
    }
}
