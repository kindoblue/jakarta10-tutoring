package com.officemanagement.resource;

import com.officemanagement.dto.OfficeRoomDTO;
import com.officemanagement.dto.SeatDTO;
import com.officemanagement.model.Floor;
import com.officemanagement.model.OfficeRoom;
import com.officemanagement.model.Seat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.hibernate.Hibernate;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Tag(name = "Room", description = "Operations related to office rooms")
public class RoomResource {
    @Inject EntityManager entityManager;

    @POST
    @Transactional
    @Operation(
            summary = "Create a new room",
            description = "Creates a new office room with the provided details.")
    public Response createRoom(OfficeRoom room) {
        if (room == null || room.getName() == null || room.getName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room name is required")
                    .build();
        }

        if (room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room number is required")
                    .build();
        }

        if (room.getFloor() == null || room.getFloor().getId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Floor reference is required")
                    .build();
        }

        Floor floor = entityManager.find(Floor.class, room.getFloor().getId());
        if (floor == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Referenced floor does not exist")
                    .build();
        }

        Long count =
                entityManager
                        .createQuery(
                                "SELECT COUNT(r) FROM OfficeRoom r WHERE r.floor.id = :floorId AND r.roomNumber = :roomNumber",
                                Long.class)
                        .setParameter("floorId", floor.getId())
                        .setParameter("roomNumber", room.getRoomNumber())
                        .getSingleResult();

        if (count > 0) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(
                            "A room with number "
                                    + room.getRoomNumber()
                                    + " already exists on this floor")
                    .build();
        }

        room.setFloor(floor);
        room.setCreatedAt(LocalDateTime.now());

        entityManager.persist(room);
        entityManager.flush();

        OfficeRoomDTO dto = new OfficeRoomDTO(room);
        return Response.status(Response.Status.CREATED).entity(dto).build();
    }

    @GET
    @Operation(summary = "Get all rooms", description = "Returns a list of all office rooms.")
    public Response getAllRooms() {
        // Implementation of getAllRooms method
        return null; // Placeholder return, actual implementation needed
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get room by ID", description = "Returns an office room by its ID.")
    public Response getRoom(@PathParam("id") Long id) {
        OfficeRoom room =
                entityManager
                        .createQuery(
                                "select distinct r from OfficeRoom r "
                                        + "left join fetch r.seats "
                                        + "left join fetch r.seats.employees "
                                        + "where r.id = :id",
                                OfficeRoom.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Convert to DTO
        OfficeRoomDTO dto = new OfficeRoomDTO(room);
        return Response.ok(dto).build();
    }

    @GET
    @Path("/{id}/seats")
    @Operation(
            summary = "Get seats in room",
            description = "Returns all seats in a specific office room.")
    public Response getRoomSeats(@PathParam("id") Long id) {
        OfficeRoom room =
                entityManager
                        .createQuery(
                                "select distinct r from OfficeRoom r "
                                        + "left join fetch r.seats "
                                        + "where r.id = :id",
                                OfficeRoom.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Set<Seat> seats = room.getSeats();

        Set<Map<String, Object>> seatsList = new java.util.HashSet<>();
        if (seats != null) {
            for (Seat seat : seats) {
                Map<String, Object> seatInfo = new HashMap<>();
                seatInfo.put("id", seat.getId());
                seatInfo.put("seatNumber", seat.getSeatNumber());
                seatInfo.put("x", seat.getX());
                seatInfo.put("y", seat.getY());
                seatInfo.put("width", seat.getWidth());
                seatInfo.put("height", seat.getHeight());
                seatInfo.put("rotation", seat.getRotation());
                seatInfo.put("roomId", id);

                seatsList.add(seatInfo);
            }
        }

        return Response.ok(seatsList).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(
            summary = "Update a room",
            description = "Updates the details of an existing office room.")
    public Response updateRoom(@PathParam("id") Long id, OfficeRoom room) {
        if (room == null || room.getName() == null || room.getName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room name is required")
                    .build();
        }

        if (room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room number is required")
                    .build();
        }

        OfficeRoom existingRoom = entityManager.find(OfficeRoom.class, id);
        if (existingRoom == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Room not found").build();
        }

        if (room.getFloor() == null || room.getFloor().getId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Floor reference is required")
                    .build();
        }

        Floor floor = entityManager.find(Floor.class, room.getFloor().getId());
        if (floor == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Referenced floor does not exist")
                    .build();
        }

        Long count =
                entityManager
                        .createQuery(
                                "SELECT COUNT(r) FROM OfficeRoom r WHERE r.floor.id = :floorId AND r.roomNumber = :roomNumber AND r.id != :roomId",
                                Long.class)
                        .setParameter("floorId", floor.getId())
                        .setParameter("roomNumber", room.getRoomNumber())
                        .setParameter("roomId", id)
                        .getSingleResult();

        if (count > 0) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(
                            "A room with number "
                                    + room.getRoomNumber()
                                    + " already exists on this floor")
                    .build();
        }

        existingRoom.setName(room.getName());
        existingRoom.setRoomNumber(room.getRoomNumber());
        existingRoom.setFloor(floor);
        if (room.getX() != null) existingRoom.setX(room.getX());
        if (room.getY() != null) existingRoom.setY(room.getY());
        if (room.getWidth() != null) existingRoom.setWidth(room.getWidth());
        if (room.getHeight() != null) existingRoom.setHeight(room.getHeight());

        OfficeRoom updatedRoom = entityManager.merge(existingRoom);

        // Return DTO
        OfficeRoomDTO dto = new OfficeRoomDTO(updatedRoom);
        return Response.ok(dto).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete a room", description = "Deletes an office room by its ID.")
    public Response deleteRoom(@PathParam("id") Long id) {
        OfficeRoom room = entityManager.find(OfficeRoom.class, id);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Room not found").build();
        }

        Long seatCount =
                entityManager
                        .createQuery(
                                "SELECT COUNT(s) FROM Seat s WHERE s.room.id = :roomId", Long.class)
                        .setParameter("roomId", id)
                        .getSingleResult();

        if (seatCount > 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Cannot delete room that has seats")
                    .build();
        }

        entityManager.remove(room);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{roomId}/geometry")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateRoomGeometry(
            @PathParam("roomId") Long roomId, Map<String, Float> geometry) {
        OfficeRoom room = entityManager.find(OfficeRoom.class, roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Room not found").build();
        }

        boolean updated = false;
        if (geometry.containsKey("x")) {
            room.setX(geometry.get("x"));
            updated = true;
        }
        if (geometry.containsKey("y")) {
            room.setY(geometry.get("y"));
            updated = true;
        }
        if (geometry.containsKey("width")) {
            room.setWidth(geometry.get("width"));
            updated = true;
        }
        if (geometry.containsKey("height")) {
            room.setHeight(geometry.get("height"));
            updated = true;
        }

        if (!updated) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No valid geometry fields provided")
                    .build();
        }

        OfficeRoom updatedRoom = entityManager.merge(room);
        entityManager.flush();

        // Initialize collections before DTO creation
        Hibernate.initialize(updatedRoom.getFloor());
        Hibernate.initialize(updatedRoom.getSeats());

        OfficeRoomDTO dto = new OfficeRoomDTO(updatedRoom);
        return Response.ok(dto).build();
    }

    @PATCH
    @Path("/{roomId}/seats/{seatId}/geometry")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateSeatGeometry(
            @PathParam("roomId") Long roomId,
            @PathParam("seatId") Long seatId,
            Map<String, Float> geometry) {

        Seat seat =
                entityManager
                        .createQuery(
                                "SELECT s FROM Seat s WHERE s.id = :seatId AND s.room.id = :roomId",
                                Seat.class)
                        .setParameter("seatId", seatId)
                        .setParameter("roomId", roomId)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

        if (seat == null) {
            // Check if room exists but seat doesn't, or if room doesn't exist
            OfficeRoom room = entityManager.find(OfficeRoom.class, roomId);
            if (room == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Room not found").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Seat not found in the specified room")
                        .build();
            }
        }

        boolean updated = false;
        if (geometry.containsKey("x")) {
            seat.setX(geometry.get("x"));
            updated = true;
        }
        if (geometry.containsKey("y")) {
            seat.setY(geometry.get("y"));
            updated = true;
        }
        if (geometry.containsKey("width")) {
            seat.setWidth(geometry.get("width"));
            updated = true;
        }
        if (geometry.containsKey("height")) {
            seat.setHeight(geometry.get("height"));
            updated = true;
        }
        if (geometry.containsKey("rotation")) {
            seat.setRotation(geometry.get("rotation"));
            updated = true;
        }

        if (!updated) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No valid geometry fields provided")
                    .build();
        }

        Seat updatedSeat = entityManager.merge(seat);
        entityManager.flush();

        // Initialize collections before DTO creation
        Hibernate.initialize(updatedSeat.getRoom());
        Hibernate.initialize(updatedSeat.getEmployees());
        if (updatedSeat.getRoom() != null) {
            Hibernate.initialize(updatedSeat.getRoom().getFloor());
        }

        SeatDTO dto = new SeatDTO(updatedSeat);
        return Response.ok(dto).build();
    }
}
