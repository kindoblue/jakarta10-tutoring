package com.officemanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {
    private Employee employee;
    private final LocalDateTime testDateTime = LocalDateTime.of(2024, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        employee = new Employee();
    }

    @Test
    void testEmployeeInitialization() {
        assertNotNull(employee);
        assertNotNull(employee.getSeats(), "Seats set should be initialized");
        assertTrue(employee.getSeats().isEmpty(), "Initial seats set should be empty");
    }

    @Test
    void testSetAndGetId() {
        Long id = 1L;
        employee.setId(id);
        assertEquals(id, employee.getId());
    }

    @Test
    void testSetAndGetFullName() {
        String fullName = "John Doe";
        employee.setFullName(fullName);
        assertEquals(fullName, employee.getFullName());
    }

    @Test
    void testSetAndGetOccupation() {
        String occupation = "Software Engineer";
        employee.setOccupation(occupation);
        assertEquals(occupation, employee.getOccupation());
    }

    @Test
    void testSetAndGetCreatedAt() {
        employee.setCreatedAt(testDateTime);
        assertEquals(testDateTime, employee.getCreatedAt());
    }

    @Test
    void testAddSeat() {
        Seat seat = new Seat();
        employee.addSeat(seat);
        
        assertTrue(employee.getSeats().contains(seat));
        assertTrue(seat.getEmployees().contains(employee));
    }

    @Test
    void testRemoveSeat() {
        Seat seat = new Seat();
        employee.addSeat(seat);
        employee.removeSeat(seat);
        
        assertFalse(employee.getSeats().contains(seat));
        assertFalse(seat.getEmployees().contains(employee));
    }

    @Test
    void testSetSeats() {
        HashSet<Seat> seats = new HashSet<>();
        Seat seat1 = new Seat();
        Seat seat2 = new Seat();
        seats.add(seat1);
        seats.add(seat2);

        employee.setSeats(seats);
        assertEquals(seats, employee.getSeats());
        assertEquals(2, employee.getSeats().size());
    }
} 