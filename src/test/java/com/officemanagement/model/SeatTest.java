package com.officemanagement.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SeatTest {
    private Seat seat;
    private final LocalDateTime testDateTime = LocalDateTime.of(2024, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        seat = new Seat();
    }

    @Test
    void testSeatInitialization() {
        assertNotNull(seat);
        assertNotNull(seat.getEmployees(), "Employees set should be initialized");
        assertTrue(seat.getEmployees().isEmpty(), "Initial employees set should be empty");
    }

    @Test
    void testSetAndGetId() {
        Long id = 1L;
        seat.setId(id);
        assertEquals(id, seat.getId());
    }

    @Test
    void testSetAndGetSeatNumber() {
        String seatNumber = "A1";
        seat.setSeatNumber(seatNumber);
        assertEquals(seatNumber, seat.getSeatNumber());
    }

    @Test
    void testSetAndGetRoom() {
        OfficeRoom room = new OfficeRoom();
        room.setId(1L);
        seat.setRoom(room);
        assertEquals(room, seat.getRoom());
    }

    @Test
    void testSetAndGetCreatedAt() {
        seat.setCreatedAt(testDateTime);
        assertEquals(testDateTime, seat.getCreatedAt());
    }

    @Test
    void testSetAndGetGeometry() {
        seat.setX(10f);
        seat.setY(20f);
        seat.setWidth(120f);
        seat.setHeight(80f);
        seat.setRotation(45f);
        assertEquals(10f, seat.getX());
        assertEquals(20f, seat.getY());
        assertEquals(120f, seat.getWidth());
        assertEquals(80f, seat.getHeight());
        assertEquals(45f, seat.getRotation());
    }

    @Test
    void testSetAndGetEmployees() {
        HashSet<Employee> employees = new HashSet<>();
        Employee emp1 = new Employee();
        emp1.setId(1L);
        Employee emp2 = new Employee();
        emp2.setId(2L);
        employees.add(emp1);
        employees.add(emp2);
        seat.setEmployees(employees);
        assertNotNull(seat.getEmployees());
        assertEquals(2, seat.getEmployees().size());
        assertTrue(seat.getEmployees().contains(emp1));
        assertTrue(seat.getEmployees().contains(emp2));
    }

    @Test
    void testIsOccupied() {
        assertFalse(seat.isOccupied(), "Seat should not be occupied initially");
        Employee emp = new Employee();
        emp.setId(1L);
        seat.getEmployees().add(emp);
        assertTrue(seat.isOccupied(), "Seat should be occupied when employees are present");
    }
}
