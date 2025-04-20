package com.officemanagement.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OfficeRoomTest {
    private OfficeRoom room;
    private final LocalDateTime testDateTime = LocalDateTime.of(2024, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        room = new OfficeRoom();
    }

    @Test
    void testOfficeRoomInitialization() {
        assertNotNull(room);
        assertNotNull(room.getSeats(), "Seats set should be initialized");
        assertTrue(room.getSeats().isEmpty(), "Initial seats set should be empty");
    }

    @Test
    void testSetAndGetId() {
        Long id = 1L;
        room.setId(id);
        assertEquals(id, room.getId());
    }

    @Test
    void testSetAndGetName() {
        String name = "Conference Room";
        room.setName(name);
        assertEquals(name, room.getName());
    }

    @Test
    void testSetAndGetRoomNumber() {
        String number = "101A";
        room.setRoomNumber(number);
        assertEquals(number, room.getRoomNumber());
    }

    @Test
    void testSetAndGetCreatedAt() {
        room.setCreatedAt(testDateTime);
        assertEquals(testDateTime, room.getCreatedAt());
    }

    @Test
    void testSetAndGetFloor() {
        Floor floor = new Floor();
        floor.setId(1L);
        room.setFloor(floor);
        assertEquals(floor, room.getFloor());
    }

    @Test
    void testSetAndGetSeats() {
        HashSet<Seat> seats = new HashSet<>();
        Seat seat1 = new Seat();
        seat1.setId(1L);
        Seat seat2 = new Seat();
        seat2.setId(2L);
        seats.add(seat1);
        seats.add(seat2);
        room.setSeats(seats);
        assertNotNull(room.getSeats());
        assertEquals(2, room.getSeats().size());
        assertTrue(room.getSeats().contains(seat1));
        assertTrue(room.getSeats().contains(seat2));
    }

    @Test
    void testSetAndGetGeometry() {
        room.setX(10f);
        room.setY(20f);
        room.setWidth(400f);
        room.setHeight(300f);
        assertEquals(10f, room.getX());
        assertEquals(20f, room.getY());
        assertEquals(400f, room.getWidth());
        assertEquals(300f, room.getHeight());
    }
}
